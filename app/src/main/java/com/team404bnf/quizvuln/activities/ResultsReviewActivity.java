package com.team404bnf.quizvuln.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.adapters.ResultsReviewAdapter;
import com.team404bnf.quizvuln.models.QuizQuestion;

import java.lang.reflect.Type;
import java.util.List;

public class ResultsReviewActivity extends AppCompatActivity {
    private RecyclerView rvReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_review);

        // enable action bar back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Review Answers");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvReview = findViewById(R.id.rvReview);

        String json = getIntent().getStringExtra("questions_json");
        if (json == null) { finish(); return; }

        Type listType = new TypeToken<List<QuizQuestion>>(){}.getType();
        List<QuizQuestion> questions = new Gson().fromJson(json, listType);

        rvReview.setLayoutManager(new LinearLayoutManager(this));
        rvReview.setAdapter(new ResultsReviewAdapter(questions));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar back button
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
