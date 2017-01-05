package com.android.strahinja.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.data.Contract;
import com.android.strahinja.stockhawk.helpers.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public final class JobSync {
    private static final String LOG_TAG = JobSync.class.getSimpleName();

    private static final String ACTION_DATA_UPDATED = "android.appwidget.action.APPWIDGET_UPDATE";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int MONTHS_OF_HISTORY = 2;

    private JobSync() {
    }

    public static void initialize(Context context) {
        setPeriodicSync(context);
    }

    static void retrieveData(Context context, String[] symbol, Handler handler) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.MONTH, -MONTHS_OF_HISTORY);

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean initialized = sharedPreferences.getBoolean(context.getString(R.string.pref_default_value_key), false);

            String[] searchParams;
            if (!initialized) {
                sharedPreferences.edit().putBoolean(context.getString(R.string.pref_default_value_key), true).commit();
                searchParams =  new String[]{"YHOO","MSFT","GOOG","FB"};
            } else {
                if (symbol != null)
                    searchParams = symbol;
                else {
                    String[] temp = getSymbols(context);
                    if (temp.length > 0) // making sure there is something in the database
                        searchParams = temp;
                    else {
                        context.getContentResolver().notifyChange(Contract.Stock.URI, null);
                        return;
                    }
                }
            }

            if (searchParams.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(searchParams);
            Iterator<String> iterator =  (new HashSet<String>(Arrays.asList(searchParams))).iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String stockSymbol = iterator.next();

                Stock stock = quotes.get(stockSymbol);
                StockQuote quote = stock.getQuote();

                if(quote.getPrice() == null)
                {
                    Message msg = new Message();
                    msg.obj = context.getString(R.string.error_invalid_stock) + stockSymbol;
                    handler.sendMessage(msg);
                    continue;
                }

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();
                String name = stock.getName();

                saveStockHistoryToDataBase(context, stock.getHistory(from, to, Interval.DAILY));

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Stock.COLUMN_SYMBOL, stockSymbol);
                quoteCV.put(Contract.Stock.COLUMN_BID, price);
                quoteCV.put(Contract.Stock.COLUMN_CHANGE_PERCENTAGE, percentChange);
                quoteCV.put(Contract.Stock.COLUMN_CHANGE, change);
                quoteCV.put(Contract.Stock.COLUMN_NAME,name);
                quoteCVs.add(quoteCV);
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Stock.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Log.i(LOG_TAG, exception.getMessage());
        }
    }

    private static String[] getSymbols(Context context) {
        Cursor cursor = context.getContentResolver().query(Contract.Stock.URI,
                Contract.Stock.STOCK_COLUMNS,
                null,
                null,
                Contract.Stock.COLUMN_SYMBOL);
        StringBuilder searchParams = new StringBuilder();

        List<String> symbols = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            boolean hasNext = true;
            do {
                symbols.add(cursor.getString(Contract.Stock.POSITION_SYMBOL));
            } while (cursor.moveToNext());
        }
        return symbols.toArray(new String[symbols.size()]);
    }

    private static void saveStockHistoryToDataBase(Context context, List<HistoricalQuote> history) {
        Collections.reverse(history);
        List<ContentValues> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (HistoricalQuote it : history) {
            ContentValues cv = new ContentValues();
            cv.put(Contract.StockHistory.COLUMN_SYMBOL, it.getSymbol());
            cv.put(Contract.StockHistory.COLUMN_DATE, format.format(it.getDate().getTime()));
            cv.put(Contract.StockHistory.COLUMN_CLOSE, it.getClose().toString());
            list.add(cv);
        }
        context.getContentResolver().bulkInsert(Contract.StockHistory.URI,
                list.toArray(new ContentValues[list.size()]));
        context.getContentResolver().delete(Contract.StockHistory.makeUriForStockHistory(history.get(0).getSymbol()), null, null);

        context.getContentResolver().bulkInsert(Contract.StockHistory.URI,
                list.toArray(new ContentValues[list.size()]));
    }

    public static void syncimmediately(Context context) {
        if (Utils.isOnline(context))
            context.startService(new Intent(context, JobIntentService.class));
        else {
            JobScheduler mJobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

            JobInfo.Builder builder = new JobInfo.Builder(
                    PERIODIC_ID,
                    new ComponentName(context.getPackageName(), JobSchedulerService.class.getName())
            );
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
            mJobScheduler.schedule(builder.build());
        }
    }


    private static void setPeriodicSync(Context context) {
        JobScheduler mJobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(
                PERIODIC_ID,
                new ComponentName(context.getPackageName(), JobSchedulerService.class.getName())
        );
        builder.setPeriodic(PERIOD);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        mJobScheduler.schedule(builder.build());
    }
}
