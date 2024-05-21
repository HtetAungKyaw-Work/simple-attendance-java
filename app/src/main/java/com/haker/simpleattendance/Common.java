package com.haker.simpleattendance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

public class Common {
    public static int mode = Activity.MODE_PRIVATE;
    public static String MyPREFS = "MyPreference";

    public static String getDataFromSharedPref(String key, Activity activity) {
        SharedPreferences spf = activity.getSharedPreferences(MyPREFS, mode);
        return spf.getString(key, "");
    }

    @SuppressLint("ApplySharedPref")
    public static void putDataToSharedPref(String key, String value, Activity activity) {
        SharedPreferences spf = activity.getSharedPreferences(MyPREFS, mode);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = spf.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
