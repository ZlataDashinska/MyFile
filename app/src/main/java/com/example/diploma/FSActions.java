package com.example.diploma;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.diploma.db.tables.FileHistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class FSActions {

    public static void deleteRecursive(File choiceFile, Context context) {
        CompositeDisposable disposable = new CompositeDisposable();
        if (choiceFile.isDirectory()) {
            for (File child : choiceFile.listFiles())
                deleteRecursive(child, context);
            choiceFile.delete();
        } else {
            choiceFile.delete();
            deleteStore(choiceFile, context);
            DisposableSingleObserver<Integer> observer = new DisposableSingleObserver<Integer>() {
                @Override
                public void onSuccess(Integer f) {
                    Log.e("DELETE", f.toString());
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            };
            disposable.add(observer);
            Single.fromCallable(() -> {
                Integer res=FileManagerApp.getDbInstance().historyDao().delete(choiceFile.getAbsolutePath());
                if (res!=null) return res;
                else return -1;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public static void pasteDirectory(File sourceLocation, File targetLocation, Context context) throws IOException {
        if (sourceLocation.isDirectory()) {
            while(targetLocation.exists()) {
                String[] tmp = targetLocation.getName().split("\\(|\\)");
                int suf=0;
                try{
                    suf=Integer.parseInt(tmp[tmp.length-2]);
                    targetLocation = new File(targetLocation.getParent(),targetLocation.getName().replace("("+suf+")","("+(suf+1)+")"));
                }catch (Exception e){
                    targetLocation = new File(targetLocation.getParent(),targetLocation.getName()+"("+(suf+1)+")");
                }

            }
            targetLocation.mkdirs();
            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                pasteDirectory(
                        new File(sourceLocation, children[i]), new File(targetLocation, children[i]), context);
            }
        } else {
            while(targetLocation.exists()) {
                String[] tmp = targetLocation.getName().split("\\(|\\)");
                int suf=0;
                try{
                    suf=Integer.parseInt(tmp[tmp.length-2]);
                    targetLocation = new File(targetLocation.getParent(),targetLocation.getName().replace("("+suf+")","("+(suf+1)+")"));
                }catch (Exception e){
                    String[] tmp2=targetLocation.getName().split("\\.");
                    String newName="";
                    newName+=tmp2[0]+"("+(suf+1)+").";
                    for (int i=1; i<tmp2.length-1; i++) newName+=tmp2[i]+".";
                    newName+=tmp2[tmp2.length-1];
                    targetLocation = new File(targetLocation.getParent(),newName);
                }

            }
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            insertStore(targetLocation, context);
        }
    }

    public static File addDirectory(File currentDirectory, String folderName, Context context) {
        String newFolder = currentDirectory.getAbsolutePath() + "/" + folderName;
        File f = new File(newFolder);
        if (!f.exists()) {
            f.mkdir();
            insertStore(f, context);
            //Toast.makeText(context,"Папка успешно создана", Toast.LENGTH_LONG).show();
            return f;
        } else {
            return null;
        }
    }

    public static File addFile(File currentDirectory, String folderName, Context context) throws IOException {
        String newFolder = currentDirectory.getAbsolutePath() + "/" + folderName;
        File f = new File(newFolder);
        if (!f.exists()) {
            f.createNewFile();
            insertStore(f, context);
            //Toast.makeText(context,"Файл успешно создан", Toast.LENGTH_LONG).show();
        }
        return f;
    }

    public static boolean renameFile(File currentName, String newName, Context context) {
        CompositeDisposable disposable = new CompositeDisposable();

        File newFileName = new File(currentName.getParent(), newName);
        boolean renamed = currentName.renameTo(newFileName);

        if (renamed) {
            Log.d("renameFile", "File renamed...");
            DisposableSingleObserver<Integer> observer = new DisposableSingleObserver<Integer>() {
                @Override
                public void onSuccess(Integer f) {
                    Log.e("UPDATE", f.toString());
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            };
            disposable.add(observer);
            Single.fromCallable(() -> {
                Integer res=FileManagerApp.getDbInstance().historyDao().updatePath(newFileName.getAbsolutePath(), currentName.getAbsolutePath());
                if (res!=null) return res;
                else return -1;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
            deleteStore(currentName, context);
            insertStore(newFileName, context);
            return true;

        } else {
            Log.d("renameFile", "File not renamed...");
        }
        return false;
    }

    public static ArrayList<File> getMediaPaths(String type, Activity activity) {
        String DATA_COLUMNS;
        String DATA;
        String selection;
        String[] selArg;
        Uri allImagesuri;
        switch (type) {
            case "video": {
                DATA_COLUMNS = MediaStore.Video.VideoColumns.DATA;
                allImagesuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                DATA = MediaStore.Video.Media.DATA;
                selection = null;
                selArg = null;
                break;
            }
            case "music": {
                DATA_COLUMNS = MediaStore.Audio.AudioColumns.DATA;
                allImagesuri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                DATA = MediaStore.Audio.Media.DATA;
                selection = null;
                selArg = null;
                break;
            }
            default: {
                DATA_COLUMNS = MediaStore.Images.ImageColumns.DATA;
                allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                DATA = MediaStore.Images.Media.DATA;
                selection = null;
                selArg = null;
                break;
            }
        }

        ArrayList<File> media = new ArrayList<>();
        String[] projection = {DATA_COLUMNS};
        Cursor cursor = activity.getContentResolver().query(allImagesuri, projection, selection, selArg, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do {
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
                media.add(new File(datapath));
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }

    public static ArrayList<File> getDocPaths(Activity activity, boolean all) {
        String DATA_COLUMNS = MediaStore.Files.FileColumns.DATA;
        String DATA = MediaStore.Files.FileColumns.DATA;
        String selection;
        String[] selArg;
        Uri allImagesuri = MediaStore.Files.getContentUri("external");
        String[] fileTypes = null;
        ArrayList<File> media = new ArrayList<>();
        String[] projection = {DATA_COLUMNS};
        if (!all) {
            fileTypes = new String[]{"pdf", "docx", "doc", "rar", "txt", "pptx", "xlsx", "ppt", "xls"};
            selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?" + "";
        } else {
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
            Cursor cursor = activity.getContentResolver().query(allImagesuri, projection, selection, null, null);
            try {
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                do {
                    String datapath = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
                    media.add(new File(datapath));
                } while (cursor.moveToNext());
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return media;
        }

        for (int i = 0; i < fileTypes.length; i++) {
            selArg = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileTypes[i])};
            Cursor cursor = activity.getContentResolver().query(allImagesuri, projection, selection, selArg, null);
            try {
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                do {
                    String datapath = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
                    media.add(new File(datapath));
                } while (cursor.moveToNext());
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return media;
    }

    public static File getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return new File(path);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<File> searchFiles(String search, String currentFile, Activity activity) {
        List<File> res = new ArrayList<>();
        List<File> allFiles = getDocPaths(activity, true);
        for (File f : allFiles) {
            String s = currentFile.replace("/sdcard", "/storage/emulated/0");
            if ((f.getParent().contains(currentFile.replace("/sdcard", "/storage/emulated/0"))
                    || f.getParent().contains(currentFile))
                    && f.getName().toLowerCase().contains(search.toLowerCase())
            )
                res.add(f);//
        }
        return res;
    }

    public static List<File> searchFiles(String search, List<File> files) {
        List<File> res = new ArrayList<>();
        for (File f : files) {
            String n = f.getName();
            boolean b = n.toLowerCase().contains(search.toLowerCase());
            if (f.getName().contains(search)) res.add(f);
        }
        return res;
    }

    public static String getExtension(File f) {
        String[] res = f.getName().split("\\.");
        return res[res.length - 1];
    }

    public static void showFile(File file, String fileType, Context context) {
        CompositeDisposable disposable = new CompositeDisposable();
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType =
                myMime.getMimeTypeFromExtension(fileType);
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            Uri fileURI = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            intent.setDataAndType(fileURI, mimeType);

        } else {
            intent.setDataAndType(Uri.fromFile(file), mimeType);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
            FileHistory fileHistory=new FileHistory(file.getAbsolutePath());
            DisposableSingleObserver<Long> observer = new DisposableSingleObserver<Long>() {
                @Override
                public void onSuccess(Long f) {
                    Log.e("INSERT", f.toString());
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            };
            disposable.add(observer);
            Single.fromCallable(() -> {
                return FileManagerApp.getDbInstance().historyDao().insert(fileHistory);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getResources().getString(R.string.msg_no_app), Toast.LENGTH_LONG).show();
        }
    }

    private static boolean insertStore(File f, Context context) {
        ContentValues values = new ContentValues();
        if (f.isDirectory()) {
            values.put
                    (MediaStore.Files.FileColumns.DATA, f.getAbsolutePath().replace("/sdcard", "/storage/emulated/0"));
            values.put
                    (MediaStore.Files.FileColumns.MEDIA_TYPE,
                            MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);
        } else {
            values.put
                    (MediaStore.Files.FileColumns.DATA, f.getAbsolutePath().replace("/sdcard", "/storage/emulated/0"));
            values.put
                    (MediaStore.Files.FileColumns.MIME_TYPE,
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                    MimeTypeMap.getFileExtensionFromUrl(f.getAbsolutePath())));
        }
        Uri insertFile = context.getContentResolver().insert
                (MediaStore.Files.getContentUri(
                        MediaStore.VOLUME_EXTERNAL), values);
        return insertFile != null;
    }

    private static boolean deleteStore(File choiseFile, Context context) {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {MediaStore.Files.FileColumns._ID};

        String selection = MediaStore.Files.FileColumns.DATA + " = ?";
        String[] selectionArgs = new String[]{choiseFile.getAbsolutePath().replace("/sdcard", "/storage/emulated/0")};

        Uri queryUri = MediaStore.Files.getContentUri("external");
        Uri deleteUri;
        Cursor c = resolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
            deleteUri = ContentUris.withAppendedId(queryUri, id);
            int numRemoved = resolver.delete(
                    deleteUri,
                    selection,
                    selectionArgs);
            return numRemoved != 0;
        }
        return false;
    }

}
