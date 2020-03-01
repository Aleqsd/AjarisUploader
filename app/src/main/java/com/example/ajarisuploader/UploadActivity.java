package com.example.ajarisuploader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/***
 * Activité visée à s'ouvrir par l'intent du share
 */
public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView textView = findViewById(R.id.urisTextView);


        Intent intent = getIntent();
        if (intent.getParcelableExtra("URI") != null)
        {
            Uri uri = intent.getParcelableExtra("URI");
            textView.setText(uri.getEncodedPath());
        }
        else if (intent.getParcelableArrayListExtra("URI") != null)
        {
            StringBuilder urisString = new StringBuilder();
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra("URI");
            for (Uri uri : uris)
                urisString.append(uri.getEncodedPath());
            textView.setText(urisString);
        }
        else
            Log.e("IntentError","L'intent reçu est invalide");

    }

}
