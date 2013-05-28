package fr.jzap.pdfview;

import android.net.Uri;

import fr.jzap.pdfview.listener.OnLayerDrawnListener;
import fr.jzap.pdfview.listener.OnLoadCompleteListener;
import fr.jzap.pdfview.listener.OnPageChangedListener;

/**
 * This interface allows to control a PdfView.
 * It basically consists of loading a file and
 * show a given page.
 * 
 * You don't have to unload or clear this view.
 * This will automatically be done when the view is detached.
 * 
 */
public interface IPDFView {

	/**
	 * Asynchronously load a file given its URI
	 * @param uri the URI to the PDF file
	 * @param listener the callback object
	 * @see #showPage(int) to display the first page
	 */
	void load(final Uri uri, final OnLoadCompleteListener listener);

	/**
	 * Asynchronously load a file given its URI, but only some pages.
	 * If you call this constructor, the page number you use in showPage()
	 * will be used as a key in the array, to retrieve the real page.
	 * @param uri the URI to the PDF file
	 * @param listener the callback object
	 * @param pages the list of pages to load (order is important)
	 * @see #showPage(int) to display the first page
	 */
	void load(Uri uri, OnLoadCompleteListener listener, int[] pages);
	
	/**
	 * Display the given page. Will play an animation if the
	 * target page is next to the current page.
	 * If the page doesn't exist, it will display a blank page.
	 * @param pageNb the target page 
	 */
	void showPage(int pageNb);
	
	/** Enable swipe moves (default: disabled) */
	void enableSwipe();
	
	/** Disable swipe moves (default: disabled) */
	void disableSwipe();
	
	/** Returns the above layer for user drawing on the current page */
	void setOnLayerDrawnListener(OnLayerDrawnListener onLayerDrawnListener);
	
	/** Allows user to listen for page changed events */
	void setOnPageChangedListener(OnPageChangedListener onPageChangedListener);
	
	/** Recycle the view, delete the loaded pages */
	void recycle();

	/** Call this method when the layer needs to be redrawn */
	void onLayerUpdate();
	
}
