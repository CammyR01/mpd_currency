// Name                 Cameron Reid
// Student ID           S2455533
// Programme of Study   Software Development

package com.example.reid_cameron_s2455533;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//Class used to handle currency conversion using user input
public class ConversionActivity extends AppCompatActivity {

    private TextView txtDirection, txtResult;
    private EditText editAmount;
    private Button btnConvert, btnSwap;

    private String code;
    private double rate;
    private boolean gbpToOther = true; //Default direction is GBP to selected currency

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        //Getting the users selected currency and corresponding exchange rate
        code = getIntent().getStringExtra("code");
        rate = getIntent().getDoubleExtra("rate", 0.0);

        txtDirection = findViewById(R.id.txtDirection);
        txtResult    = findViewById(R.id.txtResult);
        editAmount    = findViewById(R.id.edtAmount);
        btnConvert   = findViewById(R.id.btnConvert);
        btnSwap      = findViewById(R.id.btnSwap);

        updateDirectionLabel();

        btnConvert.setOnClickListener(v -> doConversion());

        btnSwap.setOnClickListener(v -> {
            gbpToOther = !gbpToOther;
            updateDirectionLabel();
            txtResult.setText("");
        });
    }

    //Updates the label to show which conversion direction is selected
    private void updateDirectionLabel() {
        if (gbpToOther) {
            txtDirection.setText("GBP → " + code);
        } else {
            txtDirection.setText(code + " → GBP");
        }
    }

    //Conversion calculation
    private void doConversion() {
        String input = editAmount.getText().toString().trim();
        if (input.isEmpty()) {
            txtResult.setText("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            txtResult.setText("Invalid number.");
            return;
        }

        double result;
        String text;

        if (gbpToOther) {
            result = amount * rate;
            text = String.format("%.2f GBP = %.2f %s", amount, result, code);
        } else {
            result = amount / rate;
            text = String.format("%.2f %s = %.2f GBP", amount, code, result);
        }

        txtResult.setText(text);
    }
}