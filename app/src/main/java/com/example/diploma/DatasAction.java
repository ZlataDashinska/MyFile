package com.example.diploma;

import com.example.diploma.archive.ArchiveFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class DatasAction {

    public static final Comparator<File> COMPARE_BY_DATE = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return Math.round(lhs.lastModified() - rhs.lastModified());
        }
    };

    public static final Comparator<File> COMPARE_BY_TYPE = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            int res = 0;
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                res = -1;
            } else if (rhs.isDirectory() && !lhs.isDirectory()) {
                res = 1;
            }
            return res;
        }
    };

    public static final Comparator<File> COMPARE_BY_NAME = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    };
    public static final Comparator<ArchiveFile> COMPARE_BY_NAME_ARCH = new Comparator<ArchiveFile>() {
        @Override
        public int compare(ArchiveFile lhs, ArchiveFile rhs) {
            return lhs.getCurName().compareTo(rhs.getCurName());
        }
    };

    public static String formatSize(long size) {
        String suffix = "B";
        if(size==-1) size=0;
        if (size >= 1024) {
            suffix = "kB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = "GB";
                    size /= 1024.0;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static void SortByArch(Comparator<ArchiveFile> comparator, List<ArchiveFile> files) {
        Collections.sort(files, comparator);
    }

    public static List<ArchiveFile> SortByNameTypeArch(List<ArchiveFile> files) {
        List<ArchiveFile> tmpDir = new ArrayList<>();
        List<ArchiveFile> tmpFile = new ArrayList<>();
        for (ArchiveFile f : files) {
            if (f.isDirectory()) {
                tmpDir.add(f);
            } else tmpFile.add(f);
        }
        SortByArch(COMPARE_BY_NAME_ARCH, tmpDir);
        SortByArch(COMPARE_BY_NAME_ARCH, tmpFile);
        tmpDir.addAll(tmpFile);
        return tmpDir;
    }

    public static final String getFullTime(final long timeInMillis) {
        final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);
        c.setTimeZone(TimeZone.getDefault());
        return format.format(c.getTime());
    }

    public static void SortBy(Comparator<File> comparator, List<File> files) {
        Collections.sort(files, comparator);
    }

    public static List<File> SortByNameType(List<File> files) {
        List<File> tmpDir = new ArrayList<>();
        List<File> tmpFile = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) {
                tmpDir.add(f);
            } else tmpFile.add(f);
        }
        SortBy(COMPARE_BY_NAME, tmpDir);
        SortBy(COMPARE_BY_NAME, tmpFile);
        tmpDir.addAll(tmpFile);
        return tmpDir;
    }
}
