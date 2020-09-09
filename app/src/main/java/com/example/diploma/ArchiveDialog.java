package com.example.diploma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.fragment.app.DialogFragment;

import com.example.diploma.events.ArchiveEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArchiveDialog extends DialogFragment {

    @BindView(R.id.etName)
    EditText tvNewFileName;

    @BindView(R.id.rb7z)
    RadioButton rbSevenZ;
    @BindView(R.id.rbTarGz)
    RadioButton rbTarGz;
    @BindView(R.id.rbZip)
    RadioButton rbZip;
    /*@BindView(R.id.progressBar)
    ProgressBar progressBar;*/

    private Unbinder unbinder;
    File file;
    private List<File> files = new ArrayList<>();

    public ArchiveDialog(File file){
        this.file=file;
    }

    public ArchiveDialog(List<File> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v=inflater.inflate(R.layout.archive_dialog, null);
        builder.setView(v);
        unbinder = ButterKnife.bind(this, v);
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EventBus.getDefault().post(new ArchiveEvent(ArchiveDialog.this, files));
            }
        }).setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        unbinder.unbind();
                        unbinder = null;
                        ArchiveDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
