package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class HistoryActivity extends AppCompatActivity {

    // ─── UI Elements ───────────────────────────────────────────
    private ListView         listViewBills;
    private TextView         textViewRecordCount;
    private View             layoutEmpty;
    private View             layoutHeader;
    private View             viewDivider;

    // ─── Database and Adapter ──────────────────────────────────
    private DatabaseHelper   databaseHelper;
    private BillAdapter      billAdapter;
    private List<BillRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Make status bar icons dark/visible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.backgroundColor));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
            getSupportActionBar().setElevation(4f);
        }

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupListClick();
        // loadRecords() is called in onResume() — no need here
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

        // Always create fresh DatabaseHelper to get latest data
        databaseHelper = new DatabaseHelper(this);
        recordList = databaseHelper.getAllRecords();

        // Debug — remove after confirming it works
        Toast.makeText(this,
                "Found " + recordList.size() + " records",
                Toast.LENGTH_SHORT).show();

        if (recordList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutHeader.setVisibility(View.GONE);
            viewDivider.setVisibility(View.GONE);
            listViewBills.setVisibility(View.GONE);

        } else {
            layoutEmpty.setVisibility(View.GONE);
            layoutHeader.setVisibility(View.VISIBLE);
            viewDivider.setVisibility(View.VISIBLE);
            listViewBills.setVisibility(View.VISIBLE);

            textViewRecordCount.setText(
                    recordList.size() + " record(s)");

            billAdapter = new BillAdapter(this, recordList);
            listViewBills.setAdapter(billAdapter);
        }
    }

    // ─── Tap list item → open DetailActivity ───────────────────
    private void setupListClick() {
        listViewBills.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {
                        BillRecord tapped = recordList.get(position);
                        Intent intent = new Intent(
                                HistoryActivity.this,
                                DetailActivity.class);
                        intent.putExtra("RECORD_ID", tapped.getId());
                        startActivity(intent);
                    }
                });
    }

    // ─── Reload every time screen becomes visible ──────────────
    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
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
                    this, About.class);  // Fixed
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}