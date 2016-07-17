package com.example.dopravo.vollyproject.data;

import org.json.JSONObject;

//receives code, temp, & text from JSON obj
public class Condition implements JSONPopulator {

    private int code;
    private int temp;
    private String description;

    public int getCode() {
        return code;
    }

    public int getTemp() {
        return temp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void populate(JSONObject data) {
        code = data.optInt("code");
        temp = data.optInt("temp");
        description = data.optString("text");
    }
}
