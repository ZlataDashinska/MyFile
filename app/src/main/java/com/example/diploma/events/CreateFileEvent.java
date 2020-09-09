package com.example.diploma.events;

import com.example.diploma.AddFolderDialog;

public class CreateFileEvent {

    public final AddFolderDialog dialog;
    public final String tag;

    public CreateFileEvent(AddFolderDialog dialog, String tag) {
        this.dialog = dialog;
        this.tag=tag;
    }
}
