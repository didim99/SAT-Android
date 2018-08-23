package com.didim99.sat.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.BuildConfig;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.network.AppUpdateInfo;
import com.didim99.sat.network.NetworkManager;
import com.didim99.sat.network.WebAPI;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.InputValidator;
import com.google.gson.Gson;

/**
 * Custom settings fragment
 * Created by didim99 on 30.01.18.
 */

public class SettingsFragment extends PreferenceFragment
  implements SharedPreferences.OnSharedPreferenceChangeListener,
  DBTask.EventListener, NetworkManager.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_setFrag";
  private static final String DATE_FORMAT = "dd.MM.yyyy";

  private Context appContext;
  private ListPreference convType, sbxName, language;
  private EditTextPreference sbxCustomName;
  private Preference prefAbout, prefUpdateDb;
  private Toast toastMsg;
  //developer mode;
  private static final int DEV_MODE_START = 10;
  private static final int DEV_MODE_FINISH = 0;
  private static final long DEV_MODE_DELAY = 300;
  private int devModeCounter = DEV_MODE_START;
  private Utils.Timer devTimer;
  private DBTask task;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "Creating new settings fragment...");
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
    appContext = getActivity().getApplicationContext();
    toastMsg = Toast.makeText(appContext, "", Toast.LENGTH_LONG);

    convType = (ListPreference) findPreference(Settings.KEY_RES_CONV_DEFAULT_TYPE);
    sbxName = (ListPreference) findPreference(Settings.KEY_SBX_EDITOR_DEFAULT_NAME);
    sbxCustomName = (EditTextPreference) findPreference(Settings.KEY_SBX_EDITOR_CUSTOM_NAME);
    language = (ListPreference) findPreference(Settings.KEY_LANGUAGE);
    updateListPrefSummary(convType);
    updateListPrefSummary(sbxName);
    updateListPrefSummary(language);
    updateSbxCustomNameState();

    sbxCustomName.setOnPreferenceChangeListener((preference, newValue) ->
      InputValidator.checkSbxName((String) newValue, true));

    prefUpdateDb = findPreference(Settings.KEY_UPDATE_DB);
    updateDbState(Settings.isHasDB());

    prefUpdateDb.setOnPreferenceClickListener(pref -> {
      task = new DBTask(appContext, SettingsFragment.this,
        Settings.isHasDB() ? DBTask.Mode.CHECK_DB_UPDATES : DBTask.Mode.CREATE);
      task.execute();
      return true;
    });

    findPreference(Settings.KEY_UPDATE_APP).setOnPreferenceClickListener(pref -> {
      new NetworkManager(SettingsFragment.this, WebAPI.ACTION_LAST_APP_VER).execute();
      return false;
    });

    String myCopy = "© didim99, " + getString(R.string.app_years);
    String aboutStr = getString(R.string.app_ver,
      getString(R.string.app_name), BuildConfig.VERSION_NAME, myCopy);
    prefAbout = findPreference(Settings.KEY_ABOUT);
    prefAbout.setSummary(aboutStr);

    if (!Settings.isDevMode()) {
      prefAbout.setOnPreferenceClickListener(preference -> {
        if (devModeCounter == DEV_MODE_START) {
          devTimer = new Utils.Timer();
          devTimer.start();
          devModeCounter--;
        } else if (devModeCounter > DEV_MODE_FINISH) {
          devTimer.stop();
          if (devTimer.getMillis() < DEV_MODE_DELAY) {
            MyLog.d(LOG_TAG, "Developer mode: " + devModeCounter);
            devModeCounter--;
            devTimer.start();
          } else
            devModeCounter = DEV_MODE_START;
        } else {
          devTimer.stop();
          if (devTimer.getMillis() < DEV_MODE_DELAY) {
            MyLog.d(LOG_TAG, "Developer mode enabled");
            prefAbout.setOnPreferenceClickListener(null);
            Settings.setDevMode(true);
            toastMsg.setText(R.string.devModeEnabled);
            toastMsg.show();
          }
          devModeCounter = DEV_MODE_START;
        }
        return true;
      });
    }

    findPreference(Settings.KEY_FEEDBACK).setOnPreferenceClickListener(
      preference -> { feedbackDialog(); return true; });
    MyLog.d(LOG_TAG, "Settings fragment created");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    setRetainInstance(true);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    Settings.getSettings().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onStop() {
    Settings.getSettings().unregisterOnSharedPreferenceChangeListener(this);
    super.onStop();
  }

  @Override
  public void onDetach() {
    if (task != null) {
      task.unregisterEventListener();
      task = null;
    }
    super.onDetach();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences settings, String key) {
    Settings.updateSettings(key);
    switch (key) {
      case Settings.KEY_RES_CONV_DEFAULT_TYPE:
        updateListPrefSummary(convType);
        break;
      case Settings.KEY_SBX_EDITOR_DEFAULT_NAME:
        updateListPrefSummary(sbxName);
        updateSbxCustomNameState();
        break;
      case Settings.KEY_SBX_EDITOR_CUSTOM_NAME:
        updateSbxCustomNameState();
        break;
      case Settings.KEY_LANGUAGE:
        updateListPrefSummary(language);
        toastMsg.setText(R.string.restartRequired);
        toastMsg.show();
        break;
    }
  }

  private void updateSbxCustomNameState() {
    String newName = Settings.getCustomSbxName();
    if (newName.isEmpty())
      sbxCustomName.setSummary(R.string.pSummary_sbxEditorCustomName);
    else
      sbxCustomName.setSummary(newName);
    sbxCustomName.setEnabled(Settings.getDefaultSbxName()
      .equals(Settings.VALUE_CUSTOM));
  }

  @Override
  public void onTaskEvent(int event, int statusCode) {
    switch (event) {
      case DBTask.Event.DATA_RECEIVED:
        updateDb(statusCode);
        break;
      case DBTask.Event.DB_LOADED:
        updateDbState(true);
        break;
    }
  }

  @Override
  public void onDataReceived(String action, int statusCode, String data) {
    if (action.equals(WebAPI.ACTION_LAST_APP_VER)) {
      if (statusCode == NetworkManager.Status.OK) {
        AppUpdateInfo info = new Gson().fromJson(data, AppUpdateInfo.class);
        if (info.getVerCode() > BuildConfig.VERSION_CODE) {
          MyLog.d(LOG_TAG, "New App version available: " + info.getVerName());
          AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
          adb.setTitle(getString(R.string.appUpdateAvailable, info.getVerName()));
          adb.setPositiveButton(R.string.dialogButtonOk,
            (dialog, which) -> openUrl(WebAPI.URL_GET_APK));
          adb.setNegativeButton(R.string.dialogButtonCancel, null);
          AlertDialog dialog = adb.create();
          MyLog.d(LOG_TAG, "Update dialog created");
          dialog.show();
        } else {
          MyLog.d(LOG_TAG, "No App updates found");
          toastMsg.setText(R.string.dbTask_noUpdates);
          toastMsg.show();
        }
      } else {
        toastMsg.setText(R.string.dbTask_webUnavailable);
        toastMsg.show();
      }
    }
  }

  private void updateListPrefSummary(ListPreference pref) {
    pref.setSummary(pref.getEntry());
  }

  private void updateDbState(boolean hasDb) {
    if (hasDb) {
      prefUpdateDb.setTitle(R.string.pTitle_updateDb);
      if (Settings.isDevMode()) {
        prefUpdateDb.setSummary(getString(R.string.pSummary_updateDbAdvanced,
          getString(R.string.pSummary_updateDb),
          DateFormat.format(DATE_FORMAT, Utils.timestampToMillis(Settings.getDbVer())),
          Storage.getSAVerInfo().get(Settings.getDbGameVer())));
      } else
        prefUpdateDb.setSummary(R.string.pSummary_updateDb);
    } else {
      prefUpdateDb.setTitle(R.string.pTitle_downloadDb);
      prefUpdateDb.setSummary(R.string.pSummary_downloadDb);
    }
  }

  private void updateDb (int newVersion) {
    if (newVersion < 0) {
      toastMsg.setText(R.string.dbTask_webUnavailable);
      toastMsg.show();
    } else if (newVersion > Settings.getDbVer()) {
      MyLog.d(LOG_TAG, "Database update available "
        + "(" + Settings.getDbVer() + " --> " + newVersion + ")");
      AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
      if (Settings.isDevMode()) {
        adb.setMessage(getString(R.string.dbTask_newDbAvailableAdvanced,
          DateFormat.format(DATE_FORMAT, Utils.timestampToMillis(newVersion))));
      } else {
        adb.setMessage(R.string.dbTask_newDbAvailable);
      }
      adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> {
        MyLog.d(LOG_TAG, "Updating database...");
        prefUpdateDb.setOnPreferenceClickListener(null);
        task = new DBTask(appContext, this, DBTask.Mode.UPDATE);
        task.execute();
      });
      adb.setNegativeButton(R.string.dialogButtonCancel, null);
      AlertDialog dialog = adb.create();
      MyLog.d(LOG_TAG, "UpdateDb dialog created");
      dialog.show();
    } else {
      MyLog.d(LOG_TAG, "No DB updates found");
      toastMsg.setText(R.string.dbTask_noUpdates);
      toastMsg.show();
    }
  }

  private void feedbackDialog() {
    MyLog.d(LOG_TAG, "Feedback dialog called");
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
    adb.setTitle(R.string.pTitle_feedback);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setView(inflater.inflate(R.layout.dialog_feedback, null));
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(feedbackDialog_showListener);
    MyLog.d(LOG_TAG, "Feedback dialog created");
    dialog.show();
  }

  private void openUrl(String url) {
    MyLog.d(LOG_TAG, "Open url: " + url);
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
  }

  private DialogInterface.OnShowListener feedbackDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String devUrl = "https://vk.com/didim99";
    private static final String groupUrl = "https://vk.com/spaceagency";

    @Override
    public void onShow(DialogInterface dialog) {
      MyLog.d(LOG_TAG, "Feedback dialog shown");
      TextView tvDeveloper = ((AlertDialog) dialog).findViewById(R.id.tvDeveloper);
      tvDeveloper.setPaintFlags(tvDeveloper.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      tvDeveloper.setOnClickListener(v -> openUrl(devUrl));
      tvDeveloper.setText(devUrl);
      TextView tvGroup = ((AlertDialog) dialog).findViewById(R.id.tvGroup);
      tvGroup.setPaintFlags(tvGroup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      tvGroup.setOnClickListener(v -> openUrl(groupUrl));
      tvGroup.setText(groupUrl);
    }
  };
}