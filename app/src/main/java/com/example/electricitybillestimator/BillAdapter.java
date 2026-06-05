package com.example.electricitybillestimator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

/**
 * BillAdapter.java
 *
 * A custom ArrayAdapter that tells Android:
 * "For each BillRecord in my list, create one
 *  list_item_bill.xml row and fill it with data."
 */
public class BillAdapter extends ArrayAdapter<BillRecord> {

    // Context = the Activity using this adapter
    private final Context context;

    // The list of records to display
    private final List<BillRecord> records;

    // ─── Constructor ────────────────────────────────────────────
    public BillAdapter(Context context, List<BillRecord> records) {
        super(context, R.layout.list_item_bill, records);
        this.context = context;
        this.records = records;
    }

    // ─── Called for EACH row in the list ────────────────────────
    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        // Reuse existing row view if available (performance)
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(
                    R.layout.list_item_bill, parent, false);
        }

        // Get the BillRecord for this row
        BillRecord record = records.get(position);

        // Find the TextViews inside list_item_bill.xml
        TextView textMonth     = convertView.findViewById(
                R.id.textViewListMonth);
        TextView textUnit      = convertView.findViewById(
                R.id.textViewListUnit);
        TextView textFinalCost = convertView.findViewById(
                R.id.textViewListFinalCost);

        // Fill TextViews with data from the record
        textMonth.setText(record.getMonth());

        textUnit.setText(String.format(Locale.getDefault(),
                "%.0f kWh  •  %d%% rebate",
                record.getUnit(),
                record.getRebate()));

        textFinalCost.setText(String.format(Locale.getDefault(),
                "RM %.2f",
                record.getFinalCost()));

        return convertView;
    }
}