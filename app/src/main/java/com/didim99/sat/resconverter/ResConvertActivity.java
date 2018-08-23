package com.didim99.sat.resconverter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.dirpicker.DirPickerActivity;
import com.didim99.sat.settings.Settings;
import java.io.File;
import java.util.List;

public class ResConvertActivity extends AppCompatActivity
  implements ResConvertTask.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexConvertAct";

  private static final int REQUEST_GET_PATH = 1;
  private static final int REQUEST_CHOOSE_DIR = 2;
  private static final int MAX_LOAD_COUNT = 3;
  //View-elements
  private EditText inputStartPath;
  private Button btnPack, btnUnpack, btnChoosePath;
  private CheckBox selectMode;
  private ProgressBar mainProgressBar;
  private TextView sysMsg;
  private Toast toastMsg;
  private ImageView imageView;
  private FileListAdapter fileListAdapter;
  private MenuItem actionSwichType;
  //converter
  private ResConvertTask resTask;
  private int loadCount = 0;
  private int convertType;
  private int convertMode;
  private int currAction;
  private String taskResult;
  private boolean uiLocked = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ResConvertActivity starting...");
    super.onCreate(savedInstanceState);
    convertType = Settings.ResConverter.getCurrentType();

    switch (convertType) {
      case ResConverter.Type.TEXTURES:
        setContentView(R.layout.act_tex_converter);
        imageView = findViewById(R.id.imageView);
        break;
      case ResConverter.Type.SOUNDS:
        setContentView(R.layout.act_sound_converter);
        fileListAdapter = new FileListAdapter(this);
        RecyclerView fileList = findViewById(R.id.soundList);
        fileList.setLayoutManager(new LinearLayoutManager(this));
        fileList.setAdapter(fileListAdapter);
        break;
    }

    setupActionBar();
    MyLog.d(LOG_TAG, "View components init...");
    inputStartPath = findViewById(R.id.inputStartPath);
    btnChoosePath = findViewById(R.id.btnOpenFileExp);
    btnPack = findViewById(R.id.btnPack);
    btnUnpack = findViewById(R.id.btnUnpack);
    selectMode = findViewById(R.id.selectMode);
    mainProgressBar = findViewById(R.id.mainProgressBar);
    sysMsg = findViewById(R.id.sysMsg);
    //Toast message init
    toastMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);
    toastMsg.setGravity(Gravity.TOP, getResources().getDimensionPixelOffset(R.dimen.toastPosX), getResources().getDimensionPixelOffset(R.dimen.toastPosY));
    MyLog.d(LOG_TAG, "View components init completed");

    //Search existing background task
    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    resTask = (ResConvertTask) getLastCustomNonConfigurationInstance();
    if (resTask == null)
      MyLog.d(LOG_TAG, "No existing background task found");
    else {
      resTask.registerEventListener(this);
      AsyncTask.Status taskStatus = resTask.getStatus();
      if (taskStatus == AsyncTask.Status.RUNNING)
        uiLock(true);
      else if (taskStatus == AsyncTask.Status.FINISHED) {
        taskResult = resTask.getResult();
        uiSet();
      }
      MyLog.d(LOG_TAG, "Connecting to background task completed (" + resTask.hashCode() + ")");
    }

    convertMode = selectMode.isChecked() ?
      ResConverter.Mode.DIRECTORY : ResConverter.Mode.SINGLE_FILE;
    btnPack.setOnClickListener(actionListener);
    btnUnpack.setOnClickListener(actionListener);
    selectMode.setOnCheckedChangeListener(modeListener);

    MyLog.d(LOG_TAG, "ResConvertActivity started");
  }

  View.OnClickListener actionListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      int id = view.getId();
      String startPath = inputStartPath.getText().toString().trim();
      if (!checkPath(startPath))
        return;

      switch (id) {
        case R.id.btnPack:
          currAction = ResConverter.Action.PACK;
          sysMsg.setText(R.string.resProcessing_packing);
          break;
        case R.id.btnUnpack:
          currAction = ResConverter.Action.UNPACK;
          sysMsg.setText(R.string.resProcessing_unpacking);
          break;
      }

      MyLog.d(LOG_TAG, "Creating new background task...");
      resTask = new ResConvertTask(getApplicationContext(), convertType);
      resTask.registerEventListener(ResConvertActivity.this);
      MyLog.d(LOG_TAG, "Background task created successful (" + resTask.hashCode() + ")");
      resTask.execute(new ResConverter.Config(startPath, convertMode, currAction));
    }
  };

  CompoundButton.OnCheckedChangeListener modeListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
      if (isChecked) {
        MyLog.d(LOG_TAG, "Convert mode: directory");
        convertMode = ResConverter.Mode.DIRECTORY;
      }
      else {
        MyLog.d(LOG_TAG, "Convert mode: single file");
        convertMode = ResConverter.Mode.SINGLE_FILE;
      }
    }
  };

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (resTask != null) {
      resTask.unregisterEventListener();
      return resTask;
    }
    else return super.onRetainCustomNonConfigurationInstance();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_res_converter, menu);

    actionSwichType = menu.findItem(R.id.action_switch_type);
    if (convertType == ResConverter.Type.SOUNDS) {
      actionSwichType.setIcon(R.drawable.ic_textures_24dp);
      actionSwichType.setTitle(R.string.mTitle_switchToTextures);
    }

    if (uiLocked)
      actionSwichType.setVisible(false);

    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_switch_type:
        switch (convertType) {
          case ResConverter.Type.TEXTURES:
            Settings.ResConverter.setCurrentType(ResConverter.Type.SOUNDS);
            break;
          case ResConverter.Type.SOUNDS:
            Settings.ResConverter.setCurrentType(ResConverter.Type.TEXTURES);
            break;
        }
        recreate();
        return true;
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (!uiLocked)
      super.onBackPressed();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_PATH || requestCode == REQUEST_CHOOSE_DIR) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String scheme = data.getScheme();
          String extPath = data.getPath();
          if (!scheme.equals("file") && !(new File(extPath).exists())) {
            MyLog.e(LOG_TAG, "Can't load file/dir. Unsupported scheme: " + scheme);
            if (++loadCount < MAX_LOAD_COUNT) {
              toastMsg.setText(R.string.unsupportedScheme);
              toastMsg.show();
              return;
            } else {
              internalExplorerDialog();
              loadCount = 0;
              return;
            }
          }
          inputStartPath.setText(extPath);
          inputStartPath.setSelection(extPath.length());
          MyLog.d(LOG_TAG, "File/dir was successfully loaded:" +
            "\n  Path: " + extPath);
        }
      }

      else if (resultCode == RESULT_CANCELED) {
        MyLog.d(LOG_TAG, "Choosing path aborted");
      }
    }
  }

  @Override
  public void onTaskEvent(int event, String result) {
    switch (event) {
      case ResConvertTask.Event.START:
        uiLock(true);
        break;
      case ResConvertTask.Event.FINISH:
        this.taskResult = result;
        uiLock(false);
        break;
    }
  }

  @Override
  public void onProgressUpdate(int max, int current) {
    int msgId = 0;
    switch (currAction) {
      case ResConverter.Action.PACK: msgId = R.string.resProcessing_packingCnt; break;
      case ResConverter.Action.UNPACK: msgId = R.string.resProcessing_unpackingCnt; break;
    }
    sysMsg.setText(getString(msgId, current, max));
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    if (actionSwichType != null)
      actionSwichType.setVisible(!state);
    inputStartPath.setEnabled(!state);
    btnChoosePath.setEnabled(!state);
    btnPack.setEnabled(!state);
    btnUnpack.setEnabled(!state);
    selectMode.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      switch (convertType) {
        case ResConverter.Type.TEXTURES:
          imageView.setImageDrawable(null);
          break;
        case ResConverter.Type.SOUNDS:
          fileListAdapter.refreshData(null);
          break;
      }
      MyLog.d(LOG_TAG, "UI cleared");
      mainProgressBar.setVisibility(ProgressBar.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    }
    else {
      mainProgressBar.setVisibility(ProgressBar.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
      uiSet();
    }
  }

  void uiSet () {
    MyLog.d(LOG_TAG, "Setting up UI");
    sysMsg.setText(taskResult);
    switch (convertType) {
      case ResConverter.Type.TEXTURES:
        imageView.setImageBitmap(resTask.getPreview());
        break;
      case ResConverter.Type.SOUNDS:
        fileListAdapter.refreshData(resTask.getFileList());
        break;
    }
    MyLog.d(LOG_TAG, "UI setup completed");
  }

  public void choosePath (View v) {
    if (convertMode == ResConverter.Mode.DIRECTORY) {
      MyLog.d(LOG_TAG, "Choose start directory from DirPicker...");
      Intent intent = new Intent(this, DirPickerActivity.class);
      intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.MODE_DIRECTORY);
      startActivityForResult(intent, REQUEST_CHOOSE_DIR);
    }
    else if (convertMode == ResConverter.Mode.SINGLE_FILE) {
      if (Settings.ResConverter.isUseInternalExplorer()) {
        MyLog.d(LOG_TAG, "Choose file from DirPicker...");
        Intent intent = new Intent(this, DirPickerActivity.class);
        intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.MODE_FILE);
        startActivityForResult(intent, REQUEST_CHOOSE_DIR);
      } else {
        MyLog.d(LOG_TAG, "Choose file from external file manager");
        Intent getFileIntent = new Intent();
        getFileIntent.setAction(Intent.ACTION_GET_CONTENT);
        getFileIntent.setType("file/*");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.
          queryIntentActivities(getFileIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
          MyLog.d(LOG_TAG, "Calling external file manager...");
          startActivityForResult(getFileIntent, REQUEST_GET_PATH);
        } else
          MyLog.e(LOG_TAG, "No any external file manager found");
      }
    }
  }

  private boolean checkPath (String path) {
    MyLog.d(LOG_TAG, "Path checking...\n  " + path);
    String logMsg = "Path check failed: ";
    String fileStatus = "";
    boolean flag = false;

    if (path.equals(""))
      fileStatus = getString(R.string.emptyPath);
    else {
      File file = new File(path);

      if (!file.exists())
        fileStatus = getString(R.string.fileNotExist);
      else if (file.isDirectory() && convertMode == ResConverter.Mode.SINGLE_FILE)
        fileStatus = getString(R.string.fileIsDir);
      else if (!file.isDirectory() && convertMode == ResConverter.Mode.DIRECTORY)
        fileStatus = getString(R.string.fileIsNotDir);
      else if (!file.canRead()) {
        fileStatus = getString(R.string.fileNotReadable);
      } else if (!file.canWrite())
        fileStatus = getString(R.string.fileNotWritable);
      else flag = true;
    }

    if (flag)
      MyLog.d(LOG_TAG, "Path check done");
    else {
      MyLog.e(LOG_TAG, logMsg + fileStatus);
      toastMsg.setText(getString(R.string.error, fileStatus));
      toastMsg.show();
    }
    return flag;
  }

  private void internalExplorerDialog() {
    MyLog.d(LOG_TAG, "Internal Explorer dialog created");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.diaTitle_useInternalExplorer);
    adb.setMessage(R.string.diaMsg_useInternalExplorer);
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> {
      Settings.ResConverter.setUseInternalExplorer(true);
      choosePath(null);
    });
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Dialog created");
    adb.create().show();
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
      switch (convertType) {
        case ResConverter.Type.TEXTURES:
          bar.setTitle(R.string.actLabel_texConverter);
          break;
        case ResConverter.Type.SOUNDS:
          bar.setTitle(R.string.actLabel_soundConverter);
          break;
      }
    }
  }
}
