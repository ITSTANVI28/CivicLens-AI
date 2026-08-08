package com.example.civiclensai.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CivicIssueDao {

    @Query("SELECT * FROM civic_issues ORDER BY timestamp DESC")
    LiveData<List<CivicIssueEntity>> getAllIssues();

    @Query("SELECT * FROM civic_issues WHERE isSynced = 0")
    List<CivicIssueEntity> getUnsyncedIssues();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssue(CivicIssueEntity issue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CivicIssueEntity> issues);

    @Update
    void updateIssue(CivicIssueEntity issue);

    @Query("DELETE FROM civic_issues WHERE id = :id")
    void deleteIssue(String id);
}
