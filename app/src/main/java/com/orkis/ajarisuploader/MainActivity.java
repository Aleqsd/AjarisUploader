package com.orkis.ajarisuploader;

import android.content.ActivityNotFoundException;
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
        getWindow().setBackgroundDrawableResource(R.drawable.ajaris_background);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_profiles, R.id.navigation_history, R.id.navigation_about)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("UPLOAD_SUCCESS"))
                navView.setSelectedItemId(R.id.navigation_history);
        }

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            uploadIntent.putExtra("URI", imageUri);
            startActivity(uploadIntent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            uploadIntent.putExtra("URIS", imageUris);
            startActivity(uploadIntent);
        }
    }

    public void openAddProfile(View view) {
        Intent intent = new Intent(MainActivity.this, AddProfile.class);
        startActivity(intent);
    }

    public void rateOnPlayStore(View view) {
        Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())));
        }
    }
}
