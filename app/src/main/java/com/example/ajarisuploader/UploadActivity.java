package com.example.ajarisuploader;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ajarisuploader.api.ProgressRequestBody;
import com.example.ajarisuploader.api.RequestAPI;
import com.example.ajarisuploader.api.RequeteService;
import com.example.ajarisuploader.api.RestService;
import com.example.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

        textView = findViewById(R.id.urisTextView);
        imageView = findViewById(R.id.imageView);
        Button buttonUpload = findViewById(R.id.buttonUpload);

        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null) {
            uri = intent.getParcelableExtra("URI");
            textView.setText(Objects.requireNonNull(uri).getPath());
            imageView.setImageURI(uri);
        } else if (intent.getParcelableArrayListExtra("URI") != null) {
            StringBuilder urisString = new StringBuilder();
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra("URI");
            for (Uri singleUri : Objects.requireNonNull(uris))
                urisString.append(singleUri.getPath());
            textView.setText(urisString);
        } else
            Log.e("IntentError", "L'intent reçu est invalide");

        verifyStoragePermissions(this);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (RequestAPI.urlIsValid(demoUrl))
                    customConnexion();
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

    private void uploadWithRetrofit() {
        setupUploadNotification();
        File image = new File(Objects.requireNonNull(getPath(uri)));

        //TODO Replace type image
        ProgressRequestBody fileBody = new ProgressRequestBody(image, "image/*", this);

        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "test");
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), "TestAndroid");
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), "6 - Generique");
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

        String sessionIdCookie = "JSESSIONID=" + sessionid;

        RequeteService requeteService = RestService.getClient().create(RequeteService.class);
        Call<ResponseBody> call = requeteService.uploadProfilePicture(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.v("Upload", "success");
                builder.setContentText("Download complete")
                        .setProgress(0, 0, false);

                notificationManager.notify(1, builder.build());
                disconnect();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                builder.setContentText("Upload error")
                        .setProgress(0, 0, false);

                notificationManager.notify(1, builder.build());
                disconnect();
            }
        });
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
                params.put("pseudo", demoLogin);
                params.put("password", demoPwd);
                params.put("ajaupmo", "test");
                return params;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i("response", response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                Log.i("cookies", Objects.requireNonNull(rawCookies));
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(getRequest);
    }

    private void setupUploadNotification() {

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
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        // Issue the initial notification with zero progress
        builder.setProgress(100, 0, false);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onProgressUpdate(int percentage) {
        builder.setProgress(100, percentage, false);
        notificationManager.notify(1, builder.build());
    }
}
