package com.example.mani.beatpolice.TodoAndIssue.IssueRelated;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;
@Dao
public interface IssueDao {

    @Query("SELECT * FROM IssueTable ORDER BY issueId DESC")
    List<IssueTable> getAllIssue();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IssueTable simpleTodo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<IssueTable> list);

    @Query("DELETE FROM IssueTable WHERE IssueTable.issueId = :issueId")
    void deleteById(int issueId);

}
