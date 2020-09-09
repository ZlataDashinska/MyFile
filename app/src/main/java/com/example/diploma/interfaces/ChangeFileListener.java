package com.example.diploma.interfaces;

import java.io.File;

public interface ChangeFileListener {

    public void OnClickDelete(int position);

    public void OnClickCopy(File file);

    public void OnClickCut(File file);

    public void OnClickRename(File file, int position);

    public void OnClickArchive(File file, int position);

    public void OnClickUnArchive(File file, int position);

    public void OnClickShare(File file);


}
