package com.team404bnf.quizvuln.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.team404bnf.quizvuln.models.Result;

import java.util.List;

@Dao
public interface ResultDao {
    @Insert
    void insert(Result result);

    @Query("SELECT * FROM results ORDER BY timestamp DESC LIMIT 20")
    List<Result> getRecentResults();
}
