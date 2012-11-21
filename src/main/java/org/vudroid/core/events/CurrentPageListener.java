package org.vudroid.core.events;

public interface CurrentPageListener
{
    void currentPageChanged(int pageIndex);

    public class CurrentPageChangedEvent extends SafeEvent<CurrentPageListener>
    {
        private final int pageIndex;

        public CurrentPageChangedEvent(int pageIndex)
        {
            this.pageIndex = pageIndex;
        }

        @Override
        public void dispatchSafely(CurrentPageListener listener)
        {
            listener.currentPageChanged(pageIndex);
        }
    }
}
