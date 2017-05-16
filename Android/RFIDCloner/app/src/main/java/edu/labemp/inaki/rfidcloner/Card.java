package edu.labemp.inaki.rfidcloner;

import org.json.JSONObject;

/**
 * Created by inaki on 5/16/17.
 */

public class Card {
    private Sector[] sectors;
    private String name;
    private String uid;
    private boolean sync;

    public Card(Sector[] sectors, String name, String uid, boolean sync) {
        this.sectors = sectors;
        this.name = name;
        this.uid = uid;
        this.sync = sync;
    }

    public Card(String name, boolean sync) {
        this.name = name;
        this.sync = sync;
    }

    public Card(JSONObject cardJSON) {
        parseJSONToCard(cardJSON);
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

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
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
