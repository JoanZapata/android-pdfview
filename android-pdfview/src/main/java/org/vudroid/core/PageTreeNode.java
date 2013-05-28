package org.vudroid.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.ref.SoftReference;

class PageTreeNode {
    private static final int SLICE_SIZE = 65535;
    private Bitmap bitmap;
    private SoftReference<Bitmap> bitmapWeakReference;
    private boolean decodingNow;
    private final RectF pageSliceBounds;
    private final Page page;
    private PageTreeNode[] children;
    private final int treeNodeDepthLevel;
    private Matrix matrix = new Matrix();
    private final Paint bitmapPaint = new Paint();
    private DocumentView documentView;
    private boolean invalidateFlag;
    private Rect targetRect;
    private RectF targetRectF;

    PageTreeNode(DocumentView documentView, RectF localPageSliceBounds, Page page, int treeNodeDepthLevel, PageTreeNode parent) {
        this.documentView = documentView;
        this.pageSliceBounds = evaluatePageSliceBounds(localPageSliceBounds, parent);
        this.page = page;
        this.treeNodeDepthLevel = treeNodeDepthLevel;
    }

    public void updateVisibility() {
        invalidateChildren();
        if (children != null) {
            for (PageTreeNode child : children) {
                child.updateVisibility();
            }
        }
        if (isVisible()) {
            if (!thresholdHit()) {
                if (getBitmap() != null && !invalidateFlag) {
                    restoreBitmapReference();
                } else {
                    decodePageTreeNode();
                }
            }
        }
        if (!isVisibleAndNotHiddenByChildren()) {
            stopDecodingThisNode();
            setBitmap(null);
        }
    }

    public void invalidate() {
        invalidateChildren();
        invalidateRecursive();
        updateVisibility();
    }

    private void invalidateRecursive() {
        invalidateFlag = true;
        if (children != null) {
            for (PageTreeNode child : children) {
                child.invalidateRecursive();
            }
        }
        stopDecodingThisNode();
    }

    void invalidateNodeBounds() {
        targetRect = null;
        targetRectF = null;
        if (children != null) {
            for (PageTreeNode child : children) {
                child.invalidateNodeBounds();
            }
        }
    }


    void draw(Canvas canvas) {
        if (getBitmap() != null) {
            canvas.drawBitmap(getBitmap(), new Rect(0, 0, getBitmap().getWidth(), getBitmap().getHeight()), getTargetRect(), bitmapPaint);
        }
        if (children == null) {
            return;
        }
        for (PageTreeNode child : children) {
            child.draw(canvas);
        }
    }

    private boolean isVisible() {
        return RectF.intersects(documentView.getViewRect(), getTargetRectF());
    }

    private RectF getTargetRectF() {
        if (targetRectF == null) {
            targetRectF = new RectF(getTargetRect());
        }
        return targetRectF;
    }

    private void invalidateChildren() {
        if (thresholdHit() && children == null && isVisible()) {
            final int newThreshold = treeNodeDepthLevel * 2;
            children = new PageTreeNode[]
                    {
                            new PageTreeNode(documentView, new RectF(0, 0, 0.5f, 0.5f), page, newThreshold, this),
                            new PageTreeNode(documentView, new RectF(0.5f, 0, 1.0f, 0.5f), page, newThreshold, this),
                            new PageTreeNode(documentView, new RectF(0, 0.5f, 0.5f, 1.0f), page, newThreshold, this),
                            new PageTreeNode(documentView, new RectF(0.5f, 0.5f, 1.0f, 1.0f), page, newThreshold, this)
                    };
        }
        if (!thresholdHit() && getBitmap() != null || !isVisible()) {
            recycleChildren();
        }
    }

    private boolean thresholdHit() {
        float zoom = documentView.zoomModel.getZoom();
        int mainWidth = documentView.getWidth();
        float height = page.getPageHeight(mainWidth, zoom);
        return (mainWidth * zoom * height) / (treeNodeDepthLevel * treeNodeDepthLevel) > SLICE_SIZE;
    }

