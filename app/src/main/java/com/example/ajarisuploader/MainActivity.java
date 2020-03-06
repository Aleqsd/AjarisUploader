package com.example.ajarisuploader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_profiles, R.id.navigation_dashboard, R.id.navigation_about)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            uploadIntent.putExtra("URI", imageUri);
            startActivity(uploadIntent);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            uploadIntent.putExtra("URI", imageUris);
            startActivity(uploadIntent);
        }
    }

    public void openAddProfile(View view) {
        Intent intent = new Intent(MainActivity.this, AddProfile.class);
        startActivity(intent);
    }
}
