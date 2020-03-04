package com.example.ajarisuploader;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AddProfile extends AppCompatActivity {

    private final String CHECK = "/upCheck.do";
    private final String LOGIN = "/upLogin.do";
    private final String LOGOUT = "/upLogout.do";
    private final String CONFIG_IMPORT = "/upSetConfigImport.do";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        System.out.println("ADD PROFILE ACTIVITY");
        if(this.urlIsValid("https://demo-interne.ajaris.com/Demo")) {
            System.out.println("URL IS VALID");
        } else {
            System.out.println("URL IS INVALID");
        }
    }

    private boolean urlIsValid(String url) {
        url = url.replaceAll("/$", "");
        boolean isValid;
        try {
            GetRequests getRequest = new GetRequests();
            String result = getRequest.execute(url + this.CHECK).get();
            isValid = this.getErrorCode(this.readXML(result)) == 0 ? true : false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            isValid = false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            isValid = false;
        }
        return isValid;
    }

    private Document readXML(String xmlString) {
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

    private String getErrorMessage(Document doc) {
        String errorMessage = "";
        if(doc == null) {
            return errorMessage;
        }
        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorMessage = eElement.getElementsByTagName("error-message").item(0).getTextContent();
        }

        return errorMessage;
    }

}
