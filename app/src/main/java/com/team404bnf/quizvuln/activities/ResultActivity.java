package com.team404bnf.quizvuln.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.database.AppDatabase;
import com.team404bnf.quizvuln.models.Profile;
import com.team404bnf.quizvuln.models.QuizQuestion;
import com.team404bnf.quizvuln.models.QuizResult;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvAccuracy, tvPoints, tvHintsUsed, tvPointsBurst;
    private Button btnReview, btnDashboard;
    private LottieAnimationView lottieConfetti;

    private int finalPoints;
    private List<QuizQuestion> questions;
    private String category, difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvScore = findViewById(R.id.tvScore);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvPoints = findViewById(R.id.tvPoints);
        tvHintsUsed = findViewById(R.id.tvHintsUsed);
        tvPointsBurst = findViewById(R.id.tvPointsBurst);
        btnReview = findViewById(R.id.btnReview);
        btnDashboard = findViewById(R.id.btnDashboard);
        lottieConfetti = findViewById(R.id.lottieConfetti);

        // Read data
        int correct = getIntent().getIntExtra("correct_count", 0);
        int total = getIntent().getIntExtra("total_count", 0);
        int accuracy = getIntent().getIntExtra("accuracy", 0);
        finalPoints = getIntent().getIntExtra("points", 0);
        int hintsUsed = getIntent().getIntExtra("hints_used", 0);

        // FIXED: matching keys
        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");

        tvScore.setText("Score: " + correct + " / " + total);
        tvAccuracy.setText("Accuracy: " + accuracy + "%");
        tvHintsUsed.setText("Hints used: " + hintsUsed);
        tvPoints.setText("0");

        // Deserialize questions
        String json = getIntent().getStringExtra("questions_json");
        if (json != null) {
            Type listType = new TypeToken<List<QuizQuestion>>() {}.getType();
            questions = new Gson().fromJson(json, listType);
        }

        animatePointsAndConfetti(finalPoints);
        saveResultToDB(correct, total, finalPoints, accuracy, hintsUsed);

        btnReview.setOnClickListener(v -> {
            Intent i = new Intent(this, ResultsReviewActivity.class);
            i.putExtra("questions_json", new Gson().toJson(questions));
            startActivity(i);
        });

        btnDashboard.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void animatePointsAndConfetti(int value) {
        ValueAnimator animator = ValueAnimator.ofInt(0, Math.max(0, value));
        animator.setDuration(900);
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            tvPoints.setText(String.valueOf(val));
        });
        animator.start();

        startPointsBurst(value);

        if (lottieConfetti != null) {
            lottieConfetti.setVisibility(View.VISIBLE);
            lottieConfetti.playAnimation();
            lottieConfetti.postDelayed(() -> {
                try { lottieConfetti.cancelAnimation(); } catch (Exception ignored) {}
                lottieConfetti.setVisibility(View.GONE);
            }, 1800);
        }
    }

    private void startPointsBurst(int points) {
        if (tvPointsBurst == null) return;
        tvPointsBurst.setText((points >= 0 ? "+" : "") + points + " pts");
        tvPointsBurst.setAlpha(1f);
        tvPointsBurst.setScaleX(1f);
        tvPointsBurst.setScaleY(1f);
        tvPointsBurst.setTranslationY(0f);
        tvPointsBurst.setVisibility(View.VISIBLE);

        tvPointsBurst.animate()
                .translationYBy(-160f)
                .alpha(0f)
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(900)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> tvPointsBurst.setVisibility(View.GONE))
                .start();
    }

    private void saveResultToDB(int correct, int total, int earnedPoints, int accuracy, int hintsUsed) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                QuizResult result = new QuizResult();
                result.category = (category != null) ? category : "OWASP";
                result.difficulty = (difficulty != null) ? difficulty : "mixed";
                result.score = correct;
                result.total = total;
                result.points = earnedPoints;
                result.accuracy = accuracy;
                result.hintsUsed = hintsUsed;
                result.timestamp = System.currentTimeMillis();
                db.quizResultDao().insert(result);

                Profile p = db.profileDao().getProfile();
                if (p == null) {
                    p = new Profile();
                    p.name = "Guest";
                    p.totalPoints = Math.max(0, earnedPoints);
                    db.profileDao().insertProfile(p);
                } else {
                    p.totalPoints = Math.max(0, p.totalPoints + earnedPoints);
                    db.profileDao().updateProfile(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}