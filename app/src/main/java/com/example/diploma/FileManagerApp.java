package com.example.diploma;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.example.diploma.db.FileManagerDatabase;

public class FileManagerApp extends MultiDexApplication {

    private static final String DB_NAME = "fileManager.db";
    private static FileManagerDatabase database;
    private static FileManagerApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        database = Room.databaseBuilder(this, FileManagerDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration().build();
    }

    public static FileManagerDatabase getDbInstance() {
        return database;
    }

    public static FileManagerApp getAppInstance() {
        return app;
    }
}