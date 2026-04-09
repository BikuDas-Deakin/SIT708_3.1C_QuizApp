package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnThemeToggle = findViewById(R.id.btnThemeToggle);

        // Restore name if returning from quiz
        String savedName = prefs.getString("user_name", "");
        etName.setText(savedName);

        // Update toggle button label
        updateThemeButton(btnThemeToggle, isDark);

        btnThemeToggle.setOnClickListener(v -> {
            boolean currentlyDark = prefs.getBoolean("dark_mode", false);
            boolean newDark = !currentlyDark;
            prefs.edit().putBoolean("dark_mode", newDark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            updateThemeButton(btnThemeToggle, newDark);
        });

        btnStart.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save name for session persistence
            prefs.edit().putString("user_name", name).apply();

            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("user_name", name);
            startActivity(intent);
        });
    }

    private void updateThemeButton(Button btn, boolean isDark) {
        btn.setText(isDark ? "☀ Light Mode" : "🌙 Dark Mode");
    }
}