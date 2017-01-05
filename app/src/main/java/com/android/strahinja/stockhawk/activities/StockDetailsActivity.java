package com.android.strahinja.stockhawk.activities;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.data.Contract;
import com.android.strahinja.stockhawk.models.Stock;
import com.android.strahinja.stockhawk.models.StockHistory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import static android.R.attr.data;
import static android.R.id.list;

public class StockDetailsActivity extends AppCompatActivity {

    private static final String SAVED_STOCK_KEY = "saved_stock";

    @BindView(R.id.activity_stock_details_toolbar)
    Toolbar mToolBar;

    @BindView(R.id.chart)
    LineChartView mChart;
    @BindView(R.id.stock_company_name)
    TextView mStockCompanyName;
    @BindView(R.id.stock_exchange)
    TextView mStockExchange;
    @BindView(R.id.bid)
    TextView mBid;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private Stock mStock;
    private DecimalFormat dollarFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChart.setVisibility(LineChartView.GONE);
        mChart.setOnValueTouchListener(new ValueTouchListener());

        if (savedInstanceState != null) mStock = (Stock) savedInstanceState.get(SAVED_STOCK_KEY);
        else mStock = getIntent().getParcelableExtra(getString(R.string.intent_stock_details_stock));

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mBid.setText(dollarFormat.format(Float.parseFloat(mStock.getBid())));
        mStockCompanyName.setText(mStock.getName());
        mStockExchange.setText(getString(R.string.stock_exchange_place_holder, mStock.getSymbol()));

        getStockHistory(mStock.getSymbol());
    }

    private void getStockHistory(String symbol) {
        AsyncTask<Void, Void, List<StockHistory>> myTask = new AsyncTask<Void, Void, List<StockHistory>>() {
            @Override
            protected void onPostExecute(List<StockHistory> stockHistories) {
                if (stockHistories != null && !stockHistories.isEmpty()) drawGraph(stockHistories);
            }

            @Override
            protected List<StockHistory> doInBackground(Void... voids) {
                List<StockHistory> list = new ArrayList<>();

                Cursor cursor = getContentResolver().query(
                        Contract.StockHistory.URI,
                        Contract.StockHistory.STOCK_HISTORY_COLUMNS,
                        Contract.StockHistory.COLUMN_SYMBOL + " = ?",
                        new String[]{mStock.getSymbol()},
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        list.add(new StockHistory(
                                cursor.getString(Contract.StockHistory.POSTION_SYMBOL),
                                cursor.getString(Contract.StockHistory.POSTION_DATE),
                                cursor.getString(Contract.StockHistory.POSTION_CLOSE)));
                    }
                    while (cursor.moveToNext());
                }

                return list;
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

    private List<StockHistory> mList;

    public void drawGraph(List<StockHistory> list) {
        mList = list;
        mChart.setVisibility(LineChartView.VISIBLE);
        mProgressBar.setVisibility(ProgressBar.GONE);

        LineChartData data;
        List<Line> lines = new ArrayList<>();

        List<PointValue> values = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            values.add(new PointValue(i, Float.parseFloat(list.get(i).getClose())));
        }

        Line line = new Line(values);
        line.setColor(Color.parseColor("#FFFFFF"));
        line.setShape(ValueShape.DIAMOND);
        line.setCubic(true);
        line.setFilled(true);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        line.setPointColor(ChartUtils.COLORS[0]);
        lines.add(line);


        data = new LineChartData(lines);

        List<AxisValue> axisValuesForX = new ArrayList<>();
        AxisValue tempAxisValue;

        for (int i = 0; i < list.size(); i++) {
            if (i % 10 == 0) {
                tempAxisValue = new AxisValue(i);
                tempAxisValue.setLabel(list.get(i).getDate());
                axisValuesForX.add(tempAxisValue);
            }
        }

        Axis axisX = new Axis(axisValuesForX);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setMaxLabelChars(4);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mChart.setLineChartData(data);
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {
        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            DecimalFormat decimalFormat = new DecimalFormat(".##");
            String date = mList.get(pointIndex).getDate();
            String close = decimalFormat.format(value.getY());
            String toastMessage = String.format(getString(R.string.toast_display_pointvalue, date, close));
            Toast.makeText(StockDetailsActivity.this,
                    toastMessage,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onValueDeselected() {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_STOCK_KEY, mStock);
    }
}
