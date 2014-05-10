/**
 * Copyright 2014 Joan Zapata
 *
 * This file is part of Android-pdfview.
 *
 * Android-pdfview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android-pdfview is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android-pdfview.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.joanzapata.pdfview;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.SurfaceView;
import com.joanzapata.pdfview.exception.FileNotFoundException;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.model.PagePart;
import com.joanzapata.pdfview.util.ArrayUtils;
import com.joanzapata.pdfview.util.Constants;
import com.joanzapata.pdfview.util.FileUtils;
import com.joanzapata.pdfview.util.NumberUtils;
import org.vudroid.core.DecodeService;

import java.io.File;
import java.io.IOException;

import static com.joanzapata.pdfview.util.Constants.Cache.CACHE_SIZE;

/**
 * @author Joan Zapata
 *         <p/>
 *         It supports animations, zoom, cache, and swipe.
 *         <p/>
 *         To fully understand this class you must know its principles :
 *         - The PDF document is seen as if we always want to draw all the pages.
 *         - The thing is that we only draw the visible parts.
 *         - All parts are the same size, this is because we can't interrupt a native page rendering,
 *         so we need these renderings to be as fast as possible, and be able to interrupt them
 *         as soon as we can.
 *         - The parts are loaded when the current offset or the current zoom level changes
 *         <p/>
 *         Important :
 *         - DocumentPage = A page of the PDF document.
 *         - UserPage = A page as defined by the user.
 *         By default, they're the same. But the user can change the pages order
 *         using {@link #load(Uri, OnLoadCompleteListener, int[])}. In this
 *         particular case, a userPage of 5 can refer to a documentPage of 17.
 */
public class PDFView extends SurfaceView {

    private static final String TAG = PDFView.class.getSimpleName();

    /** Rendered parts go to the cache manager */
    private CacheManager cacheManager;

    /** Animation manager manage all offset and zoom animation */
    private AnimationManager animationManager;

    /** Drag manager manage all touch events */
    private DragPinchManager dragPinchManager;

    /**
     * The pages the user want to display in order
     * (ex: 0, 2, 2, 8, 8, 1, 1, 1)
     */
    private int[] originalUserPages;

    /**
     * The same pages but with a filter to avoid repetition
     * (ex: 0, 2, 8, 1)
     */
    private int[] filteredUserPages;

    /**
     * The same pages but with a filter to avoid repetition
     * (ex: 0, 1, 1, 2, 2, 3, 3, 3)
     */
    private int[] filteredUserPageIndexes;

    /** Number of pages in the loaded PDF document */
    private int documentPageCount;

    /** The index of the current sequence */
    private int currentPage;

    /** The index of the current sequence */
    private int currentFilteredPage;

    /** The actual width and height of the pages in the PDF document */
    private int pageWidth, pageHeight;

    /** The optimal width and height of the pages to fit the component size */
    private float optimalPageWidth, optimalPageHeight;

    /**
     * If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture
     */
    private float currentXOffset = 0;

    /**
     * If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture
     */
    private float currentYOffset = 0;

    /** The zoom level, always >= 1 */
    private float zoom = 1f;

    /** Coordinates of the left mask on the screen */
    private RectF leftMask;

    /** Coordinates of the right mask on the screen */
    private RectF rightMask;

    /** True if the PDFView has been recycled */
    private boolean recycled = true;

    /** Current state of the view */
    private State state = State.DEFAULT;

    /** The VuDroid DecodeService used for decoding PDF and pages */
    private DecodeService decodeService;

    /** Async task used during the loading phase to decode a PDF document */
    private DecodingAsyncTask decodingAsyncTask;

    /** Async task always playing in the background and proceeding rendering tasks */
    private RenderingAsyncTask renderingAsyncTask;

    /** Call back object to call when the PDF is loaded */
    private OnLoadCompleteListener onLoadCompleteListener;

