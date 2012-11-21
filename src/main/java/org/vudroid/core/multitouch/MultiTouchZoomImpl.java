package org.vudroid.core.multitouch;

import android.view.MotionEvent;

import org.vudroid.core.models.ZoomModel;

public class MultiTouchZoomImpl implements MultiTouchZoom {
    private final ZoomModel zoomModel;
    private boolean resetLastPointAfterZoom;
    private float lastZoomDistance;

    public MultiTouchZoomImpl(ZoomModel zoomModel) {
        this.zoomModel = zoomModel;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_DOWN) == MotionEvent.ACTION_POINTER_DOWN) {
            lastZoomDistance = getZoomDistance(ev);
            return true;
        }
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_UP) == MotionEvent.ACTION_POINTER_UP) {
            lastZoomDistance = 0;
            zoomModel.commit();
            resetLastPointAfterZoom = true;
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE && lastZoomDistance != 0) {
            float zoomDistance = getZoomDistance(ev);
            zoomModel.setZoom(zoomModel.getZoom() * zoomDistance / lastZoomDistance);
            lastZoomDistance = zoomDistance;
            return true;
        }
        return false;
    }

    private float getZoomDistance(MotionEvent ev) {
        return (float) Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2) + Math.pow(ev.getY(0) - ev.getY(1), 2));
    }

    public boolean isResetLastPointAfterZoom() {
        return resetLastPointAfterZoom;
    }

    public void setResetLastPointAfterZoom(boolean resetLastPointAfterZoom) {
        this.resetLastPointAfterZoom = resetLastPointAfterZoom;
    }
}
