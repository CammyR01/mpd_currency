// Name                 Cameron Reid
// Student ID           S2455533
// Programme of Study   Software Development

package com.example.reid_cameron_s2455533;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;

import java.util.List;

//Custom ArrayAdapter used to display each currency in the ListView
public class CurrencyAdapter extends ArrayAdapter<CurrencyItem> {

    //Used to convert the row_currency.xml layout into actual View objects for the ListView
    private final LayoutInflater inflater;

    public CurrencyAdapter(Context context, List<CurrencyItem> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    //Populating the list with currencies
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

            double rate = item.getRate();

            codeView.setText(item.getCode());
            rateView.setText(String.valueOf(item.getRate()));
            titleView.setText(item.getTitle());

            //Colour coding the exchange rates
            if (rate < 1.0) {
                rateView.setTextColor(Color.parseColor("#2979FF")); //Blue = very strong
            }
            else if (rate < 5.0) {
                rateView.setTextColor(Color.parseColor("#2E7D32")); //Green = strong
            }
            else if (rate < 20.0) {
                rateView.setTextColor(Color.parseColor("#F9A825")); //Amber = okay
            }
            else {
                rateView.setTextColor(Color.parseColor("#C62828")); //Red = poor
            }
        }

        return row;
    }
}
