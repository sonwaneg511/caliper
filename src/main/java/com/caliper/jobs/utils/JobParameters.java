package com.caliper.jobs.utils;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JobParameters {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    private final Map<String, String> paramMap = new HashMap<>();

    public JobParameters(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) return;

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new org.xml.sax.InputSource(new StringReader(xml)));

        Element root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                paramMap.put(n.getNodeName(), n.getTextContent());
            }
        }
    }

    public String getString(String key) {
        return paramMap.get(key);
    }

    public String getString(String key, String defaultValue) {
        return paramMap.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(paramMap.get(key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return paramMap.containsKey(key) ? Boolean.parseBoolean(paramMap.get(key)) : defaultValue;
    }
    
    public Date getDate(String key) throws ParseException {
		return DATE_FORMAT.parse(paramMap.get(key));
	}
    
    public Date getDate(String key, Date defaultValue) throws ParseException {
    	return paramMap.containsKey(key) ? DATE_FORMAT.parse(paramMap.get(key)) : defaultValue;
	}

    public int getInt(String key) {
        return Integer.parseInt(paramMap.get(key));
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(paramMap.get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Map<String, String> getAll() {
        return paramMap;
    }

    @Override
    public String toString() {
        return paramMap.toString();
    }
}