package com.example.diploma.events;

import com.example.diploma.ArchiveDialog;

import java.io.File;
import java.util.List;

public class ArchiveEvent {

    public final ArchiveDialog dialog;

    public File file;

    public List<File> files;

    public ArchiveEvent(ArchiveDialog dialog, File file) {
        this.dialog = dialog;
        this.file = file;
    }

    public ArchiveEvent(ArchiveDialog dialog, List<File> files) {
        this.dialog = dialog;
        this.files = files;
    }
}
