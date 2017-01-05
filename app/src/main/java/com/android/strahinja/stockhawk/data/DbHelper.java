package com.android.strahinja.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.android.strahinja.stockhawk.data.Contract.Stock;
import static com.android.strahinja.stockhawk.data.Contract.StockHistory;

public class DbHelper extends SQLiteOpenHelper {

    private final static String NAME =  "StockHawk.db";
    private final static int VERSION = 1;

    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + Stock.TABLE_NAME + " ("
                + Stock._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Stock.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Stock.COLUMN_BID + " TEXT NOT NULL, "
                + Stock.COLUMN_CHANGE + " REAL NOT NULL, "
                + Stock.COLUMN_CHANGE_PERCENTAGE + " REAL NOT NULL, "
                + Stock.COLUMN_NAME + " TEXT NOT NULL, "
                + "UNIQUE (" + Stock.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_STOCK_HISTORY_TABLE = "CREATE TABLE " + StockHistory.TABLE_NAME + " ("
                + StockHistory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockHistory.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + StockHistory.COLUMN_DATE + " TEXT NOT NULL, "
                + StockHistory.COLUMN_CLOSE + " TEXT NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Stock.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StockHistory.TABLE_NAME);
    }
}
