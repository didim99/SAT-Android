package com.didim99.sat.dirpicker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * directory picker Activity
 * Created by didim99 on 26.01.18.
 */

public class DirPickerActivity extends AppCompatActivity
  implements DirListAdapter.OnItemClickListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DirPicker";

  public static final String KEY_MODE = "DirPickerActivity.mode";
  public static final int MODE_UNDEFINED = 0;
  public static final int MODE_DIRECTORY = 1;
  public static final int MODE_FILE = 2;

  private static final String FS_ROOT = "/";
  private static final int NO_SELECTION = -1;

  private Context appContext;
  private TextView tvPath, tvEmpty;
  private Button btnGo;
  private DirListAdapter adapter;
  private ArrayList<DirEntry> arrayDir = new ArrayList<>();
  private int selectedId = NO_SELECTION;
  private String path;
  private int mode;

  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "DirPickerActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_dir_picker);
    appContext = getApplicationContext();

    //loading saved instance
    State savedState = (State) getLastCustomNonConfigurationInstance();
    if (savedState != null) {
      path = savedState.path;
    }

    //View components init
    tvPath = findViewById(R.id.dirPicker_tvPath);
    tvEmpty = findViewById(R.id.dirPicker_tvEmpty);
    btnGo = findViewById(R.id.dirPicker_go);
    btnGo.setOnClickListener(v -> onClickGo(null));
    adapter = new DirListAdapter(this, this, arrayDir);
    RecyclerView listDir = findViewById(R.id.dirPicker_listDir);
    listDir.setLayoutManager(new LinearLayoutManager(this));
    listDir.setHasFixedSize(true);
    listDir.setAdapter(adapter);

    mode = getIntent().getIntExtra(KEY_MODE, MODE_UNDEFINED);
    if (mode == MODE_FILE) {
      setTitle(R.string.actLabel_filePicker);
      btnGo.setVisibility(View.GONE);
      btnGo.setOnClickListener(null);
    }

    if (path == null)
      path = Settings.getSettings().getString(
        Settings.KEY_DIR_PICKER_LAST_PATH, FS_ROOT);
    // Checking access to file system root directory
    if (path.equals(FS_ROOT) && !isDirOpened(path)) {
      Toast.makeText(appContext,
        R.string.dirPicker_fsRootUnreadable, Toast.LENGTH_LONG).show();
      path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
      //save new start path in SharedPreferences
      Settings.getSettings().edit()
        .putString(Settings.KEY_DIR_PICKER_LAST_PATH, path).apply();
    }

    MyLog.d(LOG_TAG, "DirPickerActivity started");
    updateListDir();
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return new State(path);
  }

  @Override
  public void onItemClick(int selectedId, boolean isDir) {
    if (isDir) {
      this.selectedId = selectedId;
      updateListDir();
    } else if (mode == MODE_FILE) {
      onClickGo(arrayDir.get(selectedId).getName());
    }
  }

  public void onClickBack (View view) {
    if (path.equals(FS_ROOT)) {
      setResult(RESULT_CANCELED, new Intent());
      finish();
    } else {
      path = new File(path).getParent();
      if (!path.equals(FS_ROOT))
        path += "/";
      updateListDir();
    }
  }

  public void onClickGo (String fileName) {
    //save last picked path in SharedPreferences
    Settings.getSettings().edit()
      .putString(Settings.KEY_DIR_PICKER_LAST_PATH, path).apply();
    Intent intent = new Intent();
    if (fileName != null)
      path += fileName;
    intent.setData(Uri.parse("file://" + path));
    setResult(RESULT_OK, intent);
    finish();
  }

  private void updateListDir () {
    if (selectedId != NO_SELECTION)
      path = path + arrayDir.get(selectedId).getName() + "/";
    selectedId = NO_SELECTION;
    arrayDir.clear();
    MyLog.d(LOG_TAG, "Curr path: " + path);

    ArrayList<DirEntry> arrayFiles = new ArrayList<>();
    File[] files = new File(path).listFiles();

    boolean dirOpened = files != null;
    btnGo.setEnabled(dirOpened);
    if (dirOpened) {
      if (files.length > 0) {
        tvEmpty.setVisibility(TextView.INVISIBLE);
        Arrays.sort(files);
        DirEntry entry;
        for (File file : files) {
          boolean isDir = file.isDirectory();
          entry = new DirEntry(file.getName(), isDir);
          if (isDir)
            arrayDir.add(entry);
          else
            arrayFiles.add(entry);
        }
        arrayDir.addAll(arrayFiles);
      } else {
        tvEmpty.setVisibility(TextView.VISIBLE);
      }
    } else {
      Toast.makeText(appContext,
        R.string.dirPicker_dirUnreadable, Toast.LENGTH_LONG).show();
    }

    adapter.notifyDataSetChanged();
    tvPath.setText(path);
  }

  private boolean isDirOpened(String dirName) {
    try {
      File[] files = new File(dirName).listFiles();
      for (File file : files) {}
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  static class DirEntry {
    private String name;
    private boolean isDir;

    DirEntry(String name, boolean isDir) {
      this.name = name;
      this.isDir = isDir;
    }

    String getName() {
      return name;
    }
    boolean isDir() {
      return isDir;
    }
  }

  private static class State {
    private String path;

    State(String path) {
      this.path = path;
    }
  }
}
