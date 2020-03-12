package com.mistale.ajarisuploader;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.mistale.ajarisuploader.api.RequestAPI;
import com.mistale.ajarisuploader.api.VolleyMultipartRequest;
import com.mistale.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/***
 * Activité visée à s'ouvrir par l'intent du share
 */
public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "UPL";
    private static final String demoUrl = "https://demo-interne.ajaris.com/Demo";
    private static final String demoLogin = "mistale";
    private static final String demoPwd = "software";
    private static final String upLoadServerUri = "https://demo-interne.ajaris.com/Demo/upImportDoc.do";
    private int serverResponseCode = 0;
    private TextView textView;
    private Uri uri;
    ProgressDialog dialog = null;
    Document lastDocument = null;
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
        Button buttonUpload = findViewById(R.id.buttonUpload);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        verifyStoragePermissions(this);

        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null) {
            uri = intent.getParcelableExtra("URI");
            textView.setText(Objects.requireNonNull(uri).getEncodedPath());
            showDemoNotification();
        } else if (intent.getParcelableArrayListExtra("URI") != null) {
            StringBuilder urisString = new StringBuilder();
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra("URI");
            for (Uri singleUri : Objects.requireNonNull(uris))
                urisString.append(singleUri.getEncodedPath());
            textView.setText(urisString);
            showDemoNotification();
        } else
            Log.e("IntentError", "L'intent reçu est invalide");


        buttonUpload.setOnClickListener(v -> {
            if (RequestAPI.urlIsValid(demoUrl, null))
                upLogin();
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

    private void upLogin() {
        this.lastDocument = RequestAPI.getLoginInfos(demoUrl, demoLogin, demoPwd, null);
        if (this.lastDocument == null) textView.setText("Error during login");
        textView.setText(MessageFormat.format("Session id : {0}", XMLParser.getDocumentTag(this.lastDocument, "sessionid")));
        uploadFile(Objects.requireNonNull(uri).getEncodedPath());
    }

    private void upImportDoc() {

        //TODO check permission ça existe encore ?

        //        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
        //                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
        //                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
        //            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
        //                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
        //                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
        //
        //            } else {
        //                ActivityCompat.requestPermissions(MainActivity.this,
        //                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
        //                        REQUEST_PERMISSIONS);
        //            }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        //TODO refaire le code en propre dans RequestAPI

        String url = "https://demo-interne.ajaris.com/Demo/upImportDoc.do";
        Bitmap finalBitmap = bitmap;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {

                    Document doc = XMLParser.readXML(response.toString());
                    String errorCode = doc.getElementsByTagName("error-code").item(0).getTextContent();
                    String errorMessage = doc.getElementsByTagName("error-message").item(0).getTextContent();

                    Toast.makeText(getApplicationContext(), errorCode, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("GotError", "" + error.getMessage());
                }) {


            @Override
            public Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("Filetoupload", new DataPart(imagename + ".png", getFileDataFromDrawable(finalBitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);

    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int uploadFile(String sourceFileUri) {
        System.out.println("JE SUIS ICI");
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "--***";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sdDir = Environment.getExternalStorageDirectory();
        sourceFileUri = sourceFileUri.split("raw/")[1];
        try {
            sourceFileUri = URLDecoder.decode(sourceFileUri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);*/
        //System.out.println(path);
        fileName = sourceFileUri;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            //dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    + sourceFileUri);

            String finalSourceFileUri = sourceFileUri;
            runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("Source File not exist :"
                            + finalSourceFileUri);
                }
            });

            return 0;

        }
        else
        {
            try {

                System.out.println("JE SUIS UN FICHIER");

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                /*conn.setRequestProperty("filetoupload", fileName);
                conn.setRequestProperty("ajaupmo", "ajaupmo");
                conn.setRequestProperty("contribution", "true");
                conn.setRequestProperty("jsessionid", XMLParser.getDocumentTag(this.lastDocument, "sessionid"));
                conn.setRequestProperty("ptoken", XMLParser.getDocumentTag(this.lastDocument, "ptoken"));
                conn.setRequestProperty("ContributionComment", "test");
                conn.setRequestProperty("Document_numbasedoc", "6");
                conn.setRequestProperty("filetoupload", "test.png");*/
                dos = new DataOutputStream(conn.getOutputStream());

                System.out.println("PARAMS OK");

                System.out.println("PARAMS OK - 2");
                dos.writeBytes(lineEnd);
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"ajaupmo\"");
                System.out.println("Content-Disposition: form-data; name=\"ajaupmo\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes("ajaupmo");
                System.out.println("ajaupmo");
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"contribution\"");
                System.out.println("Content-Disposition: form-data; name=\"contribution\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes("true");
                System.out.println("true");
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"ContributionComment\"");
                System.out.println("Content-Disposition: form-data; name=\"ContributionComment\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes("testAndroid");
                System.out.println("testAndroid");
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"Document_numbasedoc\"");
                System.out.println("Content-Disposition: form-data; name=\"Document_numbasedoc\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes("6");
                System.out.println("6");
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"jsessionid\"");
                System.out.println("Content-Disposition: form-data; name=\"jsessionid\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes(XMLParser.getDocumentTag(this.lastDocument, "sessionid"));
                System.out.println(XMLParser.getDocumentTag(this.lastDocument, "sessionid"));
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"ptoken\"");
                System.out.println("Content-Disposition: form-data; name=\"ptoken\"");
                dos.writeBytes(lineEnd);
                System.out.println(lineEnd);
                dos.writeBytes(XMLParser.getDocumentTag(this.lastDocument, "ptoken"));
                System.out.println(XMLParser.getDocumentTag(this.lastDocument, "ptoken"));
                dos.writeBytes(boundary + lineEnd);
                System.out.println(boundary + lineEnd);


                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=filetoupload;filename=\""
                        + fileName + "\"" + lineEnd);
                System.out.println("PARAMS OK - 3");

                   dos.writeBytes(lineEnd);

                   // create a buffer of  maximum size
                   bytesAvailable = fileInputStream.available();
                System.out.println("PARAMS OK - 4");

                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   buffer = new byte[bufferSize];

                   // read file and write it into form...
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                System.out.println("JE SUIS ARRIVE LA");

                   while (bytesRead > 0) {

                     dos.write(buffer, 0, bufferSize);
                     bytesAvailable = fileInputStream.available();
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                   // send multipart form data necesssary after file data...
                   dos.writeBytes(lineEnd);
                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                System.out.println("PUIS LA");

                   // Responses from the server (code and message)
                   serverResponseCode = conn.getResponseCode();
                   String serverResponseMessage = conn.getResponseMessage();

                InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;

                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                Log.i("uploadFile", "Input Stream is : "
                        + stringBuilder.toString());

                   Log.i("uploadFile", "HTTP Response is : "
                           + serverResponseMessage + ": " + serverResponseCode);

                   if(serverResponseCode == 200){

                       runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(UploadActivity.this, "File Upload Complete.",
                                             Toast.LENGTH_SHORT).show();
                            }
                        });
                   }
                System.out.println("PUIS ICI");

                   //close the streams //
                   fileInputStream.close();
                   dos.flush();
                   dos.close();

              } catch (MalformedURLException ex) {

                  //dialog.dismiss();
                  ex.printStackTrace();

                  runOnUiThread(new Runnable() {
                      public void run() {
                          Toast.makeText(UploadActivity.this, "MalformedURLException",
                                                              Toast.LENGTH_SHORT).show();
                      }
                  });

                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
              } catch (Exception e) {

                  //dialog.dismiss();
                  e.printStackTrace();

                  runOnUiThread(new Runnable() {
                      public void run() {
                          Toast.makeText(UploadActivity.this, "Got Exception : see logcat ",
                                  Toast.LENGTH_SHORT).show();
                      }
                  });
                  Log.e("Server Exception", "Exception : "
                                                   + e.getMessage(), e);
              }
              //dialog.dismiss();
              return serverResponseCode;

           } // End else block
         }

}
