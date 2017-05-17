package edu.labemp.inaki.rfidcloner.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.labemp.inaki.rfidcloner.Model.CardsContract.*;

/**
 * Created by inaki on 5/17/17.
 */

public class CardsDataSource {

    private CardsDbHelper mDbHelper;

    public CardsDataSource(Context context) {
        mDbHelper = new CardsDbHelper(context);
    }

    public long addCard(Card card) {
        return addCard(card.toJSON());
    }

    public long addCard(JSONObject cardJSON) {
        return addCard(cardJSON.toString());
    }

    public long addCard(String cardJSONSring) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CardEntry.COLUMN_NAME_JSONCARD, cardJSONSring);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(CardEntry.TABLE_NAME, null, values);
    }

    public List<Card> getCards() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                CardEntry._ID,
                CardEntry.COLUMN_NAME_JSONCARD
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                CardEntry._ID + " DESC";

        Cursor cursor = db.query(
                CardEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Card> cardsList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                cardsList.add(new Card(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_NAME_JSONCARD)),
                        cursor.getInt(cursor.getColumnIndex(CardEntry._ID))));
            } while (cursor.moveToNext());
        }

        return cardsList;
    }

    public Card getCard(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                CardEntry._ID,
                CardEntry.COLUMN_NAME_JSONCARD
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = CardEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(id)};


        Cursor cursor = db.query(
                CardEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                     // The columns for the WHERE clause
                selectionArgs,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        String cardJSONString = "";
        int cardId = 0;

        if (cursor.moveToFirst()) {
            do {
                cardJSONString = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_NAME_JSONCARD));
                cardId = cursor.getInt(cursor.getColumnIndex(CardEntry._ID));

            } while (cursor.moveToNext());
        }

        return new Card(cardJSONString, cardId);
    }
}
