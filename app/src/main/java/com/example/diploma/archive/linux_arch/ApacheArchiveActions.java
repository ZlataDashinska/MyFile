package com.example.diploma.archive.linux_arch;

import com.example.diploma.archive.ArchiveFile;

import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ApacheArchiveActions {

    final static int BUFFER_SIZE = 2048;

    public static void zipDirectory(List<File> inputDir, File outputFile, String nameArch, String type) throws IOException {
        String archiveName = nameArch;
        File destination = new File(outputFile.getAbsolutePath());
        Archiver archiver;
        if(type.equals("7z")){
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z);}
        else{
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        }

        File[] sources=new File[inputDir.size()-1];
        sources = inputDir.toArray(sources);
        archiver.create(archiveName, destination, sources);
    }

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
                allFiles.addAll(files);
            }
        }
        return allFiles;
    }

    public static List<ArchiveFile> getApacheFiles(File archive) {
        String FILE_PATH = archive.getAbsolutePath();
        List<ArchiveFile> res = new ArrayList<>();
        Archiver archiver;
        String[] tmp=archive.getName().split("\\.");
        if(tmp[tmp.length-1].equals("7z")){
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z);}
        else{
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        }
        ArchiveStream stream = null;
        try {
            stream = archiver.stream(archive);
            ArchiveEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                res.add(new ArchiveFile(entry.getName(), entry.getSize(),entry.getLastModifiedDate().getTime(), entry.isDirectory()));
                //String nameCurFile,int sizeCurFile, long dateCurFile, boolean isDir
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
        return res;
    }

    public static void unZipDirectory(File arch, String folderName) throws Exception {
        File destination = new File(arch.getParent(), folderName);
        while(destination.exists()) {
            String[] tmp = destination.getName().split("\\(|\\)");
            int suf=0;
            try{
                suf=Integer.parseInt(tmp[tmp.length-1]);
            }catch (Exception e){
            }
            destination = new File(arch.getParent(),folderName+"("+(suf+1)+")");
        }
        Archiver archiver;
        String[] tmp=arch.getName().split("\\.");
        if(tmp[tmp.length-1].equals("7z")){
        archiver = ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z);}
        else{
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        }
        archiver.extract(arch, destination);
    }
}
