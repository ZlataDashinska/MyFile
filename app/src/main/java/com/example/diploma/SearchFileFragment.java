package com.example.diploma;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.interfaces.ChangeFileListener;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SearchFileFragment extends Fragment {
    private static final String FILES_TYPE = "files_type";
    private static final String SEARCH_FILES = "search_files";
    private static final String CURRENT_PATH = "current_path";

    private String filesType;
    private String searchFiles;
    private String currentFile;

    private List<File> files = new ArrayList<>();
    private Context context;
    private FragmentManager fm;
    private SearchAdapter adapter;

    @BindView(R.id.rvFiles)
    RecyclerView rvFiles;

    private Unbinder unbinder;

    public SearchFileFragment() {
        // Required empty public constructor
    }

    public static SearchFileFragment newInstance(String filesType, String searchFiles, String currentFile) {
        SearchFileFragment fragment = new SearchFileFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        args.putString(SEARCH_FILES, searchFiles);
        args.putString(CURRENT_PATH, currentFile);
        fragment.setArguments(args);
        return fragment;
    }

    public static SearchFileFragment newInstance(String filesType, String searchFiles) {
        SearchFileFragment fragment = new SearchFileFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        args.putString(SEARCH_FILES, searchFiles);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filesType = getArguments().getString(FILES_TYPE);
            searchFiles = getArguments().getString(SEARCH_FILES);
            currentFile = getArguments().getString(CURRENT_PATH);
        }
        context = getContext();
        fm = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        File f = new File("/sdcard");//Environment.getRootDirectory();
        if (filesType.equals("memory")||filesType.equals("sdCard")) {
            files = FSActions.searchFiles(searchFiles, currentFile, getActivity());
        } else if (filesType.equals("doc")) {
            files = FSActions.getDocPaths(getActivity(), false);
            files = FSActions.searchFiles(searchFiles,files);
        } else {
            files = FSActions.getMediaPaths(filesType, getActivity());
            files = FSActions.searchFiles(searchFiles,files);
        }
        adapter = new SearchAdapter(files, f, fm, new ChangeFileListener() {
            @Override
            public void OnClickDelete(int position) {
                files.remove(position);
                adapter.notifyDataSetChanged();
                //Toast.makeText(_context,"Файл успешно удален", Toast.LENGTH_LONG).show();
            }

            @Override
            public void OnClickCopy(File file) {

            }

            @Override
            public void OnClickCut(File file) {

            }

            @Override
            public void OnClickRename(File file, int position) {
                adapter.setRenameFile(file);
                String[] tmp = file.getName().split("\\.");
                RenameDialog dialog = new RenameDialog(position, "." + tmp[tmp.length - 1], file);
                dialog.show(fm, "rename");
            }

            @Override
            public void OnClickArchive(File file, int position) {

            }

            @Override
            public void OnClickUnArchive(File file, int position) {

            }

            @Override
            public void OnClickShare(File file) {
                if (file != null) {
                    Uri uri = FileProvider.getUriForFile(getActivity(),
                            getActivity().getApplicationContext().getPackageName() + ".provider", file);
                    ShareCompat.IntentBuilder.from(getActivity())
                            .setStream(uri)
                            .setType(URLConnection.guessContentTypeFromName(file.getName()))
                            .startChooser();
                }
            }
        });
        rvFiles.setLayoutManager(new LinearLayoutManager(context));
        rvFiles.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }
}
