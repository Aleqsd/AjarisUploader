package com.example.ajarisuploader;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AddProfile extends AppCompatActivity {

    private final String CHECK = "/upCheck.do";
    private final String LOGIN = "/upLogin.do";
    private final String LOGOUT = "/upLogout.do";
    private final String CONFIG_IMPORT = "/upSetConfigImport.do";
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

        List<Integer> spinnerArray = new ArrayList<>();
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                AddProfile.this,
                android.R.layout.simple_spinner_item,
                spinnerArray
        );
        inputBase.setAdapter(adapter);

        inputUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                if(urlIsValid(inputUrl.getText().toString())) {
                    inputLogin.setEnabled(true);
                    inputPwd.setEnabled(true);
                } else {
                    inputLogin.setEnabled(false);
                    inputPwd.setEnabled(false);
                    if(this.lastDocument != null) {
                        Toast toast = Toast.makeText(AddProfile.this, this.getDocumentTag(this.lastDocument, "error-message"), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });

        inputLogin.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                if(!inputPwd.getText().toString().equals("")) {
                    if(this.credentialsAreValid(inputUrl.getText().toString(), inputLogin.getText().toString(), inputPwd.getText().toString())) {
                        this.isLogged = true;
                        addButton.setEnabled(true);
                        spinnerArray.add(10);
                        spinnerArray.add(11);
                    } else {
                        addButton.setEnabled(false);
                    }
                }
            }
        });

        inputPwd.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                if(!inputLogin.getText().toString().equals("")) {
                    if(this.credentialsAreValid(inputUrl.getText().toString(), inputLogin.getText().toString(), inputPwd.getText().toString())) {
                        this.isLogged = true;
                        addButton.setEnabled(true);
                        spinnerArray.add(10);
                        spinnerArray.add(11);
                    } else {
                        addButton.setEnabled(false);
                    }
                }
            }
        });

        addButton.setOnClickListener(v -> {
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
            if(this.isLogged) {
                // TODO: Logout
            }
            finish();
        });
    }

    private boolean urlIsValid(String url) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            String result = getRequest.execute(url + this.CHECK).get();
            this.lastDocument = this.readXML(result);
            isValid = this.getErrorCode(this.lastDocument) == 0 ? true : false;
        } catch (InterruptedException e) {
            isValid = false;
        } catch (ExecutionException e) {
            isValid = false;
        }
        return isValid;
    }

    private boolean credentialsAreValid(String url, String login, String pwd) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            String result = getRequest.execute(url + this.LOGIN + "?pseudo=" + login + "&password=" + pwd + "&ajaupmo=ajaupmo").get();
            this.lastDocument = this.readXML(result);
            isValid = this.getErrorCode(this.lastDocument) == 0 ? true : false;
        }  catch (InterruptedException e) {
            e.printStackTrace();
            isValid = false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            isValid = false;
        }
        return isValid;
    }

    private Document readXML(String xmlString) {
        if(xmlString == null) return null;
        xmlString = "<result>" + xmlString.split("<result>")[1];
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private int getErrorCode(Document doc) {
        if(doc == null) return -10;
        int errorCode = -1;
        if(doc == null) {
            return errorCode;
        }
        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorCode = Integer.parseInt(eElement.getElementsByTagName("error-code").item(0).getTextContent());
        }

        return errorCode;
    }

    private String getDocumentTag(Document doc, String tag) {
        if(doc == null) return null;
        String documentTag = "";
        if(doc == null) {
            return documentTag;
        }
        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            documentTag = eElement.getElementsByTagName(tag).item(0).getTextContent();
        }

        return documentTag;
    }

    private List<String> getMultipleDocumentTag(Document doc, String tag) {
        // TODO: Return list items
        return null;
    }

}
