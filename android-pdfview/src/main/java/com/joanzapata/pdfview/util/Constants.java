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

public interface Constants {

    static final boolean DEBUG_MODE = false;

    /** Size of the minimum, in percent of the component size */
    static final float MINIMAP_MAX_SIZE = 200f;

    /** Number of pages loaded (default 3) */
    static final int LOADED_SIZE = 3;

    /** Between 0 and 1, the thumbnails quality (default 0.2) */
    static final float THUMBNAIL_RATIO = 0.2f;

    /**
     * The size of the rendered parts (default 256)
     * Tinier : a little bit slower to have the whole page rendered but more reactive.
     * Bigger : user will have to wait longer to have the first visual results
     */
    static final float PART_SIZE = 256;

    /** Transparency of masks around the main page (between 0 and 255, default 50) */
    static final int MASK_ALPHA = 20;

    /** The size of the grid of loaded images around the current point */
    static final int GRID_SIZE = 7;

    public interface Cache {

        /** The size of the cache (number of bitmaps kept) */
        static final int CACHE_SIZE = (int) Math.pow(GRID_SIZE, 2d);

        static final int THUMBNAILS_CACHE_SIZE = 4;
    }

    public interface Pinch {

        static final float MAXIMUM_ZOOM = 10;

        static final float MINIMUM_ZOOM = 1;

        /**
         * A move must be quicker than this duration and longer than
         * this distance to be considered as a quick move
         */
        static final int QUICK_MOVE_THRESHOLD_TIME = 250, //

        QUICK_MOVE_THRESHOLD_DISTANCE = 50;

    }

}
