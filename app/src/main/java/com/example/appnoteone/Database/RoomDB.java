package com.example.appnoteone.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.appnoteone.Models.Notes;

@Database(entities = Notes.class, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;
    private static final String DATABASE_NAME = "NoteApp";
    public static synchronized RoomDB getInstance(Context context){
        if(database==null){
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries().build();
        }
        return database;
    }

    public abstract MainDAO mainDAO();
}
