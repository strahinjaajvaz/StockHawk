package com.android.strahinja.stockhawk.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new StockViewsFactory(this.getApplicationContext(),
                intent));
    }
}