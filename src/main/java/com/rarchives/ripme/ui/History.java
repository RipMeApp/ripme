package com.rarchives.ripme.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class History {

    private List<Entry> list = new ArrayList<Entry>();

    public void add(Entry entry) {
        list.add(entry);
    }
    public void remove(Entry entry) {
        list.remove(entry);
    }
    public void clear() {
        list.clear();
    }

    public void fromJSON(JSONArray jsonArray) {
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            list.add(new Entry().fromJSON(json));
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
            Entry entry = new Entry();
            entry.url = item;
            list.add(entry);
        }
    }

    public JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        for (Entry entry : list) {
            jsonArray.put(entry.toJSON());
        }
        return jsonArray;
    }
    
    public List<Entry> toList() {
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

    class Entry {
        public String url          = "",
                      title        = "";
        public int    count        = 0;
        public Date   startDate    = new Date(),
                      modifiedDate = new Date();

        public Entry() {
        }

        public Entry fromJSON(JSONObject json) {
            this.url          = json.getString("url");
            this.title        = json.getString("title");
            this.count        = json.getInt("count");
            this.startDate    = new Date(json.getLong("startDate"));
            this.modifiedDate = new Date(json.getLong("modifiedDate"));
            return this;
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("url",          this.url);
            json.put("title",        this.title);
            json.put("count",        this.count);
            json.put("startDate",    this.startDate.getTime());
            json.put("modifiedDate", this.modifiedDate.getTime());
            return json;
        }
        
        public String toString() {
            return this.url;
        }
    }
}