    /** Call back object to call when the page has changed */
    private OnPageChangeListener onPageChangeListener;

    /** Call back object to call when the above layer is to drawn */
    private OnDrawListener onDrawListener;

    /** Paint object for drawing */
    private Paint paint;

    /** Paint object for drawing mask */
    private Paint maskPaint;

    /** Paint object for drawing debug stuff */
    private Paint debugPaint;

    /** Paint object for minimap background */
    private Paint paintMinimapBack;

    private Paint paintMinimapFront;

    /** True if should draw map on the top right corner */
    private boolean miniMapRequired;

    /** Bounds of the minimap */
    private RectF minimapBounds;

    /** Bounds of the minimap */
    private RectF minimapScreenBounds;

    private int defaultPage = 0;

    private boolean userWantsMinimap = false;

    /** Construct the initial view */
    public PDFView(Context context, AttributeSet set) {
        super(context, set);
        miniMapRequired = false;
        cacheManager = new CacheManager();
        animationManager = new AnimationManager(this);
        dragPinchManager = new DragPinchManager(this);

        paint = new Paint();
        debugPaint = new Paint();
        debugPaint.setStyle(Style.STROKE);
        maskPaint = new Paint();
        maskPaint.setColor(Color.BLACK);
        maskPaint.setAlpha(Constants.MASK_ALPHA);
        paintMinimapBack = new Paint();
        paintMinimapBack.setStyle(Style.FILL);
        paintMinimapBack.setColor(Color.BLACK);
        paintMinimapBack.setAlpha(50);
        paintMinimapFront = new Paint();
        paintMinimapFront.setStyle(Style.FILL);
        paintMinimapFront.setColor(Color.BLACK);
        paintMinimapFront.setAlpha(50);

        // A surface view does not call
        // onDraw() as a default but we need it.
        setWillNotDraw(false);
    }

    private void load(Uri uri, OnLoadCompleteListener listener) {
        load(uri, listener, null);
    }

    private void load(Uri uri, OnLoadCompleteListener onLoadCompleteListener, int[] userPages) {

        if (!recycled) {
            throw new IllegalStateException("Don't call load on a PDF View without recycling it first.");
        }

        // Manage UserPages if not null
        if (userPages != null) {
            this.originalUserPages = userPages;
            this.filteredUserPages = ArrayUtils.deleteDuplicatedPages(originalUserPages);
            this.filteredUserPageIndexes = ArrayUtils.calculateIndexesInDuplicateArray(originalUserPages);
        }

        this.onLoadCompleteListener = onLoadCompleteListener;

        // Start decoding document
        decodingAsyncTask = new DecodingAsyncTask(uri, this);
        decodingAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        renderingAsyncTask = new RenderingAsyncTask(this);
        renderingAsyncTask.execute();
    }

    /**
     * Go to the given page.
     * @param page Page number starting from 1.
     */
    public void jumpTo(int page) {
        showPage(page - 1);
    }

