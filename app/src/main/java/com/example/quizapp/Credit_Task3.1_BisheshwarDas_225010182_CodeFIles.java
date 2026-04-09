package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

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


//Question.java
package com.example.quizapp;

public class Question {
    private String questionText;
    private String[] options;
    private int correctIndex;

    public Question(String questionText, String[] options, int correctIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}



//QuizActivity.java
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

//ResultActivity.java
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


//activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:background="?android:attr/colorBackground"
android:fillViewport="true">

    <LinearLayout
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:orientation="vertical"
android:gravity="center"
android:padding="32dp">

        <Button
android:id="@+id/btnThemeToggle"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_gravity="end"
android:text="🌙 Dark Mode"
android:backgroundTint="?attr/colorSurface"
android:textColor="?attr/colorOnSurface"
style="@style/Widget.Material3.Button.OutlinedButton"/>

        <TextView
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="SIT708 Quiz App"
android:textSize="28sp"
android:textStyle="bold"
android:textColor="?attr/colorOnBackground"
android:layout_marginTop="32dp"
android:layout_marginBottom="8dp"/>

        <TextView
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Welcome! Enter your name to begin."
android:textSize="16sp"
android:textColor="?attr/colorOnBackground"
android:layout_marginBottom="40dp"/>

        <com.google.android.material.textfield.TextInputLayout
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:hint="Enter your name"
style="@style/Widget.Material3.TextInputLayout.OutlinedBox">

            <com.google.android.material.textfield.TextInputEditText
android:id="@+id/etName"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:minHeight="48dp"
android:inputType="textPersonName"
android:contentDescription="Enter your name"
android:textColor="?attr/colorOnSurface"/>

        </com.google.android.material.textfield.TextInputLayout>

        <Button
android:id="@+id/btnStart"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginTop="32dp"
android:text="START"
android:textSize="18sp"
android:textStyle="bold"
style="@style/Widget.Material3.Button"/>

    </LinearLayout>
</ScrollView>



//activity_quiz.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="vertical"
android:background="?android:attr/colorBackground"
android:padding="20dp">

    <!-- Top Bar: Theme Toggle -->
    <LinearLayout
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:orientation="horizontal"
android:gravity="end"
android:layout_marginBottom="8dp">

        <Button
android:id="@+id/btnThemeToggle"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="🌙 Dark"
style="@style/Widget.Material3.Button.OutlinedButton"
android:backgroundTint="?attr/colorSurface"
android:textColor="?attr/colorOnSurface"/>
    </LinearLayout>

    <!-- Question Counter -->
    <TextView
android:id="@+id/tvQuestionCount"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Question 1 of 5"
android:textSize="14sp"
android:textColor="?attr/colorOnBackground"
android:layout_marginBottom="4dp"/>

    <!-- Progress Bar -->
    <ProgressBar
android:id="@+id/progressBar"
style="?android:attr/progressBarStyleHorizontal"
android:layout_width="match_parent"
android:layout_height="16dp"
android:max="5"
android:progress="0"
android:progressTint="@color/correct_green"
android:progressBackgroundTint="@color/progress_bg"
android:layout_marginBottom="4dp"/>

    <!-- Progress Text -->
    <TextView
android:id="@+id/tvProgress"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="0 / 5 completed"
android:textSize="13sp"
android:textColor="?attr/colorOnBackground"
android:layout_marginBottom="20dp"/>

    <!-- Question Text -->
    <TextView
android:id="@+id/tvQuestion"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:text="Question goes here"
android:textSize="18sp"
android:textStyle="bold"
android:textColor="?android:attr/textColorPrimary"
android:lineSpacingExtra="4dp"
android:layout_marginBottom="24dp"/>

    <!-- Option Buttons -->
    <Button
android:id="@+id/btnOption0"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginBottom="10dp"
android:text="Option A"
android:gravity="center"
style="@style/Widget.Material3.Button.OutlinedButton"/>

    <Button
android:id="@+id/btnOption1"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginBottom="10dp"
android:text="Option B"
android:gravity="center"
style="@style/Widget.Material3.Button.OutlinedButton"/>

    <Button
android:id="@+id/btnOption2"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginBottom="10dp"
android:text="Option C"
android:gravity="center"
style="@style/Widget.Material3.Button.OutlinedButton"/>

    <Button
android:id="@+id/btnOption3"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginBottom="20dp"
android:text="Option D"
android:gravity="center"
style="@style/Widget.Material3.Button.OutlinedButton"/>

    <!-- Submit / Next Buttons -->
    <Button
android:id="@+id/btnSubmit"
android:layout_width="match_parent"
android:layout_height="56dp"
android:text="Submit"
android:textSize="16sp"
style="@style/Widget.Material3.Button"/>

    <Button
android:id="@+id/btnNext"
android:layout_width="match_parent"
android:layout_height="56dp"
android:text="Next"
android:textSize="16sp"
android:visibility="gone"
style="@style/Widget.Material3.Button"/>

</LinearLayout>



//activity_reults.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="vertical"
android:background="?android:attr/colorBackground"
android:gravity="center"
android:padding="32dp">

    <Button
android:id="@+id/btnThemeToggle"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_gravity="end"
android:text="🌙 Dark Mode"
style="@style/Widget.Material3.Button.OutlinedButton"
android:layout_marginBottom="40dp"/>

    <TextView
android:id="@+id/tvCongrats"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Congratulations!"
android:textSize="24sp"
android:textStyle="bold"
android:textColor="?android:attr/textColorPrimary"
android:gravity="center"
android:layout_marginBottom="32dp"/>

    <com.google.android.material.card.MaterialCardView
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:layout_marginBottom="40dp"
app:cardCornerRadius="16dp"
app:cardElevation="4dp">

        <TextView
android:id="@+id/tvScore"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:padding="32dp"
android:text="YOUR SCORE:"
android:textSize="28sp"
android:textStyle="bold"
android:gravity="center"
android:textColor="@color/correct_green"/>

    </com.google.android.material.card.MaterialCardView>

    <Button
android:id="@+id/btnNewQuiz"
android:layout_width="match_parent"
android:layout_height="56dp"
android:layout_marginBottom="16dp"
android:text="Take New Quiz"
android:textSize="16sp"
style="@style/Widget.Material3.Button"/>

    <Button
android:id="@+id/btnFinish"
android:layout_width="match_parent"
android:layout_height="56dp"
android:text="Finish"
android:textSize="16sp"
style="@style/Widget.Material3.Button.OutlinedButton"/>

</LinearLayout>