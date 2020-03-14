package com.example.ajarisuploader;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ajarisuploader.api.MultipartRequest;
import com.example.ajarisuploader.api.NukeSSLCerts;
import com.example.ajarisuploader.api.ProgressRequestBody;
import com.example.ajarisuploader.api.RequestAPI;
import com.example.ajarisuploader.api.RequeteService;
import com.example.ajarisuploader.api.RestService;
import com.example.ajarisuploader.api.VolleyMultipartRequest;
import com.example.ajarisuploader.api.XMLParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


/***
 * Activité visée à s'ouvrir par l'intent du share
 */
public class UploadActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks {

    private static final String TAG = "UPL";
    private static final String demoUrl = "https://demo-interne.ajaris.com/Demo";
    private static final String demoLogin = "mistale";
    private static final String demoPwd = "software";
    private String sessionid;
    private String ptoken;
    private String config;
    private TextView textView;
    private ImageView imageView;
    private Uri uri;
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Voir si il faut l'enlever
        //new NukeSSLCerts().nuke();

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.example.ajarisuploader";

        textView = findViewById(R.id.urisTextView);
        imageView = findViewById(R.id.imageView);
        Button buttonUpload = findViewById(R.id.buttonUpload);

        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null) {
            uri = intent.getParcelableExtra("URI");
            textView.setText(Objects.requireNonNull(uri).getPath());
            imageView.setImageURI(uri);
            showDemoNotification();
        } else if (intent.getParcelableArrayListExtra("URI") != null) {
            StringBuilder urisString = new StringBuilder();
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra("URI");
            for (Uri singleUri : Objects.requireNonNull(uris))
                urisString.append(singleUri.getPath());
            textView.setText(urisString);
            showDemoNotification();
        } else
            Log.e("IntentError", "L'intent reçu est invalide");

        verifyStoragePermissions(this);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (RequestAPI.urlIsValid(demoUrl))
                customConnexion();
                //httpClientMaybe();
            }
        });

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    private void disconnect() {
        if (RequestAPI.isLoggedOut(demoUrl, sessionid))
            textView.setText("Disconnected !");
        else
            textView.setText("Still connected :/");
    }

    private void upLogin() {
        Document doc = RequestAPI.getLoginInfos(demoUrl, demoLogin, demoPwd);
        if (doc == null) textView.setText("Error during login");
        textView.setText(MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(doc, "sessionid")));
        sessionid = XMLParser.getDocumentTag(doc, "sessionid");
        ptoken = XMLParser.getDocumentTag(doc, "ptoken");
        config = XMLParser.getConfig(doc);
        yesAnotherTry();
    }

    private void loginLikePostman() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://demo-interne.ajaris.com/Demo/upLogin.do?pseudo=mistale&password=software&ajaupmo=test")
                .method("POST", body)
                .addHeader("Content-Type", "text/plain")
                .build();
        try {
            Response response = client.newCall(request).execute();
            textView.setText(response.toString());
            //uploadWithRetrofit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean upSetConfigImport() {
        return RequestAPI.isProfilValid(demoUrl, sessionid, ptoken, config);
    }

    private void uploadWithAUS() {
        try {

            File file = new File(getPath(uri));
            if (file.exists())
                Log.d(TAG, "File Exist");

            //TODO arrayparameter et refactor
            MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(this, demoUrl + "/upImportDoc.do");
            multipartUploadRequest
                    .addFileToUpload(file.getAbsolutePath(), "filetoupload")
                    .addHeader("Cookie","JSESSIONID="+sessionid)
                    .addParameter("jsessionid", sessionid)
                    .addParameter("ptoken", ptoken)
                    .addParameter("ajaupmo", "ajaupmo")
                    .addParameter("ContributionComment", "TestAndroid")
                    .addParameter("Document_numbasedoc", "6")
                    .addParameter("contribution", "true")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setUtf8Charset()
                    .setMaxRetries(2)
                    .startUpload();
            Log.d(TAG, "Upload request over");
            disconnect();
        } catch (FileNotFoundException | MalformedURLException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            disconnect();
        }
    }

    private void uploadWithRetrofit() {
        File image = new File(Objects.requireNonNull(getPath(uri)));

        //TODO Replace type image
        ProgressRequestBody fileBody = new ProgressRequestBody(image, "image/*",this);

        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "test");
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), "TestAndroid");
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), "6 - Generique");
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

        String sessionIdCookie = "JSESSIONID="+sessionid;

        RequeteService requeteService = RestService.getClient().create(RequeteService.class);
        Call<ResponseBody> call = requeteService.uploadProfilePicture(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.v("Upload", "success");
                builder.setContentText("Download complete")
                        .setProgress(0,0,false);

                notificationManager.notify(1, builder.build());
                disconnect();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                builder.setContentText("Upload error")
                        .setProgress(0,0,false);

                notificationManager.notify(1, builder.build());
                disconnect();
            }
        });
    }

    private void uploadWithVolley() {
        String url = "https://demo-interne.ajaris.com/Demo/upImportDoc.do";

        String filePath = getPath(uri);
        if (filePath != null) {
            Log.d("filePath", filePath);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        response -> {
                            textView.setText(new String(response.data));
                            Log.i(TAG, new String(response.data));
                            disconnect();
                        },
                        error -> {
                            Log.e("VolleyError : ", "" + error.networkResponse.statusCode);
                            disconnect();
                        }

                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("jsessionid", sessionid);
                        params.put("ptoken", ptoken);
                        params.put("ajaupmo", "test");
                        params.put("ContributionComment", "TestAndroid");
                        params.put("Document_numbasedoc", "6");
                        params.put("contribution", "true");
                        return params;
                    }

                    @Override
                    public Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        long imagename = System.currentTimeMillis();
                        params.put("filetoupload", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> header = new HashMap<>();
                        header.put("Cookie", "JSESSIONID=" + sessionid);
                        return header;
                    }
                };
                volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                        1000000000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                Volley.newRequestQueue(this).add(volleyMultipartRequest);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
    }

    public void executeMultipartPost() throws Exception {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(demoUrl + "/upImportDoc.do");
            ByteArrayBody bab = new ByteArrayBody(data, "forest.jpg");
            // File file= new File("/mnt/sdcard/forest.png");
            // FileBody bin = new FileBody(file);
            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("jsessionid", new StringBody(sessionid));
            reqEntity.addPart("filetoupload", bab);
            reqEntity.addPart("ptoken", new StringBody(ptoken));
            reqEntity.addPart("ajaupmo", new StringBody("ajaupmo"));
            reqEntity.addPart("ContributionComment", new StringBody("TestAndoird"));
            reqEntity.addPart("Document_numbasedoc", new StringBody("6 - Generique"));
            reqEntity.addPart("contribution", new StringBody("true"));
            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            System.out.println("Response: " + s);
        } catch (Exception e) {
            // handle exception here
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }

    public void customConnexion() {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.POST, demoUrl + "/upLogin.do",
                response -> {
                    Document doc = XMLParser.readXML(response);
                    if (doc == null) textView.setText("Error during login");
                    textView.setText(MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(doc, "sessionid")));
                    sessionid = XMLParser.getDocumentTag(doc, "sessionid");
                    Log.d(TAG, sessionid);
                    ptoken = XMLParser.getDocumentTag(doc, "ptoken");
                    config = XMLParser.getConfig(doc);
                    uploadWithRetrofit();
                },
                error -> {
                    Log.d("ERROR", "error => " + error.toString());
                }

        ) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pseudo", "mistale");
                params.put("password", "software");
                params.put("ajaupmo", "test");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", "PostmanRuntime/7.22.0");
                headers.put("Accept", "*/*");
                headers.put("Cache-Control", "no-cache");
                headers.put("Postman-Token", "9b6f114f-5479-4ea7-86ec-97895a266c37");
                headers.put("Host", "demo-interne.ajaris.com");
                headers.put("Accept-Encoding", "gzip, deflate, br");
                //headers.put("Cookie", "JSESSIONID=AF99F6C9A67A9A0D85ECE63ED84E93F0");
                //headers.put("Content-Length", "0");
                headers.put("Connection", "keep-alive");
                return headers;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i("response", response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                Log.i("cookies", rawCookies);
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(getRequest);
    }

    public void yesAnotherTry() {
        File file1 = new File(getPath(uri));
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("jsessionid", sessionid);
        params.put("ptoken", ptoken);
        params.put("ajaupmo", "iOS:0.1:20064");
        params.put("ContributionComment", "TestAndroid");
        params.put("Document_numbasedoc", "6");
        params.put("contribution", "true");

        HashMap<String, File> fileParams = new HashMap<>();
        fileParams.put("filetoupload", file1);

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "multipart/form-data");
        header.put("User-Agent", "PostmanRuntime/7.22.0");
        header.put("Accept", "*/*");
        //header.put("Cache-Control", "no-cache");
        //header.put("Postman-Token", "d1f6364a-eaed-4420-a669-53c4b771352c");
        header.put("Host", "demo-interne.ajaris.com");
        header.put("Accept-Encoding", "gzip, deflate");
        //header.put("Cookie", "JSESSIONID=" + sessionid);
        //.addHeader("Content-Length", "27103")
        header.put("Connection", "keep-alive");

        MultipartRequest mMultipartRequest = new MultipartRequest("https://demo-interne.ajaris.com/Demo/upImportDoc.do",
                error -> {
                    Log.e(TAG, "error");
                },
                response -> Log.i(TAG, response), fileParams, params, header
        );
        requestQueue.add(mMultipartRequest);
    }

    public void httpClientMaybe() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        File file1 = new File(getPath(uri));
        //TODO Change timeout
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        try {

            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=--------------------------104913326104647324514494");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("jsessionid", sessionid)
                    .addFormDataPart("ptoken", ptoken)
                    .addFormDataPart("ajaupmo", "test")
                    .addFormDataPart("filetoupload", "fileNameTest.png",
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    file1))
                    .addFormDataPart("ContributionComment", "TestAndroid")
                    .addFormDataPart("Document_numbasedoc", "6")
                    .addFormDataPart("contribution", "true")
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://demo-interne.ajaris.com/Demo/upImportDoc.do")
                    .method("POST", body)
                    .addHeader("Cookie", "JSESSIONID=" + sessionid)
                    .build();

            Response response = client.newCall(request).execute();
            Log.i(TAG, response.body().string());
        } catch (IOException e) {
            Log.e(TAG + "httpClientMaybe", Objects.requireNonNull(e.getMessage()));
        } finally {
            disconnect();
        }
    }

    public void unirestWhyNot() {
        // RESULT EN SESSION EXPIREE
        File file1 = new File(getPath(uri));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Unirest.setTimeouts(0, 0);
        try {
            com.mashape.unirest.http.HttpResponse<String> response = Unirest.post("https://demo-interne.ajaris.com/Demo/upImportDoc.do")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:73.0) Gecko/20100101 Firefox/73.0")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Content-Type", "multipart/form-data")
                    .header("Cookie", "JSESSIONID=" + sessionid)
                    .header("Connection", "keep-alive")
                    .field("contribution", "true")
                    .field("jsessionid", sessionid)
                    .field("ptoken", ptoken)
                    .field("filetoupload", file1)
                    .field("ContributionComment", "Test envoi Android")
                    .field("Document_numbasedoc", "6")
                    .field("ajaupmo", "test")
                    .asString();
            textView.setText(response.toString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void upImportDoc() {
        //TODO check permission ça existe encore ?

        //if (!upSetConfigImport()) textView.setText("Error during import");

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        String url = "https://demo-interne.ajaris.com/Demo/upImportDoc.do";
        Bitmap finalBitmap = bitmap;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {

                    Document doc = XMLParser.readXML(response.toString());
                    String errorCode = doc.getElementsByTagName("error-code").item(0).getTextContent();
                    String errorMessage = doc.getElementsByTagName("error-message").item(0).getTextContent();
                    textView.setText("ImageUpload : " + errorCode);
                },
                error -> {
                    Log.e("GotError", "Upload" + error.getMessage());
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jsessionid", sessionid);
                params.put("ptoken", ptoken);
                params.put("ajaupmo", "ajaupmo");
                params.put("ContributionComment", "TestAndoird");
                params.put("Document_numbasedoc", "6 - Generique");
                params.put("contribution", "true");
                return params;
            }

            @Override
            public Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("filetoupload", new DataPart(imagename + ".png", getFileDataFromDrawable(finalBitmap)));
                return params;
            }
        };


    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //TODO Try ByteBuffer, plus rapide
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void showDemoNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AjarisUploader";
            String description = "Uploader for Ajaris";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Ajaris", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, UploadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(this);

        builder = new NotificationCompat.Builder(this, "Ajaris")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Ajaris Upload")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Issue the initial notification with zero progress
        builder.setProgress(100, 0, false);
        notificationManager.notify(1, builder.build());

        // Do the job here that tracks the progress.
        // Usually, this should be in a
        // worker thread
        // To show progress, update PROGRESS_CURRENT and update the notification with:

        //// When done, update the notification one more time to remove the progress bar

    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.i(TAG,String.valueOf(percentage));
        if (percentage == 30)
            Log.i(TAG,String.valueOf(percentage));
        if (percentage == 80)
            Log.i(TAG,String.valueOf(percentage));
        builder.setProgress(100, percentage, false);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {
        Log.i(TAG,"finished");
    }
}
