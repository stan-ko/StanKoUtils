/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stanko.tools;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

/**
 * Class containing some static utility methods.
 * 
  	int	BASE	October 2008: The original, first, version of Android.
	int	BASE_1_1	February 2009: First Android update, officially called 1.1.
	int	CUPCAKE	May 2009: Android 1.5.
	int	CUR_DEVELOPMENT	Magic version number for a current development build, which has not yet turned into an official release.
	int	DONUT	September 2009: Android 1.6.
	int	ECLAIR	November 2009: Android 2.0 

	Applications targeting this or a later release will get these new changes in behavior:
 	The Service.onStartCommand function will return the new START_STICKY behavior instead of the old compatibility START_STICKY_COMPATIBILITY.
	int	ECLAIR_0_1	December 2009: Android 2.0.1 
	int	ECLAIR_MR1	January 2010: Android 2.1 
	int	FROYO	June 2010: Android 2.2 
	int	GINGERBREAD	November 2010: Android 2.3 

	Applications targeting this or a later release will get these new changes in behavior:
 	The application's notification icons will be shown on the new dark status bar background, so must be visible in this situation.
	int	GINGERBREAD_MR1	February 2011: Android 2.3.3.
	int	HONEYCOMB	February 2011: Android 3.0.
	int	HONEYCOMB_MR1	May 2011: Android 3.1.
	int	HONEYCOMB_MR2	June 2011: Android 3.2.
	int	ICE_CREAM_SANDWICH	October 2011: Android 4.0.
	int	ICE_CREAM_SANDWICH_MR1	December 2011: Android 4.0.3.
	int	JELLY_BEAN	June 2012: Android 4.1.
	int	JELLY_BEAN_MR1	Android 4.2: Moar jelly beans! 

	Applications targeting this or a later release will get these new changes in behavior:
	Content Providers: The default value of android:exported is now false.
	int	JELLY_BEAN_MR2	Android 4.3: Jelly Bean MR2, the revenge of the beans.
 * 
 */
public class SdkBuildUtils {
	
    private SdkBuildUtils() {};

