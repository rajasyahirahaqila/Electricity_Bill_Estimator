package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private TextView textViewDetailMonth;
    private TextView textViewDetailUnit;
    private TextView textViewDetailRebate;
    private TextView textViewDetailTotalCharges;
    private TextView textViewDetailFinalCost;
    private Button   buttonEdit;
    private Button   buttonDelete;

    // ─── Database ──────────────────────────────────────────────
    private DatabaseHelper databaseHelper;

    // ─── Current Record ────────────────────────────────────────
    private BillRecord currentRecord;
    private int        recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Detail");
            getSupportActionBar().setElevation(4f);
        }

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get the record ID passed from HistoryActivity
        recordId = getIntent().getIntExtra("RECORD_ID", -1);

        // Safety check — if no ID passed, go back
        if (recordId == -1) {
            Toast.makeText(this,
                    "❌ Error loading record.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadRecord();
        setupButtons();
    }

    // ─── Connect variables to XML views ────────────────────────
    private void initViews() {
        textViewDetailMonth        = findViewById(
                R.id.textViewDetailMonth);
        textViewDetailUnit         = findViewById(
                R.id.textViewDetailUnit);
        textViewDetailRebate       = findViewById(
                R.id.textViewDetailRebate);
        textViewDetailTotalCharges = findViewById(
                R.id.textViewDetailTotalCharges);
        textViewDetailFinalCost    = findViewById(
                R.id.textViewDetailFinalCost);
        buttonEdit                 = findViewById(R.id.buttonEdit);
        buttonDelete               = findViewById(R.id.buttonDelete);
    }

    // ─── Load record from database and display it ───────────────
    private void loadRecord() {
        currentRecord = databaseHelper.getRecordById(recordId);

        if (currentRecord == null) {
            Toast.makeText(this,
                    "❌ Record not found.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fill all TextViews with record data
        textViewDetailMonth.setText(
                currentRecord.getMonth());

        textViewDetailUnit.setText(
                String.format(Locale.getDefault(),
                        "%.0f kWh", currentRecord.getUnit()));

        textViewDetailRebate.setText(
                String.format(Locale.getDefault(),
                        "%d%%", currentRecord.getRebate()));

        textViewDetailTotalCharges.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", currentRecord.getTotalCharges()));

        textViewDetailFinalCost.setText(
                String.format(Locale.getDefault(),
                        "RM %.2f", currentRecord.getFinalCost()));
    }

    // ─── Edit and Delete button logic ──────────────────────────
    private void setupButtons() {

        // ── Edit Button → go to EditActivity (Phase 8) ─────────
        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DetailActivity.this,
                    EditActivity.class);
            intent.putExtra("RECORD_ID", recordId);
            startActivity(intent);
        });

        // ── Delete Button → confirm then delete ────────────────
        buttonDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });
    }

    // ─── Show confirmation dialog before deleting ───────────────
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete the "
                        + currentRecord.getMonth()
                        + " record? This cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)

                // Confirm delete
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                deleteRecord();
                            }
                        })

                // Cancel — do nothing
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    // ─── Delete the record from database ───────────────────────
    private void deleteRecord() {
        int result = databaseHelper.deleteRecord(recordId);

        if (result > 0) {
            // Success
            Toast.makeText(this,
                    "✅ Record deleted successfully!",
                    Toast.LENGTH_SHORT).show();

            // Go back to HistoryActivity
            finish();

        } else {
            // Failed
            Toast.makeText(this,
                    "❌ Failed to delete record.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Reload data when returning from EditActivity ───────────
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh display in case record was edited
        if (currentRecord != null) {
            loadRecord();
        }
    }

    // ─── Back arrow ─────────────────────────────────────────────
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}