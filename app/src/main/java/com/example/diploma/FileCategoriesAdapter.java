package com.example.diploma;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diploma.events.RenameEvent;
import com.example.diploma.interfaces.ChangeFileListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.diploma.DatasAction.SortByNameType;
import static com.example.diploma.DatasAction.formatSize;
import static com.example.diploma.DatasAction.getFullTime;
import static com.example.diploma.FSActions.getExtension;
import static com.example.diploma.FSActions.showFile;

public class FileCategoriesAdapter extends RecyclerView.Adapter<FileCategoriesAdapter.ViewHolder> {

    private List<File> files;
    private FragmentManager fm;
    private File copyDir = null;
    public List<File> getCopyDirs() {
        return copyDirs;
    }

    public void setCopyDirs(List<File> copyDirs) {
        this.copyDirs = copyDirs;
    }
    private SelectionTracker<String> selectionTracker;
    private List<File> copyDirs = null;
    public void setRenameFile(File renameFile) {
        this.renameFile = renameFile;
    }

    private File renameFile = null;
    private boolean isCut = false;
    private Context _context;
    private ChangeFileListener listener;

    public static final Comparator<File> COMPARE_BY_NAME = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    };

    public static final Comparator<File> COMPARE_BY_DATE = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return Math.round(lhs.lastModified() - rhs.lastModified());
        }
    };

    public static final Comparator<File> COMPARE_BY_TYPE = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            int res = 0;
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                res = -1;
            } else if (rhs.isDirectory() && !lhs.isDirectory()) {
                res = 1;
            }
            return res;
        }
    };

    public FileCategoriesAdapter(List<File> files, FragmentManager fm) {
        this.files = SortByNameType(files);
        //EventBus.getDefault().post(new ChangeFolderEvent(currentDirectory.getAbsolutePath()));
        this.fm = fm;
        setHasStableIds(true);
    }

    public void remove(File f){
        files.remove(f);
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
            FileCategoriesAdapter adapter = ((FileCategoriesAdapter) rv.getAdapter());
            if (adapter != null) {
                return adapter.files.get(position).getPath();
            }
            throw new IllegalStateException("RecyclerView adapter is not set!");
        }

        @Override
        public int getPosition(@NonNull String key) {
            FileCategoriesAdapter adapter = ((FileCategoriesAdapter) rv.getAdapter());
            final RecyclerView.ViewHolder vh = rv.findViewHolderForItemId(adapter.files.indexOf(new File(key)));
            if (vh != null) {
                return vh.getLayoutPosition();
            }
            return RecyclerView.NO_POSITION;
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            Log.d("bus_register", e.toString());
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        _context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(files.get(position), selectionTracker.isSelected(files.get(position).getPath()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = files.get(position);
                showFile(f, getExtension(f), v.getContext());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });
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

        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
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
            //ivIcon.setBackgroundDrawable();
            path = file.getPath();
            itemView.setActivated(isActivated);
            tvDescription.setText(file.getName());
            Long lastModified = file.lastModified();
            String date = getFullTime(lastModified);
            tvDate.setText(date);
            tvSize.setText(formatSize(file.length()));
            String type;
            String extension = getExtension(file);
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (type.contains("image") || type.contains("video"))
                Glide
                        .with(context)
                        .load(file.getAbsolutePath())
                        .into(ivIcon);
            else
                //ivIcon.setImageDrawable(Drawable.createFromPath(file.getAbsolutePath()));
                ivIcon.setImageResource(R.drawable.ic_list_alt);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRenameManagerEvent(RenameEvent event) {
        boolean renamed = FSActions.renameFile(renameFile,
                ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString(), _context);
        if (renamed) {
            files.remove(event.position);
            files.add(event.position, new File(renameFile.getParent(),
                    ((TextView) event.dialog.getDialog().findViewById(R.id.tvNewFileName)).getText().toString()));
            notifyItemChanged(event.position);
        }
    }
}
