package com.stanko.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by stan on 26.02.16.
 */
public class ScreenReceiver extends BroadcastReceiver {

    private boolean mIsScreenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            mIsScreenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            mIsScreenOff = false;
        }
    }

    public boolean isScreenOff(){
        return mIsScreenOff;
    }

}