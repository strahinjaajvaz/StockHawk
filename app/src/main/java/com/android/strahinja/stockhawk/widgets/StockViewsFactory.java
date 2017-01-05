package com.android.strahinja.stockhawk.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.data.Contract;
import com.android.strahinja.stockhawk.models.Stock;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext = null;
    private Cursor mCursor = null;
    private int appWidgetId;

    private DecimalFormat dollarFormat;

    public StockViewsFactory(Context ctxt, Intent intent) {
        this.mContext = ctxt;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mCursor = mContext.getContentResolver().query(Contract.Stock.URI,
                Contract.Stock.STOCK_COLUMNS,
                null,
                null,
                Contract.Stock.COLUMN_SYMBOL);
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormat.setMaximumFractionDigits(2);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return (mCursor.getCount());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(mContext.getPackageName(),
                R.layout.row);

        mCursor.moveToPosition(position);
        Stock stock = new Stock(mCursor.getString(Contract.Stock.POSITION_SYMBOL),
                mCursor.getString(Contract.Stock.POSITION_BID),
                mCursor.getString(Contract.Stock.POSITION_CHANGE),
                mCursor.getString(Contract.Stock.POSITION_CHANGE_PERCENTAGE),
                mCursor.getString(Contract.Stock.POSITION_NAME));

        row.setTextViewText(R.id.widget_symbol, stock.getSymbol());
        row.setTextViewText(R.id.widget_change, dollarFormat.format(Float.parseFloat(stock.getBid())));
        row.setTextViewText(R.id.widget_change_percentage, (Float.parseFloat(stock.getChange()) > 0.0 ? "+" : "") + stock.getChangeInPercentage() + "%");

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @Override
    public void onDataSetChanged() {
        if(mCursor != null)
            mCursor.close();

        final long token = Binder.clearCallingIdentity();
        try {
            mCursor = mContext.getContentResolver().query(Contract.Stock.URI,
                    Contract.Stock.STOCK_COLUMNS,
                    null,
                    null,
                    Contract.Stock.COLUMN_SYMBOL);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}