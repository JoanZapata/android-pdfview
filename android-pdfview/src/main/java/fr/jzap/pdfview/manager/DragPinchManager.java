package fr.jzap.pdfview.manager;

import android.graphics.PointF;

import fr.jzap.pdfview.Constants;
import fr.jzap.pdfview.impl.PDFView;
import fr.jzap.pdfview.util.DragPinchListener;
import fr.jzap.pdfview.util.DragPinchListener.OnDoubleTapListener;
import fr.jzap.pdfview.util.DragPinchListener.OnDragListener;
import fr.jzap.pdfview.util.DragPinchListener.OnPinchListener;

/**
 * @author Joan Zapata
 * 
 * This Manager takes care of moving the PDFView,
 * set its zoom regarding user actions.
 * 
 */
public class DragPinchManager implements OnDragListener, OnPinchListener, OnDoubleTapListener {

	@SuppressWarnings("unused")
	private static final String TAG = DragPinchManager.class.getSimpleName();

	private PDFView pdfView;

	private DragPinchListener dragPinchListener;

	private long startDragTime;

	private float startDragX;

	private boolean isSwipeEnabled;

	public DragPinchManager(PDFView pdfView) {
		this.pdfView = pdfView;	
		this.isSwipeEnabled = false;
		dragPinchListener = new DragPinchListener();
		dragPinchListener.setOnDragListener(this);
		dragPinchListener.setOnPinchListener(this);
		dragPinchListener.setOnDoubleTapListener(this);
		pdfView.setOnTouchListener(dragPinchListener);
	}

	@Override
	public void onPinch(float dr, PointF pivot) {
		float wantedZoom = pdfView.getZoom()*dr;
		if (wantedZoom < Constants.MINIMUM_ZOOM){
			dr = Constants.MINIMUM_ZOOM / pdfView.getZoom();
		} else if(wantedZoom > Constants.MAXIMUM_ZOOM){
			dr = Constants.MAXIMUM_ZOOM / pdfView.getZoom();
		}

		pdfView.zoomCenteredRelativeTo(dr, pivot);

	}

	@Override
	public void startDrag(float x, float y) {
		startDragTime = System.currentTimeMillis();
		startDragX = x;
	}

	@Override
	public void onDrag(float dx, float dy) {
		if (isZooming()||isSwipeEnabled){
			pdfView.moveRelativeTo(dx, dy);
		}
	}

	@Override
	public void endDrag(float x, float y) {
		if (!isZooming()){
			if (isSwipeEnabled){
				float distance = x - startDragX;
				long time = System.currentTimeMillis() - startDragTime;	
				int diff = distance > 0 ? -1 : +1;

				if (isQuickMove(distance, time) || isPageChange(distance)){
					pdfView.showPage(pdfView.getCurrentPage()+diff);
				} else {
					pdfView.showPage(pdfView.getCurrentPage());
				}
			}
		} else  {
			pdfView.loadPages();
		}
	}

	public boolean isZooming(){
		return pdfView.isZooming();
	}

	private boolean isPageChange(float distance) {
		return Math.abs(distance)>Math.abs(pdfView.toCurrentScale(pdfView.getOptimalPageWidth())/2);
	}

	private boolean isQuickMove(float dx, long dt) {
		return Math.abs(dx)>=Constants.QUICK_MOVE_THRESHOLD_DISTANCE && //
		dt<=Constants.QUICK_MOVE_THRESHOLD_TIME;
	}

	public void setSwipeEnabled(boolean isSwipeEnabled) {
		this.isSwipeEnabled = isSwipeEnabled;
	}

	@Override
	public void onDoubleTap(float x, float y) {
		if (isZooming()){
			pdfView.resetZoomWithAnimation();
		}
	}

}