    @TargetApi(11)
    public static void enableStrictMode() {
        if (SdkBuildUtils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

//            if (SdkBuildUtils.hasHoneycomb()) {
//                threadPolicyBuilder.penaltyFlashScreen();
//               vmPolicyBuilder 
//                        .setClassInstanceLimit(PhotoAlbumActivity.class, 1)
//                        .setClassInstanceLimit(PhotoDetailActivity.class, 1);
//            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    	//StrictMode.enableDefaults();
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	// SDK VERSION												//
	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
    public static boolean hasAPI7(){ //ECLAIR_MR1
   		return isAPI7() || hasAPI8();
    }
    
    public static boolean hasAPI8(){ //FROYO;
    	return isAPI8() || hasAPI9();
    }
    
    public static boolean hasAPI9(){ //GINGERBREAD
    	return isAPI9() || hasAPI10();
    }

    public static boolean hasAPI10(){ //GINGERBREAD_MR1
    	return isAPI10() || hasAPI11();
    }

    public static boolean hasAPI11(){ //HONEYCOMB
    	return isAPI11() || hasAPI12();
    }

    public static boolean hasAPI12(){ //HONEYCOMB_MR1
    	return isAPI12() || hasAPI13();
    }
    
    public static boolean hasAPI13(){ //HONEYCOMB_MR2
    	return isAPI13() || hasAPI14();
    }
    
    public static boolean hasAPI14(){ //ICE_CREAM_SANDWICH
    	return isAPI14() || hasAPI15();
    }
    
    public static boolean hasAPI15(){ //ICE_CREAM_SANDWICH_MR1
    	return isAPI15() || hasAPI16();
    }
    
    public static boolean hasAPI16(){ //JELLY_BEAN
    	return isAPI16() || hasAPI17();
    }
    
    public static boolean hasAPI17(){ //JELLY_BEAN_MR1
    	return isAPI17() || hasAPI18();
    }

    public static boolean hasAPI18(){ //???
    	return isAPI18();
    }

    public static boolean isAPI7(){ //ECLAIR_MR1
    	Boolean isAPI7 = null;
    	try{
    		isAPI7 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
    	} catch (Exception e){}
    	
    	if (isAPI7 == null)
    		isAPI7 = Build.VERSION.RELEASE.startsWith("2.1");

   		return isAPI7.booleanValue();
    }
    
    public static boolean isAPI8(){ //FROYO;
    	Boolean isAPI8 = null;
    	try{
    		isAPI8 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    	} catch (Exception e){}
    	
    	if (isAPI8 == null)
    		isAPI8 = Build.VERSION.RELEASE.startsWith("2.2");

   		return isAPI8.booleanValue();
    }
    
    public static boolean isAPI9(){ //GINGERBREAD
    	Boolean isAPI9 = null;
    	try{
    		isAPI9 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    	} catch (Exception e){}
    	
    	if (isAPI9 == null)
    		isAPI9 = Build.VERSION.RELEASE.startsWith("2.3") &&  !Build.VERSION.RELEASE.startsWith("2.3.");

    	return isAPI9.booleanValue();
    }

    public static boolean isAPI10(){ //GINGERBREAD_MR1
    	Boolean isAPI10 = null;
    	try{
    		isAPI10 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
    	} catch (Exception e){}
    	
    	if (isAPI10 == null)
    		isAPI10 = Build.VERSION.RELEASE.startsWith("2.3.3") || Build.VERSION.RELEASE.startsWith("2.3.4") || Build.VERSION.RELEASE.startsWith("2.3.5");

   		return isAPI10.booleanValue();
    }

    public static boolean isAPI11(){ //HONEYCOMB
    	Boolean isAPI11 = null;
    	try{
    		isAPI11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    	} catch (Exception e){}
    	
    	if (isAPI11 == null)
    		isAPI11 = Build.VERSION.RELEASE.startsWith("3.0");

   		return isAPI11.booleanValue();
    }

    public static boolean isAPI12(){ //HONEYCOMB_MR1
    	Boolean isAPI12 = null;
    	try{
    		isAPI12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    	} catch (Exception e){}
    	
    	if (isAPI12 == null)
    		isAPI12 = Build.VERSION.RELEASE.startsWith("3.1");

   		return isAPI12.booleanValue();
    }
    
    public static boolean isAPI13(){ //HONEYCOMB_MR2
    	Boolean isAPI13 = null;
    	try{
    		isAPI13 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    	} catch (Exception e){}
    	
    	if (isAPI13 == null)
    		isAPI13 = Build.VERSION.RELEASE.startsWith("3.2");

   		return isAPI13.booleanValue();    	
    }
    
    public static boolean isAPI14(){ //ICE_CREAM_SANDWICH
    	Boolean isAPI14 = null;
    	try{
    		isAPI14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    	} catch (Exception e){}
    	
    	if (isAPI14 == null)
    		isAPI14 = Build.VERSION.RELEASE.startsWith("4.0");

   		return isAPI14.booleanValue();    	
    }
    
    public static boolean isAPI15(){ //ICE_CREAM_SANDWICH_MR1
    	Boolean isAPI15 = null;
    	try{
    		isAPI15 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    	} catch (Exception e){}
    	
    	if (isAPI15 == null)
    		isAPI15 = Build.VERSION.RELEASE.startsWith("4.0.3");

   		return isAPI15.booleanValue();    	
    }
    
    public static boolean isAPI16(){ //JELLY_BEAN
    	Boolean isAPI16 = null;
    	try{
    		isAPI16 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    	} catch (Exception e){}
    	
    	if (isAPI16 == null)
    		isAPI16 = Build.VERSION.RELEASE.startsWith("4.1");

   		return isAPI16.booleanValue();    	
    }
    
    public static boolean isAPI17(){ //JELLY_BEAN_MR1
    	Boolean isAPI17 = null;
    	try{
    		isAPI17 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    	} catch (Exception e){}
    	
    	if (isAPI17 == null)
    		isAPI17 = Build.VERSION.RELEASE.startsWith("4.2");

   		return isAPI17.booleanValue();    	
    }

    public static boolean isAPI18(){ //???
    	Boolean isAPI18 = null;
//    	try{
//    		isAPI18 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUR_DEVELOPMENT;
//    	} catch (Exception e){}
//    	
//    	if (isAPI18 == null)
    		isAPI18 = Build.VERSION.RELEASE.startsWith("4.3");

   		return isAPI18.booleanValue();    	
    }
    
}