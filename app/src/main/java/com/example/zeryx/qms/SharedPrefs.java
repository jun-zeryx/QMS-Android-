package com.example.zeryx.qms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefs {
    private static SharedPrefs mInstance;
    private Context context;
    //
    private SharedPreferences preferences;

    private SharedPrefs() {
    }

    public static SharedPrefs getInstance() {
        if (mInstance == null) mInstance = new SharedPrefs();
        return mInstance;
    }

    public void Initialize(Context ctxt) {
        context = ctxt;
        //
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void setDefaults(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getDefaults(String key) {
        return preferences.getString(key,"");
    }

    public void clearAllDefaults() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public boolean checkDefaultExists(String key) {
        return preferences.contains(key);
    }
}