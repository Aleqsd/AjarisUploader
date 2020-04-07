package com.mistale.ajarisuploader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mistale.ajarisuploader.api.RequestAPI;
import com.mistale.ajarisuploader.api.XMLParser;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AddProfile extends AppCompatActivity {

    private Document lastDocument = null;
    private boolean isLogged = false;
    private List<Base> currentBases = new ArrayList<>();
    private List<String> importProfile = new ArrayList<>();
    private ProgressDialog progressDialog;
    private boolean profileModification;
    private HashMap<String, String> profileMap;
    private int position;

    EditText inputName;
    EditText inputUrl;
    EditText inputLogin;
    EditText inputPwd;
    TextView textBase;
    TextView textProfil;
    Spinner inputBase;
    Spinner inputImport;
    Button validateLogin;
    Button addButton;
    Button cancelButton;

    List<String> basesArray = new ArrayList<>();
    List<String> importsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_profile);
        getWindow().setBackgroundDrawableResource(R.drawable.ajaris_background_alt);
        this.progressDialog = new ProgressDialog(AddProfile.this, R.style.Theme_AppCompat_DayNight_Dialog);

        this.inputName = findViewById(R.id.input_name);
        this.inputUrl = findViewById(R.id.input_url);
        this.inputLogin = findViewById(R.id.input_login);
        this.inputPwd = findViewById(R.id.input_pwd);
        this.textBase = findViewById(R.id.text_base);
        this.textProfil = findViewById(R.id.text_profil);
        this.inputBase = findViewById(R.id.input_base);
        this.inputImport = findViewById(R.id.input_import);
        this.validateLogin = findViewById(R.id.input_validate_login);
        this.addButton = findViewById(R.id.button_add);
        this.cancelButton = findViewById(R.id.button_cancel);

        profileModification = false;

        Intent intentReceived = getIntent();
        Bundle extras = intentReceived.getExtras();
        if (extras != null) {
            if (extras.containsKey("profileMap")) {
                profileMap = (HashMap<String, String>) extras.getSerializable("profileMap");
                inputName.setText(profileMap.get("name"));
                this.inputLogin.setEnabled(true);
                this.inputPwd.setEnabled(true);
                inputUrl.setText(profileMap.get("url"));
                inputLogin.setText(profileMap.get("login"));
                position = Integer.parseInt(profileMap.get("position"));
                addButton.setText("Modifier le profil");

                profileModification = true;
            }
        }


        this.inputUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (RequestAPI.urlIsValid(this.inputUrl.getText().toString(), this.progressDialog)) {
                    this.inputLogin.setEnabled(true);
                    this.inputPwd.setEnabled(true);
                } else {
                    this.inputLogin.setEnabled(false);
                    this.inputPwd.setEnabled(false);
                    this.displayError(getString(R.string.wrong_url));
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

        this.validateLogin.setOnClickListener(v -> {
            if (!this.inputLogin.getText().toString().equals("") && !this.inputLogin.getText().toString().equals("")) {
                this.populateBasesAndImports();
            } else {
                this.displayError(getString(R.string.missing_fields));
            }

            try {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            } catch (Exception e) {
                Log.e("ADDPROFILE", Objects.requireNonNull(e.getMessage()));
            }
        });

        this.addButton.setOnClickListener(v -> {
            if (this.isLogged) {
                RequestAPI.isLoggedOut(inputUrl.getText().toString(), XMLParser.getDocumentTag(this.lastDocument, "sessionid"), progressDialog);
            }

            String url = this.inputUrl.getText().toString();
            if (!url.substring(url.length() - 1).equals("/"))
                url = url + "/";

            Base base = new Base();
            if (currentBases.size() == 1) {
                base.setNumber(this.currentBases.get(0).getNumber());
                base.setName(this.currentBases.get(0).getName());
            } else {
                for (int i = 0; i < this.currentBases.size(); i++) {
                    if (this.currentBases.get(i).getName().equals(this.inputBase.getSelectedItem().toString())) {
                        base.setNumber(this.currentBases.get(i).getNumber());
                        base.setName(this.currentBases.get(i).getName());
                    }
                }
            }

            String profileImport;
            if (importProfile.size() == 1)
                profileImport = importProfile.get(0);
            else
                profileImport = this.inputImport.getSelectedItem().toString();

            Profile profile = new Profile(
                    this.inputName.getText().toString(),
                    this.inputLogin.getText().toString(),
                    this.inputPwd.getText().toString(),
                    url,
                    base,
                    profileImport
            );

            if (profileModification)
                Preferences.addPreferenceToPosition(profile, position, this);
            else
                Preferences.addPreference(profile, AddProfile.this);

            Intent intent = new Intent(AddProfile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            AddProfile.this.finish();
        });

        this.cancelButton.setOnClickListener(v -> {
            if (this.isLogged) {
                RequestAPI.isLoggedOut(inputUrl.getText().toString(), XMLParser.getDocumentTag(this.lastDocument, "sessionid"), progressDialog);
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

            if (currentBases.size() == 1) {
                this.basesArray.add("Par défaut");
                this.inputBase.setBackground(Drawable.createFromPath("@null"));
                this.inputBase.setEnabled(false);
            } else {
                for (int i = 0; i < this.currentBases.size(); i++) {
                    this.basesArray.add(this.currentBases.get(i).getName());
                }
            }

            if (importProfile.size() == 1) {
                this.importsArray.add("Par défaut");
                this.inputImport.setBackground(Drawable.createFromPath("@null"));
                this.inputImport.setEnabled(false);
            } else
                this.importsArray.addAll(importProfile);

            this.inputBase.setVisibility(Spinner.VISIBLE);
            this.inputImport.setVisibility(Spinner.VISIBLE);
            this.textBase.setVisibility(TextView.VISIBLE);
            this.textProfil.setVisibility(TextView.VISIBLE);
            this.validateLogin.setVisibility(Button.INVISIBLE);
        } else {
            if (this.isLogged) {
                RequestAPI.isLoggedOut(inputUrl.getText().toString(), XMLParser.getDocumentTag(this.lastDocument, "sessionid"), progressDialog);
            }
            this.basesArray.clear();
            this.importsArray.clear();
            this.addButton.setEnabled(false);
            this.inputBase.setVisibility(Spinner.INVISIBLE);
            this.inputImport.setVisibility(Spinner.INVISIBLE);
            this.textBase.setVisibility(TextView.INVISIBLE);
            this.textProfil.setVisibility(TextView.INVISIBLE);
            this.validateLogin.setVisibility(Button.VISIBLE);
            this.displayError(getString(R.string.wrong_login));
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

    public void displayError(String msg) {
        Toast.makeText(AddProfile.this, msg, Toast.LENGTH_LONG).show();
    }
}
