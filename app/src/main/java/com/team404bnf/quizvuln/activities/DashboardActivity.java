package com.team404bnf.quizvuln.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.adapters.RecentResultsAdapter;
import com.team404bnf.quizvuln.database.AppDatabase;
import com.team404bnf.quizvuln.models.Profile;
import com.team404bnf.quizvuln.models.QuizQuestion;
import com.team404bnf.quizvuln.models.QuizResult;
import com.team404bnf.quizvuln.network.OpenRouterApiClient;
import com.team404bnf.quizvuln.network.OpenRouterService;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "QuizVuln";
    private static final int REQ_EDIT_PROFILE = 1001;

    private ImageView ivProfilePic;
    private TextView tvUserName, tvPoints;
    private Button btnPlayNow, btnStats;
    private EditText etSearch;
    private RecyclerView rvRecentActivity;

    private View loadingOverlay;
    private LottieAnimationView lottieLoading;

    private RecentResultsAdapter resultsAdapter;
    private List<QuizResult> allResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        tvPoints = findViewById(R.id.tvPoints);
        btnPlayNow = findViewById(R.id.btnPlayNow);
        btnStats = findViewById(R.id.btnStats);
        etSearch = findViewById(R.id.etSearch);
        rvRecentActivity = findViewById(R.id.rvRecentActivity);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        lottieLoading = findViewById(R.id.lottieLoading);

        // Profile edit click
        ivProfilePic.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, ProfileEditActivity.class);
            startActivity(i);
        });

        // Stats button
        btnStats.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, StatsActivity.class));
        });

        // Setup recycler
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        resultsAdapter = new RecentResultsAdapter(this);
        rvRecentActivity.setAdapter(resultsAdapter);

        // Load data
        loadProfileData();
        loadRecentResults();

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPlayNow.setOnClickListener(v -> showQuizOptionsDialog());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT_PROFILE && resultCode == RESULT_OK) {
            loadProfileData();
        }
    }

    private void filterResults(String q) {
        if (q.isEmpty()) {
            resultsAdapter.setData(allResults);
            return;
        }
        String lower = q.toLowerCase(Locale.ROOT);
        List<QuizResult> filtered = new ArrayList<>();
        for (QuizResult r : allResults) {
            if ((r.category != null && r.category.toLowerCase(Locale.ROOT).contains(lower)) ||
                    (r.difficulty != null && r.difficulty.toLowerCase(Locale.ROOT).contains(lower))) {
                filtered.add(r);
            }
        }
        resultsAdapter.setData(filtered);
    }

    private void showQuizOptionsDialog() {
        final String[] numOptions = {"5", "10", "20"};
        final String[] owaspTop10 = {
                "Broken Access Control", "Cryptographic Failures", "Injection", "Insecure Design",
                "Security Misconfiguration", "Vulnerable and Outdated Components",
                "Identification and Authentication Failures", "Software and Data Integrity Failures",
                "Security Logging and Monitoring Failures", "Server-Side Request Forgery (SSRF)"
        };
        final String[] difficultyOptions = {"easy", "hard", "advanced"};

        final int[] selNum = {0};
        final int[] selVuln = {0};
        final int[] selDiff = {0};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose number of questions")
                .setSingleChoiceItems(numOptions, 0, (dialog, which) -> selNum[0] = which)
                .setPositiveButton("Next", (dialog, which) -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Choose vulnerability")
                            .setSingleChoiceItems(owaspTop10, 0, (d, w) -> selVuln[0] = w)
                            .setPositiveButton("Next", (d, w) -> {
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Choose difficulty")
                                        .setSingleChoiceItems(difficultyOptions, 0, (di, ww) -> selDiff[0] = ww)
                                        .setPositiveButton("Start", (di, ww) -> {
                                            int numQ = Integer.parseInt(numOptions[selNum[0]]);
                                            String vuln = owaspTop10[selVuln[0]];
                                            String diff = difficultyOptions[selDiff[0]];
                                            fetchQuiz(numQ, vuln, diff);
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchQuiz(int numQ, String vuln, String difficulty) {
        btnPlayNow.setEnabled(false);
        showLoadingOverlay(true);

        OpenRouterService service = OpenRouterApiClient.getClient().create(OpenRouterService.class);
        Map<String, Object> body = buildQuizBody(numQ, vuln, difficulty);

        Log.d(TAG, "Request body: " + new Gson().toJson(body));
        service.createChatCompletion(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                runOnUiThread(() -> {
                    showLoadingOverlay(false);
                    btnPlayNow.setEnabled(true);
                });

                if (!response.isSuccessful()) {
                    Log.e(TAG, "API error: " + response.code());
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "API error", Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    Object choicesObj = response.body().get("choices");
                    if (choicesObj instanceof List) {
                        List<?> choices = (List<?>) choicesObj;
                        Map<?, ?> first = (Map<?, ?>) choices.get(0);
                        Map<?, ?> message = (Map<?, ?>) first.get("message");
                        String contentStr = String.valueOf(message.get("content"));

                        Gson gson = new Gson();
                        JsonObject root = gson.fromJson(contentStr, JsonObject.class);
                        JsonArray arr = root.getAsJsonArray("questions");

                        Type listType = new TypeToken<List<QuizQuestion>>() {}.getType();
                        List<QuizQuestion> questions = gson.fromJson(arr, listType);

                        Intent intent = new Intent(DashboardActivity.this, QuizActivity.class);
                        intent.putExtra("questions_json", gson.toJson(questions));
                        intent.putExtra("selected_category", vuln);
                        intent.putExtra("selected_difficulty", difficulty);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Parse error", Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    showLoadingOverlay(false);
                    btnPlayNow.setEnabled(true);
                    Toast.makeText(DashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private Map<String, Object> buildQuizBody(int numQ, String vuln, String difficulty) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek/deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "You are QuizGenerator. Output ONLY valid JSON EXACTLY in this schema:\n" +
                        "{ \"questions\": [ { \"question\": \"...\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correct_answer\":\"A\",\"hint\":\"...\",\"difficulty\":\"easy\",\"category\":\"SQL Injection\" } ] }"));
        messages.add(Map.of("role", "user", "content",
                "Please generate exactly " + numQ + " multiple choice OWASP-style quiz questions about: " + vuln + ". " +
                        "Each question must have 4 options labelled A-D, include 'difficulty' and 'category' fields. Return JSON only."));

        body.put("messages", messages);
        body.put("max_tokens", 1200);
        body.put("temperature", 0.1);
        body.put("response_format", Map.of("type", "json_object"));
        return body;
    }

    private void showLoadingOverlay(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                loadingOverlay.setAlpha(0f);
                loadingOverlay.setVisibility(View.VISIBLE);
                loadingOverlay.animate().alpha(1f).setDuration(300).setListener(null);
                lottieLoading.playAnimation();
            } else {
                loadingOverlay.animate().alpha(0f).setDuration(300)
                        .withEndAction(() -> loadingOverlay.setVisibility(View.GONE));
                lottieLoading.cancelAnimation();
            }
        });
    }

    private void loadProfileData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Profile profile = AppDatabase.getInstance(getApplicationContext()).profileDao().getProfile();
            runOnUiThread(() -> {
                if (profile != null) {
                    tvUserName.setText(profile.name);
                    tvPoints.setText(String.valueOf(profile.totalPoints));
                    if (profile.imagePath != null && !profile.imagePath.isEmpty())
                        Glide.with(this).load(profile.imagePath).into(ivProfilePic);
                    else
                        ivProfilePic.setImageResource(R.drawable.ic_person);
                } else {
                    tvUserName.setText("Guest");
                    tvPoints.setText("0");
                    ivProfilePic.setImageResource(R.drawable.ic_person);
                }
            });
        });
    }

    private void loadRecentResults() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<QuizResult> results = AppDatabase.getInstance(getApplicationContext())
                    .quizResultDao().getRecent(20);
            runOnUiThread(() -> {
                allResults = results != null ? results : new ArrayList<>();
                resultsAdapter.setData(allResults);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        loadRecentResults();
    }
}
