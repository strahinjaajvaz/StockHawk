package com.android.strahinja.stockhawk.models;

public class StockHistory {
    private String mSymbol;
    private String mDate;
    private String mClose;

    public StockHistory(String symbol, String date, String close){
        mSymbol = symbol;
        mDate = date;
        mClose = close;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public String getDate() {
        return mDate;
    }

    public String getClose() {
        return mClose;
    }
}
