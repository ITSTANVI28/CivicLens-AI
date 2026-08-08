package com.example.civiclensai.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CivicIssueEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "civiclens_ai_db";
    private static volatile AppDatabase instance;

    public abstract CivicIssueDao civicIssueDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DB_NAME
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
