package com.mistale.ajarisuploader.api;

import android.util.Log;

import com.mistale.ajarisuploader.Base;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
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

    public static Document readUploadXML(String xmlString) {
        if (xmlString == null) return null;
        xmlString = xmlString.replaceAll("\\t", "");
        xmlString = xmlString.replaceAll("\\n", "");
        xmlString = "<upload-result>" + xmlString.split("<upload-result>")[1];
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

    public static int getCode(Document doc) {
        if (doc == null) return -10;
        int errorCode = -1;

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorCode = Integer.parseInt(eElement.getElementsByTagName("code").item(0).getTextContent());
        }
        return errorCode;
    }

    public static int getUploadCode(Document doc) {
        if (doc == null) return -10;
        int errorCode = -1;

        NodeList nList = doc.getElementsByTagName("upload-result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorCode = Integer.parseInt(eElement.getElementsByTagName("code").item(0).getTextContent());
        }
        return errorCode;
    }

    public static int getContributionId(Document doc) {
        if (doc == null) return -10;
        int errorCode = -1;

        NodeList nList = doc.getElementsByTagName("upload-result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorCode = Integer.parseInt(eElement.getElementsByTagName("contribution-id").item(0).getTextContent());
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

    public static String getErrorMessageForLogout(Document doc) {
        if (doc == null) return "";
        String errorMessage = "";

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            errorMessage = eElement.getElementsByTagName("message").item(0).getTextContent();
        }
        return errorMessage;
    }

    public static String getConfig(Document doc) {
        if (doc == null) return "";
        String configValue = "";

        NodeList nList = doc.getElementsByTagName("result");
        Node nNode = nList.item(0);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            configValue = eElement.getElementsByTagName("imports").item(0).getFirstChild().getFirstChild().getNodeValue();
        }
        return configValue;
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

    public static List<String> getMultipleDocumentTag(Document doc, String tag) {
        List<String> results = new ArrayList<>();
        try {
            NodeList nList = doc.getElementsByTagName(tag);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    for (int name = 0; name < eElement.getElementsByTagName("name").getLength(); name++) {
                        //eElement.getAttribute("num");
                        results.add(eElement.getElementsByTagName("name").item(name).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            results.clear();
        }
        return results;
    }

    public static List<Base> getBases(Document doc) {
        List<Base> results = new ArrayList<>();
        try {
            NodeList nList = doc.getElementsByTagName("bases");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    for (int name = 0; name < eElement.getElementsByTagName("name").getLength(); name++) {
                        results.add(new Base(Integer.parseInt(eElement.getElementsByTagName("name").item(name).getAttributes().getNamedItem("num").getNodeValue()),
                                eElement.getElementsByTagName("name").item(name).getTextContent()));
                    }
                }
            }
        } catch (Exception e) {
            results.clear();
        }
        return results;
    }
}
