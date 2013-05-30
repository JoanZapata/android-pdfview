
package com.joanzapata;

import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.sample.R;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;

import java.io.*;

@EActivity(R.layout.activity_main)
public class PDFViewActivity extends SherlockActivity implements OnPageChangeListener, OnDrawListener, OnLoadCompleteListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    @ViewById
    PDFView pdfView;

    @AfterViews
    void afterViews() {
        OnDrawListener onDrawListener = this;
        OnPageChangeListener onPageChangeListener = this;
        OnLoadCompleteListener onLoadCompleteListener = this;

        pdfView.fromAsset("sample.pdf")
                .pages(0, 2, 1, 3)
                .defaultPage(0)
                .showMinimap(false)
                .enableSwipe(true)
                .onDraw(onDrawListener)
                .onLoad(onLoadCompleteListener)
                .onPageChange(onPageChangeListener)
                .load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater();
        return true;
    }

    @Override
    public void onPageChanged(int page) {
        Log.i(TAG, "Page changed to " + page);
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        Log.i(TAG, "Layer drawn");
    }

    @Override
    public void loadComplete(int nbPages) {
        Log.i(TAG, "Load complete");
    }
}
