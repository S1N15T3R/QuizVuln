package com.team404bnf.quizvuln.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.team404bnf.quizvuln.models.QuizResult;

import java.util.List;

@Dao
public interface QuizResultDao {

    @Insert
    long insert(QuizResult r);

    @Update
    void update(QuizResult r);

    @Delete
    void delete(QuizResult r);

    @Query("DELETE FROM quiz_result")
    void clearAll();

    @Query("SELECT * FROM quiz_result ORDER BY timestamp DESC LIMIT :limit")
    List<QuizResult> getRecent(int limit);

    @Query("SELECT * FROM quiz_result WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<QuizResult> getByDateRange(long from, long to);

    @Query("SELECT * FROM quiz_result WHERE category LIKE :category ORDER BY timestamp DESC")
    List<QuizResult> getByCategory(String category);

    @Query("SELECT AVG(accuracy) FROM quiz_result WHERE category LIKE :category")
    Double getAvgAccuracyByCategory(String category);

    // For search bar in Dashboard
    @Query("SELECT * FROM quiz_result WHERE category LIKE '%' || :q || '%' OR difficulty LIKE '%' || :q || '%' ORDER BY timestamp DESC LIMIT :limit")
    List<QuizResult> search(String q, int limit);

    @Query("SELECT * FROM quiz_result ORDER BY timestamp DESC")
    List<QuizResult> getAll();
}
