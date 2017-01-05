package com.android.strahinja.stockhawk.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.strahinja.stockhawk.R;
import com.android.strahinja.stockhawk.activities.StockListActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddStockDialogFragment extends DialogFragment {

    @BindView(R.id.fragment_addstock_example_stock)
    EditText mAddStock;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View custom = inflater.inflate(R.layout.fragment_addstock, null);

        ButterKnife.bind(this, custom);

        builder.setView(custom);

        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        mAddStock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                button.setEnabled(mAddStock.getText() != null && mAddStock.getText().toString().length() != 0);
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    private void addStock(){
        Activity activity = getActivity();
        if(activity instanceof StockListActivity){
            ((StockListActivity) activity).addStock(mAddStock.getText().toString());
        }
    }
}
