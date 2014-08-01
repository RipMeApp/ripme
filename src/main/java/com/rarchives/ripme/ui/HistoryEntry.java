package com.rarchives.ripme.ui;

import java.util.Date;

import org.json.JSONObject;

public class HistoryEntry {

    public String url          = "",
                  title        = "";
    public int    count        = 0;
    public Date   startDate    = new Date(),
                  modifiedDate = new Date();

    public HistoryEntry() {
    }

    public HistoryEntry fromJSON(JSONObject json) {
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
