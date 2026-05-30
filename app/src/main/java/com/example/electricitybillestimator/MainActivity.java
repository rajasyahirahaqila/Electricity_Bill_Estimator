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

public class MainActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private Spinner spinnerMonth;
    private EditText editTextUnit;
    private SeekBar seekBarRebate;
    private TextView textViewRebateValue;
    private TextView textViewTotalCharges;
    private TextView textViewFinalCost;
    private CardView cardResult;
    private View layoutActionButtons;
    private Button buttonCalculate;
    private Button buttonSave;
    private Button buttonHistory;
    private DatabaseHelper databaseHelper;
    private boolean isCalculated = false;

    // ─── Calculation Results (stored for saving later) ─────────
    private double totalCharges = 0.0;
    private double finalCost = 0.0;
    private int rebatePercent = 0;

    // ─── Tariff Constants (in Ringgit) ─────────────────────────
    private static final double RATE_BLOCK1 = 0.218; // 1   - 200  kWh
    private static final double RATE_BLOCK2 = 0.334; // 201 - 300  kWh
    private static final double RATE_BLOCK3 = 0.516; // 301 - 600  kWh
    private static final double RATE_BLOCK4 = 0.546; // 601 - 1000 kWh

    // ─── Block Thresholds ──────────────────────────────────────
    private static final double BLOCK1_LIMIT = 200;
    private static final double BLOCK2_LIMIT = 300;
    private static final double BLOCK3_LIMIT = 600;
    private static final double BLOCK4_LIMIT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Electricity Bill Estimator");
            getSupportActionBar().setElevation(4f);
        }

        // Step 1: Initialize all UI elements
        initViews();

        // Step 2: Set up Spinner with months
        setupMonthSpinner();

        // Step 3: Set up SeekBar for rebate
        setupRebateSeekBar();

        // Step 4: Set up button click listeners
        setupButtons();

        databaseHelper = new DatabaseHelper(this);
    }

    // ═══════════════════════════════════════════════════════════
    // STEP 1: Connect Java variables to XML views
    // ═══════════════════════════════════════════════════════════
    private void initViews() {
        spinnerMonth       = findViewById(R.id.spinnerMonth);
        editTextUnit       = findViewById(R.id.editTextUnit);
        seekBarRebate      = findViewById(R.id.seekBarRebate);
        textViewRebateValue= findViewById(R.id.textViewRebateValue);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost  = findViewById(R.id.textViewFinalCost);
        cardResult         = findViewById(R.id.cardResult);
        layoutActionButtons= findViewById(R.id.layoutActionButtons);
        buttonCalculate    = findViewById(R.id.buttonCalculate);
        buttonSave         = findViewById(R.id.buttonSave);
        buttonHistory      = findViewById(R.id.buttonHistory);
    }

    // ═══════════════════════════════════════════════════════════
    // STEP 2: Fill Spinner with January – December
    // ═══════════════════════════════════════════════════════════
    private void setupMonthSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.months_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerMonth.setAdapter(adapter);
    }

    // ═══════════════════════════════════════════════════════════
    // STEP 3: SeekBar moves 0 to 5, update label live
    // ═══════════════════════════════════════════════════════════
    private void setupRebateSeekBar() {
        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);

        seekBarRebate.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress,
                                                  boolean fromUser) {
                        // Update the label next to "Rebate Percentage"
                        rebatePercent = progress;
                        textViewRebateValue.setText(
                                String.format(Locale.getDefault(),
                                        "Rebate: %d%%", progress)
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Not needed but must be implemented
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Not needed but must be implemented
                    }
                });
    }

    // ═══════════════════════════════════════════════════════════
    // STEP 4: Button click listeners
    // ═══════════════════════════════════════════════════════════
    private void setupButtons() {

        // --- Calculate Button ---
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        // --- Save Button ---
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });

        // --- History Button (logic added in Phase 6) ---
        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        MainActivity.this,
                        HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // CORE FUNCTION: Validate inputs then calculate
    // ═══════════════════════════════════════════════════════════
    private void calculateBill() {

        // ── 1. Get unit input from EditText ──────────────────
        String unitInput = editTextUnit.getText().toString().trim();

        // ── 2. Validate: check if empty ──────────────────────
        if (unitInput.isEmpty()) {
            editTextUnit.setError(
                    "⚠ Please enter electricity unit used"
            );
            editTextUnit.requestFocus();
            return; // Stop here, don't calculate
        }

        // ── 3. Convert String to number ──────────────────────
        double units = Double.parseDouble(unitInput);

        // ── 4. Validate: check range 1 – 1000 ────────────────
        if (units < 1 || units > 1000) {
            editTextUnit.setError(
                    "⚠ Unit must be between 1 and 1000 kWh"
            );
            editTextUnit.requestFocus();
            return; // Stop here, don't calculate
        }

        // ── 5. Get selected month from Spinner ────────────────
        String selectedMonth = spinnerMonth
                .getSelectedItem().toString();

        // ── 6. Get rebate from SeekBar ────────────────────────
        // rebatePercent is already updated live by SeekBar listener

        // ── 7. Calculate Total Charges using block tariff ─────
        totalCharges = calculateBlockCharges(units);

        // ── 8. Calculate Final Cost after rebate ─────────────
        double rebateDecimal = rebatePercent / 100.0;
        finalCost = totalCharges - (totalCharges * rebateDecimal);

        // ── 9. Display results ────────────────────────────────
        displayResults();
        isCalculated = true;
    }

    // ═══════════════════════════════════════════════════════════
    // BLOCK TARIFF CALCULATION
    // This is the core electricity billing formula
    // ═══════════════════════════════════════════════════════════
    private double calculateBlockCharges(double units) {
        double charges = 0.0;

        if (units <= BLOCK1_LIMIT) {
            // ── All units fall in Block 1 (1–200 kWh) ──
            // Example: 150 kWh → 150 × 0.218 = RM 32.70
            charges = units * RATE_BLOCK1;

        } else if (units <= BLOCK2_LIMIT) {
            // ── Units span Block 1 + part of Block 2 ──
            // Example: 250 kWh
            //   Block 1: 200 × 0.218 = 43.60
            //   Block 2:  50 × 0.334 = 16.70
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (units - BLOCK1_LIMIT) * RATE_BLOCK2;

        } else if (units <= BLOCK3_LIMIT) {
            // ── Units span Block 1 + Block 2 + part of Block 3 ──
            // Example: 467 kWh
            //   Block 1: 200 × 0.218 = 43.60
            //   Block 2: 100 × 0.334 = 33.40
            //   Block 3: 167 × 0.516 = 86.17
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            charges += (units - BLOCK2_LIMIT) * RATE_BLOCK3;

        } else {
            // ── Units span all 4 blocks ──
            // Example: 800 kWh
            //   Block 1: 200 × 0.218 = 43.60
            //   Block 2: 100 × 0.334 = 33.40
            //   Block 3: 300 × 0.516 = 154.80
            //   Block 4: 200 × 0.546 = 109.20
            charges  = BLOCK1_LIMIT * RATE_BLOCK1;
            charges += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            charges += (BLOCK3_LIMIT - BLOCK2_LIMIT) * RATE_BLOCK3;
            charges += (units - BLOCK3_LIMIT) * RATE_BLOCK4;
        }

        return charges;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY RESULTS on screen
    // ═══════════════════════════════════════════════════════════
    private void displayResults() {

        // Format numbers as RM X.XX
        String formattedTotal = String.format(
                Locale.getDefault(), "RM %.2f", totalCharges
        );
        String formattedFinal = String.format(
                Locale.getDefault(), "RM %.2f", finalCost
        );

        // Set the TextViews with calculated values
        textViewTotalCharges.setText(formattedTotal);
        textViewFinalCost.setText(formattedFinal);

        // Show the result card (it was hidden before)
        cardResult.setVisibility(View.VISIBLE);

        // Show the Save and History buttons
        layoutActionButtons.setVisibility(View.VISIBLE);

        buttonSave.setEnabled(true);
        buttonSave.setAlpha(1.0f);

        // Show a helpful success toast
        Toast.makeText(this,
                "✅ Calculation complete!",
                Toast.LENGTH_SHORT).show();
    }

    // ═══════════════════════════════════════════════════════════
    // OPTIONS MENU (for About page navigation later)
    // ═══════════════════════════════════════════════════════════
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
                    About.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ═══════════════════════════════════════════════════════════
// SAVE RECORD to SQLite database
// ═══════════════════════════════════════════════════════════
    private void saveRecord() {

        // Guard: must calculate first before saving
        if (!isCalculated) {
            Toast.makeText(this,
                    "⚠ Please calculate the bill first before saving!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Guard: unit field not empty
        String unitInput = editTextUnit
                .getText().toString().trim();
        if (unitInput.isEmpty()) {
            Toast.makeText(this,
                    "⚠ Please enter and calculate first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedMonth = spinnerMonth
                .getSelectedItem().toString();
        double unit = Double.parseDouble(unitInput);

        // Create record object
        BillRecord record = new BillRecord(
                selectedMonth,
                unit,
                rebatePercent,
                totalCharges,
                finalCost
        );

        // Insert into database
        long result = databaseHelper.insertRecord(record);

        if (result != -1) {
            Toast.makeText(this,
                    "✅ Record saved successfully!",
                    Toast.LENGTH_SHORT).show();

            // Set flag to false — prevents saving same result twice
            isCalculated = false;

            // Visually dim but keep ENABLED so toast can show
            buttonSave.setAlpha(0.5f);

        } else {
            Toast.makeText(this,
                    "❌ Failed to save. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

