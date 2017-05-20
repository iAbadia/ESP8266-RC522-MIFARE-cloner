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
                    blocks.add(sectorsJSONArray.getString(j));
                }
                sectors.add(new Sector(sectorKey, blocks));
            }
            this.sectors = sectors;
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private static final String EMPTY_CARD_JSON = "{\n" +
            "  \"uid\":\"891723353\",\n" +
            "  \"name\":\"891723353\",\n" +
            "  \"picc\":\"MIFARE 1KB\",\n" +
            "  \"sectors\":[\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"5911e935948804008500b42ef0bb6aa8\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"blocks\":[\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"00000000000000000000000000000000\",\n" +
            "        \"000000000000ff078069ffffffffffff\"\n" +
            "      ],\n" +
            "      \"key\":\"ffffffffffff\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
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
}
