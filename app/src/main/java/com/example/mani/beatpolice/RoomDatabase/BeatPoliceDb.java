package com.example.mani.beatpolice.RoomDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueDao;
import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueTable;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.SimpleTodoDao;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.SimpleTodoTable;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.TodoTable;
import com.example.mani.beatpolice.TodoAndIssue.TodoRelated.TodoTableDao;

@Database(entities = { AreaTagTable.class, TodoTable.class, SimpleTodoTable.class,IssueTable.class}, version = 4,exportSchema = false)

public abstract class BeatPoliceDb extends RoomDatabase {

    private static final String DB_NAME = "BeatPoliceDb.db";
    private static volatile BeatPoliceDb instance;

    public static synchronized BeatPoliceDb getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static BeatPoliceDb create(final Context context) {
        return Room.databaseBuilder(
                context,
                BeatPoliceDb.class,
                DB_NAME).build();
    }


    public abstract AreaTagTableDao getAreaTagTableDao();
    public abstract TodoTableDao getTodoTableDao();
    public abstract SimpleTodoDao getSimpleTodoTableDao();
    public abstract IssueDao getIssueDao();


}


