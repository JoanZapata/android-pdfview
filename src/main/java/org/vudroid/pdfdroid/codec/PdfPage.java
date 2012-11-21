package org.vudroid.pdfdroid.codec;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import org.vudroid.core.codec.CodecPage;

import java.nio.ByteBuffer;

public class PdfPage implements CodecPage
{
    private long pageHandle;
    private long docHandle;

    private PdfPage(long pageHandle, long docHandle)
    {
        this.pageHandle = pageHandle;
        this.docHandle = docHandle;
    }

    public boolean isDecoding()
    {
        return false;  //TODO
    }

    public void waitForDecode()
    {
        //TODO
    }

    public int getWidth()
    {
        return (int) getMediaBox().width();
    }

    public int getHeight()
    {
        return (int) getMediaBox().height();
    }

    public Bitmap renderBitmap(int width, int height, RectF pageSliceBounds)
    {
        Matrix matrix = new Matrix();
        matrix.postScale(width / getMediaBox().width(), -height / getMediaBox().height());
        matrix.postTranslate(0, height);
        matrix.postTranslate(-pageSliceBounds.left*width, -pageSliceBounds.top*height);
        matrix.postScale(1/pageSliceBounds.width(), 1/pageSliceBounds.height());
        return render(new Rect(0,0,width,height), matrix);
    }

    static PdfPage createPage(long dochandle, int pageno)
    {
        return new PdfPage(open(dochandle, pageno), dochandle);
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }

    public synchronized void recycle() {
        if (pageHandle != 0) {
            free(pageHandle);
            pageHandle = 0;
        }
    }

    private RectF getMediaBox()
    {
        float[] box = new float[4];
        getMediaBox(pageHandle, box);
        return new RectF(box[0], box[1], box[2], box[3]);
    }

    public Bitmap render(Rect viewbox, Matrix matrix)
	{
        int[] mRect = new int[4];
        mRect[0] = viewbox.left;
		mRect[1] = viewbox.top;
		mRect[2] = viewbox.right;
		mRect[3] = viewbox.bottom;

        float[] matrixSource = new float[9];
        float[] matrixArray = new float[6];
        matrix.getValues(matrixSource);
		matrixArray[0] = matrixSource[0];
		matrixArray[1] = matrixSource[3];
		matrixArray[2] = matrixSource[1];
		matrixArray[3] = matrixSource[4];
		matrixArray[4] = matrixSource[2];
		matrixArray[5] = matrixSource[5];

        int width = viewbox.width();
        int height = viewbox.height();
        int[] bufferarray = new int[width * height];
        nativeCreateView(docHandle, pageHandle, mRect, matrixArray, bufferarray);
        return Bitmap.createBitmap(bufferarray, width, height, Bitmap.Config.RGB_565);
        /*ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 2);
        render(docHandle, docHandle, mRect, matrixArray, buffer, ByteBuffer.allocateDirect(width * height * 8));
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;*/
	}

    private static native void getMediaBox(long handle, float[] mediabox);

    private static native void free(long handle);

    private static native long open(long dochandle, int pageno);

    private static native void render(long dochandle, long pagehandle,
		int[] viewboxarray, float[] matrixarray,
		ByteBuffer byteBuffer, ByteBuffer tempBuffer);

    private native void nativeCreateView(long dochandle, long pagehandle,
		int[] viewboxarray, float[] matrixarray,
		int[] bufferarray);
}
