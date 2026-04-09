package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvProgress, tvQuestionCount;
    private Button[] optionButtons = new Button[4];
    private Button btnSubmit, btnNext, btnThemeToggle;
    private ProgressBar progressBar;

    private List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;
    private int selectedOption = -1;
    private boolean submitted = false;
    private String userName;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        userName = getIntent().getStringExtra("user_name");

        tvQuestion      = findViewById(R.id.tvQuestion);
        tvProgress      = findViewById(R.id.tvProgress);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        progressBar     = findViewById(R.id.progressBar);
        btnSubmit       = findViewById(R.id.btnSubmit);
        btnNext         = findViewById(R.id.btnNext);
        btnThemeToggle  = findViewById(R.id.btnThemeToggle);

        optionButtons[0] = findViewById(R.id.btnOption0);
        optionButtons[1] = findViewById(R.id.btnOption1);
        optionButtons[2] = findViewById(R.id.btnOption2);
        optionButtons[3] = findViewById(R.id.btnOption3);

        questions = buildQuestions();
        progressBar.setMax(questions.size());

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optionButtons[i].setOnClickListener(v -> {
                if (!submitted) {
                    selectedOption = idx;
                    highlightSelected();
                }
            });
        }

        btnSubmit.setOnClickListener(v -> {
            if (selectedOption == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            submitAnswer();
        });

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex >= questions.size()) {
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", questions.size());
                intent.putExtra("user_name", userName);
                startActivity(intent);
                finish();
            } else {
                loadQuestion();
            }
        });

        btnThemeToggle.setText(isDark ? "☀ Light" : "🌙 Dark");
        btnThemeToggle.setOnClickListener(v -> {
            boolean currentlyDark = prefs.getBoolean("dark_mode", false);
            boolean newDark = !currentlyDark;
            prefs.edit().putBoolean("dark_mode", newDark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            btnThemeToggle.setText(newDark ? "☀ Light" : "🌙 Dark");
        });

        loadQuestion();
    }

    private void loadQuestion() {
        submitted = false;
        selectedOption = -1;
        btnNext.setVisibility(View.GONE);
        btnNext.setEnabled(false);
        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(true);

        Question q = questions.get(currentIndex);
        tvQuestion.setText(q.getQuestionText());
        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(opts[i]);
            optionButtons[i].setEnabled(true);
            resetButtonColor(optionButtons[i]);
        }

        int done = currentIndex;
        progressBar.setProgress(done);
        tvProgress.setText(done + " / " + questions.size() + " completed");
        tvQuestionCount.setText("Question " + (currentIndex + 1) + " of " + questions.size());
    }

    private void highlightSelected() {
        for (int i = 0; i < 4; i++) {
            if (i == selectedOption) {
                optionButtons[i].setBackgroundColor(
                        ContextCompat.getColor(this, R.color.selected_blue));
                optionButtons[i].setTextColor(Color.WHITE);
            } else {
                resetButtonColor(optionButtons[i]);
            }
        }
    }

    private void submitAnswer() {
        submitted = true;
        Question q = questions.get(currentIndex);
        int correct = q.getCorrectIndex();

        for (Button b : optionButtons) b.setEnabled(false);

        if (selectedOption == correct) {
            score++;
            optionButtons[correct].setBackgroundColor(
                    ContextCompat.getColor(this, R.color.correct_green));
            optionButtons[correct].setTextColor(Color.WHITE);
        } else {
            optionButtons[selectedOption].setBackgroundColor(
                    ContextCompat.getColor(this, R.color.wrong_red));
            optionButtons[selectedOption].setTextColor(Color.WHITE);
            optionButtons[correct].setBackgroundColor(
                    ContextCompat.getColor(this, R.color.correct_green));
            optionButtons[correct].setTextColor(Color.WHITE);
        }

        progressBar.setProgress(currentIndex + 1);
        tvProgress.setText((currentIndex + 1) + " / " + questions.size() + " completed");

        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setEnabled(true);
        btnNext.setText(currentIndex == questions.size() - 1 ? "See Results" : "Next");
    }

    private void resetButtonColor(Button btn) {
        btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btn.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
    }

    private List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(
                "What does CPU stand for?",
                new String[]{"Central Processing Unit", "Computer Personal Unit",
                        "Core Power Unit", "Central Program Utility"}, 0));
        list.add(new Question(
                "Which language is primarily used for Android development?",
                new String[]{"Swift", "Kotlin / Java", "Python", "C#"}, 1));
        list.add(new Question(
                "What does HTTP stand for?",
                new String[]{"HyperText Transfer Protocol", "High Transfer Text Protocol",
                        "HyperText Transmission Process", "Home Transfer Text Protocol"}, 0));
        list.add(new Question(
                "Which data structure operates on a LIFO principle?",
                new String[]{"Queue", "LinkedList", "Stack", "Tree"}, 2));
        list.add(new Question(
                "What is the binary representation of the decimal number 10?",
                new String[]{"1010", "1100", "1001", "0110"}, 0));
        return list;
    }
}