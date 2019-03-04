package com.example.mani.beatpolice.TodoRelated;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TodoTableDao {

    @Query("SELECT * FROM TodoTable")
    List<TodoTable> getAllTodo();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TodoTable todoTable);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TodoTable> list);

    @Query("DELETE FROM TodoTable WHERE TodoTable.todoId = :todoId")
    void deleteById(int todoId);

}
