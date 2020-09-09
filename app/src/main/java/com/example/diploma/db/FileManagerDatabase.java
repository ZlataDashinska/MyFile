
package com.example.diploma.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.diploma.db.dao.HistoryDao;
import com.example.diploma.db.tables.FileHistory;

@Database(entities = {
        FileHistory.class
}, version = 1)
public abstract class FileManagerDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}
