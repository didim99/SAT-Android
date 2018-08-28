package com.didim99.sat.sbxconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils;
import java.io.File;

public class SbxConvertActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SbxConvertAct";

  //View-elements
  private EditText inputFilePath;
  private TextView outMessage;
  private Button btnCompress, btnUncompress, btnOpenFileExp;
  private ImageButton btnEdit;
  private Toast toastMsg;
  private ProgressBar mainProgressBar;
  //converter
  private SbxConvertTask task;
  private String taskResult;
  //intent request codes
  static final int GET_FILE_REQUEST = 1;
  //Local variables;
  private boolean uiLocked = false;
  private int verCode = 0;

  //methods
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SbxConvertActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_sbx_converter);

    //View components init
    MyLog.d(LOG_TAG, "View components init...");
    inputFilePath = findViewById(R.id.inputFilePath);
    btnOpenFileExp = findViewById(R.id.btnOpenFileExp);
    btnEdit = findViewById(R.id.btnEdit);
    btnCompress = findViewById(R.id.btnCompress);
    btnUncompress = findViewById(R.id.btnUncompress);
    outMessage = findViewById(R.id.outMessage);
    mainProgressBar = findViewById(R.id.mainProgressBar);
    RadioGroup verCodeGroup = findViewById(R.id.verCodeGroup);
    //Toast message init
    toastMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);
    toastMsg.setGravity(Gravity.TOP, getResources().getDimensionPixelOffset(R.dimen.toastPosX), getResources().getDimensionPixelOffset(R.dimen.toastPosY));
    MyLog.d(LOG_TAG, "View components init completed");

    //Search existing background task
    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (SbxConvertTask) getLastCustomNonConfigurationInstance();
    if (task == null)
      MyLog.d(LOG_TAG, "No existing background task found");
    else {
      task.link(this);
      AsyncTask.Status taskStatus = task.getStatus();
      if (taskStatus == AsyncTask.Status.RUNNING)
        uiLock(true);
      else if (taskStatus == AsyncTask.Status.FINISHED) {
        taskResult = task.getResult();
        uiSet();
      }
      MyLog.d(LOG_TAG, "Connecting to background task completed (" + task.hashCode() + ")");
    }

    //Starting with Intent?
    Intent startIntent = getIntent();
    Uri startData = startIntent.getData();
    if (startData != null) {
      String startPath = startData.getPath();
      MyLog.d(LOG_TAG, "Starting with file:\n  " + startPath);
      inputFilePath.setText(startPath);
      inputFilePath.setSelection(inputFilePath.getText().length());
    } else {
      setupActionBar();
    }

    btnCompress.setOnClickListener(actionClickListener);
    btnUncompress.setOnClickListener(actionClickListener);
    btnEdit.setOnClickListener(actionClickListener);
    verCodeGroup.setOnCheckedChangeListener(verChangeListener);

    MyLog.d(LOG_TAG, "SbxConvertActivity successful started");
  }

  //action listener
  View.OnClickListener actionClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      String path = String.valueOf(inputFilePath.getText()).trim();

      if (view.getId() == R.id.btnEdit) {
        if (path.equals("")) {
          toastMsg.setText(getString(R.string.error, getString(R.string.emptyPath)));
          toastMsg.show();
          return;
        }
        openEditor(path);
        return;
      }

      MyLog.d(LOG_TAG, "Creating new background task...");
      task = new SbxConvertTask(getApplicationContext());
      task.link(SbxConvertActivity.this);
      MyLog.d(LOG_TAG, "Background task created successful (" + task.hashCode() + ")");
      SbxConverter.Config config = new SbxConverter.Config(path);

      switch (view.getId()) {
        case R.id.btnCompress: {
          if (verCode < 20 || verCode > 21) {
            MyLog.e(LOG_TAG, "Version code not selected");
            toastMsg.setText(getString(R.string.error, getString(R.string.noVersionSelected)));
            toastMsg.show();
            return;
          }
          config.setAction(SbxConverter.ACTION_COMPRESS);
          config.setVerCode(verCode);
          task.execute(config);
          break;
        }
        case R.id.btnUncompress: {
          config.setAction(SbxConverter.ACTION_UNCOMPRESS);
          task.execute(config);
          break;
        }
      }
    }
  };

  //Version code listener
  RadioGroup.OnCheckedChangeListener verChangeListener = (radioGroup, currVer) -> {
    switch (currVer) {
      case R.id.verCode20:
        verCode = 20;
        break;
      case R.id.verCode21:
        verCode = 21;
        break;
    }
    MyLog.d(LOG_TAG, "Current version code: " + verCode);
  };

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unLink();
      return task;
    } else
      return super.onRetainCustomNonConfigurationInstance();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
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
    if (requestCode == GET_FILE_REQUEST) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String scheme = data.getScheme();
          String extPath = data.getPath();
          if (!scheme.equals("file") && !(new File(extPath).exists())) {
            MyLog.e(LOG_TAG, "Can't load file. Unsupported scheme: " + scheme);
            toastMsg.setText(R.string.unsupportedScheme);
            toastMsg.show();
            return;
          }
          inputFilePath.setText(extPath);
          inputFilePath.setSelection(inputFilePath.getText().length());
          MyLog.d(LOG_TAG, "File was successfully loaded from external file manager" +
            "\n  Path: " + extPath);
        }
      }

      else if (resultCode == RESULT_CANCELED) {
        MyLog.d(LOG_TAG, "Choosing file from external file manager aborted");
      }
    }
  }

  public void clickOpenExplorer (View view) {
    MyLog.d(LOG_TAG, "Choose file from external file manager");
    Intent getFileIntent = new Intent();
    getFileIntent.setAction(Intent.ACTION_GET_CONTENT);
    getFileIntent.setType("file/*");
    if (Utils.isIntentSafe(this, getFileIntent)) {
      MyLog.d(LOG_TAG, "Calling external file manager...");
      startActivityForResult(getFileIntent, GET_FILE_REQUEST);
    } else {
      MyLog.e(LOG_TAG, "No any external file manager found");
      toastMsg.setText(R.string.externalNotFound_fileManager);
      toastMsg.show();
    }
  }

  private void openEditor (String path) {
    MyLog.d(LOG_TAG, "Open file in external editor...");
    Intent editIntent = new Intent();
    editIntent.setAction(Intent.ACTION_VIEW);
    editIntent.setDataAndType(Uri.parse("file://" + path), "text/SBML");
    if (Utils.isIntentSafe(this, editIntent)) {
      MyLog.d(LOG_TAG, "Calling external text editor...");
      startActivity(editIntent);
    } else {
      MyLog.e(LOG_TAG, "No any external  text editor found");
      toastMsg.setText(R.string.externalNotFound_textEditor);
      toastMsg.show();
    }
  }

  void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    inputFilePath.setEnabled(!state);
    btnCompress.setEnabled(!state);
    btnUncompress.setEnabled(!state);
    btnOpenFileExp.setEnabled(!state);
    btnEdit.setEnabled(!state);
    uiLocked = state;

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      outMessage.setText("");
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
    outMessage.setText(taskResult);
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }

  void setTaskResult (String result) {
    this.taskResult = result;
  }
}