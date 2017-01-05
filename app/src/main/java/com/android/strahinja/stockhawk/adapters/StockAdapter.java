package com.android.strahinja.stockhawk.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.activities.StockDetailsActivity;
import com.android.strahinja.stockhawk.data.Contract;
import com.android.strahinja.stockhawk.models.Stock;
import com.android.strahinja.stockhawk.helpers.Utils;

import static android.R.attr.contentDescription;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;

    private final DecimalFormat dollarFormat;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat percentageFormat;

    private Cursor cursor;


    public StockAdapter(Context context) {
        this.context = context;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public String getSymbol(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Stock.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.row_stock_info, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.symbol.setText(cursor.getString(Contract.Stock.POSITION_SYMBOL));
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Stock.POSITION_BID)));

        float rawAbsoluteChange = cursor.getFloat(Contract.Stock.POSITION_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Stock.POSITION_CHANGE_PERCENTAGE);

        holder.change.setBackgroundResource(
                rawAbsoluteChange > 0 ?
                        R.drawable.percent_change_pill_green :
                        R.drawable.percent_change_pill_red);

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        holder.change.setText(
                Utils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key)) ?
                        change :
                        percentage);
    }

    // returns how many items there are in the cursor
    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            Intent intent = new Intent(context, StockDetailsActivity.class);
            intent.putExtra(
                    context.getString(R.string.intent_stock_details_stock),
                    new Stock(
                            cursor.getString(Contract.Stock.POSITION_SYMBOL),
                            cursor.getString(Contract.Stock.POSITION_BID),
                            cursor.getString(Contract.Stock.POSITION_CHANGE),
                            cursor.getString(Contract.Stock.POSITION_CHANGE_PERCENTAGE),
                            cursor.getString(Contract.Stock.POSITION_NAME)
                    ));
            context.startActivity(intent);
        }
    }
}


