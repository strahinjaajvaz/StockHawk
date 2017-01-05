package com.android.strahinja.stockhawk.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.strahinja.stockhawk.data.Contract.Stock;
import com.android.strahinja.stockhawk.data.Contract.StockHistory;


public class StockProvider extends ContentProvider {

    static final int STOCK = 100;
    static final int STOCK_FOR_SYMBOL = 101;

    static final int STOCK_HISTORY = 110;
    static final int STOCK_HISTORY_FOR_SYMBOL = 111;

    static UriMatcher uriMatcher = buildUriMatcher();

    private DbHelper dbHelper;

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_STOCK, STOCK);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_STOCK_WITH_SYMBOL, STOCK_FOR_SYMBOL);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_HISTORY, STOCK_HISTORY);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_STOCK_HISTORY_WITH_SYMBOL, STOCK_HISTORY_FOR_SYMBOL);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case STOCK: {
                returnCursor = db.query(
                        Stock.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case STOCK_FOR_SYMBOL: {
                returnCursor = db.query(
                        Stock.TABLE_NAME,
                        projection,
                        Stock.COLUMN_SYMBOL + " = ?",
                        new String[]{Stock.getStockFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case STOCK_HISTORY: {
                returnCursor = db.query(
                        StockHistory.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;

        switch (uriMatcher.match(uri)) {
            case STOCK: {
                db.insert(
                        Stock.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Stock.URI;
                break;
            }

            case STOCK_HISTORY: {
                db.insert(
                        StockHistory.TABLE_NAME,
                        null,
                        values
                );
                returnUri = StockHistory.URI;
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case STOCK:
                rowsDeleted = db.delete(
                        Stock.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;

            case STOCK_FOR_SYMBOL:
                String symbol = Stock.getStockFromUri(uri);
                rowsDeleted = db.delete(
                        Stock.TABLE_NAME,
                        '"' + symbol + '"' + " =" + Stock.COLUMN_SYMBOL,
                        selectionArgs
                );
                break;

            case STOCK_HISTORY_FOR_SYMBOL: {
                String symbolFromUri = StockHistory.getSymbolFromUri(uri);
                rowsDeleted = db.delete(StockHistory.TABLE_NAME,
                        '"' + symbolFromUri + '"' + '=' + StockHistory.COLUMN_SYMBOL,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case STOCK: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Stock.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return rowsInserted;
            }
            case STOCK_HISTORY: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                StockHistory.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return rowsInserted;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}

