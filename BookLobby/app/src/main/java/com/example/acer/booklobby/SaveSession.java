package com.example.acer.booklobby;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSession {

    static final String EMAIL = "email";

    static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserName(Context context, String email){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public static String getEmail(Context context){
        return getSharedPreferences(context).getString(EMAIL, "");
    }

    public static void clearEmail(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }

}
