package com.rarchives.ripme.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class History {
    private final List<HistoryEntry> list;
    private static final String[] COLUMNS = new String[] {
        "URL",
        "N",
        "created",
        "modified",
        "#",
        ""
    };

    public History() {
        this.list = new ArrayList<>();
    }

    public void add(HistoryEntry entry) {
        list.add(entry);
    }
    public void remove(HistoryEntry entry) {
        list.remove(entry);
    }
    public void remove(int index) {
        list.remove(index);
    }
    public void clear() {
        list.clear();
    }
    public HistoryEntry get(int index) {
        return list.get(index);
    }
    public String getColumnName(int index) {
        return COLUMNS[index];
    }
    public int getColumnCount() {
        return COLUMNS.length;
    }
    public Object getValueAt(int row, int col) {
        HistoryEntry entry = this.list.get(row);
        return switch (col) {
            case 0 -> entry.url;
            case 1 -> row;
            case 2 -> dateToHumanReadable(entry.startDate);
            case 3 -> dateToHumanReadable(entry.modifiedDate);
            case 4 -> entry.count;
            case 5 -> entry.selected;
            default -> null;
        };
    }
    private String dateToHumanReadable(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }

    public boolean containsURL(String url) {
        for (HistoryEntry entry : this.list) {
            if (entry.url.equals(url)) {
                return true;
            }
        }
        return false;
    }

    public HistoryEntry getEntryByURL(String url) {
        for (HistoryEntry entry : this.list) {
            if (entry.url.equals(url)) {
                return entry;
            }
        }
        throw new RuntimeException("Could not find URL " + url + " in History");
    }

    private void fromJSON(JSONArray jsonArray) {
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            list.add(new HistoryEntry().fromJSON(json));
        }
    }

    public void fromFile(String filename) throws IOException {
        try (InputStream is = new FileInputStream(filename)) {
            String jsonString = IOUtils.toString(is, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);
            fromJSON(jsonArray);
        } catch (JSONException e) {
            throw new IOException("Failed to load JSON file " + filename + ": " + e.getMessage(), e);
        }
    }

    public void fromList(List<String> stringList) {
        for (String item : stringList) {
            HistoryEntry entry = new HistoryEntry();
            entry.url = item;
            list.add(entry);
        }
    }

    private JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        for (HistoryEntry entry : list) {
            jsonArray.put(entry.toJSON());
        }
        return jsonArray;
    }

    public List<HistoryEntry> toList() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void toFile(String filename) throws IOException {
        try (OutputStream os = new FileOutputStream(filename)) {
            IOUtils.write(toJSON().toString(2), os, "UTF-8");
        }
    }
}
