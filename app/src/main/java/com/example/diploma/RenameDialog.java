package com.example.diploma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.diploma.events.RenameEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.Unbinder;

public class RenameDialog extends DialogFragment {

    int position;
    String file_type;
    File path;

    Unbinder unbinder;

    public RenameDialog(int position, String file_type, File path) {
        this.position = position;
        this.file_type = file_type;
        this.path = path;
    }

    public RenameDialog(String file_type, File path) {
        this.file_type = file_type;
        this.path = path;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v=getActivity().getLayoutInflater().inflate(R.layout.rename_file_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton("Переименовать", null)
                .setNegativeButton("Отменить", null)
                .create();

        final EditText tvNewFileName = (EditText) v.findViewById(R.id.tvNewFileName);
        final TextView tvError = (TextView) v.findViewById(R.id.tvError);

        tvNewFileName.setText(path.getName());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String name=path.getParent()+"/"+tvNewFileName.getText().toString();
                        if ((new File(name)).exists()||tvNewFileName.getText().toString()==null){
                            tvError.setText(R.string.nameError);
                        }
                        else {
                            EventBus.getDefault().post(new RenameEvent(RenameDialog.this, position));
                            dialog.dismiss();
                        }
                    }
                });

                button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                            dialog.dismiss();
                    }
                });
            }
        });
        return dialog;
    }

    /* @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        })
                .setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RenameDialog.this.getDialog().cancel();
                    }
                });

    final AlertDialog dialog = new AlertDialog.Builder(context)
            .setView(v)
            .setTitle(R.string.my_title)
            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
            .setNegativeButton(android.R.string.cancel, null)
            .create();

dialog.setOnShowListener(new DialogInterface.OnShowListener() {

        @Override
        public void onShow(DialogInterface dialogInterface) {

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // TODO Do something

                    //Dismiss once everything is OK.
                    dialog.dismiss();
                }
            });
        }
    });
dialog.show();*/

}
