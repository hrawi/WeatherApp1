package com.example.dopravo.vollyproject.data;

import org.json.JSONObject;

//receives the channel from JSON
//channel includes units and item

public class Channel implements JSONPopulator {

    private Items item;
    private Units unit;

    public Items getItem() {
        return item;
    }

    public Units getUnit() {
        return unit;
    }

    @Override
    public void populate(JSONObject data) {
        unit = new Units();
        unit.populate(data.optJSONObject("units"));

        item = new Items();
        item.populate(data.optJSONObject("item"));
    }
}
