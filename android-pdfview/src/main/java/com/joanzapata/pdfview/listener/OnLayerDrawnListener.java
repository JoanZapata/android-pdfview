package com.joanzapata.pdfview.listener;

import android.graphics.Canvas;

/**
 * This interface allows an extern class to draw
 * something on the PDFView canvas, above all images.
 */
public interface OnLayerDrawnListener {

    /**
     * This method is called when the PDFView is
     * drawing its view.
     * <p/>
     * The page is starting at (0,0)
     * @param canvas        The canvas on which to draw things.
     * @param pageWidth     The width of the current page.
     * @param pageHeight    The height of the current page.
     * @param displayedPage The current page index
     */
    void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage);
}
