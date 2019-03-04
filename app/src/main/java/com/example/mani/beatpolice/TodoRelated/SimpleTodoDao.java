package com.example.mani.beatpolice.TodoRelated;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SimpleTodoDao {

    @Query("SELECT * FROM SimpleTodoTable")
    List<SimpleTodoTable> getAllSimpleTodo();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SimpleTodoTable simpleTodo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<SimpleTodoTable> list);

    @Query("UPDATE SimpleTodoTable SET isChecked = :b WHERE SimpleTodoTable.id = :todoId")
    void setIsChecked(int todoId,boolean b);

    @Query("DELETE FROM SimpleTodoTable WHERE SimpleTodoTable.id = :todoId")
    void deleteById(int todoId);

}
