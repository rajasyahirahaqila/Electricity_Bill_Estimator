package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private ListView listViewBills;
    private TextView textViewRecordCount;
    private View     layoutEmpty;
    private View     layoutHeader;
    private View     viewDivider;

    // ─── Database and Adapter ──────────────────────────────────
    private DatabaseHelper databaseHelper;
    private BillAdapter    billAdapter;
    private List<BillRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
            getSupportActionBar().setElevation(4f);
        }

        // Initialize
        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadRecords();
        setupListClick();
    }

    // ─── Connect variables to XML views ────────────────────────
    private void initViews() {
        listViewBills       = findViewById(R.id.listViewBills);
        textViewRecordCount = findViewById(R.id.textViewRecordCount);
        layoutEmpty         = findViewById(R.id.layoutEmpty);
        layoutHeader        = findViewById(R.id.layoutHeader);
        viewDivider         = findViewById(R.id.viewDivider);

    }

    // ─── Load all records from database into ListView ──────────
    private void loadRecords() {
        recordList = databaseHelper.getAllRecords();

        if (recordList.isEmpty()) {
            // No records — show empty state
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutHeader.setVisibility(View.GONE);
            viewDivider.setVisibility(View.GONE);
            listViewBills.setVisibility(View.GONE);

        } else {
            // Has records — show the list
            layoutEmpty.setVisibility(View.GONE);
            layoutHeader.setVisibility(View.VISIBLE);
            viewDivider.setVisibility(View.VISIBLE);
            listViewBills.setVisibility(View.VISIBLE);

            // Update record count label
            textViewRecordCount.setText(
                    recordList.size() + " record(s)");

            // Create adapter and attach to ListView
            billAdapter = new BillAdapter(this, recordList);
            listViewBills.setAdapter(billAdapter);
        }
    }

    // ─── When user taps a list item → open DetailActivity ──────
    private void setupListClick() {
        listViewBills.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {

                        // Get the tapped record
                        BillRecord tapped = recordList.get(position);

                        // Open DetailActivity with the record ID
                        Intent intent = new Intent(
                                HistoryActivity.this,
                                DetailActivity.class);
                        intent.putExtra("RECORD_ID", tapped.getId());
                        startActivity(intent);
                    }
                });
    }

    // ─── Reload list when returning from DetailActivity ────────
    // (in case a record was edited or deleted)
    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    // ─── Back arrow in toolbar goes back ───────────────────────
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}