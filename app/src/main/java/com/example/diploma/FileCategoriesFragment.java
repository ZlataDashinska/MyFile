package com.example.diploma;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import com.example.diploma.interfaces.SearchClickListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FileCategoriesFragment extends Fragment implements SearchClickListener {

    private static final String FILES_TYPE = "files_type";
    private SelectionTracker<String> tracker;
    private String filesType;
    private List<File> selectedFiles = new ArrayList<>();
    private WeakReference<MainActivity> parentActivity;
    private List<File> files = new ArrayList<>();
    private Context context;
    private FragmentManager fm;
    private FileCategoriesAdapter adapter;

    @BindView(R.id.rvFiles)
    RecyclerView rvFiles;

    private Unbinder unbinder;

    public FileCategoriesFragment() {
        // Required empty public constructor
    }

    public static FileCategoriesFragment newInstance(String filesType) {
        FileCategoriesFragment fragment = new FileCategoriesFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filesType = getArguments().getString(FILES_TYPE);
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
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        File f = new File("/sdcard");//Environment.getRootDirectory();
        if (filesType.equals("doc")) {
            files = FSActions.getDocPaths(getActivity(), false);
        } else {
            files = FSActions.getMediaPaths(filesType, getActivity());
        }
        adapter = new FileCategoriesAdapter(files, fm);
        rvFiles.setLayoutManager(new LinearLayoutManager(context));
        rvFiles.setAdapter(adapter);

        parentActivity.get().bottomAppBar.getMenu().clear();
        parentActivity.get().bottomAppBar.inflateMenu(R.menu.bottom_navigation_file_category);

        Toolbar toolbar = parentActivity.get().bottomAppBar;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        for (File i : selectedFiles) {
                            FSActions.deleteRecursive(i, context);
                            adapter.remove(i);
                        }
                        adapter.notifyDataSetChanged();
                        parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                        parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
                        tracker.clearSelection();
                        return true;
                    case R.id.rename:
                        File fileR=selectedFiles.get(0);
                        adapter.setRenameFile(fileR);
                        String[] tmp = fileR.getName().split("\\.");
                        RenameDialog dialogR = new RenameDialog("." + tmp[tmp.length - 1], fileR);
                        dialogR.show(fm, "rename");
                        parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                        parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
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
                        parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                        parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
                        tracker.clearSelection();
                        return true;
                    default:
                        return false;
                }
            }
        });

        tracker = new SelectionTracker.Builder<>("file_selection", rvFiles,
                new FileCategoriesAdapter.ItemIdKeyProvider(ItemKeyProvider.SCOPE_MAPPED, rvFiles),
                new FileCategoriesAdapter.ItemLookup(rvFiles), StorageStrategy.createStringStorage())
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
                    if(!selectedFiles.contains(f)){
                        selectedFiles.add(f);
                    }
                } else {
                    selectedFiles.remove(f);
                }
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();

                final int selectionSize = tracker.getSelection().size();
                if (selectionSize == 0) {
                    parentActivity.get().bottomAppBar.setVisibility(View.GONE);
                    parentActivity.get().bNMIV.setVisibility(View.VISIBLE);
                    return;
                }
                Menu menu = parentActivity.get().bottomAppBar.getMenu();
                final boolean single = selectionSize == 1;
                menu.getItem(1).setVisible(single);
                menu.getItem(2).setVisible(single);
            }
        });
    }

    public void clearFileSelection() {
        tracker.clearSelection();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Override
    public void onClickSearch(String searchFile){
        SearchFileFragment fr = SearchFileFragment.newInstance(filesType, searchFile);
        fm.beginTransaction()
                .replace(R.id.contentMain, fr, "search_files")
                .addToBackStack("search_files")
                .commit();
    }

}