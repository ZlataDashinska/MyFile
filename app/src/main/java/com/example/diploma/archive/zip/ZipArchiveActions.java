package com.example.diploma.archive.zip;

import com.example.diploma.archive.ArchiveFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipArchiveActions {

    final static int BUFFER_SIZE = 2048;

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[]children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[]bytes = new byte[BUFFER_SIZE];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public static void zipDirectory(List<File> inputDir, File outputZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputZipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File fileToZip: inputDir) {
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        }
        zipOut.close();
        fos.close();
    }

   /* public static void zipDirectory(List<File> inputDir, File outputZipFile) throws IOException {
        outputZipFile.getParentFile().mkdirs();
        FileOutputStream fileOs = new FileOutputStream(outputZipFile);
        ZipOutputStream zipOs = new ZipOutputStream(fileOs);

        for (File f : inputDir) {
            String inputDirPath = f.getAbsolutePath();
            byte[] buffer = new byte[BUFFER_SIZE];
            List<File> allFiles;
            if (f.isDirectory()) {
                allFiles = listChildFiles(f);
                //allFiles.add(0, f);
            } else {
                allFiles = new ArrayList<>();
                allFiles.add(f);
            }
            //if (inputDir.size()>1) allFiles.add(0, f);
            for (File file : allFiles) {
                String entryName;
                if (file.isDirectory()) {
                    entryName = file.getAbsolutePath();
                    if (!entryName.endsWith("/")) {
                        entryName = entryName + "/";
                    }
                } else {
                    entryName = file.getName();
                }
                ZipEntry ze = new ZipEntry(entryName);
                zipOs.putNextEntry(ze);
                if (!file.isDirectory()) {
                    FileInputStream fileIs = new FileInputStream(file.getAbsolutePath());

                    int len;
                    while ((len = fileIs.read(buffer)) > 0) {
                        zipOs.write(buffer, 0, len);
                    }
                    fileIs.close();
                }

                zipOs.closeEntry();

            }
        }
        closeQuite(zipOs);
        closeQuite(fileOs);
    }*/

    private static void closeQuite(OutputStream out) {
        try {
            out.close();
        } catch (Exception e) {
        }
    }

    private static List<File> listChildFiles(File dir) {
        List<File> allFiles = new ArrayList<>();

        File[] childFiles = dir.listFiles();
        for (File file : childFiles) {
            if (file.isFile()) {
                allFiles.add(file);
            } else {
                List<File> files = listChildFiles(file);
                allFiles.add(file);
                allFiles.addAll(files);
            }
        }
        return allFiles;
    }

    public static List<ArchiveFile> getZipArchive(File archive) {
        String FILE_PATH = archive.getAbsolutePath();
        List<ArchiveFile> res = new ArrayList<>();
        ZipInputStream zipIs = null;
        try {
            zipIs = new ZipInputStream(new FileInputStream(FILE_PATH));

            ZipEntry entry;
            while ((entry = zipIs.getNextEntry()) != null) {
                res.add(new ArchiveFile(entry.getName(), entry.getSize(), entry.getTime(), entry.isDirectory()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zipIs.close();
            } catch (Exception e) {
            }
        }
        return res;
    }

    public static void unZipDirectory(File zip, String folderName) throws Exception {
        String FILE_PATH = zip.getAbsolutePath();
        File folder = new File(zip.getParent(), folderName);
        while (folder.exists()) {
            String[] tmp = folder.getName().split("\\(|\\)");
            int suf = 0;
            try {
                suf = Integer.parseInt(tmp[tmp.length - 1]);
            } catch (Exception e) {
            }
            folder = new File(zip.getParent(), folderName + "(" + (suf + 1) + ")");
        }
        folder.mkdirs();
        final String OUTPUT_FOLDER = folder.getAbsolutePath();
        byte[] buffer = new byte[BUFFER_SIZE];

        ZipInputStream zipIs = new ZipInputStream(new FileInputStream(FILE_PATH));

        ZipEntry entry;
        while ((entry = zipIs.getNextEntry()) != null) {
            String entryName = entry.getName();
            String outFileName = OUTPUT_FOLDER + File.separator + entryName;
            System.out.println("Unzip: " + outFileName);

            if (entry.isDirectory()) {
                new File(outFileName).mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(outFileName);
                int len;
                while ((len = zipIs.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
        }
        try {
            zipIs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
