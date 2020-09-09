package com.example.diploma;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.events.ChangeFolderEvent;
import com.example.diploma.events.ChangePathEvent;
import com.example.diploma.interfaces.ChangeFileListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.diploma.DatasAction.SortByNameType;
import static com.example.diploma.DatasAction.formatSize;
import static com.example.diploma.FSActions.getExtension;
import static com.example.diploma.FSActions.showFile;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<File> files;
    protected File currentDirectory;
    private FragmentManager fm;
    private SelectionTracker<String> selectionTracker;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    public File getCopyDir() {
        return copyDir;
    }

    public boolean isCut() {
        return isCut;
    }

    public void remove(int position){
        files.remove(position);
    }

    public void remove(File f){
        files.remove(f);
    }

    public void setCopyDir(File copyDir) {
        this.copyDir = copyDir;
    }

    public void setRenameFile(File renameFile) {
        this.renameFile = renameFile;
    }

    public void setCut(boolean cut) {
        isCut = cut;
    }

    private File copyDir = null;

    public List<File> getCopyDirs() {
        return copyDirs;
    }

    public void setCopyDirs(List<File> select) {
        copyDirs = new ArrayList<>();
        this.copyDirs.addAll(select);
    }

    private List<File> copyDirs;
    private File renameFile = null;
    private boolean isCut = false;
    private Context context;
    private WeakReference<MainActivity> parentActivity;
    private ChangeFileListener listener;

    public FileAdapter(List<File> files, File curF, FragmentManager fm) {
        this.files = SortByNameType(files);
        this.currentDirectory = curF;
        this.fm = fm;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectionTracker(SelectionTracker<String> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    static class ItemIdKeyProvider extends ItemKeyProvider<String> {
        private final RecyclerView rv;

        public ItemIdKeyProvider(int scope, RecyclerView rv) {
            super(scope);
            this.rv = rv;
        }

        @Nullable
        @Override
        public String getKey(int position) {
            FileAdapter adapter = ((FileAdapter) rv.getAdapter());
            if (adapter != null) {
                return adapter.files.get(position).getPath();
            }
            throw new IllegalStateException("RecyclerView adapter is not set!");
        }

        @Override
        public int getPosition(@NonNull String key) {
            FileAdapter adapter = ((FileAdapter) rv.getAdapter());
            final RecyclerView.ViewHolder vh = rv.findViewHolderForItemId(adapter.files.indexOf(new File(key)));
            if (vh != null) {
                return vh.getLayoutPosition();
            }
            return RecyclerView.NO_POSITION;
        }
    }

    static class ItemLookup extends ItemDetailsLookup<String> {

        private final RecyclerView rv;

        ItemLookup(final RecyclerView rv) {
            this.rv = rv;
        }

        @Nullable
        @Override
        public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
            final View view = rv.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                return ((ViewHolder) rv.getChildViewHolder(view)).getItemDetails();
            }
            return null;
        }
    }

    public File getCurrentDirectory(){
        return currentDirectory;
    }

    public File getRenameFile(){
        return renameFile;
    }

    public void add(File f){
        files.add(f);
        files=SortByNameType(files);
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        clickListener = v -> updateList(v, holder.getAdapterPosition());
        holder.itemView.setOnClickListener(clickListener);
        longClickListener = v-> true;
        holder.itemView.setOnLongClickListener(longClickListener);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.itemView.setOnClickListener(null);
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(files.get(position), selectionTracker.isSelected(files.get(position).getPath()));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        String path;

        @BindView(R.id.ivIcon)
        AppCompatImageView ivIcon;

        @BindView(R.id.tvDescription)
        AppCompatTextView tvDescription;

        @BindView(R.id.tvDate)
        TextView tvDate;

        @BindView(R.id.tvSize)
        TextView tvSize;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        ItemDetailsLookup.ItemDetails<String> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<String>() {
                @Override
                public int getPosition() {
                    return getAdapterPosition();
                }

                @Nullable
                @Override
                public String getSelectionKey() {
                    return path;
                }
            };
        }

        public void bind(File file, final boolean isActivated) {
            path = file.getPath();
            itemView.setActivated(isActivated);
            tvDescription.setText(file.getName());
            if (file.getName().equals("..")) {
                tvSize.setText("");
                tvDate.setText("");
                ivIcon.setImageResource(R.drawable.ic_arrow_left);
            } else {
                Long lastModified = file.lastModified();
                String date = getFullTime(lastModified);
                tvDate.setText(date);
                if (file.isDirectory()) {
                    int num = 0;
                    if (file.listFiles() != null) {
                        num = file.listFiles().length;
                    }
                    tvSize.setText(String.valueOf(num));
                    ivIcon.setImageResource(R.drawable.ic_folder);
                } else {
                    tvSize.setText(formatSize(file.length()));
                    ivIcon.setImageResource(R.drawable.ic_list_alt);
                }
            }
        }
    }

    private void updateList(@NonNull View v, int position) {
        File f = files.get(position);
        if (f.getName().equals("..")) {
            currentDirectory = currentDirectory.getParentFile();
            filesListChange();

        } else if (f.isDirectory()) {
            currentDirectory = f;
            filesListChange();

        } else {
            if (f.getName().contains("zip")) {
                ArchiveViewFragment fragment = ArchiveViewFragment.newInstance("zip", f.getAbsolutePath());
                fm.beginTransaction()
                        .replace(R.id.contentMain, fragment, "zip")
                        .addToBackStack("zip")
                        .commit();
                EventBus.getDefault().post(new ChangeFolderEvent(f.getName()));
                return;
            } else if (f.getName().contains("7z") || f.getName().contains("tar.gz")) {
                ArchiveViewFragment fragment = ArchiveViewFragment.newInstance("7z", f.getAbsolutePath());
                fm.beginTransaction()
                        .replace(R.id.contentMain, fragment, "7z")
                        .addToBackStack("7z")
                        .commit();
                EventBus.getDefault().post(new ChangeFolderEvent(f.getName()));
                return;
            }
            showFile(f, getExtension(f), v.getContext());
        }
    }

    public void callbackUpdate() {
        if (!currentDirectory.getName().equals("")) {
            currentDirectory = currentDirectory.getParentFile();
            filesListChange();
        }
    }

    public void filesListChange() {
        List<File> filesTmp = new ArrayList<>();
        EventBus.getDefault().post(new ChangePathEvent(currentDirectory.getPath()));
        if (currentDirectory.listFiles() != null) {
            filesTmp.addAll(Arrays.asList(currentDirectory.listFiles()));
        }
        files = SortByNameType(filesTmp);
        if (!currentDirectory.getName().equals("sdcard") && currentDirectory.getParentFile() != null) {
            files.add(0, new File(".."));
        }
        notifyDataSetChanged();
    }

    public void addFile(String tag) {
        AddFolderDialog dialog = new AddFolderDialog();
        dialog.show(fm, tag);
    }

    private static String getFullTime(final long timeInMillis) {
        final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);
        c.setTimeZone(TimeZone.getDefault());
        return format.format(c.getTime());
    }

    public void pasteDirectory() throws IOException {
        CompositeDisposable disposable = new CompositeDisposable();
        for (File copyDir:copyDirs) {
            File sourceLocation;
            File targetLocation;
            if (copyDir != null) {
                sourceLocation = copyDir;
                targetLocation = new File(currentDirectory.getAbsolutePath() + "/" + copyDir.getName());
            } else return;
            FSActions.pasteDirectory(sourceLocation, targetLocation, context);
            Toast.makeText(context, "Файл успешно вставлен", Toast.LENGTH_LONG).show();
            if (isCut) {
                FSActions.deleteRecursive(copyDir, context);
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
                    Integer res = FileManagerApp.getDbInstance().historyDao().updatePath(targetLocation.getAbsolutePath(), copyDir.getAbsolutePath());
                    if (res != null) return res;
                    else return -1;
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer);
            }
        }
        filesListChange();
        isCut = false;
    }
}
