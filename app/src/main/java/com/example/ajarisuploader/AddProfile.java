package com.example.ajarisuploader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ajarisuploader.api.RequestAPI;
import com.example.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class AddProfile extends AppCompatActivity {

    private Document lastDocument = null;
    private boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText inputName = findViewById(R.id.input_name);
        EditText inputUrl = findViewById(R.id.input_url);
        EditText inputLogin = findViewById(R.id.input_login);
        EditText inputPwd = findViewById(R.id.input_pwd);
        Spinner inputBase = findViewById(R.id.input_base);
        Spinner inputImport = findViewById(R.id.input_import);
        Button addButton = findViewById(R.id.button_add);
        Button cancelButton = findViewById(R.id.button_cancel);

        List<String> basesArray = new ArrayList<>();
        ArrayAdapter<String> adapterBases = new ArrayAdapter<>(
                AddProfile.this,
                android.R.layout.simple_spinner_item,
                basesArray
        );
        inputBase.setAdapter(adapterBases);

        List<String> importsArray = new ArrayList<>();
        ArrayAdapter<String> adapterImports = new ArrayAdapter<>(
                AddProfile.this,
                android.R.layout.simple_spinner_item,
                importsArray
        );
        inputImport.setAdapter(adapterImports);

        inputUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (RequestAPI.urlIsValid(inputUrl.getText().toString())) {
                    inputLogin.setEnabled(true);
                    inputPwd.setEnabled(true);
                } else {
                    inputLogin.setEnabled(false);
                    inputPwd.setEnabled(false);
                }
            }
        });

        inputLogin.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !inputPwd.getText().toString().equals("")) {
                this.lastDocument = RequestAPI.getLoginInfos(inputUrl.getText().toString(), inputLogin.getText().toString(), inputPwd.getText().toString());
                if (this.lastDocument != null) {
                    this.isLogged = true;
                    addButton.setEnabled(true);
                    List<String> bases = XMLParser.getMultipleDocumentTag(this.lastDocument, "bases");
                    List<String> importProfile = XMLParser.getMultipleDocumentTag(this.lastDocument, "imports");
                    basesArray.addAll(bases);
                    importsArray.addAll(importProfile);
                } else {
                    addButton.setEnabled(false);
                }
            }
        });

        inputPwd.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !inputLogin.getText().toString().equals("")) {
                this.lastDocument = RequestAPI.getLoginInfos(inputUrl.getText().toString(), inputLogin.getText().toString(), inputPwd.getText().toString());
                if (this.lastDocument != null) {
                    this.isLogged = true;
                    addButton.setEnabled(true);
                    List<String> bases = XMLParser.getMultipleDocumentTag(this.lastDocument, "bases");
                    List<String> importProfile = XMLParser.getMultipleDocumentTag(this.lastDocument, "imports");
                    basesArray.addAll(bases);
                    importsArray.addAll(importProfile);
                } else {
                    addButton.setEnabled(false);
                }
            }
        });

        addButton.setOnClickListener(v -> {
            if (this.isLogged) {
                // TODO: Logout
            }
            Profile profile = new Profile(
                    inputName.getText().toString(),
                    inputLogin.getText().toString(),
                    inputPwd.getText().toString(),
                    inputUrl.getText().toString(),
                    0,
                    " "
            );
            Preferences.addPreference(profile, AddProfile.this);
            Intent intent = new Intent(AddProfile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            AddProfile.this.finish();
        });

        cancelButton.setOnClickListener(v -> {
            if (this.isLogged) {
                // TODO: Logout inside RequestAPI
            }
            finish();
        });
    }
}
