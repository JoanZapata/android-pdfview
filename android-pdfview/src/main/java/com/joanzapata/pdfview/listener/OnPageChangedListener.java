package com.joanzapata.pdfview.listener;

/**
 * Implements this interface to receive events from IPDFView
 * when a page has changed through swipe
 */
public interface OnPageChangedListener {

    /**
     * Called when the user use swipe to change page
     * @param page the new page displayed
     */
    void onPageChanged(int page);

}
