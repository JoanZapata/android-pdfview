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
package com.joanzapata.pdfview.util;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * @author Joan Zapata
 *         <p/>
 *         This class manage MotionEvents. Use it on your view with
 *         setOnTouchListener(dragManager);
 *         <p/>
 *         Use {@link #setOnDragListener(OnDragListener)} and {@link #setOnPinchListener(OnPinchListener)}
 *         to receive events when a drag or pinch event occurs.
 */
public class DragPinchListener implements OnTouchListener {

    /**
     * Max time a finger can stay pressed before this
     * action is considered as a non-click (in ms)
     */
    private static final long MAX_CLICK_TIME = 500;

    /**
     * Max distance a finger can move before this action
     * is considered as a non-click (in px)
     */
    private static final float MAX_CLICK_DISTANCE = 5;

    /**
     * Max time between 2 clicks to be considered as a
     * double click
     */
    private static final float MAX_DOUBLE_CLICK_TIME = 280;

    private static final int POINTER1 = 0, POINTER2 = 1;

    /** Implement this interface to receive Drag events */
    public static interface OnDragListener {

        /**
         * @param dx The differential X offset
         * @param dy The differential Y offset
         */
        void onDrag(float dx, float dy);

        /** Called when a drag event starts */
        void startDrag(float x, float y);

        /** Called when a drag event stops */
        void endDrag(float x, float y);

    }

    /** Implement this interface to receive Pinch events */
    public static interface OnPinchListener {

        /**
         * @param dr    The differential ratio
         * @param pivot The pivot point on which the redim occurs
         */
        void onPinch(float dr, PointF pivot);

    }

    /** Implement this interface to receive Double Tap events */
    public interface OnDoubleTapListener {

        /**
         * Called when a double tap happens.
         * @param x X-offset of event.
         * @param y Y-offset of event.
         */
        void onDoubleTap(float x, float y);

    }

    enum State {NONE, ZOOM, DRAG}

    private State state = State.NONE;

    private float dragLastX, dragLastY;

    private float pointer2LastX, pointer2LastY;

    private float zoomLastDistance;

    private OnDragListener onDragListener;

    private OnPinchListener onPinchListener;

    private OnDoubleTapListener onDoubleTapListener;

    private float lastDownX, lastDownY;

    private long lastClickTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            // NORMAL CASE : FIRST POINTER DOWN
            case MotionEvent.ACTION_DOWN:
                // Start dragging
                startDrag(event);
                state = State.DRAG;
                lastDownX = event.getX();
                lastDownY = event.getY();
                break;

            // NORMAL CASE : SECOND POINTER DOWN
            case MotionEvent.ACTION_POINTER_2_DOWN:
                startDrag(event);
                startZoom(event);
                state = State.ZOOM;
                break;

            // NORMAL CASE : SECOND POINTER UP
            case MotionEvent.ACTION_POINTER_2_UP:
                // End zooming, goes back to dragging
                state = State.DRAG;
                break;

            // NORMAL CASE : FIRST POINTER UP
            case MotionEvent.ACTION_UP:
                // End everything
                state = State.NONE;
                endDrag();

                // Treat clicks
                if (isClick(event, lastDownX, lastDownY, event.getX(), event.getY())) {
                    long time = System.currentTimeMillis();
                    if (time - lastClickTime < MAX_DOUBLE_CLICK_TIME) {
                        if (onDoubleTapListener != null) {
                            onDoubleTapListener.onDoubleTap(event.getX(), event.getY());
                        }
                        lastClickTime = 0;
                    } else {
                        lastClickTime = System.currentTimeMillis();
                    }
                }
                break;

            // TRICKY CASE : FIRST POINTER UP WHEN SECOND STILL DOWN
            case MotionEvent.ACTION_POINTER_1_UP:

                // FIXME Probably not the good value
                dragLastX = pointer2LastX;
                dragLastY = pointer2LastY;
                startDrag(event);
                state = State.DRAG;
                break;

            // TRICKY CASE : FIRST POINTER UP THEN DOWN WHILE SECOND POINTER STILL UP
            case MotionEvent.ACTION_POINTER_1_DOWN:
                pointer2LastX = event.getX(POINTER1);
                pointer2LastY = event.getY(POINTER1);

                startDrag(event);
                startZoom(event);
                state = State.ZOOM;
                break;

            // NORMAL CASE : MOVE
            case MotionEvent.ACTION_MOVE:

                switch (state) {
                    case ZOOM:
                        pointer2LastX = event.getX(POINTER2);
                        pointer2LastY = event.getY(POINTER2);
                        zoom(event);

                    case DRAG:
                        drag(event);
                        break;
                    default:
                        break;
                }
                break;
        }

        return true;
    }

    private void endDrag() {
        onDragListener.endDrag(dragLastX, dragLastY);
    }

    private void startZoom(MotionEvent event) {
        zoomLastDistance = distance(event);
    }

    private void zoom(MotionEvent event) {
        float zoomCurrentDistance = distance(event);

        if (onPinchListener != null) {
            onPinchListener.onPinch(zoomCurrentDistance / zoomLastDistance, //
                    new PointF(event.getX(POINTER1), event.getY(POINTER1)));
        }

        zoomLastDistance = zoomCurrentDistance;
    }

    private void startDrag(MotionEvent event) {
        dragLastX = event.getX(POINTER1);
        dragLastY = event.getY(POINTER1);
        onDragListener.startDrag(dragLastX, dragLastY);
    }

    private void drag(MotionEvent event) {
        float dragCurrentX = event.getX(POINTER1);
        float dragCurrentY = event.getY(POINTER1);

        if (onDragListener != null) {
            onDragListener.onDrag(dragCurrentX - dragLastX,
                    dragCurrentY - dragLastY);
        }

        dragLastX = dragCurrentX;
        dragLastY = dragCurrentY;
    }

    /** Calculates the distance between the 2 current pointers */
    private float distance(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0;
        }
        return PointF.length(event.getX(POINTER1) - event.getX(POINTER2), //
                event.getY(POINTER1) - event.getY(POINTER2));
    }

    /**
     * Test if a MotionEvent with the given start and end offsets
     * can be considered as a "click".
     * @param upEvent The final finger-up event.
     * @param xDown   The x-offset of the down event.
     * @param yDown   The y-offset of the down event.
     * @param xUp     The x-offset of the up event.
     * @param yUp     The y-offset of the up event.
     * @return true if it's a click, false otherwise
     */
    private boolean isClick(MotionEvent upEvent, float xDown, float yDown, float xUp, float yUp) {
        if (upEvent == null) return false;
        long time = upEvent.getEventTime() - upEvent.getDownTime();
        float distance = PointF.length( //
                xDown - xUp, //
                yDown - yUp);
        return time < MAX_CLICK_TIME && distance < MAX_CLICK_DISTANCE;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    public void setOnPinchListener(OnPinchListener onPinchListener) {
        this.onPinchListener = onPinchListener;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.onDoubleTapListener = onDoubleTapListener;
    }

}
