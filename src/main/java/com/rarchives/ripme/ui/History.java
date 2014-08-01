package com.rarchives.ripme.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class History {

    private List<HistoryEntry> list = new ArrayList<HistoryEntry>();

    public void add(HistoryEntry entry) {
        list.add(entry);
    }
    public void remove(HistoryEntry entry) {
        list.remove(entry);
    }
    public void clear() {
        list.clear();
    }

    public void fromJSON(JSONArray jsonArray) {
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            list.add(new HistoryEntry().fromJSON(json));
        }
    }
    
    public void fromFile(String filename) throws IOException {
        InputStream is = new FileInputStream(filename);
        try {
            String jsonString = IOUtils.toString(is);
            JSONArray jsonArray = new JSONArray(jsonString);
            fromJSON(jsonArray);
        } finally {
            is.close();
        }
    }
    
    public void fromList(List<String> stringList) {
        for (String item : stringList) {
            HistoryEntry entry = new HistoryEntry();
            entry.url = item;
            list.add(entry);
        }
    }

    public JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        for (HistoryEntry entry : list) {
            jsonArray.put(entry.toJSON());
        }
        return jsonArray;
    }
    
    public List<HistoryEntry> toList() {
        return list;
    }
    
    public void toFile(String filename) throws IOException {
        OutputStream os = new FileOutputStream(filename);
        try {
            IOUtils.write(toJSON().toString(), os);
        } finally {
            os.close();
        }
    }

}
