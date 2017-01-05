package com.android.strahinja.stockhawk.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.adapters.StockAdapter;
import com.android.strahinja.stockhawk.data.Contract;
import com.android.strahinja.stockhawk.fragments.AddStockDialogFragment;
import com.android.strahinja.stockhawk.helpers.Utils;
import com.android.strahinja.stockhawk.sync.JobIntentService;
import com.android.strahinja.stockhawk.sync.JobSync;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class StockListActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = StockListActivity.class.getSimpleName();
    private static final String INSTANCE_KEY = "instance_key";
    private static final int STOCK_LOADER_ID = 1;

    @BindView(R.id.acitivty_stock_list_toolbar)
    Toolbar mToolBar;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.activity_stock_list_fab)
    FloatingActionButton mFab;
    @BindView(R.id.activy_stock_list_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.error_no_data)
    View mErrorLayout;
    @BindView(R.id.no_network_text)
    TextView mErrorMessage;

    private StockAdapter mStockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);

        mErrorLayout.setVisibility(View.GONE);

        mStockAdapter = new StockAdapter(this);
        mRecyclerView.setAdapter(mStockAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (Utils.isOnline(StockListActivity.this))
                    new AddStockDialogFragment().show(getFragmentManager(), "StockAddDialogFragment");
                else {
                    Toast.makeText(StockListActivity.this, R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = mStockAdapter.getSymbol(viewHolder.getAdapterPosition());
                ContentResolver contentResolver = getContentResolver();
                contentResolver.delete(Contract.Stock.makeUriForStock(symbol), null, null);
                contentResolver.delete(Contract.StockHistory.makeUriForStockHistory(symbol), null, null);
            }
        }).attachToRecyclerView(mRecyclerView);

        if (savedInstanceState != null) {
            getSupportLoaderManager().initLoader(STOCK_LOADER_ID, null, this);
        } else {
            JobSync.initialize(this);
            getSupportLoaderManager().initLoader(STOCK_LOADER_ID, null, this);
        }
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        item.setIcon(Utils.getDisplayMode(this).equals(getString(
                R.string.pref_display_mode_absolute_key)) ?
                R.drawable.ic_dollar :
                R.drawable.ic_percentage
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acitivty_stock_list, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            Utils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            mStockAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        JobSync.syncimmediately(this);
        mSwipeRefreshLayout.setRefreshing(false);
        if(!Utils.isOnline(this)){
            Toast.makeText(this, R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Stock.URI,
                null,
                null,
                null,
                Contract.Stock.COLUMN_SYMBOL);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStockAdapter.setCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) mErrorLayout.setVisibility(View.VISIBLE);
        else mErrorLayout.setVisibility(View.GONE);
        mStockAdapter.setCursor(data);
    }

    public void addStock(final String symbol) {
        AsyncTask<Void, Void, Boolean> myTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                Cursor cursor = getContentResolver().query(Contract.Stock.URI,
                        Contract.Stock.STOCK_COLUMNS,
                        Contract.Stock.COLUMN_SYMBOL + " = ?",
                        new String[]{symbol.toUpperCase()},
                        null);

                if (cursor != null && cursor.moveToFirst()) return true;
                else return false;
            }

            @Override
            protected void onPostExecute(Boolean allreadyAdded) {
                if (allreadyAdded) {
                    Toast.makeText(StockListActivity.this, getString(R.string.toast_already_follow_stock) + symbol.toUpperCase(), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(StockListActivity.this, JobIntentService.class);
                    intent.putExtra(Intent.EXTRA_TEXT, symbol);
                    startService(intent);
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        else
            myTask.execute((Void[]) null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSTANCE_KEY, true);
    }
}
