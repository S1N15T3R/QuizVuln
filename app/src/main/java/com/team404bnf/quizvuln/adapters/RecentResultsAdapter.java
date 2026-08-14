package com.team404bnf.quizvuln.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.models.QuizResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentResultsAdapter extends RecyclerView.Adapter<RecentResultsAdapter.ResultViewHolder> {

    private final Context context;
    private List<QuizResult> results = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public RecentResultsAdapter(Context ctx) {
        this.context = ctx;
    }

    public void setData(List<QuizResult> newResults) {
        this.results = newResults != null ? newResults : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recent_result, parent, false);
        return new ResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        QuizResult r = results.get(position);

        // Title = Category (or fallback)
        holder.tvTitle.setText(r.category != null ? r.category : "OWASP (mixed)");

        // Score / Points
        holder.tvScore.setText("Score: " + r.score + "/" + r.total +
                "   Points: " + r.points);

        // Difficulty
        holder.tvDifficulty.setText("Difficulty: " + (r.difficulty != null ? r.difficulty : "mixed"));

        // Date
        holder.tvDate.setText(sdf.format(new Date(r.timestamp)));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvScore, tvDate, tvDifficulty;
        ResultViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvScore = v.findViewById(R.id.tvScore);
            tvDate = v.findViewById(R.id.tvDate);
            tvDifficulty = v.findViewById(R.id.tvDifficulty);
        }
    }
}
