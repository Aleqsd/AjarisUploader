package com.example.ajarisuploader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Preferences {

    private static final String AjarisPREFERENCES = "@AjarisUploader" ;
    private static final String AjarisKEY = "profiles" ;

    public static ArrayList<Profile> getPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(AjarisPREFERENCES, Context.MODE_PRIVATE);
        ArrayList<Profile> profiles = new ArrayList<>();
        String[] profilesString = sharedPref.getString(AjarisKEY, "").split(",");
        for (int i = 0; i < profilesString.length; i++) {
            if(!Profile.stringToProfile(profilesString[i]).isEmpty()) {
                profiles.add(Profile.stringToProfile(profilesString[i]));
            }
        }
        return profiles;
    }

    public static void savePreferences(ArrayList<Profile> profiles, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(AjarisPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String profilesString = "";
        for (Profile profile : profiles) {
            profilesString += profile.toString() + ",";
        }
        editor.putString(AjarisKEY, profilesString.replaceAll(",$", ""));
        editor.commit();
    }

    public static void addPreference(Profile profile, Context context) {
        if(profile.isEmpty()) return;
        ArrayList<Profile> profiles = Preferences.getPreferences(context);
        if(profiles.isEmpty()) {
            profiles.add(profile);
        } else if(profiles.get(0).isEmpty()) {
            profiles.set(0, profile);
        } else {
            profiles.add(profile);
        }
        Preferences.savePreferences(profiles, context);
    }

    public static void addPreferenceToPosition(Profile profile, int position, Context context) {
        if(profile.isEmpty()) return;
        ArrayList<Profile> profiles = Preferences.getPreferences(context);
        if(position > profiles.size()) return;
        if(profiles.isEmpty()) {
            profiles.add(profile);
        } else if(position < profiles.size()){
            ArrayList<Profile> p = new ArrayList<>();
            for(int i = position; i < profiles.size(); i++) {
                p.add(profiles.get(i));
            }
            profiles.set(position, profile);
            for(int i = position + 1; i < profiles.size(); i++) {
                profiles.remove(i);
            }
            for(int i = 0; i < p.size(); i++) {
                profiles.add(p.get(i));
            }
        } else {
            profiles.add(profile);
        }
        Preferences.savePreferences(profiles, context);
    }

    public static void removePreference(Profile profile, Context context) {
        ArrayList<Profile> profiles = Preferences.getPreferences(context);
        if(profiles.get(0).isEmpty()) {
            Preferences.removeAllPreferences(context);
        } else {
            for(int i = 0; i < profiles.size(); i++) {
                if(profiles.get(i).equals(profile)) {
                    profiles.remove(i);
                    break;
                }
            }
        }
        Preferences.savePreferences(profiles, context);
    }

    public static void removePreferenceFromPosition(int position, Context context) {
        ArrayList<Profile> profiles = Preferences.getPreferences(context);
        if(profiles.get(0).isEmpty()) {
            Preferences.removeAllPreferences(context);
        } else {
            profiles.remove(position);
        }
        Preferences.savePreferences(profiles, context);
    }

    public static void removeAllPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(AjarisPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }
}