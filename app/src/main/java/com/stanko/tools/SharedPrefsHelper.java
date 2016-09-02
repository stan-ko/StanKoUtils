package com.stanko.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.securepreferences.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Created by Stan Koshutsky <Stan.Koshutsky@gmail.com>
 */
public class SharedPrefsHelper {

    private static final Class<SharedPrefsHelper> LOGTAG = SharedPrefsHelper.class;

    private static final String SHARED_PREFS_AVATAR_FILE_NAME = "avatar.jpg";

    private static String mLastUsedSharedPrefsName = "SharedPrefsHelper";

    private static Context appContext;

    private static boolean isSecuredMode;

    private final static HashMap<String, SharedPreferences> sharedPreferencesInstances = new HashMap<String, SharedPreferences>();

    // context NPE not safe
    public static synchronized void init(final Context context) {
        init(context, context.getApplicationContext().getPackageName());
    }

    // context NPE not safe
    public static synchronized void init(final Context context, final String sharedPrefsName) {
        SharedPrefsHelper.appContext = context.getApplicationContext();
        getSharedPreferences(appContext, sharedPrefsName);
    }

    // context NPE not safe
    public static synchronized void initSecured(final Context context) {
        Exception caughtException = null;
        try {
            initSecured(context, context.getApplicationContext().getPackageName());
        } catch (Exception e) {
            caughtException = e;
            // most probably NoSuchAlgorithmException
        }
        if (caughtException != null) {
            Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
            try {
                initSecured(context, context.getApplicationContext().getPackageName());
            } catch (Exception e) {
                // most probably NoSuchAlgorithmException
                caughtException = e;
            }
        }
        if (caughtException != null) {
            Log.e(caughtException);
            Log.e("Initializing regular (not secured) version");
            init(context);
        }
    }

    // context NPE not safe
    public static synchronized void initSecured(final Context context, final String sharedPrefsName) {
        isSecuredMode = true;
        SharedPrefsHelper.appContext = context.getApplicationContext();
        getSharedPreferences(appContext, sharedPrefsName);
    }

    public static SharedPreferences getSharedPreferences() {
        if (appContext == null && sharedPreferencesInstances.size() == 0)
            return null;
        if (!sharedPreferencesInstances.containsKey(mLastUsedSharedPrefsName))
            init(appContext, mLastUsedSharedPrefsName);
        return sharedPreferencesInstances.get(mLastUsedSharedPrefsName);
    }

    public static SharedPreferences getSharedPreferences(final String sharedPrefsName) {
        if (!TextUtils.isEmpty(sharedPrefsName) && !mLastUsedSharedPrefsName.equalsIgnoreCase(sharedPrefsName) && !sharedPreferencesInstances.containsKey(sharedPrefsName))
            return getSharedPreferences(appContext, sharedPrefsName);
        return sharedPreferencesInstances.get(sharedPrefsName);
    }

    public static SharedPreferences getSharedPreferences(final Context context) {
        if (context == null)
            return getSharedPreferences(null, null);
        else
            return getSharedPreferences(context.getApplicationContext(), context.getApplicationContext().getPackageName());
    }

    public static SharedPreferences getSharedPreferences(final Context context, final String sharedPrefsName) {
        if (TextUtils.isEmpty(sharedPrefsName))
            return sharedPreferencesInstances.get(mLastUsedSharedPrefsName);

        if (!TextUtils.isEmpty(sharedPrefsName) && !mLastUsedSharedPrefsName.equals(sharedPrefsName) && !sharedPreferencesInstances.containsKey(sharedPrefsName)) {
            mLastUsedSharedPrefsName = sharedPrefsName;
            if (appContext == null && context != null)
                appContext = context.getApplicationContext();
            if (appContext != null) {
                if (isSecuredMode)
                    sharedPreferencesInstances.put(mLastUsedSharedPrefsName, new SecurePreferences(
                            appContext,
                            Hash.getMD5(appContext.getPackageName()),
                            sharedPrefsName));
                else
                    sharedPreferencesInstances.put(mLastUsedSharedPrefsName, appContext.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE));
            }
        }
        return sharedPreferencesInstances.get(mLastUsedSharedPrefsName);
    }

    //private static SharedPreferences.Editor sharedPreferencesEditorInstance; // not a thread safe
    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        final SharedPreferences sharedPreferences = getSharedPreferences(mLastUsedSharedPrefsName);
        if (sharedPreferences != null)
            return sharedPreferences.edit();//sharedPreferencesEditorInstance;
        else
            return null;
        //return sharedPreferencesInstances.get(mLastUsedSharedPrefsName).edit();//sharedPreferencesEditorInstance;
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(final String sharedPrefsName) {
        if (!TextUtils.isEmpty(sharedPrefsName))
            return getSharedPreferencesEditor(appContext, sharedPrefsName);
        final SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null)
            return sharedPreferences.edit();//sharedPreferencesEditorInstance;
        else
            return null;
