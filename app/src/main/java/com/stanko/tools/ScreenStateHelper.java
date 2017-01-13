package com.stanko.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * by Devlight
 *
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 */
public class ScreenStateHelper extends BroadcastReceiver {

    private boolean mIsScreenOff;

    private IScreenStateReceiver mIScreenStateReceiver;
    public ScreenStateHelper(IScreenStateReceiver callback) {
        mIScreenStateReceiver = callback;
    }

    public ScreenStateHelper() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            mIsScreenOff = true;
            if (mIScreenStateReceiver!=null)
                mIScreenStateReceiver.onStateReceived(true);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            mIsScreenOff = false;
            if (mIScreenStateReceiver!=null)
                mIScreenStateReceiver.onStateReceived(false);
        }
    }

    public boolean isScreenOff(){
        return mIsScreenOff;
    }


    public static BroadcastReceiver registerAndGetReceiver(final Context context){
        // INITIALIZE RECEIVER
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver broadcastReceiver = new ScreenStateHelper();
        context.registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

    public static BroadcastReceiver registerAndGetReceiver(final Context context, final ScreenStateHelper.IScreenStateReceiver callback){
        // INITIALIZE RECEIVER
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver broadcastReceiver = new ScreenStateHelper(callback);
        context.registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

    public interface IScreenStateReceiver {
        void onStateReceived(boolean isScreenOff);
    }
}