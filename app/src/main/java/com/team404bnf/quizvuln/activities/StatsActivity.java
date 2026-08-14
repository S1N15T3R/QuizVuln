package com.team404bnf.quizvuln.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.database.AppDatabase;
import com.team404bnf.quizvuln.models.QuizResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class StatsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private PieChart pieChartAccuracy, pieChartVuln, pieChartDifficulty;
    private BarChart barChartTime;
    private TextView tvTotalQuizzes, tvAvgAccuracy, tvTotalPoints, tvWeeklyTrend;
    private LinearLayout chartContainer;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        lineChart = findViewById(R.id.lineChart);
        pieChartAccuracy = findViewById(R.id.pieChartAccuracy);
        pieChartVuln = findViewById(R.id.pieChartVuln);
        pieChartDifficulty = findViewById(R.id.pieChartDifficulty);
        barChartTime = findViewById(R.id.barChartTime);

        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvAvgAccuracy = findViewById(R.id.tvAvgAccuracy);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvWeeklyTrend = findViewById(R.id.tvWeeklyTrend);
        chartContainer = findViewById(R.id.chartContainer);

        loadStats();
    }

    private void loadStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<QuizResult> results = AppDatabase.getInstance(getApplicationContext())
                    .quizResultDao().getAll();

            if (results == null || results.isEmpty()) {
                runOnUiThread(() -> {
                    tvTotalQuizzes.setText("0");
                    tvAvgAccuracy.setText("0%");
                    tvTotalPoints.setText("0");
                    tvWeeklyTrend.setText("No data to compare yet 📊");
                    pieChartAccuracy.clear();
                    pieChartVuln.clear();
                    pieChartDifficulty.clear();
                    barChartTime.clear();
                    lineChart.clear();
                });
                return;
            }

            int totalPoints = 0;
            float totalAccuracy = 0f;
            int correctAnswers = 0;
            int wrongAnswers = 0;

            Map<String, Integer> vulnCount = new HashMap<>();
            Map<String, Integer> difficultyCount = new HashMap<>();
            Map<String, List<Long>> difficultyTimes = new HashMap<>();

            Collections.sort(results, Comparator.comparingLong(r -> r.timestamp));

            for (int i = 0; i < results.size(); i++) {
                QuizResult r = results.get(i);
                totalPoints += r.points;
                totalAccuracy += r.accuracy;

                float avgCorrect = (r.accuracy / 100f) * r.total;
                correctAnswers += Math.round(avgCorrect);
                wrongAnswers += r.total - Math.round(avgCorrect);

                if (r.category != null && !r.category.isEmpty()) {
                    vulnCount.put(r.category, vulnCount.getOrDefault(r.category, 0) + 1);
                }
                if (r.difficulty != null && !r.difficulty.isEmpty()) {
                    difficultyCount.put(r.difficulty, difficultyCount.getOrDefault(r.difficulty, 0) + 1);
                }

                // Estimate quiz time (gap between two attempts)
                if (i < results.size() - 1) {
                    long diffMillis = results.get(i + 1).timestamp - r.timestamp;
                    if (diffMillis > 0) {
                        String diffKey = (r.difficulty != null) ? r.difficulty : "unknown";
                        difficultyTimes
                                .computeIfAbsent(diffKey, k -> new ArrayList<>())
                                .add(diffMillis / 1000 / 60);
                    }
                }
            }

            int totalQuizzes = results.size();
            float avgAccuracy = totalAccuracy / totalQuizzes;

            final int finalTotalPoints = totalPoints;
            final int finalCorrectAnswers = correctAnswers;
            final int finalWrongAnswers = wrongAnswers;
            final Map<String, Integer> finalVulnCount = new HashMap<>(vulnCount);
            final Map<String, Integer> finalDifficultyCount = new HashMap<>(difficultyCount);
            final Map<String, List<Long>> finalDifficultyTimes = new HashMap<>(difficultyTimes);

            final List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                entries.add(new Entry(i, results.get(i).accuracy));
            }

            final String weeklyTrend = calculateWeeklyTrend(results);

            runOnUiThread(() -> {
                tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
                tvAvgAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", avgAccuracy));
                tvTotalPoints.setText(String.valueOf(finalTotalPoints));
                tvWeeklyTrend.setText(weeklyTrend);

                showLineChart(entries, results);
                showPieChartAccuracy(finalCorrectAnswers, finalWrongAnswers);
                showPieChartGeneric(pieChartVuln, finalVulnCount, "Vulnerability Type Distribution");
                showPieChartGeneric(pieChartDifficulty, finalDifficultyCount, "Difficulty Distribution");
                showBarChartTime(finalDifficultyTimes);
            });
        });
    }

    private void showPieChartAccuracy(int correct, int wrong) {
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(correct, "Correct"));
        pieEntries.add(new PieEntry(wrong, "Wrong"));

        PieDataSet dataSet = new PieDataSet(pieEntries, "Answer Distribution");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(11f);

        PieData pieData = new PieData(dataSet);
        pieChartAccuracy.setData(pieData);
        pieChartAccuracy.setUsePercentValues(true);
        pieChartAccuracy.setHoleColor(Color.TRANSPARENT);
        pieChartAccuracy.setEntryLabelColor(Color.BLACK);
        pieChartAccuracy.setEntryLabelTextSize(11f);
        pieChartAccuracy.setBackgroundColor(Color.parseColor("#F7F7F7"));
        pieChartAccuracy.getDescription().setEnabled(false);

        Legend legend = pieChartAccuracy.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        pieChartAccuracy.animateY(1000);
        pieChartAccuracy.invalidate();
    }

    private void showPieChartGeneric(PieChart chart, Map<String, Integer> map, String title) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        if (entries.isEmpty()) {
            chart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, title);
        dataSet.setColors(Color.parseColor("#2196F3"), Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"), Color.parseColor("#009688"), Color.parseColor("#E91E63"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.parseColor("#F9F9F9"));
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(11f);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        chart.animateY(1200);
        chart.invalidate();
    }

    private void showBarChartTime(Map<String, List<Long>> difficultyTimes) {
        if (difficultyTimes.isEmpty()) {
            barChartTime.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, List<Long>> entry : difficultyTimes.entrySet()) {
            List<Long> times = entry.getValue();
            float avg = 0;
            for (Long t : times) avg += t;
            avg /= times.size();
            entries.add(new BarEntry(i, avg));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Average Quiz Time (minutes)");
        List<Integer> colors = new ArrayList<>();
        for (String label : labels) {
            switch (label.toLowerCase()) {
                case "easy":
                    colors.add(Color.parseColor("#4CAF50"));
                    break;
                case "hard":
                    colors.add(Color.parseColor("#F44336"));
                    break;
                case "advance":
                case "advanced":
                    colors.add(Color.parseColor("#9C27B0"));
                    break;
                default:
                    colors.add(Color.parseColor("#2196F3"));
                    break;
            }
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartTime.setData(barData);
        barChartTime.getDescription().setEnabled(false);
        barChartTime.getAxisRight().setEnabled(false);
        barChartTime.getAxisLeft().setTextColor(Color.BLACK);
        barChartTime.setBackgroundColor(Color.parseColor("#F5F5F5"));
        barChartTime.animateY(1000);

        XAxis xAxis = barChartTime.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        Legend legend = barChartTime.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        barChartTime.invalidate();
    }

    private void showLineChart(List<Entry> entries, List<QuizResult> results) {
        LineDataSet dataSet = new LineDataSet(entries, "Accuracy Over Time");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.parseColor("#388E3C"));
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(Color.BLACK);
        lineChart.setBackgroundColor(Color.parseColor("#F9F9F9"));
        lineChart.animateY(1000);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < results.size()) {
                    long time = results.get(index).timestamp;
                    return sdf.format(new Date(time));
                }
                return "";
            }
        });

        lineChart.invalidate();
    }

    private String calculateWeeklyTrend(List<QuizResult> results) {
        long now = System.currentTimeMillis();
        long oneWeekMillis = 7L * 24 * 60 * 60 * 1000;

        float thisWeekAccuracy = 0f;
        float lastWeekAccuracy = 0f;
        int thisWeekCount = 0;
        int lastWeekCount = 0;

        for (QuizResult r : results) {
            long diff = now - r.timestamp;
            if (diff <= oneWeekMillis) {
                thisWeekAccuracy += r.accuracy;
                thisWeekCount++;
            } else if (diff <= 2 * oneWeekMillis) {
                lastWeekAccuracy += r.accuracy;
                lastWeekCount++;
            }
        }

        if (thisWeekCount == 0 && lastWeekCount == 0)
            return "No data yet for weekly comparison.";

        float thisAvg = thisWeekCount > 0 ? thisWeekAccuracy / thisWeekCount : 0;
        float lastAvg = lastWeekCount > 0 ? lastWeekAccuracy / lastWeekCount : 0;
        float diffPercent = thisAvg - lastAvg;

        if (lastWeekCount == 0)
            return String.format(Locale.getDefault(),
                    "Your average this week is %.1f%%. Great start! 🌟", thisAvg);

        if (diffPercent > 0)
            return String.format(Locale.getDefault(),
                    "You improved by +%.1f%% this week! 🚀 Keep it up!", diffPercent);
        else if (diffPercent < 0)
            return String.format(Locale.getDefault(),
                    "Your accuracy dropped by %.1f%% this week 😕 — review your mistakes!", Math.abs(diffPercent));
        else
            return "Your performance stayed the same this week ⚖️.";
    }
}