    void showPage(int pageNb) {
        state = State.SHOWN;

        // Check the page number and makes the
        // difference between UserPages and DocumentPages
        pageNb = determineValidPageNumberFrom(pageNb);
        currentPage = pageNb;
        currentFilteredPage = pageNb;
        if (filteredUserPageIndexes != null) {
            if (pageNb >= 0 && pageNb < filteredUserPageIndexes.length) {
                pageNb = filteredUserPageIndexes[pageNb];
                currentFilteredPage = pageNb;
            }
        }

        // Reset the zoom and center the page on the screen
        resetZoom();
        animationManager.startXAnimation(currentXOffset, calculateCenterOffsetForPage(pageNb));
        loadPages();

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageChanged(currentPage + 1, getPageCount());
        }
    }

    public int getPageCount() {
        if (originalUserPages != null) {
            return originalUserPages.length;
        }
        return documentPageCount;
    }

    public void enableSwipe(boolean enableSwipe) {
        dragPinchManager.setSwipeEnabled(enableSwipe);
    }

    private void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    private void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public void recycle() {

        // Stop tasks
        if (renderingAsyncTask != null) {
            renderingAsyncTask.cancel(true);
        }
        if (decodingAsyncTask != null) {
            decodingAsyncTask.cancel(true);
        }

        // Clear caches
        cacheManager.recycle();

        recycled = true;
        state = State.DEFAULT;
    }

    @Override
    protected void onDetachedFromWindow() {
        recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        animationManager.stopAll();
        calculateOptimalWidthAndHeight();
        loadPages();
        moveTo(calculateCenterOffsetForPage(currentFilteredPage), currentYOffset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // As I said in this class javadoc, we can think of this canvas as a huge
        // strip on which we draw all the images. We actually only draw the rendered
        // parts, of course, but we render them in the place they belong in this huge
        // strip.

        // That's where Canvas.translate(x, y) becomes very helpful.
        // This is the situation :
        //  _______________________________________________
        // |   			 |					 			   |
        // | the actual  |					The big strip  |
        // |	canvas	 | 								   |
        // |_____________|								   |
        // |_______________________________________________|
        //
        // If the rendered part is on the bottom right corner of the strip
        // we can draw it but we won't see it because the canvas is not big enough.

        // But if we call translate(-X, -Y) on the canvas just before drawing the object :
        //  _______________________________________________
        // |   			  					  _____________|
        // |   The big strip     			 |			   |
        // |		    					 |	the actual |
        // |								 |	canvas	   |
        // |_________________________________|_____________|
        //
        // The object will be on the canvas.
        // This technique is massively used in this method, and allows
        // abstraction of the screen position when rendering the parts.

        // Draws background
        canvas.drawColor(Color.WHITE);

        if (state != State.SHOWN) {
            return;
        }

        // Moves the canvas before drawing any element
        float currentXOffset = this.currentXOffset;
        float currentYOffset = this.currentYOffset;
        canvas.translate(currentXOffset, currentYOffset);

        // Draws thumb nails
        for (PagePart part : cacheManager.getThumbnails()) {
            drawPart(canvas, part);
        }

        // Draws parts
        for (PagePart part : cacheManager.getPageParts()) {
            drawPart(canvas, part);
        }

        // Draws the user layer
        if (onDrawListener != null) {
            canvas.translate(toCurrentScale(currentFilteredPage * optimalPageWidth), 0);

            onDrawListener.onLayerDrawn(canvas, //
                    toCurrentScale(optimalPageWidth), //
                    toCurrentScale(optimalPageHeight),
                    currentPage);

            canvas.translate(-toCurrentScale(currentFilteredPage * optimalPageWidth), 0);
        }

        // Restores the canvas position
        canvas.translate(-currentXOffset, -currentYOffset);

        // Draws mask around current page
        canvas.drawRect(leftMask, maskPaint);
        canvas.drawRect(rightMask, maskPaint);

        // If minimap shown draws it
        if (userWantsMinimap && miniMapRequired) {
            drawMiniMap(canvas);
        }
    }

    public void onLayerUpdate() {
        invalidate();
    }

    /** Draw a given PagePart on the canvas */
    private void drawPart(Canvas canvas, PagePart part) {
        // Can seem strange, but avoid lot of calls
        RectF pageRelativeBounds = part.getPageRelativeBounds();
        Bitmap renderedBitmap = part.getRenderedBitmap();

        // Move to the target page
        float localTranslation = toCurrentScale(part.getUserPage() * optimalPageWidth);
        canvas.translate(localTranslation, 0);

        Rect srcRect = new Rect(0, 0, renderedBitmap.getWidth(), //
                renderedBitmap.getHeight());

        float offsetX = toCurrentScale(pageRelativeBounds.left * optimalPageWidth);
        float offsetY = toCurrentScale(pageRelativeBounds.top * optimalPageHeight);
        float width = toCurrentScale(pageRelativeBounds.width() * optimalPageWidth);
        float height = toCurrentScale(pageRelativeBounds.height() * optimalPageHeight);

        // If we use float values for this rectangle, there will be
        // a possible gap between page parts, especially when
        // the zoom level is high.
        RectF dstRect = new RectF((int) offsetX, (int) offsetY, //
                (int) (offsetX + width), //
                (int) (offsetY + height));

        // Check if bitmap is in the screen
        float translationX = currentXOffset + localTranslation;
        float translationY = currentYOffset;
        if (translationX + dstRect.left >= getWidth() || translationX + dstRect.right <= 0 ||
                translationY + dstRect.top >= getHeight() || translationY + dstRect.bottom <= 0) {
            canvas.translate(-localTranslation, 0);
            return;
        }

        canvas.drawBitmap(renderedBitmap, srcRect, dstRect, paint);

        if (Constants.DEBUG_MODE) {
            debugPaint.setColor(part.getUserPage() % 2 == 0 ? Color.RED : Color.BLUE);
            canvas.drawRect(dstRect, debugPaint);
        }

        // Restore the canvas position
        canvas.translate(-localTranslation, 0);

    }

    private void drawMiniMap(Canvas canvas) {
        canvas.drawRect(minimapBounds, paintMinimapBack);
        canvas.drawRect(minimapScreenBounds, paintMinimapFront);
    }

    /**
     * Load all the parts around the center of the screen,
     * taking into account X and Y offsets, zoom level, and
     * the current page displayed
     */
    public void loadPages() {
        if (optimalPageWidth == 0 || optimalPageHeight == 0) {
            return;
        }

        // Cancel all current tasks
        renderingAsyncTask.removeAllTasks();
        cacheManager.makeANewSet();

        // Find current index in filtered user pages
        int index = currentPage;
        if (filteredUserPageIndexes != null) {
            index = filteredUserPageIndexes[currentPage];
        }

        // Loop through the pages like [...][4][2][0][1][3][...]
        // loading as many parts as it can.
        int parts = 0;
        for (int i = 0; i <= Constants.LOADED_SIZE / 2 && parts < CACHE_SIZE; i++) {
            parts += loadPage(index + i, CACHE_SIZE - parts);
            if (i != 0 && parts < CACHE_SIZE) {
                parts += loadPage(index - i, CACHE_SIZE - parts);
            }
        }

        invalidate();
    }

    /**
     * Render a page, creating 1 to <i>nbOfPartsLoadable</i> page parts. <br><br>
     * <p/>
     * This is one of the trickiest method of this library. It finds
     * the DocumentPage associated with the given UserPage, loads its
     * thumbnail, cut this page into 256x256 blocs considering the
     * current zoom level, calculate the bloc containing the center of
     * the screen, and start loading these parts in a spiral {@link SpiralLoopManager},
     * only if the given part is not already in the Cache, in which case it
     * moves the part up in the cache.
     * @param userPage          The user page to load.
     * @param nbOfPartsLoadable Maximum number of parts it can load.
     * @return The number of parts loaded.
     */
    private int loadPage(final int userPage, final int nbOfPartsLoadable) {

        // Finds the document page associated with the given userPage
        int documentPage = userPage;
        if (filteredUserPages != null) {
            if (userPage < 0 || userPage >= filteredUserPages.length) {
                return 0;
            } else {
                documentPage = filteredUserPages[userPage];
            }
        }
        final int documentPageFinal = documentPage;
        if (documentPage < 0 || userPage >= documentPageCount) {
            return 0;
        }

        // Render thumbnail of the page
        if (!cacheManager.containsThumbnail(userPage, documentPage, //
                (int) (optimalPageWidth * Constants.THUMBNAIL_RATIO), //
                (int) (optimalPageHeight * Constants.THUMBNAIL_RATIO), //
                new RectF(0, 0, 1, 1))) {
            renderingAsyncTask.addRenderingTask(userPage, documentPage, //
                    (int) (optimalPageWidth * Constants.THUMBNAIL_RATIO), //
                    (int) (optimalPageHeight * Constants.THUMBNAIL_RATIO), //
                    new RectF(0, 0, 1, 1), true, 0);
        }

        // When we want to render a 256x256 bloc, we also need to provide
        // the bounds (left, top, right, bottom) of the rendered part in
        // the PDF page. These four coordinates are ratios (0 -> 1), where
        // (0,0) is the top left corner of the PDF page, and (1,1) is the
        // bottom right corner.
        float ratioX = 1f / (float) optimalPageWidth;
        float ratioY = 1f / (float) optimalPageHeight;
        final float partHeight = (Constants.PART_SIZE * ratioY) / zoom;
        final float partWidth = (Constants.PART_SIZE * ratioX) / zoom;
        final int nbRows = (int) Math.ceil(1f / partHeight);
        final int nbCols = (int) Math.ceil(1f / partWidth);
        final float pageRelativePartWidth = 1f / (float) nbCols;
        final float pageRelativePartHeight = 1f / (float) nbRows;

        // To improve user experience, we need to start displaying the
        // 256x256 blocs with the middle of the screen. Imagine the cut
        // page as a grid. This part calculates which cell of this grid
        // is currently in the middle of the screen, given the current
        // zoom level and the offsets.
        float middleOfScreenX = (-currentXOffset + getWidth() / 2);
        float middleOfScreenY = (-currentYOffset + getHeight() / 2);
        float middleOfScreenPageX = middleOfScreenX - userPage * toCurrentScale(optimalPageWidth);
        float middleOfScreenPageY = middleOfScreenY;
        float middleOfScreenPageXRatio = middleOfScreenPageX / toCurrentScale(optimalPageWidth);
        float middleOfScreenPageYRatio = middleOfScreenPageY / toCurrentScale(optimalPageHeight);
        int startingRow = (int) (middleOfScreenPageYRatio * nbRows);
        int startingCol = (int) (middleOfScreenPageXRatio * nbCols);

        // Avoid outside values
        startingRow = NumberUtils.limit(startingRow, 0, nbRows);
        startingCol = NumberUtils.limit(startingCol, 0, nbCols);

        // Prepare the loop listener
        class SpiralLoopListenerImpl implements SpiralLoopManager.SpiralLoopListener {
            int nbItemTreated = 0;

            @Override
            public boolean onLoop(int row, int col) {

                // Create relative page bounds
                float relX = pageRelativePartWidth * col;
                float relY = pageRelativePartHeight * row;
                float relWidth = pageRelativePartWidth;
                float relHeight = pageRelativePartHeight;

                // Adjust width and height to
                // avoid being outside the page
                float renderWidth = Constants.PART_SIZE / relWidth;
                float renderHeight = Constants.PART_SIZE / relHeight;
                if (relX + relWidth > 1) {
                    relWidth = 1 - relX;
                }
                if (relY + relHeight > 1) {
                    relHeight = 1 - relY;
                }
                renderWidth *= relWidth;
                renderHeight *= relHeight;
                RectF pageRelativeBounds = new RectF(relX, relY, relX + relWidth, relY + relHeight);

                if (renderWidth != 0 && renderHeight != 0) {

                    // Check it the calculated part is already contained in the Cache
                    // If it is, this call will insure the part will go to the right
                    // place in the cache and won't be deleted if the cache need space.
                    if (!cacheManager.upPartIfContained(userPage, documentPageFinal, //
                            renderWidth, renderHeight, pageRelativeBounds, nbItemTreated)) {

                        // If not already in cache, register the rendering
                        // task for further execution.
                        renderingAsyncTask.addRenderingTask(userPage, documentPageFinal, //
                                renderWidth, renderHeight, pageRelativeBounds, false, nbItemTreated);
                    }

                }

                nbItemTreated++;
                if (nbItemTreated >= nbOfPartsLoadable) {
                    // Return false to stop the loop
                    return false;
                }
                return true;
            }
        }

        // Starts the loop
        SpiralLoopListenerImpl spiralLoopListener;
        new SpiralLoopManager(spiralLoopListener = new SpiralLoopListenerImpl())//
                .startLoop(nbRows, nbCols, startingRow, startingCol);

        return spiralLoopListener.nbItemTreated;
    }

    /** Called when the PDF is loaded */
    public void loadComplete(DecodeService decodeService) {
        this.decodeService = decodeService;
        this.documentPageCount = decodeService.getPageCount();

        // We assume all the pages are the same size
        this.pageWidth = decodeService.getPageWidth(0);
        this.pageHeight = decodeService.getPageHeight(0);
        state = State.LOADED;
        calculateOptimalWidthAndHeight();

        // Notify the listener
        jumpTo(defaultPage);
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.loadComplete(documentPageCount);
        }
    }

    /**
     * Called when a rendering task is over and
     * a PagePart has been freshly created.
     * @param part The created PagePart.
     */
    public void onBitmapRendered(PagePart part) {
        if (part.isThumbnail()) {
            cacheManager.cacheThumbnail(part);
        } else {
            cacheManager.cachePart(part);
        }
        invalidate();
    }

    /**
     * Given the UserPage number, this method restrict it
     * to be sure it's an existing page. It takes care of
     * using the user defined pages if any.
     * @param userPage A page number.
     * @return A restricted valid page number (example : -2 => 0)
     */
    private int determineValidPageNumberFrom(int userPage) {
        if (userPage <= 0) {
            return 0;
        }
        if (originalUserPages != null) {
            if (userPage >= originalUserPages.length) {
                return originalUserPages.length - 1;
            }
        } else {
            if (userPage >= documentPageCount) {
                return documentPageCount - 1;
            }
        }
        return userPage;
    }

    /**
     * Calculate the x-offset needed to have the given
     * page centered on the screen. It doesn't take into
     * account the zoom level.
     * @param pageNb The page number.
     * @return The x-offset to use to have the pageNb centered.
     */
    private float calculateCenterOffsetForPage(int pageNb) {
        float imageX = -(pageNb * optimalPageWidth);
        imageX += getWidth() / 2 - optimalPageWidth / 2;
        return imageX;
    }

    /**
     * Calculate the optimal width and height of a page
     * considering the area width and height
     */
    private void calculateOptimalWidthAndHeight() {
        if (state == State.DEFAULT || getWidth() == 0) {
            return;
        }

        float maxWidth = getWidth(), maxHeight = getHeight();
        float w = pageWidth, h = pageHeight;
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        if (h > maxHeight) {
            h = maxHeight;
            w = (float) Math.floor(maxHeight * ratio);
        }

        optimalPageWidth = w;
        optimalPageHeight = h;

        calculateMasksBounds();
        calculateMinimapBounds();
    }

    /**
     * Place the minimap background considering the optimal width and height
     * and the MINIMAP_MAX_SIZE.
     */
    private void calculateMinimapBounds() {
        float ratioX = Constants.MINIMAP_MAX_SIZE / optimalPageWidth;
        float ratioY = Constants.MINIMAP_MAX_SIZE / optimalPageHeight;
        float ratio = Math.min(ratioX, ratioY);
        float minimapWidth = optimalPageWidth * ratio;
        float minimapHeight = optimalPageHeight * ratio;
        minimapBounds = new RectF(getWidth() - 5 - minimapWidth, 5, getWidth() - 5, 5 + minimapHeight);
        calculateMinimapAreaBounds();
    }

    /**
     * Place the minimap current rectangle considering the minimap bounds
     * the zoom level, and the current X/Y offsets
     */
    private void calculateMinimapAreaBounds() {
        if (minimapBounds == null) {
            return;
        }

        if (zoom == 1f) {
            miniMapRequired = false;
        } else {
            // Calculates the bounds of the current displayed area
            float x = (-currentXOffset - toCurrentScale(currentFilteredPage * optimalPageWidth)) //
                    / toCurrentScale(optimalPageWidth) * minimapBounds.width();
            float width = getWidth() / toCurrentScale(optimalPageWidth) * minimapBounds.width();
            float y = -currentYOffset / toCurrentScale(optimalPageHeight) * minimapBounds.height();
            float height = getHeight() / toCurrentScale(optimalPageHeight) * minimapBounds.height();
            minimapScreenBounds = new RectF(minimapBounds.left + x, minimapBounds.top + y, //
                    minimapBounds.left + x + width, minimapBounds.top + y + height);
            minimapScreenBounds.intersect(minimapBounds);
            miniMapRequired = true;
        }
    }

    /** Place the left and right masks around the current page. */
    private void calculateMasksBounds() {
        leftMask = new RectF(0, 0, getWidth() / 2 - toCurrentScale(optimalPageWidth) / 2, getHeight());
        rightMask = new RectF(getWidth() / 2 + toCurrentScale(optimalPageWidth) / 2, 0, getWidth(), getHeight());
    }

    /**
     * Move to the given X and Y offsets, but check them ahead of time
     * to be sure not to go outside the the big strip.
     * @param offsetX The big strip X offset to use as the left border of the screen.
     * @param offsetY The big strip Y offset to use as the right border of the screen.
     */
    public void moveTo(float offsetX, float offsetY) {

        // Check Y offset
        if (toCurrentScale(optimalPageHeight) < getHeight()) {
            offsetY = getHeight() / 2 - toCurrentScale(optimalPageHeight) / 2;
        } else {
            if (offsetY > 0) {
                offsetY = 0;
            } else if (offsetY + toCurrentScale(optimalPageHeight) < getHeight()) {
                offsetY = getHeight() - toCurrentScale(optimalPageHeight);
            }
        }

        // Check X offset
        if (isZooming()) {
            if (toCurrentScale(optimalPageWidth) < getWidth()) {
                miniMapRequired = false;
                offsetX = getWidth() / 2 - toCurrentScale((currentFilteredPage + 0.5f) * optimalPageWidth);
            } else {
                miniMapRequired = true;
                if (offsetX + toCurrentScale(currentFilteredPage * optimalPageWidth) > 0) {
                    offsetX = -toCurrentScale(currentFilteredPage * optimalPageWidth);
                } else if (offsetX + toCurrentScale((currentFilteredPage + 1) * optimalPageWidth) < getWidth()) {
                    offsetX = getWidth() - toCurrentScale((currentFilteredPage + 1) * optimalPageWidth);
                }
            }

        } else {

            float maxX = calculateCenterOffsetForPage(currentFilteredPage + 1);
            float minX = calculateCenterOffsetForPage(currentFilteredPage - 1);
            if (offsetX < maxX) {
                offsetX = maxX;
            } else if (offsetX > minX) {
                offsetX = minX;
            }
        }

        currentXOffset = offsetX;
        currentYOffset = offsetY;
        calculateMinimapAreaBounds();
        invalidate();
    }

    /**
     * Move relatively to the current position.
     * @param dx The X difference you want to apply.
     * @param dy The Y difference you want to apply.
     * @see #moveTo(float, float)
     */
    public void moveRelativeTo(float dx, float dy) {
        moveTo(currentXOffset + dx, currentYOffset + dy);
    }

    /** Change the zoom level */
    public void zoomTo(float zoom) {
        this.zoom = zoom;
        calculateMasksBounds();
    }

    /**
     * Change the zoom level, relatively to a pivot point.
     * It will call moveTo() to make sure the given point stays
     * in the middle of the screen.
     * @param zoom  The zoom level.
     * @param pivot The point on the screen that should stays.
     */
    public void zoomCenteredTo(float zoom, PointF pivot) {
        float dzoom = zoom / this.zoom;
        zoomTo(zoom);
        float baseX = currentXOffset * dzoom;
        float baseY = currentYOffset * dzoom;
        baseX += (pivot.x - pivot.x * dzoom);
        baseY += (pivot.y - pivot.y * dzoom);
        moveTo(baseX, baseY);
    }

    /** @see #zoomCenteredTo(float, PointF) */
    public void zoomCenteredRelativeTo(float dzoom, PointF pivot) {
        zoomCenteredTo(zoom * dzoom, pivot);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public float getCurrentXOffset() {
        return currentXOffset;
    }

    public float getCurrentYOffset() {
        return currentYOffset;
    }

    public float toRealScale(float size) {
        return size / zoom;
    }

    public float toCurrentScale(float size) {
        return size * zoom;
    }

    public float getZoom() {
        return zoom;
    }

    DecodeService getDecodeService() {
        return decodeService;
    }

    public boolean isZooming() {
        return zoom != 1;
    }

    public float getOptimalPageWidth() {
        return optimalPageWidth;
    }

    private void setUserWantsMinimap(boolean userWantsMinimap) {
        this.userWantsMinimap = userWantsMinimap;
    }

    private void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void resetZoom() {
        zoomTo(1);
    }

    public void resetZoomWithAnimation() {
        animationManager.startZoomAnimation(zoom, 1f);
    }

    /** Use an asset file as the pdf source */
    public Configurator fromAsset(String assetName) {
        try {
            File pdfFile = FileUtils.fileFromAsset(getContext(), assetName);
            return fromFile(pdfFile);
        } catch (IOException e) {
            throw new FileNotFoundException(assetName + " does not exist.", e);
        }
    }

    /** Use a file as the pdf source */
    public Configurator fromFile(File file) {
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + "does not exist.");
        return new Configurator(Uri.fromFile(file));
    }

    private enum State {DEFAULT, LOADED, SHOWN}

    public class Configurator {

        private final Uri uri;

        private int[] pageNumbers = null;

        private boolean enableSwipe = true;

        private OnDrawListener onDrawListener;

        private OnLoadCompleteListener onLoadCompleteListener;

        private OnPageChangeListener onPageChangeListener;

        private int defaultPage = 1;

        private boolean showMinimap = false;

        private Configurator(Uri uri) {
            this.uri = uri;
        }

        public Configurator pages(int... pageNumbers) {
            this.pageNumbers = pageNumbers;
            return this;
        }

        public Configurator enableSwipe(boolean enableSwipe) {
            this.enableSwipe = enableSwipe;
            return this;
        }

        public Configurator onDraw(OnDrawListener onDrawListener) {
            this.onDrawListener = onDrawListener;
            return this;
        }

        public Configurator onLoad(OnLoadCompleteListener onLoadCompleteListener) {
            this.onLoadCompleteListener = onLoadCompleteListener;
            return this;
        }

        public Configurator onPageChange(OnPageChangeListener onPageChangeListener) {
            this.onPageChangeListener = onPageChangeListener;
            return this;
        }

        public Configurator defaultPage(int defaultPage) {
            this.defaultPage = defaultPage;
            return this;
        }

        public void load() {
            PDFView.this.recycle();
            PDFView.this.setOnDrawListener(onDrawListener);
            PDFView.this.setOnPageChangeListener(onPageChangeListener);
            PDFView.this.enableSwipe(enableSwipe);
            PDFView.this.setDefaultPage(defaultPage);
            PDFView.this.setUserWantsMinimap(showMinimap);
            if (pageNumbers != null) {
                PDFView.this.load(uri, onLoadCompleteListener, pageNumbers);
            } else {
                PDFView.this.load(uri, onLoadCompleteListener);
            }
        }

        public Configurator showMinimap(boolean showMinimap) {
            this.showMinimap = showMinimap;
            return this;
        }
    }
}
