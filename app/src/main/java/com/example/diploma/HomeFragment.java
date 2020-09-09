package com.example.diploma;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.diploma.events.ChangeFolderEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    @BindView(R.id.ivMemory)
    ImageView ivMemory;
    @BindView(R.id.ivDoc)
    ImageView ivDoc;
    @BindView(R.id.ivSd)
    ImageView ivSD;
    @BindView(R.id.ivImages)
    ImageView ivPhoto;
    @BindView(R.id.ivVideos)
    ImageView ivVideos;
    @BindView(R.id.ivMusic)
    ImageView ivMusic;

    public Menu mainMenu;

    private FragmentManager fm;

    private Unbinder unbinder;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm=getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, v);
                ivMemory.setOnClickListener(v1 -> {
            setFragment("memory", this.getContext().getResources().getString(R.string.memory));
        });
        ivDoc.setOnClickListener(v1 -> {
            setCategoriesFragment("doc",this.getContext().getResources().getString(R.string.doc));
        });
        ivMusic.setOnClickListener(v1 -> {
            setCategoriesFragment("music", this.getContext().getResources().getString(R.string.audio));
        });
        ivPhoto.setOnClickListener(v1 -> {
            setCategoriesFragment("photo",this.getContext().getResources().getString(R.string.images));
        });
        ivVideos.setOnClickListener(v1 -> {
            setCategoriesFragment("video", this.getContext().getResources().getString(R.string.video));
        });
        ivSD.setOnClickListener(v1 -> {
            if (FSActions.getExternalStoragePath(this.getContext(), true)!=null){
            setFragment("sdCard", this.getContext().getResources().getString(R.string.sd_card));
            }else Toast.makeText(this.getContext(), this.getContext().getResources().getString(R.string.msg_no_sd),Toast.LENGTH_SHORT).show();
        });
        return v;
    }

    private void setFragment(String type, String name){
        FilesFragment f = FilesFragment.newInstance(type);
        fm.beginTransaction()
                .replace(R.id.contentMain, f, type)
                .addToBackStack(type)
                .commit();
        ((MainActivity)getActivity()).navigationStack.add(0);
        Menu menu = ((MainActivity)getActivity()).mainMenu;
        MenuItem m = menu.getItem(0);
                m.setVisible(true);
        EventBus.getDefault().post(new ChangeFolderEvent(name));
    }

    private void setCategoriesFragment(String type, String name){
        FileCategoriesFragment f = FileCategoriesFragment.newInstance(type);
        fm.beginTransaction()
                .replace(R.id.contentMain, f, type)
                .addToBackStack(type)
                .commit();
        ((MainActivity)getActivity()).navigationStack.add(0);
        Menu menu = ((MainActivity)getActivity()).mainMenu;
        MenuItem m = menu.getItem(0);
        m.setVisible(true);
        EventBus.getDefault().post(new ChangeFolderEvent(name));
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

}
