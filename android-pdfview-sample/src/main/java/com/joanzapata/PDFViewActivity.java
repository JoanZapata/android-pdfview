
package com.joanzapata;

import android.content.Intent;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.*;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.sample.R;

import static java.lang.String.format;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.actionbar)
public class PDFViewActivity extends SherlockActivity implements OnPageChangeListener {

    public static final String SAMPLE_FILE = "sample.pdf";

    public static final String ABOUT_FILE = "about.pdf";

    @ViewById
    PDFView pdfView;

    @NonConfigurationInstance
    String pdfName = SAMPLE_FILE;

    @NonConfigurationInstance
    Integer pageNumber = 1;

    @AfterViews
    void afterViews() {
        display(pdfName, false);
    }

    @OptionsItem
    public void about() {
        if (!displaying(ABOUT_FILE))
            display(ABOUT_FILE, true);
    }

    private void display(String assetFileName, boolean jumpToFirstPage) {
        if (jumpToFirstPage) pageNumber = 1;
        setTitle(pdfName = assetFileName);

        pdfView.fromAsset(assetFileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(format("%s %s / %s", pdfName, page, pageCount));
    }

    @Override
    public void onBackPressed() {
        if (ABOUT_FILE.equals(pdfName)) {
            display(SAMPLE_FILE, true);
        } else {
            super.onBackPressed();
        }
    }

    private boolean displaying(String fileName) {
        return fileName.equals(pdfName);
    }
}
