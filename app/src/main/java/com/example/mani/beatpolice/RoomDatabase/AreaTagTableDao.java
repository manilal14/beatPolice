package com.example.mani.beatpolice.RoomDatabase;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AreaTagTableDao {

    @Query("SELECT * FROM AreaTagTable")
    List<AreaTagTable> getAllAreaTags();

    @Query("SELECT * FROM AreaTagTable WHERE AreaTagTable.id = :id")
    AreaTagTable getTagById(int id);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AreaTagTable tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<AreaTagTable> list);

    @Query("DELETE FROM AreaTagTable WHERE AreaTagTable.id = :id")
    void deleteById(int id);


    @Query("DELETE FROM AreaTagTable")
    void deleteAll();
}
