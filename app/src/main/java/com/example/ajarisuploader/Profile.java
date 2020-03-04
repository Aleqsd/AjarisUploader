package com.example.ajarisuploader;

import android.util.Log;

import androidx.annotation.NonNull;

public class Profile {
    private String name;
    private String login;
    private String pwd;
    private String url;
    private int base;
    private String importProfile;

    public Profile() {
        this.setBase(0);
        this.setName("");
        this.setImportProfile("");
        this.setLogin("");
        this.setPwd("");
        this.setUrl("");
    }

    public Profile(String name, String login, String pwd, String url, int base, String importProfile) {
        this.setBase(base);
        this.setName(name);
        this.setImportProfile(importProfile);
        this.setLogin(login);
        this.setPwd(pwd);
        this.setUrl(url);
    }

    public int getBase() {
        return base;
    }

    public String getImportProfile() {
        return importProfile;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPwd() {
        return pwd;
    }

    public String getUrl() {
        return url;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public void setImportProfile(String importProfile) {
        this.importProfile = importProfile;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getName() + ";" + this.getLogin() + ";" + this.getPwd() + ";" + this.getUrl() + ";" + Integer.toString(this.getBase()) + ";" + this.getImportProfile();
    }

    public static Profile stringToProfile(String profile) {
        String[] profileString = profile.split(";");
        if(profileString.length != 6) {
            return new Profile();
        }
        String name = profileString[0];
        String login = profileString[1];
        String pwd = profileString[2];
        String url = profileString[3];
        int base = Integer.parseInt(profileString[4]);
        String importProfile = profileString[5];
        return new Profile(name, login, pwd, url, base, importProfile);
    }

    public boolean isEmpty() {
        if(this.getName().equals("") && this.getLogin().equals("") && this.getPwd().equals("") && this.getUrl().equals("") && this.getBase() == 0 && this.getImportProfile().equals("")) {
            return true;
        }
        return false;
    }
}