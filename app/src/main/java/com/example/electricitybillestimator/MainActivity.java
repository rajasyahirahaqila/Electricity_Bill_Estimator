package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import java.util.Locale;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private Spinner  spinnerMonth;
    private EditText editTextUnit;
    private SeekBar  seekBarRebate;
    private TextView textViewRebateValue;
    private TextView textViewTotalCharges;
    private TextView textViewFinalCost;
    private CardView cardResult;
    private Button   buttonCalculate;
    private Button   buttonHistory;

    // ─── Database ──────────────────────────────────────────────
    private DatabaseHelper databaseHelper;

    // ─── Calculation Results ───────────────────────────────────
    private double totalCharges  = 0.0;
    private double finalCost     = 0.0;
    private int    rebatePercent = 0;

    // ─── Tariff Constants (in Ringgit) ─────────────────────────
    private static final double RATE_BLOCK1  = 0.218;
    private static final double RATE_BLOCK2  = 0.334;
    private static final double RATE_BLOCK3  = 0.516;
    private static final double RATE_BLOCK4  = 0.546;
    private static final double BLOCK1_LIMIT = 200;
    private static final double BLOCK2_LIMIT = 300;
    private static final double BLOCK3_LIMIT = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make status bar icons dark/visible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.backgroundColor));
        }

        databaseHelper = new DatabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Electricity Bill Estimator");
            getSupportActionBar().setElevation(4f);
        }

        initViews();
        setupMonthSpinner();
        setupRebateSeekBar();
        setupButtons();
    }

    // ─── Connect variables to XML views ────────────────────────
    private void initViews() {
        spinnerMonth         = findViewById(R.id.spinnerMonth);
        editTextUnit         = findViewById(R.id.editTextUnit);
        seekBarRebate        = findViewById(R.id.seekBarRebate);
        textViewRebateValue  = findViewById(R.id.textViewRebateValue);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost    = findViewById(R.id.textViewFinalCost);
        cardResult           = findViewById(R.id.cardResult);
        buttonCalculate      = findViewById(R.id.buttonCalculate);
        buttonHistory        = findViewById(R.id.buttonHistory);
    }

    // ─── Fill Spinner with January – December ──────────────────
    private void setupMonthSpinner() {
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.months_array,
                        android.R.layout.simple_spinner_item
                );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerMonth.setAdapter(adapter);
    }

    // ─── SeekBar: 0 to 5, update label live ────────────────────
    private void setupRebateSeekBar() {
        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);

        seekBarRebate.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress,
                                                  boolean fromUser) {
                        rebatePercent = progress;
                        textViewRebateValue.setText(
                                String.format(Locale.getDefault(),
                                        "Rebate: %d%%", progress)
                        );
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)  {}
                });
    }

    // ─── Button click listeners ─────────────────────────────────
    private void setupButtons() {

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        MainActivity.this,
                        HistoryActivity.class);
                // Force fresh instance every time
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    // ─── Validate inputs then calculate ────────────────────────
    private void calculateBill() {

        String unitInput = editTextUnit
                .getText().toString().trim();

        // Validate: empty check
        if (unitInput.isEmpty()) {
            editTextUnit.setError(
                    "⚠ Please enter electricity unit used");
            editTextUnit.requestFocus();
            return;
        }

        double units = Double.parseDouble(unitInput);

        // Validate: range check
        if (units < 1 || units > 1000) {
            editTextUnit.setError(
                    "⚠ Unit must be between 1 and 1000 kWh");
            editTextUnit.requestFocus();
            return;
        }

        // Calculate charges
        totalCharges = calculateBlockCharges(units);
        double rebateDecimal = rebatePercent / 100.0;
        finalCost = totalCharges - (totalCharges * rebateDecimal);

        // Display then save
        displayResults();
        autoSaveRecord();
    }

    // ─── Block tariff calculation ───────────────────────────────
    private double calculateBlockCharges(double units) {
        double charges = 0.0;

        if (units <= BLOCK1_LIMIT) {
            charges = units * RATE_BLOCK1;

        } else if (units <= BLOCK2_LIMIT) {
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (units - BLOCK1_LIMIT) * RATE_BLOCK2;

        } else if (units <= BLOCK3_LIMIT) {
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            charges += (units - BLOCK2_LIMIT) * RATE_BLOCK3;

        } else {
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            charges += (BLOCK3_LIMIT - BLOCK2_LIMIT) * RATE_BLOCK3;
            charges += (units - BLOCK3_LIMIT) * RATE_BLOCK4;
        }

        return charges;
    }

    // ─── Display results on screen ─────────────────────────────
    private void displayResults() {
        textViewTotalCharges.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", totalCharges));

        textViewFinalCost.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", finalCost));

        // Show result card
        cardResult.setVisibility(View.VISIBLE);
    }

    // ─── Auto save to database ──────────────────────────────────
    private void autoSaveRecord() {
        try {
            String unitInput = editTextUnit
                    .getText().toString().trim();

            if (unitInput.isEmpty()) return;

            String selectedMonth = spinnerMonth
                    .getSelectedItem().toString();
            double unit = Double.parseDouble(unitInput);

            BillRecord record = new BillRecord(
                    selectedMonth,
                    unit,
                    rebatePercent,
                    totalCharges,
                    finalCost
            );

            long result = databaseHelper.insertRecord(record);

            if (result != -1) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "✅ Bill calculated and saved!",
                                Toast.LENGTH_LONG).show()
                );
            } else {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "⚠ Could not save record.",
                                Toast.LENGTH_LONG).show()
                );
            }

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(MainActivity.this,
                            "❌ Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show()
            );
        }
    }

    // ─── Options Menu ───────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "About");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent intent = new Intent(
                    MainActivity.this,
                    About.class);  // Fixed: was About.class
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}