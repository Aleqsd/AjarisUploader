package com.mistale.ajarisuploader.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RequestAPI {
    private static final String TAG = "REQUESTAPI";

    private static final String CHECK = "/upCheck.do";
    private static final String LOGIN = "/upLogin.do";
    private static final String LOGOUT = "/upLogout.do";
    private static final String USER_AGENT = "Mozilla/5.0 AjarisUpLoaderMobile";

    public static boolean urlIsValid(String url, ProgressDialog progressDialog) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Vérification de l'url");
            String result = getRequest.execute(url + CHECK + "?User-Agent=" + USER_AGENT).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static boolean urlIsValid(String url, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequestsWithoutDialog getRequest = new GetRequestsWithoutDialog();
            String result = getRequest.execute(url + CHECK + "?User-Agent=" + USER_AGENT).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, "URL du profil non valide", Toast.LENGTH_LONG).show();
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
            String result = getRequest.execute(url + LOGIN + "?pseudo=" + login + "&password=" + pwd + "&ajaupmo=" + USER_AGENT + "&User-Agent=" + USER_AGENT).get();
            document = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(document) == 0;
            if (!isValid) return null;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return null;
        }
        return document;
    }

    public static boolean isLoggedOut(String url, String jsessionid, ProgressDialog progressDialog) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Clôture de la session");
            String result = getRequest.execute(url + LOGOUT + "?jsessionid=" + jsessionid + "&User-Agent=" + USER_AGENT).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static boolean isLoggedOut(String url, String jsessionid) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequestsWithoutDialog getRequest = new GetRequestsWithoutDialog();
            String result = getRequest.execute(url + LOGOUT + "?jsessionid=" + jsessionid + "&User-Agent=" + USER_AGENT).get();
            Document lastDocument = XMLParser.readXML(result);
            isValid = XMLParser.getCode(lastDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }
}
