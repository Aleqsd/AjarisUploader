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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
public class UploadActivity extends AppCompatActivity {

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
        new NukeSSLCerts().nuke();

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
                //if (RequestAPI.urlIsValid(demoUrl))
                customConnexion();
                //yesAnotherTry();
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
    }

    private boolean upSetConfigImport() {
        return RequestAPI.isProfilValid(demoUrl, sessionid, ptoken, config);
    }

    private void uploadWithAUS() {
        try {

            File file = new File(uri.getPath());
            if (file.exists())
                Log.d(TAG, "File Exist");

            //TODO arrayparameter et refactor
            MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(this, demoUrl + "/upImportDoc.do");
            multipartUploadRequest
                    .addFileToUpload(uri.getPath(), "filetoupload")
                    .addParameter("jsessionid", sessionid)
                    .addParameter("ptoken", ptoken)
                    .addParameter("ajaupmo", "ajaupmo")
                    .addParameter("ContributionComment", "TestAndroid")
                    .addParameter("Document_numbasedoc", "6 - Generique")
                    .addParameter("contribution", "true")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setUtf8Charset()
                    .setMaxRetries(2)
                    .startUpload();
            Log.d(TAG, "Upload request over");
        } catch (FileNotFoundException | MalformedURLException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void uploadWithRetrofit() {
        File image = new File(Objects.requireNonNull(getPath(uri)));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), requestFile);

        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "ajaupmo");
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), "TestAndroid");
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), "6 - Generique");
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");


        RequeteService requeteService = RestService.getClient().create(RequeteService.class);
        Call<ResponseBody> call = requeteService.uploadProfilePicture(body, sess, ptok, ajau, cont, docu, contr);
        final File finalImage = image;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
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
                        response -> textView.setText(new String(response.data)),
                        error -> Log.e("VolleyError : ", "" + error.networkResponse.statusCode)

                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("jsessionid", sessionid);
                        params.put("ptoken", ptoken);
                        params.put("ajaupmo", "test");
                        params.put("ContributionComment", "TestAndroid");
                        params.put("Document_numbasedoc", "6 - Generique");
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
                };

                Volley.newRequestQueue(this).add(volleyMultipartRequest);
            } catch (IOException e) {
                e.printStackTrace();
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
        StringRequest getRequest = new StringRequest(Request.Method.POST, demoUrl+"/upLogin.do",
                response -> {
                    Log.d("Response", response);
                    Document doc = XMLParser.readXML(response);
                    if (doc == null) textView.setText("Error during login");
                    textView.setText(MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(doc, "sessionid")));
                    sessionid = XMLParser.getDocumentTag(doc, "sessionid");
                    ptoken = XMLParser.getDocumentTag(doc, "ptoken");
                    config = XMLParser.getConfig(doc);
                    uploadWithRetrofit();
                },
                error -> {
                    Log.d("ERROR","error => "+error.toString());
                }

        ) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("pseudo", "mistale");
                params.put("password", "software");
                params.put("ajaupmo", "test");
                return params;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                Log.i("response",response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                Log.i("cookies",rawCookies);
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
        params.put("ajaupmo", "test");
        params.put("ContributionComment", "TestAndroid");
        params.put("Document_numbasedoc", "6 - Generique");
        params.put("contribution", "true");

        HashMap<String, File> fileParams = new HashMap<>();
        fileParams.put("filetoupload", file1);

        Map<String, String> header = new HashMap<>();
        header.put("Set-Cookie","JSESSIONID="+sessionid+"; Path=/; Secure; HttpOnly");

        MultipartRequest mMultipartRequest = new MultipartRequest("https://demo-interne.ajaris.com/Demo/upImportDoc.do",
                error -> {
                    Log.e(TAG, String.valueOf(error.networkResponse.statusCode));
                },
                response -> Log.i(TAG, response), fileParams, params, header
        );
        mMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(mMultipartRequest);
    }

    public void httpClientMaybe() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        File file1 = new File(getPath(uri));
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("jsessionid", sessionid)
                .addFormDataPart("ptoken", ptoken)
                .addFormDataPart("filetoupload", "fileNameTest",
                        RequestBody.create(MediaType.parse("application/octet-stream"), file1))
                .addFormDataPart("ContributionComment", "TestAndroid")
                .addFormDataPart("Document_numbasedoc", "6 - Generique")
                .addFormDataPart("contribution", "true")
                .addFormDataPart("ajaupmo", "test")
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://demo-interne.ajaris.com/Demo/upImportDoc.do")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            textView.setText(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
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
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("contribution", "true")
                    .field("jsessionid", sessionid)
                    .field("ptoken", ptoken)
                    .field("filetoupload", file1)
                    .field("ContributionComment", "TestAndroid")
                    .field("Document_numbasedoc", "6 - Generique")
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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Ajaris")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Ajaris Upload")
                .setContentText("Upload in progress...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 50;
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(1, builder.build());

        // Do the job here that tracks the progress.
        // Usually, this should be in a
        // worker thread
        // To show progress, update PROGRESS_CURRENT and update the notification with:
        // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        // notificationManager.notify(notificationId, builder.build());

        //// When done, update the notification one more time to remove the progress bar
        //        builder.setContentText("Download complete")
        //                .setProgress(0,0,false);

        notificationManager.notify(1, builder.build());
    }

}
