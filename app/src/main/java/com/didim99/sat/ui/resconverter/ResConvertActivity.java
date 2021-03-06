package com.didim99.sat.ui.resconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.core.resconverter.ResConvertTask;
import com.didim99.sat.core.resconverter.ResConverter;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.ui.BaseActivity;
import com.didim99.sat.ui.dirpicker.DirPickerActivity;
import com.didim99.sat.ui.sbxeditor.UIManager;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.R;
import java.io.File;

public class ResConvertActivity extends BaseActivity
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
  private ResConverter.Mode convertMode;
  private ResConverter.Action currAction;
  private String taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ResConvertActivity starting...");
    convertType = Settings.ResConverter.getCurrentType();
    super.onCreate(savedInstanceState);

    switch (convertType) {
      case ResConverter.Type.TEXTURES:
        setContentView(R.layout.act_tex_converter);
        imageView = findViewById(R.id.imageView);
        break;
      case ResConverter.Type.SOUNDS:
        setContentView(R.layout.act_sound_converter);
        fileListAdapter = new FileListAdapter();
        RecyclerView fileList = findViewById(R.id.soundList);
        fileList.setLayoutManager(new LinearLayoutManager(this));
        fileList.setAdapter(fileListAdapter);
        break;
    }

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
    btnPack.setOnClickListener(this::onAction);
    btnUnpack.setOnClickListener(this::onAction);

    selectMode.setOnCheckedChangeListener((button, c) -> {
      MyLog.d(LOG_TAG, "Convert mode: " + (c ? "directory" : "single file"));
      convertMode = c ? ResConverter.Mode.DIRECTORY : ResConverter.Mode.SINGLE_FILE;
    });

    MyLog.d(LOG_TAG, "ResConvertActivity started");
  }

  public void onAction(View view) {
    int id = view.getId();
    String startPath = inputStartPath.getText().toString().trim();
    if (!Utils.checkPath(ResConvertActivity.this, startPath,
      convertMode == ResConverter.Mode.DIRECTORY))
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

  @Override
  public ResConvertTask onRetainCustomNonConfigurationInstance() {
    if (resTask != null) {
      resTask.unregisterEventListener();
      return resTask;
    } else
      return null;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_res_converter, menu);

    actionSwichType = menu.findItem(R.id.action_switch_type);
    if (convertType == ResConverter.Type.SOUNDS) {
      actionSwichType.setIcon(UIManager.getInstance().resolveAttr(R.attr.ic_textures));
      actionSwichType.setTitle(R.string.mTitle_switchToTextures);
    }

    if (uiLocked)
      actionSwichType.setVisible(false);

    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_switch_type) {
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
    } else return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_PATH || requestCode == REQUEST_CHOOSE_DIR) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String scheme = data.getScheme();
          String extPath = data.getPath();
          if (scheme == null || extPath == null
            || !scheme.equals("file") && !(new File(extPath).exists())) {
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
      case PACK: msgId = R.string.resProcessing_packingCnt; break;
      case UNPACK: msgId = R.string.resProcessing_unpackingCnt; break;
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

  void uiSet() {
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

  public void choosePath(View v) {
    if (convertMode == ResConverter.Mode.DIRECTORY) {
      MyLog.d(LOG_TAG, "Choose start directory from DirPicker...");
      Intent intent = new Intent(this, DirPickerActivity.class);
      intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.MODE_DIRECTORY);
      startActivityForResult(intent, REQUEST_CHOOSE_DIR);
    } else if (convertMode == ResConverter.Mode.SINGLE_FILE) {
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

        if (Utils.isIntentSafe(this, getFileIntent)) {
          MyLog.d(LOG_TAG, "Calling external file manager...");
          startActivityForResult(getFileIntent, REQUEST_GET_PATH);
        } else
          MyLog.e(LOG_TAG, "No any external file manager found");
      }
    }
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

  @Override
  protected void onSetupActionBar(ActionBar bar) {
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
