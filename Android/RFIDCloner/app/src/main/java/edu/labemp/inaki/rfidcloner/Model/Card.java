package edu.labemp.inaki.rfidcloner.Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by inaki on 5/16/17.
 */

public class Card {
    private Sector[] sectors;
    private String name;
    private String uid;
    private int id;


    public Card(Sector[] sectors, String name, String uid, int id) {
        this.sectors = sectors;
        this.name = name;
        this.uid = uid;
        this.id = id;
    }

    public Card(String cardJSONString, int id) {
        try {
            parseJSONToCard(new JSONObject(cardJSONString));
            this.id = id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Card(String name) {
        this.name = name;
    }

    public Card(JSONObject cardJSON, int id) {
        parseJSONToCard(cardJSON);
        this.id = id;
    }

    public Sector[] getSectors() {
        return sectors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = sectors;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JSONObject toJSON() {
        return null;
    }

    private void parseJSONToCard(JSONObject cardJSON) {

    }
}

class Sector {

    private String key;
    private String[] blocks;

    public Sector(String key, String[] blocks) {
        this.key = key;
        this.blocks = blocks;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String[] getBlocks() {
        return blocks;
    }

    public void setBlocks(String[] blocks) {
        this.blocks = blocks;
    }
}
