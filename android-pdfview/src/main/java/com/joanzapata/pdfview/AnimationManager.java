package com.joanzapata.pdfview;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;

import com.joanzapata.pdfview.PDFView;

/**
 * @author Joan Zapata
 *         <p/>
 *         This manager is used by the PDFView to launch animations.
 *         It uses the ValueAnimator appeared in API 11 to start
 *         an animation, and call moveTo() on the PDFView as a result
 *         of each animation update.
 */
class AnimationManager {

    /** PDF View */
    private PDFView pdfView;

    private ValueAnimator animation;

    public AnimationManager(PDFView pdfView) {
        this.pdfView = pdfView;
    }

    public void startXAnimation(float xFrom, float xTo) {
        if (animation != null) {
            animation.cancel();
        }
        animation = ValueAnimator.ofFloat(xFrom, xTo);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addUpdateListener(new XAnimation());
        animation.setDuration(400);
        animation.start();
    }

    public void startZoomAnimation(float zoomFrom, float zoomTo) {
        if (animation != null) {
            animation.cancel();
        }
        animation = ValueAnimator.ofFloat(zoomFrom, zoomTo);
        animation.setInterpolator(new DecelerateInterpolator());
        ZoomAnimation zoomAnim = new ZoomAnimation();
        animation.addUpdateListener(zoomAnim);
        animation.addListener(zoomAnim);
        animation.setDuration(400);
        animation.start();
    }

    public void stopAll() {
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
    }

    class XAnimation implements AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float offset = (Float) animation.getAnimatedValue();
            pdfView.moveTo(offset, pdfView.getCurrentYOffset());
        }

    }

    class ZoomAnimation implements AnimatorUpdateListener, AnimatorListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float zoom = (Float) animation.getAnimatedValue();
            pdfView.zoomCenteredTo(zoom, new PointF(pdfView.getWidth() / 2, pdfView.getHeight() / 2));
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            pdfView.loadPages();
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

    }

}
