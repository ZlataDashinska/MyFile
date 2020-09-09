package com.example.diploma.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diploma.db.tables.FileHistory;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(FileHistory history);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract int update(FileHistory history, FileHistory newHistory);

    @Query("SELECT * FROM file_history")
    public abstract Single<List<FileHistory>> getFiles();

    @Query("UPDATE file_history SET path = :newPath WHERE path=:oldPath")
    public abstract int updatePath(String newPath, String oldPath);

    @Query("DELETE from file_history WHERE path=:delPath")
    public abstract int delete(String delPath);

}
