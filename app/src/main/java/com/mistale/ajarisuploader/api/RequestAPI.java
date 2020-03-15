package com.mistale.ajarisuploader.api;

import android.app.ProgressDialog;
import android.util.Log;

import com.mistale.ajarisuploader.R;

import org.w3c.dom.Document;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RequestAPI {
    private static final String TAG = "REQUESTAPI";

    private static final String CHECK = "/upCheck.do";
    private static final String LOGIN = "/upLogin.do";
    private static final String LOGOUT = "/upLogout.do";
    private static final String CONFIG_IMPORT = "/upSetConfigImport.do";

    public static boolean urlIsValid(String url, ProgressDialog progressDialog) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Vérification de l'url");
            String result = getRequest.execute(url + CHECK).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static Document getLoginInfos(String url, String login, String pwd, ProgressDialog progressDialog) {
        url = url.replaceAll("/$", "");
        Document document = null;
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Récupération du profil");
            String result = getRequest.execute(url + LOGIN + "?pseudo=" + login + "&password=" + pwd + "&ajaupmo=ajaupmo").get();
            document = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(document) == 0;
            if (!isValid) return null;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return null;
        }
        return document;
    }

    public static boolean isProfilValid(String url, String jsessionid, String ptoken, String config) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            String result = getRequest.execute(url + CONFIG_IMPORT + "?jsessionid=" + jsessionid + "&ptoken=" + ptoken + "&config" + config + "&ajaupmo=ajaupmo").get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static boolean isLoggedOut(String url, String jsessionid, ProgressDialog progressDialog) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Discconect");
            String result = getRequest.execute(url + LOGOUT + "?jsessionid=" + jsessionid).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }
}
