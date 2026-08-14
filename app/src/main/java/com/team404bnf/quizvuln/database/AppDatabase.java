package com.team404bnf.quizvuln.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.team404bnf.quizvuln.dao.ProfileDao;
import com.team404bnf.quizvuln.dao.QuizResultDao;
import com.team404bnf.quizvuln.models.Profile;
import com.team404bnf.quizvuln.models.QuizResult;

@Database(entities = {Profile.class, QuizResult.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ProfileDao profileDao();
    public abstract QuizResultDao quizResultDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quizvuln.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
