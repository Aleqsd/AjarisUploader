package com.example.ajarisuploader;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String AjarisPREFERENCES = "@AjarisUploader" ;
    private static final String AjarisKEY = "profiles" ;

    public static String getPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(AjarisPREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getString(AjarisKEY, "");
    }

    public static void savePreferences(String profiles, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(AjarisPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AjarisKEY, profiles);
        editor.commit();
    }

    public static void addPreference(String profile, Context context) {
        // TODO
    }

    public static void removePreference(String profile, Context context) {
        // TODO
    }

    public static void removeAllPreferences() {
        // TODO
    }
}