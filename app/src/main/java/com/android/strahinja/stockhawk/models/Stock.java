package com.android.strahinja.stockhawk.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Stock implements Parcelable{
    private String symbol;
    private String bid;
    private String change;
    private String name;
    private String changeInPercentage;

    public Stock(String symbol, String bid, String change, String changeInPercentage, String name){
        this.symbol = symbol;
        this.bid = bid;
        this.change = change;
        this.changeInPercentage = changeInPercentage;
        this.name = name;
    }

    public String getChangeInPercentage() {
        return changeInPercentage;
    }

    public String getName() {
        return name;
    }

    public String getChange() {
        return change;
    }

    public String getBid() {
        return bid;
    }

    public String getSymbol() {
        return symbol;
    }

    public Stock(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        this.symbol = data[0];
        this.bid = data[1];
        this.change = data[2];
        this.changeInPercentage = data[3];
        this.name = data[4];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.symbol,
                this.bid,
                this.change,
                this.changeInPercentage,
                this.name
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Stock createFromParcel(Parcel in) {
            return new Stock(in);
        }

        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };
}
