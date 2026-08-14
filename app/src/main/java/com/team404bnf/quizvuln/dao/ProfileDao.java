package com.team404bnf.quizvuln.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.team404bnf.quizvuln.models.Profile;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile LIMIT 1")
    Profile getProfile();

    @Insert
    long insertProfile(Profile profile);

    @Update
    void updateProfile(Profile profile);
}
