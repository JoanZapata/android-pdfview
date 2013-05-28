package org.vudroid.core.models;

import org.vudroid.core.events.CurrentPageListener;
import org.vudroid.core.events.EventDispatcher;

public class CurrentPageModel extends EventDispatcher
{
    private int currentPageIndex;

    public void setCurrentPageIndex(int currentPageIndex)
    {
        if (this.currentPageIndex != currentPageIndex)
        {
            this.currentPageIndex = currentPageIndex;
            dispatch(new CurrentPageListener.CurrentPageChangedEvent(currentPageIndex));
        }
    }
}
