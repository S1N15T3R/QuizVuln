package com.team404bnf.quizvuln.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.models.QuizQuestion;

import java.util.List;

public class ResultsReviewAdapter extends RecyclerView.Adapter<ResultsReviewAdapter.VH> {
    private final List<QuizQuestion> questions;

    public ResultsReviewAdapter(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvQ, tvOptionA, tvOptionB, tvOptionC, tvOptionD, tvYour, tvCorrect;

        public VH(View v) {
            super(v);
            card = v.findViewById(R.id.cardContainer);
            tvQ = v.findViewById(R.id.tvQuestionReview);
            tvOptionA = v.findViewById(R.id.tvOptionA);
            tvOptionB = v.findViewById(R.id.tvOptionB);
            tvOptionC = v.findViewById(R.id.tvOptionC);
            tvOptionD = v.findViewById(R.id.tvOptionD);
            tvYour = v.findViewById(R.id.tvYourAnswer);
            tvCorrect = v.findViewById(R.id.tvCorrectAnswer);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        QuizQuestion q = questions.get(pos);
        h.tvQ.setText("Q" + (pos + 1) + ": " + q.question);

        // 🧠 FIX: prevent duplicate "A) A)" when API already includes prefixes
        if (q.options != null && q.options.size() >= 4) {
            h.tvOptionA.setText(formatOption("A", q.options.get(0)));
            h.tvOptionB.setText(formatOption("B", q.options.get(1)));
            h.tvOptionC.setText(formatOption("C", q.options.get(2)));
            h.tvOptionD.setText(formatOption("D", q.options.get(3)));
        } else {
            h.tvOptionA.setText("");
            h.tvOptionB.setText("");
            h.tvOptionC.setText("");
            h.tvOptionD.setText("");
        }

        String ua = (q.userAnswer == null) ? "-" : q.userAnswer;
        h.tvYour.setText("Your Answer: " + ua);
        h.tvCorrect.setText("Correct Answer: " + q.correct_answer);

        // reset background colors
        h.tvOptionA.setBackgroundColor(Color.TRANSPARENT);
        h.tvOptionB.setBackgroundColor(Color.TRANSPARENT);
        h.tvOptionC.setBackgroundColor(Color.TRANSPARENT);
        h.tvOptionD.setBackgroundColor(Color.TRANSPARENT);

        // highlight correct and incorrect answers
        String correct = (q.correct_answer == null) ? "" : q.correct_answer.toUpperCase();
        String user = (q.userAnswer == null) ? "" : q.userAnswer.toUpperCase();

        // highlight correct green
        if ("A".equals(correct)) h.tvOptionA.setBackgroundColor(Color.parseColor("#DFFFD6"));
        if ("B".equals(correct)) h.tvOptionB.setBackgroundColor(Color.parseColor("#DFFFD6"));
        if ("C".equals(correct)) h.tvOptionC.setBackgroundColor(Color.parseColor("#DFFFD6"));
        if ("D".equals(correct)) h.tvOptionD.setBackgroundColor(Color.parseColor("#DFFFD6"));

        // user answered wrong, highlight red
        if (!user.isEmpty() && !user.equals(correct)) {
            if ("A".equals(user)) h.tvOptionA.setBackgroundColor(Color.parseColor("#FFD6D6"));
            if ("B".equals(user)) h.tvOptionB.setBackgroundColor(Color.parseColor("#FFD6D6"));
            if ("C".equals(user)) h.tvOptionC.setBackgroundColor(Color.parseColor("#FFD6D6"));
            if ("D".equals(user)) h.tvOptionD.setBackgroundColor(Color.parseColor("#FFD6D6"));
        }
    }

    // helper: avoids adding duplicate A)/B)/C)/D)
    private String formatOption(String label, String optionText) {
        String clean = optionText.trim();
        if (clean.matches("^[A-D][).].*")) {
            // already starts with "A)" or "A." — don't prefix again
            return clean;
        } else {
            return label + ") " + clean;
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }
}
