package com.example.diploma;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diploma.archive.ArchiveFile;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.diploma.DatasAction.SortByNameTypeArch;
import static com.example.diploma.DatasAction.formatSize;
import static com.example.diploma.DatasAction.getFullTime;

public class ArchiveViewAdapter extends RecyclerView.Adapter<ArchiveViewAdapter.ViewHolder> {

    public void setCurList(List<ArchiveFile> cList) {
        this.curList = cList;
        notifyDataSetChanged();
    }

    private List<ArchiveFile> curList;
    private FragmentManager fm;
    private Context _context;

    public ArchiveViewAdapter(List<ArchiveFile> files, FragmentManager fm) {
        this.curList = SortByNameTypeArch(files);
        this.fm = fm;
    }

    public ArchiveFile getFirstFile(){
        return curList.get(0);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        _context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(curList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(v, position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {//только извлечение
  return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return curList.size();
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(ArchiveFile file) {
            //ivIcon.setBackgroundDrawable();
            String[] tmp=file.getCurName().split("/");
            tvDescription.setText(tmp[tmp.length-1]);
                long lastModified = file.getCurTime();
                String date = getFullTime(lastModified);
                tvDate.setText(date);
                if (file.isDirectory()) {
                       int num = file.getZipEntryList().size();
                    tvSize.setText(String.valueOf(num));
                    ivIcon.setImageResource(R.drawable.ic_folder);
                } else {
                    tvSize.setText(formatSize(file.getCurSize()));
                    ivIcon.setImageResource(R.drawable.ic_list_alt);
                }
            }
        }

    private void updateList(@NonNull View v, int position) {
        ArchiveFile current = curList.get(position);
        if(current.isDirectory()){
            curList=current.getZipEntryList();
            SortByNameTypeArch(curList);
            notifyDataSetChanged();
        }
    }


}
