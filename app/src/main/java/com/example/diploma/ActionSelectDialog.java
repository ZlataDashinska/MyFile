package com.example.diploma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.diploma.interfaces.ChangeFileListener;

import java.io.File;

public class ActionSelectDialog extends DialogFragment{

    private  String[] data= new String[]{"Копировать","Удалить", "Вырезать", "Переименовать", "Архивировать", "Разархивировать", "Поделиться"};
    private Context context;
    private File choiceFile;
    private FragmentManager fm;
    private int position;

    private ChangeFileListener changeListener;

    public ActionSelectDialog(Context context, File choiceFile, FragmentManager fm, int position, ChangeFileListener deleteListener) {
        this.context = context;
        this.choiceFile = choiceFile;
        this.fm = fm;
        this.position = position;
        this.changeListener = deleteListener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setItems(data, myItemsListener);
        return builder.create();
    }

    DialogInterface.OnClickListener myItemsListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case 0: {
                    changeListener.OnClickCopy(ActionSelectDialog.this.choiceFile);
                    break;
                }
                case 1:{
                    FSActions.deleteRecursive(choiceFile, getContext());
                    changeListener.OnClickDelete(position);
                   // Toast.makeText(getContext(),"Файл успешно удален", Toast.LENGTH_LONG).show();
                    break;
                }
                case 2:{
                    changeListener.OnClickCut(ActionSelectDialog.this.choiceFile);
                    break;
                }
                case 3:{
                    changeListener.OnClickRename(ActionSelectDialog.this.choiceFile,position);
                    break;
                }
                case 4:{
                    changeListener.OnClickArchive(ActionSelectDialog.this.choiceFile,position);
                    break;
                }
                case 5:{
                    changeListener.OnClickUnArchive(ActionSelectDialog.this.choiceFile,position);
                    break;
                }
                case 6:{
                    changeListener.OnClickShare(ActionSelectDialog.this.choiceFile);
                }
            }
        }

    };

}
