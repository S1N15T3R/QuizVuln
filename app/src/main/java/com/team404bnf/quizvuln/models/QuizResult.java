package com.team404bnf.quizvuln.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "quiz_result")
public class QuizResult {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp")
    public long timestamp; // when quiz finished

    @ColumnInfo(name = "category")
    public String category; // e.g. "Injection - SQL"

    @ColumnInfo(name = "difficulty")
    public String difficulty; // easy | hard | mixed

    @ColumnInfo(name = "score")
    public int score; // number correct

    @ColumnInfo(name = "total")
    public int total; // number of questions

    @ColumnInfo(name = "points")
    public int points;

    @ColumnInfo(name = "hints_used")
    public int hintsUsed;

    @ColumnInfo(name = "accuracy")
    public int accuracy; // percentage

    @ColumnInfo(name = "questions_json")
    public String questionsJson; // save serialized question list for review
}
