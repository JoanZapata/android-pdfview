package org.vudroid.pdfdroid.codec;

import android.content.ContentResolver;

import org.vudroid.core.VuDroidLibraryLoader;
import org.vudroid.core.codec.CodecContext;
import org.vudroid.core.codec.CodecDocument;

public class PdfContext implements CodecContext
{
    static
    {
        VuDroidLibraryLoader.load();
    }

    public CodecDocument openDocument(String fileName)
    {
        return PdfDocument.openDocument(fileName, "");
    }

    public void setContentResolver(ContentResolver contentResolver)
    {
        //TODO
    }

    public void recycle() {
    }
}
