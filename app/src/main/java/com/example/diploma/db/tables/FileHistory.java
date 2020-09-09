package com.example.diploma.db.tables;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "file_history", indices = {
        @Index(value = "path", unique = true)
})
public class FileHistory {

    @NonNull
    @PrimaryKey
    private String path;

    private String dateLastSelected;

    public FileHistory() {

    }

    public FileHistory(String path) {
        this.path = path;
        Date date=new Date();
        this.dateLastSelected=date.toString();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDateLastSelected() {
        return dateLastSelected;
    }

    public void setDateLastSelected(String dateLastSelected) {
        this.dateLastSelected = dateLastSelected;
    }
}
