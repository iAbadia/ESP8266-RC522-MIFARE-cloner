package edu.labemp.inaki.rfidcloner.Controller;

import android.content.Context;
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

import edu.labemp.inaki.rfidcloner.Model.Card;
import edu.labemp.inaki.rfidcloner.Model.CardsDataSource;

/**
 * Created by inaki on 5/17/17.
 */

public class ESP8266Connector {

    private String ESPURL = "http://192.168.0.136";
    private final String listCardsUrl = "/listcards";
    private final String cardUrl = "/card";
    private Context mContext;
    CardsDataSource mCardsDataSource;

    public ESP8266Connector(Context context) {
        this.mContext = context;
        this.mCardsDataSource = new CardsDataSource(context);
    }

    public void getNewCards() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ESPURL + listCardsUrl;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        try{
                            JSONArray names = response.getJSONArray("names");
                            for (int i = 0; i<response.getInt("number"); i++) {
                                getNewCard(names.getString(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public void getNewCard(String name) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ESPURL + cardUrl + "?name=" + name;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        long id = mCardsDataSource.addCard(response);
                        Log.d("Response", response.toString());
                        Log.d("Card added to DB, ID: ", Long.toString(id));

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }

}
