package com.didim99.sat.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.network.NetworkManager;
import com.didim99.sat.network.WebAPI;
import com.didim99.sat.ui.resconverter.ResConvertActivity;
import com.didim99.sat.ui.sbxconverter.SbxConvertActivity;
import com.didim99.sat.core.sbxeditor.Sandbox;
import com.didim99.sat.core.sbxeditor.SbxEditConfig;
import com.didim99.sat.core.sbxeditor.Storage;
import com.didim99.sat.core.sbxeditor.utils.InputValidator;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.ui.sbxeditor.PartInfoActivity;
import com.didim99.sat.ui.sbxeditor.SandboxActivity;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.settings.SettingsActivity;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import java.io.File;

public class StartActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StartAct";
  private static final String NBX_NAME_DATE_FORMAT = "yyyyMMdd_HHmmss";

  private Toast toastMsg;
  //intent request codes
  static final int GET_FILE_REQUEST = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "StartActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_start);
    dialogManager.updateContext(this, null);

    //Toast message init
    toastMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);

    if (!Settings.isHasDB() && !Settings.isIgnoreDb())
      dbDownloadDialog();

    MyLog.d(LOG_TAG, "StartActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_start, menu);
    if (Settings.isHasDB())
      menu.findItem(R.id.action_partInfo).setVisible(true);
    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.action_partInfo:
        startActivity(new Intent(this, PartInfoActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
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
          MyLog.d(LOG_TAG, "File was successfully loaded from external file manager" +
            "\n  Path: " + extPath);
          Storage.setEditConfig(new SbxEditConfig
            .Builder(Sandbox.Mode.OPEN).setFileName(extPath).build());
          startActivity(new Intent(this, SandboxActivity.class));
        }
      }

      else if (resultCode == RESULT_CANCELED) {
        MyLog.d(LOG_TAG, "Choosing file from external file manager aborted");
      }
    }
  }

  public void createSandbox (View view) {
    MyLog.d(LOG_TAG, "CreateSandbox dialog called");
    LayoutInflater inflater = LayoutInflater.from(this);
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.actStart_btnText_create);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(inflater.inflate(R.layout.dialog_sandbox_create, null));
    AlertDialog createDialog = adb.create();
    createDialog.setOnShowListener(createDialog_showListener);
    MyLog.d(LOG_TAG, "CreateSandbox dialog created");
    createDialog.show();
  }

  public void openSandbox (View view) {
    MyLog.d(LOG_TAG, "Choose file from external file manager");
    Intent getFileIntent = new Intent();
    getFileIntent.setAction(Intent.ACTION_GET_CONTENT);
    getFileIntent.setType("file/*");
    if (Utils.isIntentSafe(this, getFileIntent)) {
      MyLog.d(LOG_TAG, "Calling external file manager...");
      startActivityForResult(getFileIntent, GET_FILE_REQUEST);
    }
    else
      MyLog.e(LOG_TAG, "No any external file manager found");
  }

  public void startSbxConverter(View view) {
    startActivity(new Intent(this, SbxConvertActivity.class));
  }

  public void StartResConverter(View view) {
    startActivity(new Intent(this, ResConvertActivity.class));
  }

  //CreateSandbox dialog show listener
  private DialogInterface.OnShowListener createDialog_showListener
    = new DialogInterface.OnShowListener() {
    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "CreateSandbox dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final EditText sbxName = dialog.findViewById(R.id.etSbxName);
      final EditText sbxUid = dialog.findViewById(R.id.etSbxUid);
      final CheckBox cbAddStdMarkers = dialog.findViewById(R.id.addStdMarkers);

      String defaultName = Settings.getDefaultSbxName();
      switch (defaultName) {
        case Settings.VALUE_DEFAULT:
          defaultName = SBML.DEFAULT_SANDBOX_NAME;
          break;
        case Settings.VALUE_DATE:
          defaultName = DateFormat.format
            (NBX_NAME_DATE_FORMAT, System.currentTimeMillis()).toString();
          break;
        case Settings.VALUE_CUSTOM:
          defaultName = Settings.getCustomSbxName();
          break;
      }

      sbxName.setText(defaultName);
      sbxName.setSelection(defaultName.length());
      if (Settings.isDbLoaded()) {
        cbAddStdMarkers.setChecked(Settings.isCreateWithMarkers());
        cbAddStdMarkers.setVisibility(View.VISIBLE);
      }

      ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(view -> {
          MyLog.d(LOG_TAG, "Checking values...");
          String name = sbxName.getText().toString();
          String uidStr = sbxUid.getText().toString();

          if (!InputValidator.getInstance().checkSbxName(name, false))
            return;

          Integer uid = null;
          if (!uidStr.isEmpty()) {
            try {
              uid = Integer.parseInt(uidStr);
              if (uid <= 0)
                throw new NumberFormatException("UID must be positive");
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect UID");
              toastMsg.setText(R.string.editErr_incorrectUID);
              toastMsg.show();
              return;
            }
          }

          boolean addMarkers = Settings.isDbLoaded() && cbAddStdMarkers.isChecked();
          Settings.setCreateWithMarkers(addMarkers);
          Storage.setEditConfig(new SbxEditConfig.Builder(Sandbox.Mode.CREATE)
            .setSbxName(name).setSbxUid(uid).setAddMarkers(addMarkers).build());
          Intent intent = new Intent(StartActivity.this, SandboxActivity.class);
          dialogInterface.dismiss();
          startActivity(intent);
        });
    }
  };

  private void dbDownloadDialog() {
    MyLog.d(LOG_TAG, "DB download dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.diaTitle_needDB);
    adb.setMessage(R.string.diaMsg_needDB);
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> {
      Context appContext = getApplicationContext();
      new DBTask(appContext, ((SAT) appContext)::onDBTaskEvent, DBTask.Mode.CREATE).execute();
    });
    adb.setNeutralButton(R.string.noAskMore, (dialog, which) -> {
      new NetworkManager(WebAPI.LogEvent.DB_IGNORE).execute();
      Settings.setIgnoreDb(true);
    });
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    AlertDialog createDialog = adb.create();
    MyLog.d(LOG_TAG, "DB download dialog created");
    createDialog.show();
  }
}
