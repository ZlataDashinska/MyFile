package com.example.diploma;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.db.tables.FileHistory;
import com.example.diploma.interfaces.ChangeFileListener;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class HistoryFragment extends Fragment {

    private List<File> files = new ArrayList<>();
    private Context context;
    private FragmentManager fm;
    private HistoryAdapter adapter;

    @BindView(R.id.rvFiles)
    RecyclerView rvFiles;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    protected CompositeDisposable disposable = new CompositeDisposable();

    private Unbinder unbinder;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        context = getContext();
        fm = getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        DisposableSingleObserver<List<FileHistory>> observer = new DisposableSingleObserver<List<FileHistory>>() {
            @Override
            public void onSuccess(List<FileHistory> f) {
                progressBar.setVisibility(View.GONE);
                rvFiles.setVisibility(View.VISIBLE);
                adapter = new HistoryAdapter(f, null, fm, new ChangeFileListener() {
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
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        disposable.add(observer);
        FileManagerApp.getDbInstance().historyDao().getFiles().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }
}
