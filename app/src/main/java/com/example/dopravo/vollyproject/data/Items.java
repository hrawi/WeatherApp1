package com.example.dopravo.vollyproject.data;

import org.json.JSONObject;

//receives the condition from the JSON data

public class Items implements JSONPopulator {
    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    @Override
    public void populate(JSONObject data) {
        condition = new Condition();
        condition.populate(data.optJSONObject("condition"));
    }
}
