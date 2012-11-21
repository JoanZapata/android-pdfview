package fr.jzap.pdfview.listener;

import android.graphics.Canvas;

/**
 * This interface allows an extern class to draw
 * something on the PDFView canvas, above all images.
 */
public interface OnLayerDrawnListener {

	/**
	 * This method is called when the PDFView is
	 * drawing its view.
	 * 
	 * The page is starting at (0,0)
	 * 
	 * @param canvas The canvas on which to draw things.
	 * @param width The width of the current page.
	 * @param height The height of the current page.
	 */
	void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage);
}
