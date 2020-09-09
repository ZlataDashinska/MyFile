package com.example.diploma;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.archive.ArchiveActions;
import com.example.diploma.archive.ArchiveFile;
import com.example.diploma.archive.linux_arch.ApacheArchiveActions;
import com.example.diploma.archive.zip.ZipArchiveActions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ArchiveViewFragment extends Fragment{

    private static final String FILES_TYPE = "files_type";
    private static final String ARCH_NAME = "archive_name";

    private String filesType;
    private String archiveName;

    private List<ArchiveFile> files = new ArrayList<>();
    private Context context;
    private FragmentManager fm;
    private ArchiveViewAdapter adapter;

    @BindView(R.id.rvFiles)
    RecyclerView rvFiles;

    @BindView(R.id.button)
    Button bBack;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    protected CompositeDisposable disposable = new CompositeDisposable();

    private Unbinder unbinder;

    public ArchiveViewFragment() {
        // Required empty public constructor
    }

    public static ArchiveViewFragment newInstance(String filesType,String archiveName) {
        ArchiveViewFragment fragment = new ArchiveViewFragment();
        Bundle args = new Bundle();
        args.putString(FILES_TYPE, filesType);
        args.putString(ARCH_NAME, archiveName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filesType = getArguments().getString(FILES_TYPE);
            archiveName = getArguments().getString(ARCH_NAME);
        }
        context = getContext();
        fm = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_arch, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        DisposableSingleObserver<List<ArchiveFile>> observer = new DisposableSingleObserver<List<ArchiveFile>>() {
            @Override
            public void onSuccess(List<ArchiveFile> f) {
                progressBar.setVisibility(View.GONE);
                rvFiles.setVisibility(View.VISIBLE);
                bBack.setVisibility(View.VISIBLE);
                adapter = new ArchiveViewAdapter(f, fm);
                rvFiles.setLayoutManager(new LinearLayoutManager(context));
                rvFiles.setAdapter(adapter);
                bBack.setOnClickListener(v -> {
                    ArchiveFile tmp = adapter.getFirstFile().getPervFile().getPervFile();
                    if(adapter.getFirstFile().getPervFile().getCurName()!=null){
                        adapter.setCurList(tmp.getZipEntryList());
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        disposable.add(observer);
        Single.fromCallable(() -> {
            List<ArchiveFile> res=null;
            if (filesType.equals("zip")) {
                ArchiveFile tmp = ArchiveActions.getArchiveFiles(ZipArchiveActions.getZipArchive(new File(archiveName)));
                if(tmp!=null)
                    res= tmp.getZipEntryList();
            } else if(filesType.equals("7z")){
                ArchiveFile tmp = ArchiveActions.getArchiveFiles(ApacheArchiveActions.getApacheFiles(new File(archiveName)));
                if(tmp!=null)
                    res= tmp.getZipEntryList();
            }
            return res;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

       /* if (filesType.equals("zip")) {
            ArchiveFile tmp = ArchiveActions.getArchiveFiles(ZipArchiveActions.getZipArchive(new File(archiveName)));
            if(tmp!=null)
            files = tmp.getZipEntryList();
        } else if(filesType.equals("7z")){
            ArchiveFile tmp = ArchiveActions.getArchiveFiles(ApacheArchiveActions.getApacheFiles(new File(archiveName)));
            if(tmp!=null)
                files = tmp.getZipEntryList();
        }
        adapter = new ArchiveViewAdapter(files, fm);
        rvFiles.setLayoutManager(new LinearLayoutManager(context));
        rvFiles.setAdapter(adapter);
        bBack.setOnClickListener(v -> {
            ArchiveFile tmp = adapter.getFirstFile().getPervFile().getPervFile();
            if(adapter.getFirstFile().getPervFile().getCurName()!=null){
                adapter.setCurList(tmp.getZipEntryList());
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

}