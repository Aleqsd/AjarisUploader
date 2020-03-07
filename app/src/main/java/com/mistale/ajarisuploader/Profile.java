package com.mistale.ajarisuploader;

import androidx.annotation.NonNull;

public class Profile {
    private String name;
    private String login;
    private String pwd;
    private String url;
    private Base base;
    private String importProfile;

    public Profile() {
        this.setBase(new Base());
        this.setName("");
        this.setImportProfile("");
        this.setLogin("");
        this.setPwd("");
        this.setUrl("");
    }

    public Profile(String name, String login, String pwd, String url, Base base, String importProfile) {
        this.setBase(base);
        this.setName(name);
        this.setImportProfile(importProfile);
        this.setLogin(login);
        this.setPwd(pwd);
        this.setUrl(url);
    }

    public Base getBase() {
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

    public void setBase(Base base) {
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
        return this.getName() + ";" + this.getLogin() + ";" + this.getPwd() + ";" + this.getUrl() + ";" + this.getBase().toString() + ";" + this.getImportProfile();
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
        Base base = Base.stringToBase(profileString[4]);
        String importProfile = profileString[5];
        return new Profile(name, login, pwd, url, base, importProfile);
    }

    public boolean isEmpty() {
        return this.getName().equals("") && this.getLogin().equals("") && this.getPwd().equals("") && this.getUrl().equals("") && this.getBase().isEmpty() && this.getImportProfile().equals("");
    }

    public boolean equals(Profile profile) {
        return this.getName().equals(profile.getName()) &&
               this.getLogin().equals(profile.getLogin()) &&
                this.getUrl().equals(profile.getUrl()) &&
                this.getPwd().equals(profile.getPwd()) &&
                this.getImportProfile().equals(profile.getImportProfile()) &&
                this.getBase().equals(profile.getBase());
    }
}