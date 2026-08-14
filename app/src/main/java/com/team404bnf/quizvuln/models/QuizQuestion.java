package com.team404bnf.quizvuln.models;

import java.util.List;

public class QuizQuestion {
    public String question;
    public List<String> options;
    public String correct_answer;  // "A","B","C","D"
    public String hint;
    public String difficulty;      // easy / hard / advanced
    public String category; // e.g., SQL Injection
    public String userAnswer;
}
