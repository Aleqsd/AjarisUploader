package com.example.ajarisuploader.api;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser {
    private static final String TAG = "XMLPARSER";

    public static Document readXML(String xmlString) {
        if (xmlString == null) return null;
        xmlString = xmlString.replaceAll("\\t", "");
        xmlString = xmlString.replaceAll("\\n", "");
        xmlString = "<result>" + xmlString.split("<result>")[1];
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static int getErrorCode(Document doc) {
        if (doc == null) return -10;
        int errorCode = -1;

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorCode = Integer.parseInt(eElement.getElementsByTagName("error-code").item(0).getTextContent());
        }
        return errorCode;
    }

    public static String getErrorMessage(Document doc) {
        if (doc == null) return "";
        String errorMessage = "";

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorMessage = eElement.getElementsByTagName("error-message").item(0).getTextContent();
        }
        return errorMessage;
    }

    public static String getDocumentTag(Document doc, String tag) {
        if (doc == null) return null;
        String documentTag = "";

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            documentTag = eElement.getElementsByTagName(tag).item(0).getTextContent();
        }
        return documentTag;
    }

    public List<String> getMultipleDocumentTag(Document doc, String tag) {
        // TODO: Return list items
        return null;
    }
}
