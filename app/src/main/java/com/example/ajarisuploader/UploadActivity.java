package com.example.ajarisuploader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

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

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("URI");
        TextView textView = findViewById(R.id.urisTextView);
        if (uri != null)
            textView.setText(uri.toString());
    }

}
