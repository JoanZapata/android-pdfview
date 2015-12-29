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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnErrorListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.sample.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static java.lang.String.format;

public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener {

    public static final String SAMPLE_FILE = "sample.pdf";

    public static final String ABOUT_FILE = "about.pdf";

    @Bind(R.id.pdfView) PDFView pdfView;

    String pdfName = SAMPLE_FILE;

    Integer pageNumber = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        display(pdfName, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                if (!displaying(ABOUT_FILE)) {
                    display(ABOUT_FILE, true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void display(String assetFileName, boolean jumpToFirstPage) {
        if (jumpToFirstPage) pageNumber = 1;
        setTitle(pdfName = assetFileName);

        pdfView.fromAsset(assetFileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(PDFViewActivity.this, "Ops, got an error: " + t.toString(), Toast.LENGTH_LONG).show();
                    }
                })
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
