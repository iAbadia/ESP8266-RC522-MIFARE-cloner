package edu.labemp.inaki.rfidcloner.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.labemp.inaki.rfidcloner.Model.CardsDataSource;
import edu.labemp.inaki.rfidcloner.View.SettingsActivity;

/**
 * Created by inaki on 5/17/17.
 */

public class ESP8266Connector {

    public static String UPDATE_ACTION = "rfid.UPDATE_CARDS_LIST";
    public static String UPDATE_ACTION_NO_NEW = "rfid.UPDATE_ACTION_NO_NEW";

    private static final String ESPDEFURL = "192.168.0.136";

    private final String listCardsUrl = "/listcards";
    private final String cardUrl = "/card";
    private Context mContext;
    CardsDataSource mCardsDataSource;

    private List<String> pendingCards = new ArrayList<>();

    public ESP8266Connector(Context context) {
        this.mContext = context;
        this.mCardsDataSource = new CardsDataSource(context);
    }

    public void getNewCards() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String ESPURL = "http://" + sharedPref.getString("esp8266_ip", ESPDEFURL);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ESPURL + listCardsUrl;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        try {
                            JSONArray names = response.getJSONArray("names");
                            for (int i = 0; i < response.getInt("number"); i++) {
                                pendingCards.add(names.getString(i));
                            }
                            if (response.getInt("number") <= 0) {
                                Intent intent = new Intent();
                                Log.d("ESP8266Connector", "Sending: " + UPDATE_ACTION_NO_NEW);
                                intent.setAction(UPDATE_ACTION_NO_NEW);
                                mContext.sendBroadcast(intent);
                            } else {
                                getPendingCards();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Response", response.toString());
                        // Log jic

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public void getPendingCards () {
        // ESP IP
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String ESPURL = "http://" + sharedPref.getString("esp8266_ip", ESPDEFURL);
        // Get card name
        String name = pendingCards.get(0);
        pendingCards.remove(0);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ESPURL + cardUrl + "?name=" + name;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        long id = mCardsDataSource.addCard(response);
                        Log.d("Response", response.toString());
                        Log.d("Card added to DB, ID: ", Long.toString(id));
                        // Notify CardsListActivity
                        Intent intent = new Intent();
                        Log.d("ESP8266Connector", "Sending: " + UPDATE_ACTION);
                        intent.setAction(UPDATE_ACTION);
                        mContext.sendBroadcast(intent);
                        // Recursive
                        if (pendingCards.size() > 0) {
                            // Still pending cards
                            getPendingCards();
                        }
                }
    },
            new Response.ErrorListener()

    {
        @Override
        public void onErrorResponse (VolleyError error){
        Log.d("Error.Response", error.toString());
    }
    }
        );

    // add it to the RequestQueue
        queue.add(getRequest);
}

}
