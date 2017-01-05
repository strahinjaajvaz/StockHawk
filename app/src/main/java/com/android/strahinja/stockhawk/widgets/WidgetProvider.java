package com.android.strahinja.stockhawk.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.activities.StockListActivity;

public class WidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            try {
                Intent svcIntent = new Intent(ctxt, WidgetService.class);

                svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

                RemoteViews widget = new RemoteViews(ctxt.getPackageName(),
                        R.layout.widget);

                widget.setRemoteAdapter(appWidgetId, R.id.words,
                        svcIntent);

                Intent clickIntent = new Intent(ctxt, StockListActivity.class);
                PendingIntent clickPI = PendingIntent
                        .getActivity(ctxt, 0,
                                clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                widget.setPendingIntentTemplate(R.id.words, clickPI);

                appWidgetManager.updateAppWidget(appWidgetId, widget);
            } catch (Exception e) {
                Log.i(LOG_TAG, e.getMessage());
            }

            super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
        }
    }
}