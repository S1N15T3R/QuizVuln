package com.team404bnf.quizvuln.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "results")
public class Result {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String category;     // e.g. "Injection"
    public String difficulty;   // easy/hard/advanced
    public int score;           // correct answers
    public int total;           // total questions
    public int accuracy;        // %
    public int points;          // with hint deductions
    public int hintsUsed;
    public long timestamp;      // for sorting by date
}
