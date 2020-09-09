package com.example.diploma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.diploma.events.CreateFileEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

public class AddFolderDialog extends DialogFragment {

    @BindView(R.id.tvNewFileName)
    EditText tvNewFileName;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (getTag().equals("add_dir"))
            builder.setView(inflater.inflate(R.layout.add_folder_dialog, null));
        else builder.setView(inflater.inflate(R.layout.add_file_dialog, null));
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EventBus.getDefault().post(new CreateFileEvent(AddFolderDialog.this, getTag()));
            }
        })
                .setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddFolderDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
