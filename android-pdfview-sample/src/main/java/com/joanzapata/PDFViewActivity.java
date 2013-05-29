
package com.joanzapata;

import android.net.Uri;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.joanzapata.pdfview.sample.R;
import fr.jzap.pdfview.impl.PDFView;
import fr.jzap.pdfview.listener.OnLoadCompleteListener;

import java.io.*;

@EActivity(R.layout.activity_main)
public class PDFViewActivity extends SherlockActivity {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    @ViewById
    PDFView pdfView;

    @AfterViews
    void afterViews() {
        try {
            File pdfFile = new File(getFilesDir(), "sample.pdf");
            if (!pdfFile.exists()) {
                copy(getAssets().open("sample.pdf"), pdfFile);
            }
            pdfView.enableSwipe();
            pdfView.load(Uri.fromFile(pdfFile), new OnLoadCompleteListener() {
                public void loadComplete(int arg0) {
                    pdfView.showPage(0);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy the initial pdf file", e);
        }
    }

    private void copy(InputStream inputStream, File output) {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(output);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater();
        return true;
    }

}