    public Bitmap getBitmap() {
        return bitmapWeakReference != null ? bitmapWeakReference.get() : null;
    }

    private void restoreBitmapReference() {
        setBitmap(getBitmap());
    }

    private void decodePageTreeNode() {
        if (isDecodingNow()) {
            return;
        }
        setDecodingNow(true);
        documentView.decodeService.decodePage(this, page.index, new DecodeService.DecodeCallback() {
            public void decodeComplete(final Bitmap bitmap) {
                documentView.post(new Runnable() {
                    public void run() {
                        setBitmap(bitmap);
                        invalidateFlag = false;
                        setDecodingNow(false);
                        page.setAspectRatio(documentView.decodeService.getPageWidth(page.index), documentView.decodeService.getPageHeight(page.index));
                        invalidateChildren();
                    }
                });
            }
        }, documentView.zoomModel.getZoom(), pageSliceBounds);
    }

    private RectF evaluatePageSliceBounds(RectF localPageSliceBounds, PageTreeNode parent) {
        if (parent == null) {
            return localPageSliceBounds;
        }
        final Matrix matrix = new Matrix();
        matrix.postScale(parent.pageSliceBounds.width(), parent.pageSliceBounds.height());
        matrix.postTranslate(parent.pageSliceBounds.left, parent.pageSliceBounds.top);
        final RectF sliceBounds = new RectF();
        matrix.mapRect(sliceBounds, localPageSliceBounds);
        return sliceBounds;
    }

    private void setBitmap(Bitmap bitmap) {
        if (bitmap != null && bitmap.getWidth() == -1 && bitmap.getHeight() == -1) {
            return;
        }
        if (this.bitmap != bitmap) {
            if (bitmap != null) {
                if (this.bitmap != null) {
                    this.bitmap.recycle();
                }
                bitmapWeakReference = new SoftReference<Bitmap>(bitmap);
                documentView.postInvalidate();
            }
            this.bitmap = bitmap;
        }
    }

    private boolean isDecodingNow() {
        return decodingNow;
    }

    private void setDecodingNow(boolean decodingNow) {
        if (this.decodingNow != decodingNow) {
            this.decodingNow = decodingNow;
            if (decodingNow) {
                documentView.progressModel.increase();
            } else {
                documentView.progressModel.decrease();
            }
        }
    }

    private Rect getTargetRect() {
        if (targetRect == null) {
            matrix.reset();
            matrix.postScale(page.bounds.width(), page.bounds.height());
            matrix.postTranslate(page.bounds.left, page.bounds.top);
            RectF targetRectF = new RectF();
            matrix.mapRect(targetRectF, pageSliceBounds);
            targetRect = new Rect((int) targetRectF.left, (int) targetRectF.top, (int) targetRectF.right, (int) targetRectF.bottom);
        }
        return targetRect;
    }

    private void stopDecodingThisNode() {
        if (!isDecodingNow()) {
            return;
        }
        documentView.decodeService.stopDecoding(this);
        setDecodingNow(false);
    }

    private boolean isHiddenByChildren() {
        if (children == null) {
            return false;
        }
        for (PageTreeNode child : children) {
            if (child.getBitmap() == null) {
                return false;
            }
        }
        return true;
    }

    private void recycleChildren() {
        if (children == null) {
            return;
        }
        for (PageTreeNode child : children) {
            child.recycle();
        }
        if (!childrenContainBitmaps()) {
            children = null;
        }
    }

    private boolean containsBitmaps() {
        return getBitmap() != null || childrenContainBitmaps();
    }

    private boolean childrenContainBitmaps() {
        if (children == null) {
            return false;
        }
        for (PageTreeNode child : children) {
            if (child.containsBitmaps()) {
                return true;
            }
        }
        return false;
    }

    private void recycle() {
        stopDecodingThisNode();
        setBitmap(null);
        if (children != null) {
            for (PageTreeNode child : children) {
                child.recycle();
            }
        }
    }

    private boolean isVisibleAndNotHiddenByChildren() {
        return isVisible() && !isHiddenByChildren();
    }

}
