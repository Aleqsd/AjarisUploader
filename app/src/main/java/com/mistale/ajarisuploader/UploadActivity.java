package com.mistale.ajarisuploader;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mistale.ajarisuploader.api.ProgressRequestBody;
import com.mistale.ajarisuploader.api.RequestAPI;
import com.mistale.ajarisuploader.api.RequeteService;
import com.mistale.ajarisuploader.api.RestService;
import com.mistale.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private TextView uploadTextMessage;
    private ImageView imageView;
    private Button buttonUpload;
    private Button createProfileButton;
    private EditText editTextDescription;
    private Spinner profileSpinner;
    private Profile selectedProfile;
    private Uri uri;
    private List<Uri> uris;
    private int filesToUpload;
    private ArrayList<Profile> profiles;
    private ProgressDialog progressDialog;
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

        uploadTextMessage = findViewById(R.id.uploadTextMessage);
        imageView = findViewById(R.id.imageView);
        buttonUpload = findViewById(R.id.buttonUpload);
        createProfileButton = findViewById(R.id.createProfileButton);
        editTextDescription = findViewById(R.id.editTextDescription);
        profileSpinner = findViewById(R.id.profileSpinner);

        progressDialog = new ProgressDialog(UploadActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);

        profiles = Preferences.getPreferences(this);
        setupUI();

        filesToUpload = 0;
        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null) {
            uri = intent.getParcelableExtra("URI");
            uploadTextMessage.setText(Objects.requireNonNull(uri).getPath());
            imageView.setImageURI(uri);
            filesToUpload = 1;
        } else if (intent.getParcelableArrayListExtra("URI") != null) {
            StringBuilder urisString = new StringBuilder();
            uris = intent.getParcelableArrayListExtra("URI");
            filesToUpload = uris.size();
            for (Uri singleUri : Objects.requireNonNull(uris))
                urisString.append(singleUri.getPath());
            uploadTextMessage.setText(urisString);
        } else
            Log.e("IntentError", "L'intent reçu est invalide");

        verifyStoragePermissions(this);

        buttonUpload.setOnClickListener(v -> {
            if (RequestAPI.urlIsValid(demoUrl, progressDialog))
                customConnexion();
        });

    }

    private void setupUI() {
        if (profiles.isEmpty()) {
            profileSpinner.setVisibility(View.INVISIBLE);
            editTextDescription.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            buttonUpload.setVisibility(View.INVISIBLE);
            createProfileButton.setEnabled(true);
            createProfileButton.setOnClickListener(v -> {
                //TODO A tester
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            });
            uploadTextMessage.setText("Vous devez d'abord créer un profil");
        } else {
            profileSpinner.setEnabled(true);
            createProfileButton.setVisibility(View.INVISIBLE);
            uploadTextMessage.setText("Sélectionnez votre profil");
            Map<String, Profile> nameProfileMap = new HashMap<>();
            List<String> profileNames = new ArrayList<>();
            for (Profile profile : profiles) {
                nameProfileMap.put(profile.getName(), profile);
                profileNames.add(profile.getName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, profileNames);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            profileSpinner.setAdapter(arrayAdapter);
            profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String profileNameSelected = parent.getItemAtPosition(position).toString();
                    selectedProfile = nameProfileMap.get(profileNameSelected);
                    Log.i(TAG, "Selected Profile : " + selectedProfile.getName());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
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
        if (RequestAPI.isLoggedOut(demoUrl, sessionid, progressDialog))
            uploadTextMessage.setText("Disconnected !");
        else
            uploadTextMessage.setText("Still connected");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    private void uploadMultipleWithRetrofit() {
        for (Uri uri : uris) {
            File image = new File(Objects.requireNonNull(getPath(uri)));

            String mime = getMimeType(uri.toString());

            ProgressRequestBody fileBody = new ProgressRequestBody(image, mime, this);
            MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

            RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
            RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
            RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "test");
            //TODO Faire un ensemble de check, genre si la description est pas nulle, le setimport, etc.
            String description = editTextDescription.getText().toString();
            RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), description);
            RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), selectedProfile.getBase().getName());
            RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

            String sessionIdCookie = "JSESSIONID=" + sessionid;

            RequeteService requeteService = RestService.getClient().create(RequeteService.class);
            Call<ResponseBody> call = requeteService.uploadSingleFile(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    writeInUploadHistory(response, description, selectedProfile, image.getName(), uri);
                    Log.v("Upload", "success");
                    if (filesToUpload > 1) {
                        builder.setProgress(100, 100 / filesToUpload, false);
                        notificationManager.notify(1, builder.build());
                    } else {
                        builder.setContentText("Upload complete")
                                .setProgress(0, 0, false);
                        notificationManager.notify(1, builder.build());
                        disconnect();
                    }
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
            filesToUpload -= 1;
        }
    }


    private void uploadWithRetrofit() {
        File image = new File(Objects.requireNonNull(getPath(uri)));
        String mime = getMimeType(uri.toString());

        ProgressRequestBody fileBody = new ProgressRequestBody(image, mime, this);
        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

        //TODO Gestion URL
        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "test");
        //TODO Faire un ensemble de check, genre si la description est pas nulle, le setimport, etc.
        String description = editTextDescription.getText().toString();
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), selectedProfile.getBase().getName());
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

        String sessionIdCookie = "JSESSIONID=" + sessionid;

        RequeteService requeteService = RestService.getClient().create(RequeteService.class);

        Call<ResponseBody> call = requeteService.uploadSingleFile(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                writeInUploadHistory(response, description, selectedProfile, image.getName(), uri);
                Log.v("Upload", "success");
                builder.setContentText("Upload complete")
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

    public void writeInUploadHistory(retrofit2.Response<ResponseBody> response, String comment, Profile profile, String fileName, Uri uri) {
        try {
            String xmlResponse = response.body().string();
            Document document = XMLParser.readUploadXML(xmlResponse);
            //TODO Check quelque part si code != 0 ?
            int code = XMLParser.getUploadCode(document);
            int contributionId = XMLParser.getContributionId(document);
            Date date = Calendar.getInstance().getTime();
            Upload myUpload = new Upload(fileName, date, comment, profile, getPath(uri));
            ArrayList<Contribution> allContributions = UploadHistory.getPreferences(this);
            Contribution myContribution = Contribution.getContributionById(allContributions, contributionId);
            if (myContribution != null) {
                ArrayList<Upload> myUploads = myContribution.getUploads();
                myUploads.add(myUpload);
                myContribution.setUploads(myUploads);
                UploadHistory.addPreference(myContribution, this);
            } else {
                ArrayList<Upload> myUploads = new ArrayList<>();
                myUploads.add(myUpload);
                myContribution = new Contribution(contributionId, myUploads);
                UploadHistory.addPreference(myContribution, this);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public void customConnexion() {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.POST, demoUrl + "/upLogin.do",
                response -> {
                    Document doc = XMLParser.readXML(response);
                    if (doc == null) uploadTextMessage.setText("Error during login");
                    uploadTextMessage.setText(MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(doc, "sessionid")));
                    sessionid = XMLParser.getDocumentTag(doc, "sessionid");
                    Log.d(TAG, sessionid);
                    ptoken = XMLParser.getDocumentTag(doc, "ptoken");
                    config = XMLParser.getConfig(doc);
                    setupUploadNotification();
                    if (filesToUpload > 1)
                        uploadMultipleWithRetrofit();
                    else
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

        //TODO modifier par l'intent de l'historique
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(this);

        builder = new NotificationCompat.Builder(this, "Ajaris")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Ajaris Upload")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        builder.setProgress(100, 0, false);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onProgressUpdate(int percentage) {
        builder.setProgress(100, percentage, false);
        uploadTextMessage.setText("Upload progress : " + percentage + "%");
        notificationManager.notify(1, builder.build());
    }
}
