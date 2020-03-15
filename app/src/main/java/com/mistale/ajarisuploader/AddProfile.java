package com.mistale.ajarisuploader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.mistale.ajarisuploader.api.RequestAPI;
import com.mistale.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class AddProfile extends AppCompatActivity {

    private Document lastDocument = null;
    private boolean isLogged = false;
    private List<Base> currentBases = new ArrayList<>();
    private List<String> importProfile = new ArrayList<>();
    private ProgressDialog progressDialog;

    EditText inputName;
    EditText inputUrl;
    EditText inputLogin;
    EditText inputPwd;
    Spinner inputBase;
    Spinner inputImport;
    Button addButton;
    Button cancelButton;

    List<String> basesArray = new ArrayList<>();
    List<String> importsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_profile);
        getWindow().setBackgroundDrawableResource(R.drawable.ajaris_background_alt) ;
        this.progressDialog = new ProgressDialog(AddProfile.this, R.style.Theme_AppCompat_DayNight_Dialog);

        this.inputName = findViewById(R.id.input_name);
        this.inputUrl = findViewById(R.id.input_url);
        this.inputLogin = findViewById(R.id.input_login);
        this.inputPwd = findViewById(R.id.input_pwd);
        this.inputBase = findViewById(R.id.input_base);
        this.inputImport = findViewById(R.id.input_import);
        this.addButton = findViewById(R.id.button_add);
        this.cancelButton = findViewById(R.id.button_cancel);

        this.inputUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (RequestAPI.urlIsValid(this.inputUrl.getText().toString(), this.progressDialog)) {
                    this.inputLogin.setEnabled(true);
                    this.inputPwd.setEnabled(true);
                } else {
                    this.inputLogin.setEnabled(false);
                    this.inputPwd.setEnabled(false);
                }
            }
        });

        this.inputLogin.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !this.inputPwd.getText().toString().equals("")) {
                this.populateBasesAndImports();
            }
        });

        this.inputPwd.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !this.inputLogin.getText().toString().equals("")) {
                this.populateBasesAndImports();
            }
        });

        this.addButton.setOnClickListener(v -> {
            if (this.isLogged) {
                // TODO: Logout
            }
            Base base = new Base();
            for(int i = 0; i < this.currentBases.size(); i++) {
                if(this.currentBases.get(i).getName().equals(this.inputBase.getSelectedItem().toString())) {
                    base.setNumber(this.currentBases.get(i).getNumber());
                    base.setName(this.currentBases.get(i).getName());
                }
            }
            Profile profile = new Profile(
                    this.inputName.getText().toString(),
                    this.inputLogin.getText().toString(),
                    this.inputPwd.getText().toString(),
                    this.inputUrl.getText().toString(),
                    base,
                    this.inputImport.getSelectedItem().toString()
            );
            Preferences.addPreference(profile, AddProfile.this);
            Intent intent = new Intent(AddProfile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            AddProfile.this.finish();
        });

        this.cancelButton.setOnClickListener(v -> {
            if (this.isLogged) {
                // TODO: Logout inside RequestAPI
            }
            finish();
        });
    }

    public void populateBasesAndImports() {
        this.lastDocument = RequestAPI.getLoginInfos(this.inputUrl.getText().toString(), this.inputLogin.getText().toString(), this.inputPwd.getText().toString(), this.progressDialog);
        if (this.lastDocument != null) {
            this.isLogged = true;
            this.addButton.setEnabled(true);
            this.currentBases = XMLParser.getBases(this.lastDocument);
            this.importProfile = XMLParser.getMultipleDocumentTag(this.lastDocument, "imports");
            this.basesArray.clear();
            this.importsArray.clear();
            for(int i = 0; i < this.currentBases.size(); i++) {
                this.basesArray.add(this.currentBases.get(i).getName());
            }
            this.importsArray.addAll(importProfile);
            this.inputBase.setVisibility(Spinner.VISIBLE);
            this.inputImport.setVisibility(Spinner.VISIBLE);
        } else {
            this.basesArray.clear();
            this.importsArray.clear();
            this.addButton.setEnabled(false);
            this.inputBase.setVisibility(Spinner.INVISIBLE);;
            this.inputImport.setVisibility(Spinner.INVISIBLE);
        }

        ArrayAdapter<String> adapterBases = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                this.basesArray
        );
        adapterBases.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        this.inputBase.setAdapter(adapterBases);

        ArrayAdapter<String> adapterImports = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                this.importsArray
        );
        adapterImports.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        this.inputImport.setAdapter(adapterImports);
    }
}
