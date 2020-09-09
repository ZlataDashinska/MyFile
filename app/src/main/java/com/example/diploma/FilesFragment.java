package com.example.diploma;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.archive.linux_arch.ApacheArchiveActions;
import com.example.diploma.archive.zip.ZipArchiveActions;
import com.example.diploma.events.ArchiveEvent;
import com.example.diploma.events.ChangePathEvent;
import com.example.diploma.events.CreateFileEvent;
import com.example.diploma.events.RenameEvent;
import com.example.diploma.interfaces.MenuItemClickListener;
import com.example.diploma.interfaces.PreviousFolderListener;
import com.example.diploma.interfaces.SearchClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.diploma.FSActions.deleteRecursive;
import static com.example.diploma.FSActions.getExtension;

public class FilesFragment extends Fragment implements PreviousFolderListener, MenuItemClickListener, SearchClickListener {

    private static final String FILES_TYPE = "files_type";
    private static final String FILE_PATH = "file_path";

    private String filesType;
    private String filePath;
    protected CompositeDisposable disposable = new CompositeDisposable();

    private List<File> files = new ArrayList<>();
    private WeakReference<MainActivity> parentActivity;
    private Context context;
    private FragmentManager fm;
    private FileAdapter adapter;
    private List<File> selectedFiles = new ArrayList<>();


    @BindView(R.id.rvFiles)
    RecyclerView rvFiles;

    @BindView(R.id.tvFilePath)
    TextView tvFilePath;

    private Unbinder unbinder;

    private SelectionTracker<String> tracker;

    public FilesFragment() {
        // Required empty public constructor
    }

