package com.stanko.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by stan on 26.02.16.
 */
public class ScreenStateHelper {

    public static BroadcastReceiver registerAndGetScreenStateReceiver(final Context context){
        // INITIALIZE RECEIVER
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver broadcastReceiver = new ScreenReceiver();
        context.registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

}
