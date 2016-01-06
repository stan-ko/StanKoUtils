package com.stanko.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by stan on 06.01.16.
 */
public class IntentHelper {

    public static void openUrlInBrowser(final Context context, final String url){
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        // could be device with no browser installed
        try {
            context.startActivity(browserIntent);
//        } catch (SecurityException e) {
//            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
