package com.example.diploma.events;

import com.example.diploma.RenameDialog;

public class RenameEvent {

    public final RenameDialog dialog;

    public int position;

    public RenameEvent(RenameDialog dialog, int position) {
        this.dialog = dialog;
        this.position = position;
    }
}
