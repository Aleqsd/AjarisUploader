package com.mistale.ajarisuploader;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Upload {
    private String file;
    private Date date;
    private String comment;
    private Profile profile;

    public Upload() {
        this.setFile("");
        this.setDate(null);
        this.setComment("");
        this.setProfile(new Profile());
    }

    public Upload(String file, Date date, String comment, Profile profile) {
        this.setFile(file);
        this.setDate(date);
        this.setComment(comment);
        this.setProfile(profile);
    }

    public String getFile() {
        return this.file;
    }

    public Date getDate() {
        return this.date;
    }

    public String getComment() {
        return this.comment;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return this.getFile() + "&upload" + date.format(this.getDate()) + "&upload" + this.getComment() + "&upload" + this.getProfile().toString();
    }

    public static Upload stringToUpload(String upload) {
        DateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String[] uploadString = upload.split("&upload");
        System.out.println(uploadString.length);
        if(uploadString.length != 4) {
            return new Upload();
        }
        try {
            System.out.println(uploadString[1]);
            return new Upload(uploadString[0], date.parse(uploadString[1]), uploadString[2], Profile.stringToProfile(uploadString[3]));
        } catch(ParseException error) {
            return new Upload();
        }
    }

    public boolean equals(Upload upload) {
        return this.getFile().equals(upload.getFile()) && this.getDate().equals(upload.getDate()) && this.getComment().equals(upload.getComment()) && this.getProfile().equals(upload.getProfile());
    }

    public boolean isEmpty() {
        return this.getFile().equals("") && this.getDate() == null && this.getComment().equals("") && this.getProfile().isEmpty();
    }
}