    public static FilesFragment newInstance(String filesType) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        fragment.setArguments(args);
        return fragment;
    }

    public static FilesFragment newInstance(String filesType, String filePath) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        args.putString(FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filesType = getArguments().getString(FILES_TYPE);
            filePath = getArguments().getString(FILE_PATH);
        }
        context = getContext();
        fm = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        unbinder = ButterKnife.bind(this, v);
        parentActivity = new WeakReference<>((MainActivity) getActivity());
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        File f = new File("/sdcard");
        if (filePath != null) {
            f = new File(filePath);
        } else if (filesType.equals("memory")) {
            f = new File("/sdcard");
            files = Arrays.asList(f.listFiles());
        } else if (filesType.equals("sdCard")) {
            f = FSActions.getExternalStoragePath(this.context, true);
        }
        if (f != null) {
            files = Arrays.asList(f.listFiles());
            tvFilePath.setText(f.getPath());
        }

        adapter = new FileAdapter(files, f, fm);

        rvFiles.setLayoutManager(new LinearLayoutManager(context));
        rvFiles.setAdapter(adapter);

        parentActivity.get().bottomAppBar.getMenu().clear();
        parentActivity.get().bottomAppBar.inflateMenu(R.menu.bottom_navigation_file);

        Toolbar toolbar = parentActivity.get().bottomAppBar;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.arch:
                        ArchiveDialog dialog = new ArchiveDialog(selectedFiles);
                        dialog.show(fm, "archive_dialog");
                        tracker.clearSelection();
                        return true;
                    case R.id.de_arch:
                        File file = selectedFiles.get(0);
                        try {
                            if (getExtension(file).equals("zip"))
                                ZipArchiveActions.unZipDirectory(file, file.getName().split("\\.")[0]);
                            else
                                ApacheArchiveActions.unZipDirectory(file, file.getName().split("\\.")[0]);
                            adapter.filesListChange();
                        } catch (Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                        }
                        tracker.clearSelection();
                        return true;
                    case R.id.delete:
                        Iterator<File> iterator = selectedFiles.iterator();
                        while (iterator.hasNext()) {
                            File i = iterator.next();
                            FSActions.deleteRecursive(i, context);
                            adapter.remove(i);
                        }
                        adapter.notifyDataSetChanged();
                        tracker.clearSelection();
                        return true;
                    case R.id.rename:
                        File fileR = selectedFiles.get(0);
                        adapter.setRenameFile(fileR);
                        String[] tmp = fileR.getName().split("\\.");
                        RenameDialog dialogR = new RenameDialog("." + tmp[tmp.length - 1], fileR);
                        dialogR.show(fm, "rename");
                        tracker.clearSelection();
                        return true;
                    case R.id.copy:
                        adapter.setCopyDirs(selectedFiles);
                        tracker.clearSelection();
                        return true;
                    case R.id.cut:
                        adapter.setCopyDirs(selectedFiles);
                        adapter.setCut(true);
                        tracker.clearSelection();
                        return true;
                    case R.id.share:
                        if (selectedFiles.get(0) != null) {
                            Uri uri = FileProvider.getUriForFile(getActivity(),
                                    getActivity().getApplicationContext().getPackageName() + ".provider", selectedFiles.get(0));
                            ShareCompat.IntentBuilder.from(getActivity())
                                    .setStream(uri)
                                    .setType(URLConnection.guessContentTypeFromName(selectedFiles.get(0).getName()))
                                    .startChooser();
                        }
                        tracker.clearSelection();
                        return true;
                    default:
                        return false;
                }
            }
        });

        tracker = new SelectionTracker.Builder<>("file_selection", rvFiles,
                new FileAdapter.ItemIdKeyProvider(ItemKeyProvider.SCOPE_MAPPED, rvFiles),
                new FileAdapter.ItemLookup(rvFiles), StorageStrategy.createStringStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();
        adapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            final String TAG = "rvFiles SelectObserver";

            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                // key - file path
                File f = new File(key);
                if (selected) {
                    if (parentActivity.get().bottomAppBar.getVisibility() == View.GONE) {
                        parentActivity.get().bNMIV.setVisibility(View.GONE);
                        parentActivity.get().bottomAppBar.setVisibility(View.VISIBLE);
                    }
                    if (!selectedFiles.contains(f)) {
                        selectedFiles.add(f);
                    }
                } else {
                    selectedFiles.remove(f);
                }
            }

            @Override
            public void onSelectionCleared() {
                parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();

                boolean isDir = false;
                boolean isArch = false;
                final int selectionSize = tracker.getSelection().size();
                if (selectionSize == 0) {
                    parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                    parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
                    return;
                }
                for (int i = 0; i < selectedFiles.size(); i++) {
                    isDir = selectedFiles.get(i).isDirectory();
                    isArch = (selectedFiles.get(i).getName().contains(".zip") ||
                            selectedFiles.get(i).getName().contains(".7z") ||
                            selectedFiles.get(i).getName().contains(".tar.gz"));
                }
                Menu menu = parentActivity.get().bottomAppBar.getMenu();
                final boolean single = selectionSize == 1;
                final boolean archive = isArch && single;
                final boolean dir = !isDir && single;
                menu.getItem(5).setVisible(archive);
                menu.getItem(6).setVisible(dir);
                menu.getItem(3).setVisible(single);
            }
        });
    }

    public String getCurrentPath() {
        Log.i("QWERTY", adapter.currentDirectory.getPath());
        return adapter.currentDirectory.getPath();
    }

    public void clearFileSelection() {
        tracker.clearSelection();
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Override
    public void onPreviousFolderClick() {
        adapter.callbackUpdate();
    }

    @Override
    public void onClickAddFile(String tag) {
        adapter.addFile(tag);
    }

    @Override
    public void onClickPaste() {
        try {
            adapter.pasteDirectory();
        } catch (Exception e) {
            Log.d("paste", e.toString());
        }
    }

    @Override
    public void onClickSearch(String searchFile) {
        SearchFileFragment fr = SearchFileFragment.newInstance(filesType, searchFile, getCurrentPath());
        fm.beginTransaction()
                .replace(R.id.contentMain, fr, "search_files")
                .addToBackStack("search_files")
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateManagerEvent(CreateFileEvent event) {
        File f = null;
        if (event.tag.equals("add_dir")) {
            f = FSActions.addDirectory(adapter.getCurrentDirectory(),
                    ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString(), getContext());
        } else try {
            f = FSActions.addFile(adapter.getCurrentDirectory(),
                    ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString(),
                    getContext());
        } catch (Exception e) {
            f = null;
            Log.e("addFile", e.toString());
        }
        if (f != null) {
            adapter.add(f);
            Toast.makeText(getActivity(), "Файл успешно создан", Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArchiveEvent(ArchiveEvent event) {
        //event.dialog.progressBar.setVisibility(View.VISIBLE);
        DisposableSingleObserver<File> observer = new DisposableSingleObserver<File>() {
            @Override
            public void onSuccess(File f) {
                adapter.filesListChange();
                Toast.makeText(getActivity(), "Архив успешно создан", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        disposable.add(observer);
        Single.fromCallable(() -> archFile(event, event.files)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private File archFile(ArchiveEvent event, List<File> tmp) {
        File res = null;
        try {
            if (event.dialog.rbZip.isChecked()) {
                res = new File(tmp.get(0).getParent() + "/" + event.dialog.tvNewFileName.getText().toString() + ".zip");
                ZipArchiveActions.zipDirectory(tmp, res);
            } else if (event.dialog.rbSevenZ.isChecked()) {
                res = new File(tmp.get(0).getParent());
                ApacheArchiveActions.zipDirectory(tmp, res,
                        event.dialog.tvNewFileName.getText().toString(), "7z");
            } else if (event.dialog.rbTarGz.isChecked()) {
                res = new File(tmp.get(0).getParent());
                ApacheArchiveActions.zipDirectory(tmp, res,
                        event.dialog.tvNewFileName.getText().toString(), "tar.gz");
            }
            return res;
        } catch (IOException e) {
            deleteRecursive(new File(event.file.getParent(), event.dialog.tvNewFileName.getText().toString()), getContext());
            return res;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRenameManagerEvent(RenameEvent event) {
        boolean renamed = FSActions.renameFile(adapter.getRenameFile(),
                ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString(), getContext());
        if (renamed) {
            adapter.filesListChange();
            Toast.makeText(getActivity(), "Файл успешно переименован", Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangeFolderEvent(ChangePathEvent event) {
        if(event.name!=null){
            tvFilePath.setText(event.name);
        }
    }
}