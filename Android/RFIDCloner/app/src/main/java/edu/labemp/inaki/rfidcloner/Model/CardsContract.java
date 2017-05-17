package edu.labemp.inaki.rfidcloner.Model;

import android.provider.BaseColumns;

/**
 * Created by inaki on 5/17/17.
 */

public final class CardsContract {

    private CardsContract(){}

    public static class CardEntry implements BaseColumns {
        public static final String TABLE_NAME = "cards";
        public static final String COLUMN_NAME_JSONCARD = "jsoncard";
    }
}
