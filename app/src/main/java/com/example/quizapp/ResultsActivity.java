package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        int score   = getIntent().getIntExtra("score", 0);
        int total   = getIntent().getIntExtra("total", 5);
        String name = getIntent().getStringExtra("user_name");

        TextView tvCongrats   = findViewById(R.id.tvCongrats);
        TextView tvScore      = findViewById(R.id.tvScore);
        Button btnNewQuiz     = findViewById(R.id.btnNewQuiz);
        Button btnFinish      = findViewById(R.id.btnFinish);
        Button btnThemeToggle = findViewById(R.id.btnThemeToggle);

        tvCongrats.setText("Congratulations, " + name + "!");
        tvScore.setText("YOUR SCORE:\n" + score + " / " + total);

        btnThemeToggle.setText(isDark ? "☀ Light" : "🌙 Dark");
        btnThemeToggle.setOnClickListener(v -> {
            boolean currentlyDark = prefs.getBoolean("dark_mode", false);
            boolean newDark = !currentlyDark;
            prefs.edit().putBoolean("dark_mode", newDark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        btnNewQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnFinish.setOnClickListener(v -> finishAffinity());
    }
}