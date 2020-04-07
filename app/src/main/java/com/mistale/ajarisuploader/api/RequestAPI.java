package com.mistale.ajarisuploader.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mistale.ajarisuploader.AddProfile;
import com.mistale.ajarisuploader.BuildConfig;

import org.w3c.dom.Document;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RequestAPI {
    private static final String TAG = "REQUESTAPI";

    private static final String CHECK = "/upCheck.do";
    private static final String LOGIN = "/upLogin.do";
    private static final String LOGOUT = "/upLogout.do";
    private static final String USER_AGENT = "Mozilla/5.0 AjarisUpLoaderMobile";
    private static final String AJAUPMO_VALUE = "Android:"+android.os.Build.VERSION.RELEASE+":"+ BuildConfig.VERSION_NAME;

    public static boolean urlIsValid(String url, ProgressDialog progressDialog, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        Document urlDocument = null;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Vérification de l'url");
            String result = getRequest.execute(url + CHECK + "?User-Agent=" + USER_AGENT).get();
            urlDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(urlDocument) == 0;
            if (result == null)
                Toast.makeText(context, "URL invalide", Toast.LENGTH_LONG).show();
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, XMLParser.getErrorMessage(urlDocument), Toast.LENGTH_LONG).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static boolean urlIsValid(String url, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        Document urlDocument = null;
        try {
            GetRequestsWithoutDialog getRequest = new GetRequestsWithoutDialog();
            String result = getRequest.execute(url + CHECK + "?User-Agent=" + USER_AGENT).get();
            urlDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(urlDocument) == 0;
            if (result == null)
                Toast.makeText(context, "URL invalide", Toast.LENGTH_LONG).show();
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, XMLParser.getErrorMessage(urlDocument), Toast.LENGTH_LONG).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static Document getLoginInfos(String url, String login, String pwd, ProgressDialog progressDialog, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        Document loginDocument = null;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Récupération du profil");
            String result = getRequest.execute(url + LOGIN + "?pseudo=" + login + "&password=" + pwd + "&ajaupmo=" + AJAUPMO_VALUE + "&User-Agent=" + USER_AGENT).get();
            loginDocument = XMLParser.readXML(result);
            isValid = XMLParser.getErrorCode(loginDocument) == 0;
            if (!isValid) {
                Toast.makeText(context, XMLParser.getErrorMessage(loginDocument), Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, XMLParser.getErrorMessage(loginDocument), Toast.LENGTH_LONG).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return null;
        }
        return loginDocument;
    }

    public static boolean isLoggedOut(String url, String jsessionid, ProgressDialog progressDialog, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        Document logoutDocument = null;
        try {
            GetRequests getRequest = new GetRequests();
            getRequest.setProgressDialog(progressDialog, "Clôture de la session");
            String result = getRequest.execute(url + LOGOUT + "?jsessionid=" + jsessionid + "&User-Agent=" + USER_AGENT).get();
            logoutDocument = XMLParser.readXML(result);
            isValid = XMLParser.getCode(logoutDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, XMLParser.getErrorMessageForLogout(logoutDocument), Toast.LENGTH_LONG).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }

    public static boolean isLoggedOut(String url, String jsessionid, Context context) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        Document logoutDocument = null;
        try {
            GetRequestsWithoutDialog getRequest = new GetRequestsWithoutDialog();
            String result = getRequest.execute(url + LOGOUT + "?jsessionid=" + jsessionid + "&User-Agent=" + USER_AGENT).get();
            logoutDocument = XMLParser.readXML(result);
            isValid = XMLParser.getCode(logoutDocument) == 0;
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(context, XMLParser.getErrorMessageForLogout(logoutDocument), Toast.LENGTH_LONG).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            isValid = false;
        }
        return isValid;
    }
}
