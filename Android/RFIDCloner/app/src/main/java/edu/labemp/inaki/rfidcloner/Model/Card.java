package edu.labemp.inaki.rfidcloner.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inaki on 5/16/17.
 */

public class Card {
    private List<Sector> sectors;
    private String name;
    private String uid;
    private int id;
    private String picc;


    public Card() {
        new Card(EMPTY_CARD_JSON);
    }


    public Card(List<Sector> sectors, String name, String uid, int id) {
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

    public List<Sector> getSectors() {
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

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPicc() {
        return picc;
    }

    public void setPicc(String picc) {
        this.picc = picc;
    }

    public static String getEmptyCardJson() {
        return EMPTY_CARD_JSON;
    }

    private void parseJSONToCard(JSONObject cardJSON) {
        try {
            // Extract data
            this.uid = cardJSON.getString("uid");
            this.name = cardJSON.getString("name");
            this.picc = cardJSON.getString("picc");
            JSONArray sectorsJSONArray = cardJSON.getJSONArray("sectors");
            // Get sectors
            List<Sector> sectors = new ArrayList<>();
            for (int i = 0; i<sectorsJSONArray.length(); i++) {
                JSONObject sectorJSONObj = sectorsJSONArray.getJSONObject(i);
                String sectorKey = sectorJSONObj.getString("key");
                JSONArray sectorBlocks = sectorJSONObj.getJSONArray("blocks");
                // Get blocks
                List<String> blocks = new ArrayList<>();
                for (int j = 0; j<sectorBlocks.length(); j++) {
                    blocks.add(sectorBlocks.getString(j));
                }
                sectors.add(new Sector(sectorKey, blocks));
            }
            this.sectors = sectors;
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJSONString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject cardJSON = new JSONObject();
        try {
            cardJSON.put("name", this.name);
            cardJSON.put("uid", this.uid);
            cardJSON.put("picc", this.picc);
            //Add serctos
            JSONArray sectorsJSONArray = new JSONArray();
            cardJSON.put("sectors", sectorsJSONArray);
            for(Sector sector: this.sectors) {
                sectorsJSONArray.put(sector.toJSON());
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return cardJSON;
    }
    private static final String EMPTY_CARD_JSON = "{\"uid\":\"891723353\",\"name\":\"891723353\",\"picc\":\"MIFARE 1KB\",\"sectors\":[{\"blocks\":[\"5911e935948804008500b42ef0bb6aa8\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff078069ffffffffffff\"],\"key\":\"ffffffffffff\"},{\"blocks\":[\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"00000000000000000000000000000000\",\"000000000000ff0780bcffffffffffff\"],\"key\":\"ffffffffffff\"}]}";
}

class Sector {

    private String key;
    private List<String> blocks;

    public Sector(String key, List<String> blocks) {
        this.key = key;
        this.blocks = blocks;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<String> blocks) {
        this.blocks = blocks;
    }

    public JSONObject toJSON() {
        JSONObject sectorJSON = new JSONObject();
        try {
            sectorJSON.put("key", this.key);
            JSONArray blocksJSON = new JSONArray();
            for (String block : this.blocks) {
                blocksJSON.put(block);
            }
            sectorJSON.put("blocks", blocksJSON);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return sectorJSON;
    }
}
