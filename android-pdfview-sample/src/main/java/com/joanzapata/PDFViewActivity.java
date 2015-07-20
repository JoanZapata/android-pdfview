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
package com.joanzapata;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnErrorOccurredListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.sample.R;

import static java.lang.String.format;

public class PDFViewActivity extends Activity implements OnPageChangeListener, OnLoadCompleteListener,
        OnErrorOccurredListener {

    public static final String SAMPLE_FILE = "sample.pdf";

    public static final String ABOUT_FILE = "about.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView = (PDFView)findViewById(R.id.pdfView);

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .onErrorOccured(this)
                .load();
    }

    PDFView pdfView;


    String pdfName = SAMPLE_FILE;


    Integer pageNumber = 1;

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(format("%s %s / %s", pdfName, page, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        Log.d("AS  ", "f");
    }

    @Override
    public void errorOccured() {
        Log.d("error   ", "A");
    }

    private boolean displaying(String fileName) {
        return fileName.equals(pdfName);
    }
}
