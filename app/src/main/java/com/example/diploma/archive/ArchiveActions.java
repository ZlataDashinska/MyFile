package com.example.diploma.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ArchiveActions {

    public static ArchiveFile getArchiveFiles(List<ArchiveFile> entryList) {
        if (entryList.size() == 0) return null;
        List<ArchiveFile> res = new ArrayList<>();

        List<ArchiveFile> tmpFolder = new ArrayList<>();
        List<ArchiveFile> tmpFile = new ArrayList<>();
        for (int i = 0; i < entryList.size(); i++) {
            if (entryList.get(i).isDirectory())
                tmpFolder.add(entryList.get(i));
            else
                tmpFile.add(entryList.get(i));
        }

        for (ArchiveFile zeD : tmpFolder) {
            for (Iterator<ArchiveFile> i = tmpFile.iterator(); i.hasNext(); ) {
                ArchiveFile zeF = i.next();
                if (zeF.getCurName().contains(zeD.getCurName()) &&
                        (zeF.getCurName().split("/").length -
                                zeD.getCurName().split("/").length) < 2) {
                    zeF.setPrevFile(zeD);
                    zeD.add(zeF);
                    i.remove();
                }
            }
        }

        List<Integer> depth = new ArrayList<>();
//проверку на пустоту массива папок
        if (tmpFolder.size() != 0) {
            for (ArchiveFile zeD : tmpFolder) {
                depth.add(zeD.getCurName().split("/").length);
            }
            int cur = Collections.max(depth);
            for (int i = 0; i < cur - 1; i++) {
                int curDepth = Collections.max(depth);
                List<ArchiveFile> tmp1 = new ArrayList<>();
                List<ArchiveFile> tmp2 = new ArrayList<>();
                for (ArchiveFile zeD : tmpFolder) {
                    if (zeD.getCurName().split("/").length == curDepth)
                        tmp1.add(zeD);
                    else if (zeD.getCurName().split("/").length == curDepth - 1)
                        tmp2.add(zeD);
                }
                for (ArchiveFile zTmp1 : tmp2) {
                    for (ArchiveFile zTmp2 : tmp1) {
                        if (zTmp2.getCurName().contains(zTmp1.getCurName())) {
                            zTmp1.add(zTmp2);
                            zTmp2.setPrevFile(zTmp1);
                            tmpFolder.remove(zTmp2);
                        }
                    }
                }
                while (depth.contains(curDepth)) {
                    depth.remove(depth.indexOf(curDepth));
                }
            }
        }
        res.addAll(tmpFolder);
        res.addAll(tmpFile);
        ArchiveFile resFile = new ArchiveFile(null, 0, 0, null, res, false);
        for (ArchiveFile zf : resFile.getZipEntryList()) {
            zf.setPrevFile(resFile);
        }
        return resFile;
    }

}
