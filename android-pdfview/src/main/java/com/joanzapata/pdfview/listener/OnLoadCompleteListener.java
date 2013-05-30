package com.joanzapata.pdfview.listener;

/**
 * Implements this interface to receive events from IPDFView
 * when loading is compete.
 */
public interface OnLoadCompleteListener {

    /**
     * Called when the PDF is loaded
     * @param nbPages the number of pages in this PDF file
     */
    void loadComplete(int nbPages);
}
