package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import android.content.Intent;
import android.view.Menu;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class EditActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private Spinner  spinnerEditMonth;
    private EditText editTextEditUnit;
    private SeekBar  seekBarEditRebate;
    private TextView textViewEditRebateValue;
    private TextView textViewEditTotalCharges;
    private TextView textViewEditFinalCost;
    private CardView cardEditResult;
    private Button   buttonRecalculate;
    private Button   buttonSaveChanges;

    // ─── Database ──────────────────────────────────────────────
    private DatabaseHelper databaseHelper;
    private BillRecord     currentRecord;
    private int            recordId;

    // ─── Calculation Results ───────────────────────────────────
    private double newTotalCharges = 0.0;
    private double newFinalCost    = 0.0;
    private int    rebatePercent   = 0;
    private boolean isRecalculated = false;

    // ─── Tariff Constants ──────────────────────────────────────
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
        setContentView(R.layout.activity_edit);

        // Make status bar icons dark/visible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.backgroundColor));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Record");
            getSupportActionBar().setElevation(4f);
        }

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get record ID from DetailActivity
        recordId = getIntent().getIntExtra("RECORD_ID", -1);

        // Safety check
        if (recordId == -1) {
            Toast.makeText(this,
                    "❌ Error loading record.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupMonthSpinner();
        setupRebateSeekBar();
        loadExistingRecord();
        setupButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "About");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Connect variables to XML views ────────────────────────
    private void initViews() {
        spinnerEditMonth        = findViewById(R.id.spinnerEditMonth);
        editTextEditUnit        = findViewById(R.id.editTextEditUnit);
        seekBarEditRebate       = findViewById(R.id.seekBarEditRebate);
        textViewEditRebateValue = findViewById(
                R.id.textViewEditRebateValue);
        textViewEditTotalCharges= findViewById(
                R.id.textViewEditTotalCharges);
        textViewEditFinalCost   = findViewById(
                R.id.textViewEditFinalCost);
        cardEditResult          = findViewById(R.id.cardEditResult);
        buttonRecalculate       = findViewById(R.id.buttonRecalculate);
        buttonSaveChanges       = findViewById(R.id.buttonSaveChanges);
    }

    // ─── Fill Spinner with months ───────────────────────────────
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
        spinnerEditMonth.setAdapter(adapter);
    }

    // ─── SeekBar setup ─────────────────────────────────────────
    private void setupRebateSeekBar() {
        seekBarEditRebate.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress,
                                                  boolean fromUser) {
                        rebatePercent = progress;
                        textViewEditRebateValue.setText(
                                String.format(Locale.getDefault(),
                                        "Rebate: %d%%", progress));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)  {}
                });
    }

    // ─── Load existing record and pre-fill all fields ──────────
    private void loadExistingRecord() {
        currentRecord = databaseHelper.getRecordById(recordId);

        if (currentRecord == null) {
            Toast.makeText(this,
                    "❌ Record not found.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ── Pre-fill Month Spinner ──────────────────────────────
        // Find the position of the saved month in the array
        String[] months = getResources()
                .getStringArray(R.array.months_array);
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(currentRecord.getMonth())) {
                spinnerEditMonth.setSelection(i);
                break;
            }
        }

        // ── Pre-fill Unit EditText ──────────────────────────────
        editTextEditUnit.setText(
                String.format(Locale.getDefault(),
                        "%.0f", currentRecord.getUnit()));

        // ── Pre-fill Rebate SeekBar ─────────────────────────────
        rebatePercent = currentRecord.getRebate();
        seekBarEditRebate.setProgress(rebatePercent);
        textViewEditRebateValue.setText(
                String.format(Locale.getDefault(),
                        "Rebate: %d%%", rebatePercent));

        // ── Show existing results ───────────────────────────────
        newTotalCharges = currentRecord.getTotalCharges();
        newFinalCost    = currentRecord.getFinalCost();

        textViewEditTotalCharges.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", newTotalCharges));
        textViewEditFinalCost.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", newFinalCost));

        cardEditResult.setVisibility(View.VISIBLE);
        isRecalculated = false;
    }

    // ─── Button logic ──────────────────────────────────────────
    private void setupButtons() {

        // ── Recalculate button ──────────────────────────────────
        buttonRecalculate.setOnClickListener(v -> {
            recalculate();
        });

        // ── Save Changes button ─────────────────────────────────
        buttonSaveChanges.setOnClickListener(v -> {
            saveChanges();
        });
    }

    // ─── Recalculate with new inputs ───────────────────────────
    private void recalculate() {

        String unitInput = editTextEditUnit
                .getText().toString().trim();

        // Validate empty
        if (unitInput.isEmpty()) {
            editTextEditUnit.setError(
                    "⚠ Please enter electricity unit used");
            editTextEditUnit.requestFocus();
            return;
        }

        double units = Double.parseDouble(unitInput);

        // Validate range
        if (units < 1 || units > 1000) {
            editTextEditUnit.setError(
                    "⚠ Unit must be between 1 and 1000 kWh");
            editTextEditUnit.requestFocus();
            return;
        }

        // Calculate new charges
        newTotalCharges = calculateBlockCharges(units);
        double rebateDecimal = rebatePercent / 100.0;
        newFinalCost = newTotalCharges
                - (newTotalCharges * rebateDecimal);

        // Display new results
        textViewEditTotalCharges.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", newTotalCharges));
        textViewEditFinalCost.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", newFinalCost));

        cardEditResult.setVisibility(View.VISIBLE);
        isRecalculated = true;

        Toast.makeText(this,
                "✅ Recalculated! Press Save Changes to update.",
                Toast.LENGTH_SHORT).show();
    }

    // ─── Save updated record to database ───────────────────────
    private void saveChanges() {

        // Must recalculate before saving
        if (!isRecalculated) {
            Toast.makeText(this,
                    "⚠ Please recalculate before saving!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String unitInput = editTextEditUnit
                .getText().toString().trim();

        // Validate empty
        if (unitInput.isEmpty()) {
            Toast.makeText(this,
                    "⚠ Please enter unit value!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double unit = Double.parseDouble(unitInput);

        // Validate range
        if (unit < 1 || unit > 1000) {
            editTextEditUnit.setError(
                    "⚠ Unit must be between 1 and 1000 kWh");
            return;
        }

        String selectedMonth = spinnerEditMonth
                .getSelectedItem().toString();

        // Update the existing record object
        currentRecord.setMonth(selectedMonth);
        currentRecord.setUnit(unit);
        currentRecord.setRebate(rebatePercent);
        currentRecord.setTotalCharges(newTotalCharges);
        currentRecord.setFinalCost(newFinalCost);

        // Save to database
        int result = databaseHelper.updateRecord(currentRecord);

        if (result > 0) {
            Toast.makeText(EditActivity.this,
                    "✅ Record updated successfully!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(EditActivity.this,
                    "❌ Failed to update record.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─── Block tariff calculation (same as MainActivity) ───────
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
}