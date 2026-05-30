package com.example.electricitybillestimator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {

    // ─── GitHub URL — change this to your real GitHub URL ──────
    private static final String GITHUB_URL =
            "https://github.com/YOURUSERNAME/YOURREPO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // Show back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        setupGithubLink();
    }

    // ─── Make GitHub URL clickable ──────────────────────────────
    private void setupGithubLink() {
        TextView textViewGithubUrl = findViewById(
                R.id.textViewGithubUrl);

        textViewGithubUrl.setOnClickListener(v -> {
            try {
                // Open URL in phone browser
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(GITHUB_URL));
                startActivity(browserIntent);

            } catch (Exception e) {
                Toast.makeText(this,
                        "Could not open URL. Please check your internet connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
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