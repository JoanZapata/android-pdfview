package org.vudroid.core;

public class VuDroidLibraryLoader
{
    private static boolean alreadyLoaded = false;

    public static void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        System.loadLibrary("vudroid");
        alreadyLoaded = true;
    }
}
