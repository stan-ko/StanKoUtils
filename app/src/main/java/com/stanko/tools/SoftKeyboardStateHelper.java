package com.stanko.tools;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

public class SoftKeyboardStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened(int keyboardHeightInPx);

        void onSoftKeyboardClosed();
    }

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<SoftKeyboardStateListener>();
    private final View mActivityRootView;
    private int lastSoftKeyboardHeightInPx;
    private boolean mIsSoftKeyboardOpened;
    private final Rect mRect = new Rect();

    /**
     * Constructor using activity root view
     *
     * @param activityRootView
     */
    public SoftKeyboardStateHelper(final View activityRootView) {
        this(activityRootView, false);
    }

    /**
     * Constructor using activity root view and a listener
     *
     * @param activityRootView
     * @param listener
     */
    public SoftKeyboardStateHelper(final View activityRootView, final SoftKeyboardStateListener listener) {
        this(activityRootView, false);
        addSoftKeyboardStateListener(listener);
    }

    /**
     * Constructor using activity root view and current keyboard state
     *
     * @param activityRootView
     * @param isSoftKeyboardOpened
     */
    public SoftKeyboardStateHelper(final View activityRootView, boolean isSoftKeyboardOpened) {
        this.mActivityRootView = activityRootView;
        this.mIsSoftKeyboardOpened = isSoftKeyboardOpened;
        DeviceInfo.init(activityRootView.getContext()); //if not initialized eventually
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        //r will be populated with the coordinates of your view that area still visible.
        mActivityRootView.getWindowVisibleDisplayFrame(mRect);
        final int heightDiff = mActivityRootView.getRootView().getHeight() - (mRect.bottom - mRect.top) - DeviceInfo.navigationBarHeight - DeviceInfo.statusBarHeight;
        if (!mIsSoftKeyboardOpened && heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
            mIsSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (mIsSoftKeyboardOpened && heightDiff < 100) {
            mIsSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.mIsSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return mIsSoftKeyboardOpened;
    }

    /**
     * Default value is zero (0)
     *
     * @return last saved keyboard height in px
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;

        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened(keyboardHeightInPx);
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }
}