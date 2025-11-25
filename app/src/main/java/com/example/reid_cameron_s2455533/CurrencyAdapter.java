package com.example.reid_cameron_s2455533;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CurrencyAdapter extends ArrayAdapter<CurrencyItem> {

    private final LayoutInflater inflater;

    public CurrencyAdapter(Context context, List<CurrencyItem> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.row_currency, parent, false);
        }

        CurrencyItem item = getItem(position);
        if (item != null) {
            TextView codeView = row.findViewById(R.id.codeTextView);
            TextView rateView = row.findViewById(R.id.rateTextView);
            TextView titleView = row.findViewById(R.id.titleTextView);

            codeView.setText(item.getCode());
            rateView.setText(String.valueOf(item.getRate()));
            titleView.setText(item.getTitle());
        }

        return row;
    }
}
