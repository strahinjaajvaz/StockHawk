package com.android.strahinja.stockhawk.sync;


import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import static com.android.strahinja.stockhawk.R.id.symbol;

public class JobIntentService extends IntentService {

    public JobIntentService() {
        super(JobIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] symbol = null;
        if (intent.hasExtra(Intent.EXTRA_TEXT) && intent.getStringExtra(Intent.EXTRA_TEXT).length() > 0)
            symbol = new String[]{intent.getStringExtra(Intent.EXTRA_TEXT)};
        JobSync.retrieveData(this, symbol, mJobHandler);
    }

    public Handler mJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(),
                    msg.obj.toString(), Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
    });
}
