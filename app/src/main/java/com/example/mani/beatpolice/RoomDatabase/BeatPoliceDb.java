package com.example.mani.beatpolice.RoomDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = { AreaTagTable.class }, version = 1,exportSchema = false)

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

}


