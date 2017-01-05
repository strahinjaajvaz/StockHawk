package com.android.strahinja.stockhawk.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {

    static final String AUTHORITY = "com.android.strahinja.stockhawk";
    static final String PATH_STOCK = "stock";
    static final String PATH_HISTORY = "stock_history";
    static final String PATH_STOCK_WITH_SYMBOL = "stock/*";
    static final String PATH_STOCK_HISTORY_WITH_SYMBOL = "stock_history/*";
    static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private Contract() {

    }

    public static final class Stock implements BaseColumns {

        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_STOCK).build();

        public static final String TABLE_NAME = "stock";
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_BID = "bid";
        public static final String COLUMN_CHANGE = "change";
        public static final String COLUMN_CHANGE_PERCENTAGE = "change_percentage";
        public static final String COLUMN_NAME = "name";

        public static final int POSITION_ID = 0;
        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_BID = 2;
        public static final int POSITION_CHANGE = 3;
        public static final int POSITION_CHANGE_PERCENTAGE = 4;
        public static final int POSITION_NAME = 5;

        public static final String[] STOCK_COLUMNS = new String[]{
                _ID,
                COLUMN_SYMBOL,
                COLUMN_BID,
                COLUMN_CHANGE,
                COLUMN_CHANGE_PERCENTAGE,
                COLUMN_NAME
        };

        public static Uri makeUriForStock(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        static String getStockFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }
    }

    public static final class StockHistory implements BaseColumns {

        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_HISTORY).build();

        public static final String TABLE_NAME = "stock_history";
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CLOSE = "close";

        public static final int POSTION_ID = 0;
        public static final int POSTION_SYMBOL = 1;
        public static final int POSTION_DATE = 2;
        public static final int POSTION_CLOSE = 3;

        public static final String[] STOCK_HISTORY_COLUMNS = new String[]{
                _ID,
                COLUMN_SYMBOL,
                COLUMN_DATE,
                COLUMN_CLOSE
        };

        public static Uri makeUriForStockHistory(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        public static String getSymbolFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }
    }


}
