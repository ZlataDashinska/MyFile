package com.example.diploma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.events.ChangeFolderEvent;
import com.example.diploma.events.RenameEvent;
import com.example.diploma.interfaces.ChangeFileListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.diploma.DatasAction.SortByNameType;
import static com.example.diploma.DatasAction.formatSize;
import static com.example.diploma.FSActions.getExtension;
import static com.example.diploma.FSActions.showFile;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<File> files;
    protected File currentDirectory;
    private FragmentManager fm;
    private File copyDir = null;

    public void setRenameFile(File renameFile) {
        this.renameFile = renameFile;
    }

    private File renameFile = null;
    private boolean isCut = false;
    private Context _context;
    private ChangeFileListener listener;


    public SearchAdapter(List<File> files, File curF, FragmentManager fm, ChangeFileListener listener) {
        this.files = SortByNameType(files);
        this.currentDirectory = curF;
        //EventBus.getDefault().post(new ChangeFolderEvent(currentDirectory.getAbsolutePath()));
        this.fm = fm;
        this.listener=listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        _context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(files.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(v, position);
                //EventBus.getDefault().post(new ChangeFolderEvent(currentDirectory.getAbsolutePath()));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ActionSelectDialog dialog = new ActionSelectDialog(v.getContext(), files.get(position), fm, position, listener);
                dialog.show(fm, "action_select");
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivIcon)
        AppCompatImageView ivIcon;

        @BindView(R.id.tvDescription)
        AppCompatTextView tvDescription;

        @BindView(R.id.tvDate)
        TextView tvDate;

        @BindView(R.id.tvSize)
        TextView tvSize;

        @BindView(R.id.tvPath)
        TextView tvPath;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(File file) {
            //ivIcon.setBackgroundDrawable();
            tvDescription.setText(file.getName());
            if (file.getName() == "..") {
                tvSize.setText("");
                tvDate.setText("");
                ivIcon.setImageResource(R.drawable.ic_arrow_left);
            } else {
                Long lastModified = file.lastModified();
                String date = getFullTime(lastModified);
                tvDate.setText(date);
                tvPath.setText(file.getParent().replace("/storage/emulated/0","/").concat("/"));
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
        if (f.isDirectory()) {
            FilesFragment fragment = FilesFragment.newInstance("", f.getAbsolutePath());
            fm.beginTransaction()
                    .replace(R.id.contentMain, fragment, "Память")
                    .addToBackStack("memory")
                    .commit();
            EventBus.getDefault().post(new ChangeFolderEvent("Память"));
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

    private void filesListChange() {
        List<File> filesTmp = new ArrayList<>();
        if (currentDirectory.listFiles() != null) {
            filesTmp.addAll(Arrays.asList(currentDirectory.listFiles()));
        }
        files = filesTmp;
        //SortBy(COMPARE_BY_TYPE, files);
        files = SortByNameType(files);
        if (!currentDirectory.getName().equals("sdcard") && currentDirectory.getParentFile() != null) {
            files.add(0, new File(".."));
        }
        notifyDataSetChanged();
    }

    private static String getFullTime(final long timeInMillis) {
        final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);
        c.setTimeZone(TimeZone.getDefault());
        return format.format(c.getTime());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRenameManagerEvent(RenameEvent event) {
        boolean renamed = FSActions.renameFile(renameFile,
                ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString(), _context);
        if (renamed) {
            filesListChange();
        }
    }
}
