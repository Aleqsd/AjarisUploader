package com.mistale.ajarisuploader;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class UploadHistory {

    private static final String HistoryPREFERENCES = "@AjarisUploaderHistory" ;
    private static final String HistoryKEY = "uploads" ;

    public static ArrayList<Contribution> getPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(HistoryPREFERENCES, Context.MODE_PRIVATE);
        ArrayList<Contribution> contributions = new ArrayList<>();
        String[] contributionsString = sharedPref.getString(HistoryKEY, "").split(",");
        for (int i = 0; i < contributionsString.length; i++) {
            if(!Contribution.stringToContribution(contributionsString[i]).isEmpty()) {
                System.out.println("JE SUIS ICI");
                contributions.add(Contribution.stringToContribution(contributionsString[i]));
            }
        }
        return contributions;
    }

    public static void savePreferences(ArrayList<Contribution> contributions, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(HistoryPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String contributionsString = "";
        for (Contribution contribution : contributions) {
            contributionsString += contribution.toString() + ",";
        }
        editor.putString(HistoryKEY, contributionsString.replaceAll(",$", ""));
        editor.commit();
    }

    public static void addPreference(Contribution contribution, Context context) {
        if(contribution.isEmpty()) return;
        ArrayList<Contribution> contributions = UploadHistory.getPreferences(context);
        if(contributions.isEmpty()) {
            contributions.add(contribution);
        } else if(contributions.get(0).isEmpty()) {
            contributions.set(0, contribution);
        } else {
            int position = -1;
            for(int i = 0; i < contributions.size(); i++) {
                if(contributions.get(i).getId() == contribution.getId()) {
                    position = i;
                    break;
                }
            }
            if(position > -1) {
                contributions.set(position, contribution);
            } else {
                contributions.add(contribution);
            }
        }
        UploadHistory.savePreferences(contributions, context);
    }

    public static void addPreferenceToPosition(Contribution contribution, int position, Context context) {
        if(contribution.isEmpty()) return;
        ArrayList<Contribution> contributions = UploadHistory.getPreferences(context);
        if(position > contributions.size()) return;
        if(contributions.isEmpty()) {
            contributions.add(contribution);
        } else if(position < contributions.size()){
            ArrayList<Contribution> c = new ArrayList<>();
            for(int i = position; i < contributions.size(); i++) {
                c.add(contributions.get(i));
            }
            contributions.set(position, contribution);
            for(int i = position + 1; i < contributions.size(); i++) {
                contributions.remove(i);
            }
            for(int i = 0; i < c.size(); i++) {
                contributions.add(c.get(i));
            }
        } else {
            contributions.add(contribution);
        }
        UploadHistory.savePreferences(contributions, context);
    }

    public static void removePreference(Contribution contribution, Context context) {
        ArrayList<Contribution> contributions = UploadHistory.getPreferences(context);
        if(contributions.get(0).isEmpty()) {
            UploadHistory.removeAllPreferences(context);
        } else {
            for(int i = 0; i < contributions.size(); i++) {
                if(contributions.get(i).equals(contribution)) {
                    contributions.remove(i);
                    break;
                }
            }
        }
        UploadHistory.savePreferences(contributions, context);
    }

    public static void removePreferenceFromPosition(int position, Context context) {
        ArrayList<Contribution> contributions = UploadHistory.getPreferences(context);
        if(contributions.get(0).isEmpty()) {
            Preferences.removeAllPreferences(context);
        } else {
            contributions.remove(position);
        }
        UploadHistory.savePreferences(contributions, context);
    }

    public static void removeAllPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(HistoryPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }
}