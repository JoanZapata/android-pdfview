
package com.joanzapata;

import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.joanzapata.pdfview.sample.R;

@EActivity(R.layout.activity_main)
public class PDFViewActivity
        extends SherlockActivity {

    @ViewById
    TextView hello;

    @ViewById(R.id.hello)
    TextView truc;

    @AfterViews
    void afterViews() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater();
        return true;
    }

}
