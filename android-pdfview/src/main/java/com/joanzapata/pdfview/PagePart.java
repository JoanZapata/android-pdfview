package com.joanzapata.pdfview;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class PagePart {

    private int userPage;

    private int page;

    private Bitmap renderedBitmap;

    private float width, height;

    private RectF pageRelativeBounds;

    private boolean thumbnail;

    private int cacheOrder;

    public PagePart(int userPage, int page, Bitmap renderedBitmap, float width, float height, RectF pageRelativeBounds, boolean thumbnail, int cacheOrder) {
        super();
        this.userPage = userPage;
        this.page = page;
        this.renderedBitmap = renderedBitmap;
        this.pageRelativeBounds = pageRelativeBounds;
        this.thumbnail = thumbnail;
        this.cacheOrder = cacheOrder;
    }

    public int getCacheOrder() {
        return cacheOrder;
    }

    public int getPage() {
        return page;
    }

    public int getUserPage() {
        return userPage;
    }

    public Bitmap getRenderedBitmap() {
        return renderedBitmap;
    }

    public RectF getPageRelativeBounds() {
        return pageRelativeBounds;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setCacheOrder(int cacheOrder) {
        this.cacheOrder = cacheOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PagePart)) {
            return false;
        }

        PagePart part = (PagePart) obj;
        return part.getPage() == page
                && part.getUserPage() == userPage
                && part.getWidth() == width
                && part.getHeight() == height
                && part.getPageRelativeBounds().left == pageRelativeBounds.left
                && part.getPageRelativeBounds().right == pageRelativeBounds.right
                && part.getPageRelativeBounds().top == pageRelativeBounds.top
                && part.getPageRelativeBounds().bottom == pageRelativeBounds.bottom;
    }

}