//		return getSharedPreferences().edit();//sharedPreferencesEditorInstance;
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(final Context context) {
        if (context == null)
            return getSharedPreferencesEditor(null, null);
        else
            return getSharedPreferencesEditor(context.getApplicationContext(), context.getApplicationContext().getPackageName());
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(final Context context, final String sharedPrefsName) {
        if (context == null && appContext == null && sharedPreferencesInstances.size() == 0)
            return null;

        if (!TextUtils.isEmpty(sharedPrefsName))
            init(context, sharedPrefsName);

        final SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null)
            return sharedPreferences.edit();//sharedPreferencesEditorInstance;
        else
            return null;
    }

    public static boolean put(final String theKey, final Object value) {
        return save(theKey, value);
    }

    public static boolean put(Context context, final String theKey, final Object value) {
        return save(context, theKey, value);
    }

    public static boolean put(SharedPreferences.Editor prefsEditor, final String theKey, final Object value) {
        return save(prefsEditor, theKey, value);
    }

    public static boolean save(final String theKey, final Object value) {
        return save(getSharedPreferencesEditor(), theKey, value);
    }

    public static boolean save(Context context, final String theKey, final Object value) {
        if (context == null) {
            logNullContext(theKey);
            return save(getSharedPreferencesEditor(), theKey, value);
        } else
            return save(getSharedPreferencesEditor(context), theKey, value);
    }

    public static boolean save(SharedPreferences.Editor prefsEditor, final String theKey, final Object value) {
        if (prefsEditor == null || value == null ||
                !(value instanceof String)
                        && !(value instanceof Number)
                        && !(value instanceof Boolean)
                        && !(value instanceof JSONObject || value instanceof JSONArray)
                        && !(value instanceof Serializable)) {
            Log.e(LOGTAG, new NullPointerException("save(): null or incompatible type (not String, Number, Boolean, JSONObject, JSONArray or Serializable) in parameters. Key: " + theKey + " Value: " + value));
            return false;
        }

        //final SharedPreferences.Editor prefsEditor = getSharedPreferencesEditor(context);
//		Log.d(LOGTAG,"Saving the key: "+theKey+" with value: "+value);

        if (value instanceof String)
            prefsEditor.putString(theKey, (String) value);
        else if (value instanceof Integer)
            prefsEditor.putInt(theKey, (Integer) value);
        else if (value instanceof Float)
            prefsEditor.putFloat(theKey, (float) value);
        else if (value instanceof Double)
            prefsEditor.putFloat(theKey, (float) ((double) value)); //unwrap Double and cast to float
        else if (value instanceof Long)
            prefsEditor.putLong(theKey, (long) value);
        else if (value instanceof Boolean)
            prefsEditor.putBoolean(theKey, (Boolean) value);
        else if (value instanceof JSONObject || value instanceof JSONArray)
            prefsEditor.putString(theKey, value.toString());
        else if (value instanceof Serializable)
            try {
                prefsEditor.putString(theKey, getStringFromObject((Serializable) value));
            } catch (Exception ignored) {
                return false;
            }

        return prefsEditor.commit();
    }


    public static boolean put(final String[] keys, final Object[] values) {
        return save(keys, values);
    }

    public static boolean put(Context context, final String[] keys, final Object[] values) {
        return save(context, keys, values);
    }

    public static boolean put(SharedPreferences.Editor prefsEditor, final String[] keys, final Object[] values) {
        return save(prefsEditor, keys, values);
    }

    public static boolean save(final String[] keys, final Object[] values) {
        return save(getSharedPreferencesEditor(), keys, values);
    }

    public static boolean save(Context context, final String[] keys, final Object[] values) {
        if (context == null) {
            logNullContext(keys.toString());
            return save(getSharedPreferencesEditor(), keys, values);
        } else
            return save(getSharedPreferencesEditor(context), keys, values);
    }

    public static boolean save(SharedPreferences.Editor prefsEditor, final String[] keys, final Object[] values) {
        if (prefsEditor == null || keys == null || keys.length == 0 || values == null) {
            //Log.e(LOGTAG, "save(): null in parameters");
            logNullParams(keys.toString());
            return false;
        }

        String theKey = null;
        Object value = null;
        final int keysCount = keys.length;
        final int valuesCount = values.length;
        for (int index = 0; index < keysCount; index++) {
            theKey = keys[index];
            value = null;
            if (index > valuesCount - 1)
                prefsEditor.putString(theKey, null);
            else {
                value = values[index];
                if (value instanceof String)
                    prefsEditor.putString(theKey, (String) value);
                else if (value instanceof Integer)
                    prefsEditor.putInt(theKey, (Integer) value);
                else if (value instanceof Float)
                    prefsEditor.putFloat(theKey, (float) value);
                else if (value instanceof Double)
                    prefsEditor.putFloat(theKey, (float) ((double) value)); //unwrap Double and cast to float
                else if (value instanceof Long)
                    prefsEditor.putLong(theKey, (Long) value);
                else if (value instanceof Boolean)
                    prefsEditor.putBoolean(theKey, (Boolean) value);
            }
        }

        return prefsEditor.commit();
    }

    /*
     * MAP OF VALUES
     */
    public static boolean put(final Map<String, Object> keysAndValues) {
        return save(keysAndValues);
    }

    public static boolean put(Context context, final Map<String, Object> keysAndValues) {
        return save(context, keysAndValues);
    }

    public static boolean put(SharedPreferences.Editor prefsEditor, final Map<String, Object> keysAndValues) {
        return save(prefsEditor, keysAndValues);
    }

    public static boolean save(final Map<String, Object> keysAndValues) {
        return save(getSharedPreferencesEditor(), keysAndValues);
    }

    public static boolean save(Context context, final Map<String, Object> keysAndValues) {
        if (context == null) {
            logNullContext(keysAndValues.toString());
            return save(getSharedPreferencesEditor(), keysAndValues);
        } else
            return save(getSharedPreferencesEditor(context), keysAndValues);
    }

    public static boolean save(SharedPreferences.Editor prefsEditor, final Map<String, Object> keysAndValues) {
        if (prefsEditor == null || keysAndValues == null || keysAndValues.size() == 0) {
            //Log.e(LOGTAG, "save(): null in parameters");
            logNullParams(keysAndValues.getClass().getName());
            return false;
        }

        Set<String> keys = keysAndValues.keySet();
        Object value = null;
        for (String theKey : keys) {
            value = keysAndValues.get(theKey);
            if (value == null)
                prefsEditor.putString(theKey, null);
            else {
                if (value instanceof String)
                    prefsEditor.putString(theKey, (String) value);
                else if (value instanceof Integer)
                    prefsEditor.putInt(theKey, (Integer) value);
                else if (value instanceof Float)
                    prefsEditor.putFloat(theKey, (float) value);
                else if (value instanceof Double)
                    prefsEditor.putFloat(theKey, (float) ((double) value)); //unwrap Double and cast to float
                else if (value instanceof Long)
                    prefsEditor.putLong(theKey, (Long) value);
                else if (value instanceof Boolean)
                    prefsEditor.putBoolean(theKey, (Boolean) value);
            }
        }

        return prefsEditor.commit();
    }

    /*
     * STRING VALUE
     */
    public static String getString(final String theKey, final String defaultValue) {
        return getString(getSharedPreferences(), theKey, defaultValue);
    }

    public static String getString(final String theKey) {
        return getString(getSharedPreferences(), theKey, null);
    }

    public static String getString(Context context, final String theKey) {
        return getString(context, theKey, null);
    }

    public static String getString(Context context, final String theKey, final String defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getString(getSharedPreferences(), theKey, defaultValue);
        }
        return getString(getSharedPreferences(context), theKey, defaultValue);
    }

    public static String getString(final SharedPreferences prefs, final String theKey) {
        return getString(prefs, theKey, null);
    }

    public static String getString(final SharedPreferences prefs, final String theKey, final String defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getString(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }

        //restorePreferences
        String result = defaultValue;
        //final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(theKey))
            try {
                result = prefs.getString(theKey, null);
            } catch (ClassCastException e) {
                result = null;
                e.printStackTrace();
            }

        return result;
    }


    /*
     * INT AND INTEGER VALUE
     */
    public static int getInt(final String theKey) {
        return getInteger(getSharedPreferences(), theKey, 0);
    }

    public static int getInt(final String theKey, final int defaultValue) {
        return getInteger(getSharedPreferences(), theKey, defaultValue);
    }

    public static int getInt(final Context context, final String theKey) {
        return getInt(context, theKey, 0);
    }

    public static int getInt(final Context context, final String theKey, final int defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getInteger(getSharedPreferences(), theKey, defaultValue);
        }
        return getInteger(getSharedPreferences(context), theKey, defaultValue);
    }

    public static int getInt(final SharedPreferences prefs, final String theKey) {
        return getInteger(prefs, theKey, 0);
    }

    public static Integer getInteger(final String theKey) {
        return getInteger(getSharedPreferences(), theKey, 0);
    }

    public static Integer getInteger(final String theKey, final Integer defaultValue) {
        return getInteger(getSharedPreferences(), theKey, defaultValue);
    }

    public static Integer getInteger(final Context context, final String theKey) {
        return getInteger(context, theKey, 0);
    }

    public static Integer getInteger(final Context context, final String theKey, final Integer defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getInteger(getSharedPreferences(), theKey, defaultValue);
        }
        return getInteger(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Integer getInteger(final SharedPreferences prefs, final String theKey) {
        return getInteger(prefs, theKey, null);
    }

    public static Integer getInteger(final SharedPreferences prefs, final String theKey, final Integer defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getInteger(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }
//		Log.d(LOGTAG, "Loading value for key: "+theKey+" with default: "+defaultValue);
        //restorePreferences
        Integer result = defaultValue;
        //final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(theKey))
            try {
                result = prefs.getInt(theKey, 0);
            } catch (ClassCastException e) {
                result = null;
                e.printStackTrace();
            }

        return result;
    }

    /*
     * LONG VALUE
     */
    public static Long getLong(final String theKey) {
        return getLong(getSharedPreferences(), theKey, 0L);
    }

    public static Long getLong(final String theKey, final Long defaultValue) {
        return getLong(getSharedPreferences(), theKey, defaultValue);
    }

    public static Long getLong(Context context, final String theKey) {
        return getLong(context, theKey, 0L);
    }

    public static Long getLong(Context context, final String theKey, final Long defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getLong(getSharedPreferences(), theKey, defaultValue);
        }
        return getLong(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Long getLong(final SharedPreferences prefs, final String theKey) {
        return getLong(prefs, theKey, 0L);
    }

    public static Long getLong(final SharedPreferences prefs, final String theKey, final Long defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getLong(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }

        //restorePreferences
        Long result = defaultValue;
        //final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(theKey))
            try {
                result = prefs.getLong(theKey, 0);
            } catch (ClassCastException e) {
                result = null;
                e.printStackTrace();
            }

        return result;
    }

    /*
     * DOUBLE VALUE
     */
    public static Double getDouble(final String theKey) {
        return getDouble(getSharedPreferences(), theKey, 0d);
    }

    public static Double getDouble(final String theKey, final Double defaultValue) {
        return getDouble(getSharedPreferences(), theKey, defaultValue);
    }

    public static Double getDouble(Context context, final String theKey) {
        return getDouble(context, theKey, 0d);
    }

    public static Double getDouble(Context context, final String theKey, final Double defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getDouble(getSharedPreferences(), theKey, defaultValue);
        }
        return getDouble(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Double getDouble(final SharedPreferences prefs, final String theKey) {
        return getDouble(prefs, theKey, 0d);
    }

    public static Double getDouble(final SharedPreferences prefs, final String theKey, final Double defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getDouble(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }
        if (defaultValue == null)
            return (double) getFloat(prefs, theKey, null);
        else
            return (double) getFloat(prefs, theKey, defaultValue.floatValue());
    }

    /*
     * FLOAT VALUE
     */
    public static Float getFloat(final String theKey) {
        return getFloat(getSharedPreferences(), theKey, 0f);
    }

    public static Float getFloat(final String theKey, final Float defaultValue) {
        return getFloat(getSharedPreferences(), theKey, defaultValue);
    }

    public static Float getFloat(Context context, final String theKey) {
        return getFloat(context, theKey, 0f);
    }

    public static Float getFloat(Context context, final String theKey, final Float defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getFloat(getSharedPreferences(), theKey, defaultValue);
        }
        return getFloat(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Float getFloat(final SharedPreferences prefs, final String theKey) {
        return getFloat(prefs, theKey, 0f);
    }

    public static Float getFloat(final SharedPreferences prefs, final String theKey, final Float defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getFloat(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }

        //restorePreferences
        Float result = defaultValue;
        //final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(theKey))
            try {
                result = prefs.getFloat(theKey, 0);
            } catch (ClassCastException e) {
                result = null;
                e.printStackTrace();
            }

        return result;
    }

    /*
     * BOOLEAN VALUE
     */
    public static Boolean getBoolean(final String theKey) {
        return getBoolean(getSharedPreferences(), theKey, false);
    }

    public static Boolean getBoolean(final String theKey, final Boolean defaultValue) {
        return getBoolean(getSharedPreferences(), theKey, defaultValue);
    }

    public static Boolean getBoolean(Context context, final String theKey) {
        return getBoolean(context, theKey, false);
    }

    public static Boolean getBoolean(Context context, final String theKey, final Boolean defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getBoolean(getSharedPreferences(), theKey, defaultValue);
        }
        return getBoolean(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Boolean getBoolean(final SharedPreferences prefs, final String theKey) {
        return getBoolean(prefs, theKey, false);
    }

    public static Boolean getBoolean(final SharedPreferences prefs, final String theKey, final Boolean defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            //Log.e(LOGTAG, "getBoolean(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            logNullParams(theKey);
            return null;
        }
        //restorePreferences
        Boolean result = defaultValue;
//        final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(theKey))
            try {
                result = prefs.getBoolean(theKey, false);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

        return result;
    }

    /*
     * OBJECT VALUE
     */
    public static Object getObject(final String theKey) {
        return getObject(getSharedPreferences(), theKey, null);
    }

    public static Object getObject(final String theKey, final Object defaultValue) {
        return getObject(getSharedPreferences(), theKey, defaultValue);
    }

    public static Object getObject(Context context, final String theKey) {
        return getObject(context, theKey, null);
    }

    public static Object getObject(Context context, final String theKey, final Object defaultValue) {
        if (context == null) {
            logNullContext(theKey);
            return getObject(getSharedPreferences(), theKey, defaultValue);
        }
        return getObject(getSharedPreferences(context), theKey, defaultValue);
    }

    public static Object getObject(final SharedPreferences prefs, final String theKey) {
        return getObject(prefs, theKey, null);
    }

    public static Object getObject(final SharedPreferences prefs, final String theKey, final Object defaultValue) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            logNullParams(theKey);
            //Log.e(LOGTAG, "getObject(): null in parameters. Key: "+theKey+" defaultValue: "+defaultValue);
            return null;
        }
        //restorePreferences
        if (!prefs.contains(theKey))
            return null;

        Object objectToReturn = defaultValue;
        final String objectInString = getString(prefs, theKey, null);
        if (!TextUtils.isEmpty(objectInString))
            try {
                objectToReturn = getObjectFromString(objectInString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        return objectToReturn;
    }

    private static String getStringFromObject(Serializable object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
    }

    private static Object getObjectFromString(String string) throws IOException, ClassNotFoundException {
        if (TextUtils.isEmpty(string))
            return null;
        byte[] data = Base64.decode(string, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /*
     * REMOVE
     */
    public static boolean remove(final String theKey) {
        return remove(getSharedPreferencesEditor(), theKey);
    }

    public static boolean remove(Context context, final String theKey) {
        if (context == null) {
            logNullContext(theKey);
            return remove(getSharedPreferencesEditor(), theKey);
        } else
            return remove(getSharedPreferencesEditor(context), theKey);
    }

    public static boolean remove(final SharedPreferences.Editor prefsEditor, final String theKey) {
        if (prefsEditor == null || TextUtils.isEmpty(theKey)) {
            logNullParams(theKey);
            return false;
        }

        prefsEditor.remove(theKey);
        return prefsEditor.commit();
    }


    /*
     * CHECK IF SHARED PREFS HAS/CONTAINS KV
     */
    public static boolean contains(final String theKey) {
        return has(getSharedPreferences(), theKey);
    }

    public static boolean contains(Context context, final String theKey) {
        if (context == null) {
            logNullContext(theKey);
            return has(getSharedPreferences(), theKey);
        } else
            return has(getSharedPreferences(context), theKey);
    }

    public static boolean has(final String theKey) {
        return has(getSharedPreferences(), theKey);
    }

    public static boolean has(Context context, final String theKey) {
        if (context == null) {
            logNullContext(theKey);
            return has(getSharedPreferences(), theKey);
        } else
            return has(getSharedPreferences(context), theKey);
    }

    public static boolean has(final SharedPreferences prefs, final String theKey) {
        if (prefs == null || TextUtils.isEmpty(theKey)) {
            logNullParams(theKey);
            return false;
        }

        return prefs.contains(theKey);
    }


    /*
     * CLEAR
     */
    public static void clearStorage() {
        clearStorage(getSharedPreferencesEditor());
    }

    public static void clearStorage(Context context) {
        if (context == null) {
            logNullContext("");
            clearStorage(getSharedPreferencesEditor());
        }
        clearStorage(getSharedPreferencesEditor(context));
    }

    public static void clearStorage(final SharedPreferences.Editor prefsEditor) {
        prefsEditor.clear();
        prefsEditor.commit();
        removeAvatar();
    }

    private static void logNullContext(final String theKey) {
        Log.w(LOGTAG, new NullPointerException("null context in parameters. Key: " + theKey).toString());
    }

    private static void logNullParams(final String theKey) {
        Log.e(LOGTAG, new NullPointerException("null in parameters. Key: " + theKey));
    }


    // Avatar set/get
    public static boolean saveAvatar(final Bitmap avatar) {
        return setAvatar(avatar);
    }

    public static boolean setAvatar(final Bitmap avatar) {
        if (avatar == null || appContext == null)
            return false;
        FileOutputStream fos = null;
        boolean result = true;
        try {
            fos = appContext.openFileOutput(SHARED_PREFS_AVATAR_FILE_NAME, Context.MODE_PRIVATE);
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//			setAvatarSyncronized(false);
            // this.uploadUserUpdate();
        } catch (FileNotFoundException e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
        }
        return result;
    }

    public static boolean saveAvatar(final byte[] avatar) {
        return setAvatar(avatar);
    }

    public static boolean setAvatar(final byte[] avatar) {
        if (avatar == null || appContext == null)
            return false;
        FileOutputStream fos = null;
        boolean result = true;
        try {
            fos = appContext.openFileOutput(SHARED_PREFS_AVATAR_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(avatar);
            fos.flush();
            FileUtils.sync(fos);
            // this.uploadUserUpdate();
        } catch (FileNotFoundException e) {
            result = false;
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
        }
        return result;
    }

//	public static void setAvatarSyncronized(boolean isSyncronized) {
//		SharedPrefsHelper.put(SHARED_PREFS_IS_AVATAR_SYNCED, isSyncronized);
//	}

    public static Bitmap getAvatarBitmap() {
        Bitmap avatar = null;
        File filePath = getAvatarFile();
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(filePath);
            avatar = BitmapFactory.decodeStream(fi);
        } catch (FileNotFoundException ignored) {
        } finally {
            if (fi != null)
                try {
                    fi.close();
                } catch (IOException ignored) {
                }
        }
        return avatar;
    }

    public static byte[] getAvatarBytes() {
        final File avatarFile = getAvatarFile();
        if (avatarFile == null)
            return null;
        byte[] avatar = new byte[(int) avatarFile.length()];
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(avatarFile);
            fi.read(avatar);
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        } finally {
            if (fi != null)
                try {
                    fi.close();
                } catch (IOException ignored) {
                }
        }
        return avatar;
    }

    public static File getAvatarFile() {
        if (appContext == null)
            return null;
        return appContext.getFileStreamPath(SHARED_PREFS_AVATAR_FILE_NAME);
    }


    public static boolean clearAvatar() {
        return removeAvatar();
    }

    public static boolean removeAvatar() {
        return appContext.deleteFile(SHARED_PREFS_AVATAR_FILE_NAME);
    }

}