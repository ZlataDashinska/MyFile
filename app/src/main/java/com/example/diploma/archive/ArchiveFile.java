package com.example.diploma.archive;

import java.util.ArrayList;
import java.util.List;

public class ArchiveFile {

    private String  nameCurFile;
    private boolean isDirectory;
    private long  dateCurFile;
    private long  sizeCurFile;

    public void setPrevFile(ArchiveFile pervFile) {
        this.pervFile = pervFile;
    }

    private ArchiveFile pervFile;

    private List<ArchiveFile> zipEntryList;

    public boolean isDirectory() {
        return isDirectory;
    }

    public List<ArchiveFile> getZipEntryList() {
        return zipEntryList;
    }

    public ArchiveFile getPervFile() {
        return pervFile;
    }

    public ArchiveFile(String nameCurFile, long sizeCurFile,long dateCurFile, ArchiveFile pervFile, List<ArchiveFile> zafl, boolean isDir) {
        this.nameCurFile = nameCurFile;
        this.sizeCurFile = sizeCurFile;
        this.dateCurFile = dateCurFile;
        this.zipEntryList = zafl;
        this.pervFile = pervFile;
        isDirectory = isDir;
    }

    public ArchiveFile(String nameCurFile, long sizeCurFile ,long dateCurFile, boolean isDir) {
        this.nameCurFile = nameCurFile;
        this.sizeCurFile = sizeCurFile;
        this.dateCurFile = dateCurFile;
        this.zipEntryList = new ArrayList<>();
        this.pervFile = null;
        isDirectory = isDir;
    }

    public void add(ArchiveFile ze) {
        zipEntryList.add(ze);
    }

    public String getCurName(){
        return nameCurFile;
    }

    public long getCurTime(){
        return dateCurFile;
    }

    public long getCurSize(){
        return sizeCurFile;
    }
}
