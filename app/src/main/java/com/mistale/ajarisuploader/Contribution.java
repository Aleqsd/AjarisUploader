package com.mistale.ajarisuploader;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Contribution {
    private int id;
    private ArrayList<Upload> uploads;

    public Contribution() {
        this.setId(-1);
        this.setUploads(null);
    }

    public Contribution(int id, ArrayList<Upload> uploads) {
        this.setId(id);
        this.setUploads(uploads);
    }

    @NonNull
    @Override
    public String toString() {
        if(this.getUploads() == null) {
            return Integer.toString(this.getId());
        }
        String uploadString = "";
        for (Upload upload : this.getUploads()) {
            uploadString += upload.toString() + ";";
        }
        uploadString.replaceAll(";$", "");
        return Integer.toString(this.getId()) + ";" + uploadString;
    }

    public static Contribution stringToContribution(String contribution) {
        String[] contributionString = contribution.split(";");
        if(contributionString.length < 1) {
            return new Contribution();
        } else if(contributionString.length == 1) {
            return new Contribution(Integer.parseInt(contributionString[0]), null);
        }
        ArrayList<Upload> uploads = new ArrayList<>();
        for(int i = 1; i < contributionString.length; i++) {
            uploads.add(Upload.stringToUpload(contributionString[i]));
        }
        return new Contribution(Integer.parseInt(contributionString[0]), uploads);
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<Upload> getUploads() {
        return this.uploads;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUploads(ArrayList<Upload> uploads) {
        this.uploads = uploads;
    }

    public boolean equals(Contribution contribution) {
        return this.getId() == contribution.getId() && this.getUploads().equals(contribution.getUploads());
    }

    public int getNumberOfUploads() {
        return this.getUploads().size();
    }

    public boolean isEmpty() {
        return this.getId() == -1 && this.getUploads() == null;
    }
}
