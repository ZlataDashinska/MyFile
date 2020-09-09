package com.example.diploma;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.diploma.events.ChangeFolderEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fm;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public List<Integer> navigationStack = new ArrayList<>();
    public List<String> titleStack = new ArrayList<>();
    BottomNavigationView bNMIV;
    Toolbar bottomAppBar;
    public Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = getSupportFragmentManager();
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        HomeFragment f = HomeFragment.newInstance();
        fm.beginTransaction()
                .replace(R.id.contentMain, f, "home")
                .addToBackStack("home")
                .commit();
        navigationStack.add(0);
        titleStack.add(getString(R.string.app_name));

        bNMIV = findViewById(R.id.bottom_navigation);
        bottomAppBar = findViewById(R.id.bottom_function);


        bNMIV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.home: {
                        HomeFragment f = HomeFragment.newInstance();
                        fm.beginTransaction()
                                .replace(R.id.contentMain, f, "home")
                                .addToBackStack("home")
                                .commit();
                        navigationStack.add(0);
                        EventBus.getDefault().post(new ChangeFolderEvent(getString(R.string.app_name)));
                        break;
                    }
                    case R.id.history: {
                        HistoryFragment f = HistoryFragment.newInstance();
                        fm.beginTransaction()
                                .replace(R.id.contentMain, f, "history")
                                .addToBackStack("history")
                                .commit();
                        navigationStack.add(1);
                        //mainMenu.getItem(0).setVisible(true);
                        EventBus.getDefault().post(new ChangeFolderEvent(getString(R.string.history)));
                        break;
                    }
                    case R.id.storage: {
                        MemoryFragment f = MemoryFragment.newInstance("storage", "");
                        fm.beginTransaction()
                                .replace(R.id.contentMain, f, "storage")
                                .addToBackStack("storage")
                                .commit();
                        navigationStack.add(2);
                        //mainMenu.getItem(0).setVisible(true);
                        EventBus.getDefault().post(new ChangeFolderEvent(getString(R.string.storage)));
                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_actions, menu);
        mainMenu = menu;
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment f = fm.findFragmentById(R.id.contentMain);
                if (f instanceof FilesFragment)  ((FilesFragment)f).onClickSearch(query);
                if (f instanceof FileCategoriesFragment)  ((FileCategoriesFragment)f).onClickSearch(query);
                navigationStack.add(0);
                EventBus.getDefault().post(new ChangeFolderEvent(getString(R.string.search)));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment f = fm.findFragmentById(R.id.contentMain);
        if (bottomAppBar.getVisibility() == View.VISIBLE) {
            bNMIV.setVisibility(View.VISIBLE);
            bottomAppBar.setVisibility(View.GONE);
            if (f instanceof FilesFragment) {
                ((FilesFragment) f).clearFileSelection();
            } else if (f instanceof FileCategoriesFragment) {
                ((FileCategoriesFragment) f).clearFileSelection();
            }
            return;
        }
        if (navigationStack.size() > 1) {
            if (f instanceof FilesFragment &&
                    ((!((FilesFragment) f).getCurrentPath().equals("/sdcard")
                            &&!((FilesFragment) f).getCurrentPath().equals("/storage/emulated/0"))&&
                            !((FilesFragment) f).getCurrentPath().equals(FSActions.getExternalStoragePath(this.getBaseContext(), true)))) {
                ((FilesFragment) f).onPreviousFolderClick();
            } else {
                titleStack.remove(titleStack.size() - 1);
                getSupportActionBar().setTitle(titleStack.get(titleStack.size() - 1));
                navigationStack.remove(navigationStack.size() - 1);
                bNMIV.getMenu().getItem(navigationStack.get(navigationStack.size() - 1)).setChecked(true);
                fm.popBackStack();
            }
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = fm.findFragmentById(R.id.contentMain);
        switch (item.getItemId()) {
            case R.id.createFolder:
                if (f instanceof FilesFragment)
                    ((FilesFragment)f).onClickAddFile("add_dir");
                break;
            case R.id.paste:
                if (f instanceof FilesFragment)
                    ((FilesFragment)f).onClickPaste();
                break;
            case R.id.createFile:
                if (f instanceof FilesFragment)
                    ((FilesFragment)f).onClickAddFile("add_file");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateManagerEvent(ChangeFolderEvent event) {
        titleStack.add(event.name);
        if(event.name.contains("zip")||event.name.contains("7z")
                ||event.name.contains("tar.gz")||
                event.name.equals("Память")) navigationStack.add(0);
        getSupportActionBar().setTitle(event.name);
    }
}
