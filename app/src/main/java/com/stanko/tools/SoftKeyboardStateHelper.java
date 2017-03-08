package com.stanko.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.ResultReceiver;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;
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
     * Constructor using activity
     *
     * @param activity
     */
    public SoftKeyboardStateHelper(final Activity activity) {
        this(activity.findViewById(android.R.id.content), false);
    }

    /**
     * Constructor using activity and initial keyboard state
     *
     * @param activity
     */
    public SoftKeyboardStateHelper(final Activity activity, boolean isSoftKeyboardOpened) {
        this(activity.findViewById(android.R.id.content), isSoftKeyboardOpened);
    }

    /**
     * Constructor using activity and a listener
     *
     * @param activity
     * @param listener
     */
    public SoftKeyboardStateHelper(final Activity activity, final SoftKeyboardStateListener listener) {
        this(activity.findViewById(android.R.id.content), false);
        addSoftKeyboardStateListener(listener);
    }

    /**
     * Constructor using activity, listener and initial keyboard state
     *
     * @param activity
     * @param listener
     */
    public SoftKeyboardStateHelper(final Activity activity, final SoftKeyboardStateListener listener, boolean isSoftKeyboardOpened) {
        this(activity.findViewById(android.R.id.content), isSoftKeyboardOpened);
        addSoftKeyboardStateListener(listener);
    }

    /**
     * Constructor using android.app.Fragment
     *
     * @param fragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SoftKeyboardStateHelper(final android.app.Fragment fragment) {
        this(fragment.getActivity().findViewById(android.R.id.content), false);
    }

    /**
     * Constructor using android.app.Fragment and initial keyboard state
     *
     * @param fragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SoftKeyboardStateHelper(final android.app.Fragment fragment, boolean isSoftKeyboardOpened) {
        this(fragment.getActivity().findViewById(android.R.id.content), isSoftKeyboardOpened);
    }

    /**
     * Constructor using android.app.Fragment and a listener
     *
     * @param fragment
     * @param listener
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SoftKeyboardStateHelper(final android.app.Fragment fragment, final SoftKeyboardStateListener listener) {
        this(fragment.getActivity().findViewById(android.R.id.content), false);
        addSoftKeyboardStateListener(listener);
    }

    /**
     * Constructor using android.app.Fragment, listener and initial keyboard state
     *
     * @param fragment
     * @param listener
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SoftKeyboardStateHelper(final android.app.Fragment fragment, final SoftKeyboardStateListener listener, boolean isSoftKeyboardOpened) {
        this(fragment.getActivity().findViewById(android.R.id.content), isSoftKeyboardOpened);
        addSoftKeyboardStateListener(listener);
    }

//    /**
//     * Constructor using android.support.v4.app.Fragment
//     *
//     * @param fragment
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public SoftKeyboardStateHelper(final android.support.v4.app.Fragment fragment) {
//        this(fragment.getActivity().findViewById(android.R.id.content), false);
//    }
//
//    /**
//     * Constructor using android.support.v4.app.Fragment and initial keyboard state
//     *
//     * @param fragment
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public SoftKeyboardStateHelper(final android.support.v4.app.Fragment fragment, boolean isSoftKeyboardOpened) {
//        this(fragment.getActivity().findViewById(android.R.id.content), isSoftKeyboardOpened);
//    }
//
//    /**
//     * Constructor using android.support.v4.app.Fragment and a listener
//     *
//     * @param fragment
//     * @param listener
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public SoftKeyboardStateHelper(final android.support.v4.app.Fragment fragment, final SoftKeyboardStateListener listener) {
//        this(fragment.getActivity().findViewById(android.R.id.content), false);
//        addSoftKeyboardStateListener(listener);
//    }
//
//    /**
//     * Constructor using android.support.v4.app.Fragment, listener and initial keyboard state
//     *
//     * @param fragment
//     * @param listener
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public SoftKeyboardStateHelper(final android.support.v4.app.Fragment fragment, final SoftKeyboardStateListener listener, boolean isSoftKeyboardOpened) {
//        this(fragment.getActivity().findViewById(android.R.id.content), isSoftKeyboardOpened);
//        addSoftKeyboardStateListener(listener);
//    }

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
     * Constructor using activity root view, listener and initial keyboard state
     *
     * @param activityRootView
     * @param listener
     */
    public SoftKeyboardStateHelper(final View activityRootView, final SoftKeyboardStateListener listener, boolean isSoftKeyboardOpened) {
        this(activityRootView, isSoftKeyboardOpened);
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
        final int heightDiff = mActivityRootView.getRootView().getHeight() - (mRect.bottom - mRect.top) - DeviceInfo.getNavigationBarHeight() - DeviceInfo.getStatusBarHeight();
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

    public static void showKeyboard(final EditText view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.requestLayout();
        view.setCursorVisible(true);
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        view.postInvalidate();
    }

    public static void hideKeyboard(final EditText... editTexts) {
        for (EditText editText : editTexts) {
            if (editText != null && editText.hasFocus()) {
                hideKeyboard(editText);
                return;
            }
        }
    }

    public static void hideKeyboard(final EditText view) {
        view.clearFocus();
//        editText.setCursorVisible(false);
        view.requestLayout();
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public static void showIme(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        // the public methods don't seem to work for me, so… reflection.
        try {
            Method showSoftInputUnchecked = InputMethodManager.class.getMethod(
                    "showSoftInputUnchecked", int.class, ResultReceiver.class);
            showSoftInputUnchecked.setAccessible(true);
            showSoftInputUnchecked.invoke(imm, 0, null);
        } catch (Exception e) {
            // ho hum
        }
    }
}