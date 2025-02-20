package at.tewan.androidmapper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;

import at.tewan.androidmapper.beatmap.info.Info;
import at.tewan.androidmapper.beatmap.Beatmaps;
import at.tewan.androidmapper.mainmenu.BeatmapListAdapter;
import at.tewan.androidmapper.preferences.Preferences;
import at.tewan.androidmapper.util.ErrorPrinter;

/**
 * @author Stefan Heinz
 *
 * The main menu of android mapper.
 */
public class MainmenuActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Main Menu Activity";

    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 200;

    private RecyclerView beatmapList;
    private ArrayList<Info> beatmaps;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences.loadPreferences(this);

        // Set dark theme
        if(Preferences.isDarkTheme()) {

            Log.i(LOG_TAG, "Using dark theme");
            setTheme(R.style.AppTheme_Dark);

        } else {
            Log.i(LOG_TAG, "Using light theme");
        }

        // Requesting permissions
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "External Storage permissions not granted. Requesting...");

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
        } else {
            try {
                Beatmaps.init();
                this.onPermissionsGranted();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * Gets called when all permissions have been granted and the app is ready to roll!
     */
    private void onPermissionsGranted() {

        setContentView(R.layout.activity_mainmenu);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(newBeatmapListener);

        swipeRefreshLayout = findViewById(R.id.beatmapListRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshBeatmaps);

        beatmapList = findViewById(R.id.beatmapList);
        beatmapList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        beatmapList.setItemAnimator(new DefaultItemAnimator());

        refreshBeatmaps();

        //SongConverter.init(this); // <= Check SongConverter.java

        /*
        beatmapList.setOnItemLongClickListener((parent, view, position, id) -> {

            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.beatmap_index_context, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {

                if(item.getItemId() == R.id.delete) {

                    Info selectedItem = beatmaps.get(position);
                    boolean success = Beatmaps.deleteBeatmap(selectedItem);

                    if(success)
                        beatmaps.remove(position); // Remove the list item
                        beatmapList.invalidateViews(); // Refresh the list view

                }

                return true;
            });

            popupMenu.show();

            return true;

        });
        */
    }

    private void refreshBeatmaps() {
        try {
            beatmaps = Beatmaps.readAllBeatmapInfos(this);
        } catch (IOException ex) {
            ErrorPrinter.msg(this, "Failed to load beatmap infos", ex);
        }

        beatmapList.setAdapter(new BeatmapListAdapter(getApplicationContext(), beatmaps));
        swipeRefreshLayout.setRefreshing(false);
    }

    View.OnClickListener newBeatmapListener = view -> {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(R.string.new_beatmap_title);
        dialogBuilder.setView(R.layout.dialog_new_beat_map);

        Dialog dialog = dialogBuilder.show();

        EditText songName = dialog.findViewById(R.id.inputSongName);
        EditText songAuthor = dialog.findViewById(R.id.inputSongAuthor);
        EditText levelAuthor = dialog.findViewById(R.id.inputLevelAuthor);
        EditText bpm = dialog.findViewById(R.id.inputbpm);
        Button createButton = dialog.findViewById(R.id.create);

        levelAuthor.setText(Preferences.getDefaultAuthorKey());


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                boolean songNameEmpty = songName.getText().toString().trim().isEmpty();
                boolean songAuthorEmpty = songAuthor.getText().toString().trim().isEmpty();
                boolean levelAuthorEmpty = levelAuthor.getText().toString().trim().isEmpty();
                boolean bpmEmpty = bpm.getText().toString().trim().isEmpty();

                createButton.setEnabled( !(songNameEmpty || songAuthorEmpty || levelAuthorEmpty || bpmEmpty) );
            }
        };

        songName.addTextChangedListener(watcher);
        songAuthor.addTextChangedListener(watcher);
        levelAuthor.addTextChangedListener(watcher);
        bpm.addTextChangedListener(watcher);

        createButton.setOnClickListener(v -> {
            Log.i(LOG_TAG, "Creating new Beatmap");
            Log.i(LOG_TAG, "Song Name: " + songName.getText());
            Log.i(LOG_TAG, "Song Author: " + songAuthor.getText());
            Log.i(LOG_TAG, "Level Author: " + levelAuthor.getText());
            Log.i(LOG_TAG, "BPM: " + bpm.getText());

            Info result = Beatmaps.createBeatmap(this, songName.getText().toString(), songAuthor.getText().toString(), levelAuthor.getText().toString(), Float.parseFloat(bpm.getText().toString()));

            if(result != null) {

                dialog.cancel();
                beatmaps.add(result);
                refreshBeatmaps();

            } else {
                Snackbar bar = Snackbar.make(view, R.string.invalid_beatmap_name, Snackbar.LENGTH_LONG);
                bar.show();
            }

        });

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);

                return true;
            case R.id.action_refresh:
                refreshBeatmaps();

                return true;

            case R.id.action_about:
                openAboutDialog();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_about);
        builder.setMessage(R.string.dialog_about);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {

            // We only request one permission, so we can be sure to only get one response
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionsGranted(); // Permissions were granted -> Continue application
            } else {
                ErrorPrinter.msg(this, getString(R.string.dialog_permissions_not_granted))
                .setOnDismissListener(dialog -> finish()); // Close app on dialog dismiss

            }

        }

    }
}
