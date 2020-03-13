package com.mistale.ajarisuploader;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Upload {
    private String file;
    private Date date;
    private String comment;

    public Upload() {
        this.setFile("");
        this.setDate(null);
        this.setComment("");
    }

    public Upload(String file, Date date, String comment) {
        this.setFile(file);
        this.setDate(date);
        this.setComment(comment);
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

    public void setFile(String file) {
        this.file = file;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
        return this.getFile() + "&" + date.format(this.getDate()) + "&" + this.getComment();
    }

    public static Upload stringToUpload(String upload) {
        SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
        String[] uploadString = upload.split("&");
        if(uploadString.length != 3) {
            return new Upload();
        }
        try {
            return new Upload(uploadString[0], date.parse(uploadString[1]), uploadString[2]);
        } catch(ParseException error) {
            return new Upload();
        }
    }

    public boolean equals(Upload upload) {
        return this.getFile().equals(upload.getFile()) && this.getDate().equals(upload.getDate()) && this.getComment().equals(upload.getComment());
    }

    public boolean isEmpty() {
        return this.getFile().equals("") && this.getDate() == null && this.getComment().equals("");
    }
}
