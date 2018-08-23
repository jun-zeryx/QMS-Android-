package com.example.zeryx.qms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;

public abstract class QMS extends Application {
    /**
     * Server Credentials
     */
    static public String serverAddress = "qms-fyp.ddns.net/qms/";
    static public String serverID = "gaia";
    static public String serverPwd = "zx3356";

    /**
     * Global Variables (User)
     */
    static public Integer uid;
    static public String username;
    static public String lastName;
    static public String firstName;
    static public String nric;

    /**
     * Global Variables (Merchant)
     */
    static public Integer mid;
    static public String merchantUsername;
    static public String merchantName;

}