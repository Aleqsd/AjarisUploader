package com.mistale.ajarisuploader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.concurrent.atomic.AtomicInteger;

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
    //private static final String demoUrl = "https://demo-interne.ajaris.com/Demo";
    //private static final String demoLogin = "mistale";
    //private static final String demoPwd = "software";
    private String sessionid;
    private String ptoken;
    private String config;
    private String description;
    private int uploadmaxfilesize;
    private int fileSize;
    private int totalFileSize;
    private List<Integer> fileSizes;
    private int percentageMax;
    private int changePercentageMax;
    private boolean uploadFinished;
    private Bitmap imagePreview;
    private boolean isBaseInProfile;
    private boolean isImportInProfile;
    private List<String> baseNamesFromConnexion;
    private List<String> importNamesFromConnexion;

    private ImageView imageView;
    private TextView textViewFileNumber;
    private Button buttonUpload;
    private Button createProfileButton;
    private EditText textDescription;
    private TextInputLayout textInputLayoutDescription;
    private Spinner profileSpinner;
    private ProgressBar progressBar;
    private TextView textViewPercentage;
    private TextView textViewSize;

    private Profile selectedProfile;
    private Uri uri;
    private List<Uri> uris;
    private int filesToUpload;
    private int filesLeftToUpload;
    private ArrayList<Profile> profiles;
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
        getWindow().setBackgroundDrawableResource(R.drawable.ajaris_background_alt);

        imageView = findViewById(R.id.imageView);
        textViewFileNumber = findViewById(R.id.textViewFileNumber);
        buttonUpload = findViewById(R.id.buttonUpload);
        createProfileButton = findViewById(R.id.createProfileButton);
        textDescription = findViewById(R.id.textDescription);
        textInputLayoutDescription = findViewById(R.id.textInputLayoutDescription);
        profileSpinner = findViewById(R.id.profileSpinner);
        progressBar = findViewById(R.id.progressBar);
        textViewPercentage = findViewById(R.id.textViewPercentage);
        textViewSize = findViewById(R.id.textViewSize);

        fileSizes = new ArrayList<>();
        changePercentageMax = 0;
        uploadFinished = false;

        profiles = Preferences.getPreferences(this);
        setupUI();

        filesToUpload = 0;
        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null) { // Un seul fichier
            uri = intent.getParcelableExtra("URI");

            String mime = getMimeType(uri);
            setupThumbnailImageView(mime, uri);

            filesToUpload = 1;
        } else if (intent.getParcelableArrayListExtra("URIS") != null) { // Plusieurs fichiers
            StringBuilder urisString = new StringBuilder();
            uris = intent.getParcelableArrayListExtra("URIS");
            filesToUpload = uris.size();

            String mime = getMimeType(uris.get(0));
            setupThumbnailImageView(mime, uris.get(0));
            textViewFileNumber.setText(" +" + (uris.size() - 1));

            for (Uri singleUri : Objects.requireNonNull(uris))
                urisString.append(singleUri.getPath());
            Log.d(TAG, urisString.toString());
        } else
            Log.e("IntentError", "L'intent reçu est invalide");

        verifyStoragePermissions(this);

        buttonUpload.setOnClickListener(v -> {
            description = textDescription.getText().toString();
            if (isRequestValid()) {
                if (RequestAPI.urlIsValid(selectedProfile.getUrl(), this))
                    connexion();
                else
                    Toast.makeText(UploadActivity.this, "URL du profil non valide", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uploadFinished) {
            Intent intent = new Intent(UploadActivity.this, MainActivity.class);
            intent.putExtra("UPLOAD_SUCCESS", true);
            startActivity(intent);
        }
    }

    // ======================================= UI FUNCTIONS =======================================

    private void setupUI() {
        if (profiles.isEmpty()) {
            profileSpinner.setVisibility(View.INVISIBLE);
            textDescription.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            buttonUpload.setVisibility(View.INVISIBLE);
            textViewFileNumber.setVisibility(View.INVISIBLE);
            textDescription.setVisibility(View.INVISIBLE);
            textInputLayoutDescription.setVisibility(View.INVISIBLE);
            createProfileButton.setEnabled(true);
            createProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            });
            Toast.makeText(UploadActivity.this, "Vous devez d'abord créer un profil", Toast.LENGTH_LONG).show();
        } else {
            profileSpinner.setEnabled(true);
            createProfileButton.setVisibility(View.INVISIBLE);
            profileSpinner.setVisibility(View.VISIBLE);
            textDescription.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            buttonUpload.setVisibility(View.VISIBLE);
            textViewFileNumber.setVisibility(View.VISIBLE);
            textDescription.setVisibility(View.VISIBLE);
            textInputLayoutDescription.setVisibility(View.VISIBLE);
            Map<String, Profile> nameProfileMap = new HashMap<>();
            List<String> profileNames = new ArrayList<>();
            for (Profile profile : profiles) {
                nameProfileMap.put(profile.getName(), profile);
                profileNames.add(profile.getName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, profileNames);
            arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
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

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap adaptRotation(Uri uri, String mime) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(getPath(uri));
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        int orientation = 0;
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        }
        Bitmap rotatedBitmap;
        Bitmap bitmap;

        if (mime.contains("video")) {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(getPath(uri), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            Matrix matrix = new Matrix();
            bitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
        } else if (mime.contains("image"))
            bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(getPath(uri)), 1000, 1000);
        else
            return null;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    private void setupThumbnailImageView(String mime, Uri firstUri) {
        if (mime == null)
            imageView.setVisibility(ImageView.INVISIBLE);
        imagePreview = adaptRotation(firstUri, mime);
        if (imagePreview == null)
            imageView.setVisibility(ImageView.INVISIBLE);
        else
            imageView.setImageBitmap(imagePreview);
    }

    // ======================================= API FUNCTIONS =======================================

    public void connexion() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.POST, selectedProfile.getUrl() + "/upLogin.do",
                response -> {
                    Document doc = XMLParser.readXML(response);
                    if (doc == null)
                        Toast.makeText(UploadActivity.this, "Erreur de connexion", Toast.LENGTH_LONG).show();
                    Log.d(TAG, MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(doc, "sessionid")));
                    sessionid = XMLParser.getDocumentTag(doc, "sessionid");
                    ptoken = XMLParser.getDocumentTag(doc, "ptoken");
                    uploadmaxfilesize = Integer.parseInt(removeLastChar(Objects.requireNonNull(XMLParser.getDocumentTag(doc, "uploadmaxfilesize"))));
                    config = XMLParser.getConfig(doc);

                    baseNamesFromConnexion = XMLParser.getMultipleDocumentTag(doc, "bases");
                    importNamesFromConnexion = XMLParser.getMultipleDocumentTag(doc, "imports");
                    isBaseInProfile = isBaseInProfile(baseNamesFromConnexion);
                    isImportInProfile = isImportInProfile(importNamesFromConnexion);

                    if (!isBaseInProfile)
                        chooseBasePopup(baseNamesFromConnexion);
                    else if (!isImportInProfile)
                        chooseImportPopup(importNamesFromConnexion);
                    else
                        checkImport();
                },
                error -> {
                    Toast.makeText(UploadActivity.this, "Erreur de connexion", Toast.LENGTH_LONG).show();
                    Log.e("ERROR", "error => " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pseudo", selectedProfile.getLogin());
                params.put("password", selectedProfile.getPwd());
                params.put("ajaupmo", "androidUpload");
                return params;
            }
        };
        queue.add(getRequest);
    }

    public void chooseBasePopup(List<String> baseNames) {
        if (baseNames.size() == 1) {
            String newBaseName = baseNames.get(0);
            int numBase = Integer.parseInt(String.valueOf(newBaseName.charAt(0)));
            for (int i = 0; i < profiles.size(); i++) {
                if (selectedProfile.getName().equals(profiles.get(i).getName())) {
                    Profile updatedProfile = selectedProfile;
                    updatedProfile.setBase(new Base(numBase, newBaseName));
                    selectedProfile = updatedProfile;
                    Preferences.addPreferenceToPosition(updatedProfile, i, this);
                }
            }
            isBaseInProfile = true;
            if (isImportInProfile)
                checkImport();
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Choisissez une base");

            String[] basesArray = baseNames.toArray(new String[0]);

            AtomicInteger checkedItem = new AtomicInteger();
            dialogBuilder.setSingleChoiceItems(basesArray, checkedItem.get(), (dialog, which) -> checkedItem.set(which));

            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                String newBaseName = basesArray[checkedItem.get()];
                int numBase = Integer.parseInt(String.valueOf(newBaseName.charAt(0)));
                for (int i = 0; i < profiles.size(); i++) {
                    if (selectedProfile.getName().equals(profiles.get(i).getName())) {
                        Profile updatedProfile = selectedProfile;
                        updatedProfile.setBase(new Base(numBase, newBaseName));
                        selectedProfile = updatedProfile;
                        Preferences.addPreferenceToPosition(updatedProfile, i, this);
                    }
                }
                isBaseInProfile = true;
                if (isImportInProfile)
                    checkImport();
            });
            dialogBuilder.show();
        }
    }

    public void chooseImportPopup(List<String> importNames) {
        if (importNames.size() == 1) {
            String newImportName = importNames.get(0);
            for (int i = 0; i < profiles.size(); i++) {
                if (selectedProfile.getName().equals(profiles.get(i).getName())) {
                    Profile updatedProfile = selectedProfile;
                    updatedProfile.setImportProfile(newImportName);
                    selectedProfile = updatedProfile;
                    Preferences.addPreferenceToPosition(updatedProfile, i, this);
                }
            }
            isImportInProfile = true;
            checkImport();
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Choisissez un profil d'import");

            String[] importArray = importNames.toArray(new String[0]);

            AtomicInteger checkedItem = new AtomicInteger();
            dialogBuilder.setSingleChoiceItems(importArray, checkedItem.get(), (dialog, which) -> checkedItem.set(which));

            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                String newImportName = importArray[checkedItem.get()];
                for (int i = 0; i < profiles.size(); i++) {
                    if (selectedProfile.getName().equals(profiles.get(i).getName())) {
                        Profile updatedProfile = selectedProfile;
                        updatedProfile.setImportProfile(newImportName);
                        selectedProfile = updatedProfile;
                        Preferences.addPreferenceToPosition(updatedProfile, i, this);
                    }
                }
                isImportInProfile = true;
                checkImport();
            });
            dialogBuilder.show();
        }
    }

    public boolean isBaseInProfile(List<String> baseNames) {
        for (String baseName : baseNames) {
            if (baseName.equals(selectedProfile.getBase().getName()))
                return true;
        }
        return false;
    }

    public boolean isImportInProfile(List<String> importsNames) {
        for (String importName : importsNames) {
            if (importName.equals(selectedProfile.getImportProfile()))
                return true;
        }
        return false;
    }

    private boolean isRequestValid() {
        if (description == null || description.isEmpty()) {
            Toast.makeText(UploadActivity.this, "Entrez une description valide", Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }

    public void checkImport() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.POST, selectedProfile.getUrl() + "/upSetConfigImport.do",
                response -> {
                    Document doc = XMLParser.readXML(response);
                    if (doc == null) {
                        Toast.makeText(UploadActivity.this, "Erreur de profil d'import", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (XMLParser.getCode(doc) == 0) {
                        setupUploadNotification();
                        if (filesToUpload > 1)
                            initMultipleUpload();
                        else
                            uploadWithRetrofit();
                    } else {
                        Toast.makeText(UploadActivity.this, "Erreur de profil d'import", Toast.LENGTH_LONG).show();
                        disconnect(false);
                    }

                },
                error -> {
                    Toast.makeText(UploadActivity.this, "Erreur de profil d'import", Toast.LENGTH_LONG).show();
                    Log.e("ERROR", "error => " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jsessionid", sessionid);
                params.put("ptoken", ptoken);
                params.put("config", config);
                params.put("ajaupmo", "androidUpload");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "JSESSIONID=" + sessionid);
                return headers;
            }
        };
        queue.add(getRequest);
    }

    private void uploadWithRetrofit() {
        File image = new File(Objects.requireNonNull(getPath(uri)));
        if (!isFileSizeOk(image)) return;
        String mime = getMimeType(uri);

        buttonUpload.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        textViewPercentage.setVisibility(View.VISIBLE);
        textViewSize.setVisibility(View.VISIBLE);

        ProgressRequestBody fileBody = new ProgressRequestBody(image, mime, this);
        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "androidUpload");
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedProfile.getBase().getNumber()));
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

        String sessionIdCookie = "JSESSIONID=" + sessionid;

        RequeteService requeteService = RestService.getClient(selectedProfile.getUrl()).create(RequeteService.class);

        Call<ResponseBody> call = requeteService.uploadSingleFile(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                writeInUploadHistory(response, description, selectedProfile, getPath(uri));
                progressBar.setVisibility(View.INVISIBLE);
                textViewPercentage.setVisibility(View.INVISIBLE);
                textViewSize.setVisibility(View.INVISIBLE);
                Log.v("Upload", "success");
                notificationManager.cancel(1);

                disconnect(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", Objects.requireNonNull(t.getMessage()));
                progressBar.setVisibility(View.INVISIBLE);
                textViewPercentage.setVisibility(View.INVISIBLE);
                textViewSize.setVisibility(View.INVISIBLE);
                builder.setContentText("Upload error")
                        .setProgress(0, 0, false);
                Toast.makeText(UploadActivity.this, "Erreur durant l'upload", Toast.LENGTH_LONG).show();

                notificationManager.notify(1, builder.build());
                disconnect(false);
            }
        });
    }

    private void initMultipleUpload() {
        totalFileSize = 0;
        filesLeftToUpload = filesToUpload;
        int current = uris.size() - filesLeftToUpload;

        for (Uri uri : uris) {
            File image = new File(Objects.requireNonNull(getPath(uri)));
            if (!isFileSizeOk(image)) return;
            totalFileSize += fileSize;
            fileSizes.add(fileSize);
        }
        uploadMultipleWithRetrofit(uris.get(current), current);
    }

    private void uploadMultipleWithRetrofit(Uri currentUri, int current) {

        File image = new File(Objects.requireNonNull(getPath(currentUri)));
        String mime = getMimeType(currentUri);

        buttonUpload.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        textViewPercentage.setVisibility(View.VISIBLE);
        textViewSize.setVisibility(View.VISIBLE);

        ProgressRequestBody fileBody = new ProgressRequestBody(image, mime, this);
        MultipartBody.Part body = MultipartBody.Part.createFormData("filetoupload", image.getName(), fileBody);

        RequestBody sess = RequestBody.create(MediaType.parse("text/plain"), sessionid);
        RequestBody ptok = RequestBody.create(MediaType.parse("text/plain"), ptoken);
        RequestBody ajau = RequestBody.create(MediaType.parse("text/plain"), "androidUpload");
        String description = textDescription.getText().toString();
        RequestBody cont = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody docu = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedProfile.getBase().getNumber()));
        RequestBody contr = RequestBody.create(MediaType.parse("text/plain"), "true");

        String sessionIdCookie = "JSESSIONID=" + sessionid;

        RequeteService requeteService = RestService.getClient(selectedProfile.getUrl()).create(RequeteService.class);
        Call<ResponseBody> call = requeteService.uploadSingleFile(sessionIdCookie, body, sess, ptok, ajau, cont, docu, contr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                writeInUploadHistory(response, description, selectedProfile, getPath(currentUri));
                Log.v("Upload", "success");
                if (filesLeftToUpload > 1) {
                    builder.setProgress(100, 100 / filesLeftToUpload, false);
                    notificationManager.notify(1, builder.build());

                    filesLeftToUpload -= 1;

                    uploadMultipleWithRetrofit(uris.get(current + 1), current + 1);
                } else {
                    notificationManager.cancel(1);

                    progressBar.setVisibility(View.INVISIBLE);
                    textViewPercentage.setVisibility(View.INVISIBLE);
                    textViewSize.setVisibility(View.INVISIBLE);
                    disconnect(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", Objects.requireNonNull(t.getMessage()));
                builder.setContentText("Upload error")
                        .setProgress(0, 0, false);
                Toast.makeText(UploadActivity.this, "Erreur durant l'upload", Toast.LENGTH_LONG).show();

                notificationManager.notify(1, builder.build());
                disconnect(false);
            }
        });

    }

    private void disconnect(boolean uploadSucceded) {
        if (uploadSucceded) {
            if (RequestAPI.isLoggedOut(selectedProfile.getUrl(), sessionid)) {
                uploadSuccessAction();
            } else
                Toast.makeText(UploadActivity.this, "Problème survenu lors de la déconnexion", Toast.LENGTH_LONG).show();
        } else
            RequestAPI.isLoggedOut(selectedProfile.getUrl(), sessionid);
    }

    private void uploadSuccessAction() {
        Toast.makeText(UploadActivity.this, "Envoi terminé", Toast.LENGTH_LONG).show();
        Handler handler = new Handler();
        uploadFinished = true;
        setupUploadFinishedNotification();
        handler.postDelayed(() -> {
            Intent intent = new Intent(UploadActivity.this, MainActivity.class);
            intent.putExtra("UPLOAD_SUCCESS", true);
            startActivity(intent);
        }, 1500);
    }

    public String getMimeType(Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = this.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    // ====================================== UTILS FUNCTIONS ======================================


    private boolean isFileSizeOk(File file) {
        fileSize = (int) (file.length() / 1024);
        int fileSizeInMo = fileSize / 1024;
        if (fileSizeInMo > uploadmaxfilesize) {
            Toast.makeText(UploadActivity.this, "Taille du fichier superieur à " + uploadmaxfilesize + "Mo", Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }

    public void writeInUploadHistory(retrofit2.Response<ResponseBody> response, String comment, Profile profile, String fileName) {
        try {
            String xmlResponse = response.body().string();
            Document document = XMLParser.readUploadXML(xmlResponse);
            int code = XMLParser.getUploadCode(document);
            int contributionId = XMLParser.getContributionId(document);
            Date date = Calendar.getInstance().getTime();
            Upload myUpload = new Upload(fileName, date, comment, profile);
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

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
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

        notificationManager = NotificationManagerCompat.from(this);

        builder = new NotificationCompat.Builder(this, "Ajaris")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Ajaris Upload")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        builder.setProgress(100, 0, false);
        notificationManager.notify(1, builder.build());
    }

    private void setupUploadFinishedNotification() {

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

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UPLOAD_SUCCESS", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);

        notificationManager = NotificationManagerCompat.from(this);

        builder = new NotificationCompat.Builder(this, "Ajaris")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Ajaris Upload")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0L})
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        builder.setContentText("Envoi terminé")
                .setProgress(0, 0, false);
        notificationManager.notify(2, builder.build());
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

    @Override
    public void onProgressUpdate(int percentage) {
        if (filesToUpload == 1) {
            builder.setProgress(100, percentage, false);
            progressBar.setProgress(percentage);
            textViewPercentage.setText(percentage + "%");
            if (fileSize < 1024) {
                double fileSizeUploaded = ((double) percentage / 100.00) * fileSize;
                String result = String.format("%.2f", fileSizeUploaded);
                textViewSize.setText(result + "Ko/" + fileSize + "Ko");
            } else {
                double fileSizeUploaded = ((double) percentage / 100.00) * (fileSize / 1024);
                String result = String.format("%.2f", fileSizeUploaded);
                textViewSize.setText(result + "Mo/" + fileSize / 1024 + "Mo");
            }
            notificationManager.notify(1, builder.build());
        } else { // Mode plusieurs fichiers, exemple sur 5
            int n = uris.size(); // 5
            int current = n - filesLeftToUpload; // 0, 1, 2, 3, 4

            int printedValue;

            int size = fileSizes.get(current);

            if (current == 0) {
                if (changePercentageMax == current) {
                    changePercentageMax++;
                    percentageMax = size * 100 / totalFileSize;
                }
                printedValue = size * percentage / totalFileSize;
            } else {
                printedValue = percentageMax + size * percentage / totalFileSize;
                if (printedValue > 100)
                    printedValue = 100;

                if (changePercentageMax == current && percentage == 100) {
                    percentageMax = percentageMax + size * 100 / totalFileSize;
                    if (percentageMax > 100)
                        percentageMax = 100;
                    changePercentageMax++;
                }
            }

            builder.setProgress(100, printedValue, false);
            progressBar.setProgress(printedValue);
            textViewPercentage.setText(printedValue + "%");
            if (totalFileSize < 1024) {
                double fileSizeUploaded = ((double) printedValue / 100.00) * totalFileSize;
                String result = String.format("%.2f", fileSizeUploaded);
                textViewSize.setText(result + "Ko/" + totalFileSize + "Ko");
            } else {
                double fileSizeUploaded = ((double) printedValue / 100.00) * (totalFileSize / 1024);
                String result = String.format("%.2f", fileSizeUploaded);
                textViewSize.setText(result + "Mo/" + totalFileSize / 1024 + "Mo");
                Log.i(TAG, result);
            }
            if (percentage % 10 == 0) // To reduce lags
                notificationManager.notify(1, builder.build());
        }
    }
}
