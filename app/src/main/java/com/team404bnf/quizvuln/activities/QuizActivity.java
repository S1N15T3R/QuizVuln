package com.team404bnf.quizvuln.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.models.QuizQuestion;

import java.lang.reflect.Type;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvHint, tvProgress;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private Button btnNext, btnHint;

    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int correctAnswers = 0;
    private int totalPoints = 0;
    private int hintsUsed = 0;

    private String selectedCategory, selectedDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvHint = findViewById(R.id.tvHint);
        tvProgress = findViewById(R.id.tvProgress);
        rgOptions = findViewById(R.id.rgOptions);
        rbA = findViewById(R.id.rbA);
        rbB = findViewById(R.id.rbB);
        rbC = findViewById(R.id.rbC);
        rbD = findViewById(R.id.rbD);
        btnNext = findViewById(R.id.btnNext);
        btnHint = findViewById(R.id.btnHint);

        selectedCategory = getIntent().getStringExtra("selected_category");
        selectedDifficulty = getIntent().getStringExtra("selected_difficulty");

        String json = getIntent().getStringExtra("questions_json");
        if (json == null) {
            Toast.makeText(this, "No quiz data received!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Type listType = new TypeToken<List<QuizQuestion>>(){}.getType();
        questions = new Gson().fromJson(json, listType);

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Failed to load questions", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showQuestion();

        btnHint.setOnClickListener(v -> {
            QuizQuestion q = questions.get(currentIndex);
            tvHint.setText(q.hint == null ? "No hint available." : q.hint);
            tvHint.setVisibility(TextView.VISIBLE);
            hintsUsed++;
            Toast.makeText(QuizActivity.this, "Hint used (-2 points)", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(v -> {
            if (!checkAnswer()) return;

            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                showQuestion();
            } else {
                finishQuiz();
            }
        });
    }

    private void showQuestion() {
        QuizQuestion q = questions.get(currentIndex);
        tvQuestion.setText("Q" + (currentIndex + 1) + ": " + q.question);
        rbA.setText("A) " + cleanOption(q.options.get(0)));
        rbB.setText("B) " + cleanOption(q.options.get(1)));
        rbC.setText("C) " + cleanOption(q.options.get(2)));
        rbD.setText("D) " + cleanOption(q.options.get(3)));
        tvHint.setVisibility(TextView.GONE);
        rgOptions.clearCheck();
        tvProgress.setText((currentIndex + 1) + " / " + questions.size());
    }

    private String cleanOption(String option) {
        return option.replaceAll("^[A-D][).]\\s*", "").trim();
    }

    private boolean checkAnswer() {
        int checkedId = rgOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return false;
        }

        String userAnswer = "";
        if (checkedId == R.id.rbA) userAnswer = "A";
        else if (checkedId == R.id.rbB) userAnswer = "B";
        else if (checkedId == R.id.rbC) userAnswer = "C";
        else if (checkedId == R.id.rbD) userAnswer = "D";

        QuizQuestion q = questions.get(currentIndex);
        q.userAnswer = userAnswer;

        if (userAnswer.equalsIgnoreCase(q.correct_answer)) {
            correctAnswers++;

            int diffPts = 3;
            String diff = selectedDifficulty != null ? selectedDifficulty.toLowerCase() : "easy";
            if (diff.contains("hard")) diffPts = 7;
            else if (diff.contains("advanced")) diffPts = 10;

            totalPoints += diffPts;
        }

        return true;
    }

    private void finishQuiz() {
        int finalPoints = totalPoints - (hintsUsed * 2);
        int total = questions.size();
        int accuracy = (total > 0) ? (correctAnswers * 100 / total) : 0;

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("questions_json", new Gson().toJson(questions));
        intent.putExtra("correct_count", correctAnswers);
        intent.putExtra("total_count", total);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("points", finalPoints);
        intent.putExtra("hints_used", hintsUsed);
        // ✅ fixed: now matches what ResultActivity expects
        intent.putExtra("category", selectedCategory);
        intent.putExtra("difficulty", selectedDifficulty);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
