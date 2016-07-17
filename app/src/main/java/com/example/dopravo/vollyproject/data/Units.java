package com.example.dopravo.vollyproject.data;

import org.json.JSONObject;

//receives the temp from the JSON data
public class Units implements JSONPopulator {

    private String temp;

    public String getTemp() {
        return temp;
    }

    @Override
    public void populate(JSONObject data) {
        temp = data.optString("temperature");
    }
}
