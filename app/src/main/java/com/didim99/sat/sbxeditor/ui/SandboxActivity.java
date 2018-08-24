package com.didim99.sat.sbxeditor.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import com.didim99.sat.BaseActivity;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.Utils;
import com.didim99.sat.dirpicker.DirPickerActivity;
import com.didim99.sat.sbxeditor.Sandbox;
import com.didim99.sat.sbxeditor.SbxEditConfig;
import com.didim99.sat.sbxeditor.SbxEditTask;
import com.didim99.sat.sbxeditor.Station;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.settings.Settings;
import java.io.File;
import java.util.ArrayList;

public class SandboxActivity extends BaseActivity
  implements StationListAdapter.EventListener<Station>,
    SbxEditTask.EventListener, DialogManager.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SbxAct";

  private static final class Request {
    private static final int PICK_MODULE = 1;
    private static final int PICK_MODULE_FOR_COLONY = 2;
    private static final int EDIT_NAVICOMP = 3;
    private static final int EDIT_STATION = 4;
    private static final int SBX_SAVE_INTERNAL = 5;
  }

  //view-elements
  private MenuItem
    actionSave, actionSbxInfo, actionNav, actionOptimize,
    actionAdd, actionPartInfo, actionDevTools;
  private ProgressBar sbxProgressBar;
  private RecyclerView rvStationList;
  private ActionMode actionMode;
  //adapter & task
  private StationListAdapter adapter;
  private SbxEditTask task;
  private boolean uiLocked = false;
  private boolean exitRequired = false;
  private ArrayList<Station> selected;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SandboxActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_sbx_main);
    dialogManager.updateContext(this, this);
    SbxEditConfig config;

    //Search saved instance
    MyLog.d(LOG_TAG, "Loading saved instance...");
    SavedState instance = (SavedState) getLastCustomNonConfigurationInstance();
    if (instance == null)
      instance = new SavedState();

    //View components init
    MyLog.d(LOG_TAG, "View components init...");
    adapter = new StationListAdapter(this, getMenuInflater(), this);
    if (instance.adapterState != null) {
      MyLog.d(LOG_TAG, "Selected list found, enable multi-selection mode");
      adapter.initSelection(Storage.getSandbox(), instance.adapterState);
    }
    sbxProgressBar = findViewById(R.id.sbxProgressBar);
    rvStationList = findViewById(R.id.rvStationList);
    rvStationList.setLayoutManager(new LinearLayoutManager(this));
    rvStationList.setHasFixedSize(true);
    rvStationList.setAdapter(adapter);
    MyLog.d(LOG_TAG, "View components init completed");

    //Starting with Intent?
    config = Storage.getEditConfig();
    Uri startData = getIntent().getData();
    if (startData != null) {
      String name = startData.getPath();
      MyLog.d(LOG_TAG, "Starting with file:\n  " + name);
      config = new SbxEditConfig(Sandbox.Mode.OPEN, name);
    } else {
      setupActionBar();
    }

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = instance.task;
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
      startTask(config);
      Storage.setSandbox(null);
    } else {
      task.registerEventListener(this);
      AsyncTask.Status taskStatus = task.getStatus();
      if (taskStatus == AsyncTask.Status.RUNNING)
        uiLock(true);
      else if (taskStatus == AsyncTask.Status.FINISHED) {
        uiSet();
      }
      MyLog.d(LOG_TAG, "Connecting to background task completed (" + task.hashCode() + ")");
    }

    MyLog.d(LOG_TAG, "SandboxActivity started");
  }

  @Override
  public SavedState onRetainCustomNonConfigurationInstance() {
    SavedState state = new SavedState();

    if (task != null) {
      task.unregisterEventListener();
      state.task = task;
    }

    if (adapter.inMultiSelectionMode())
      state.adapterState = adapter.getState();

    return state;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_sandbox, menu);
    actionSave = menu.findItem(R.id.action_save);
    actionSbxInfo = menu.findItem(R.id.action_sbxInfo);
    actionOptimize = menu.findItem(R.id.action_optimize);
    actionNav = menu.findItem(R.id.action_nav);
    actionAdd = menu.findItem(R.id.action_add);
    actionPartInfo = menu.findItem(R.id.action_partInfo);
    actionDevTools = menu.findItem(R.id.action_devTools);

    if (Settings.isDevMode()) {
      menu.findItem(R.id.action_addAll).setVisible(true);
      menu.findItem(R.id.action_addAllFont).setVisible(true);
      actionDevTools.setVisible(true);
    }

    if (Settings.isDbLoaded()) {
      actionAdd.setVisible(true);
      actionPartInfo.setVisible(true);
    }

    if (uiLocked) {
      actionSave.setVisible(false);
      actionSbxInfo.setVisible(false);
      actionOptimize.setVisible(false);
      actionNav.setVisible(false);
      actionAdd.setVisible(false);
      actionPartInfo.setVisible(false);
      actionDevTools.setVisible(false);
    }

    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_sbxInfo:
        dialogManager.sandboxInfo();
        return true;
      case R.id.action_nav:
        startActivityForResult(new Intent(
          this, NaviCompActivity.class), Request.EDIT_NAVICOMP);
        return true;
      case R.id.action_optimize:
        dialogManager.createDialog(DialogManager.DialogID.SBX_OPTIMIZE);
        return true;
      case R.id.action_addShuttle:
        dialogManager.addModuleDialog(SBML.PART_ID_SHUTTLE);
        return true;
      case R.id.action_addSpy:
        dialogManager.addModuleDialog(SBML.PART_ID_SPY);
        return true;
      case R.id.action_addModule:
        Intent moduleIntent = new Intent(this, PartInfoActivity.class);
        moduleIntent.setAction(SAT.ACTION_PICK_MODULE);
        startActivityForResult(moduleIntent, Request.PICK_MODULE);
        return true;
      case R.id.action_addColony:
        Intent colonyIntent = new Intent(this, PartInfoActivity.class);
        colonyIntent.setAction(SAT.ACTION_PICK_MODULE);
        startActivityForResult(colonyIntent, Request.PICK_MODULE_FOR_COLONY);
        return true;
      case R.id.action_addText:
        dialogManager.createDialog(DialogManager.DialogID.ADD_TEXT);
        return true;
      case R.id.action_addAll:
        dialogManager.createDialog(DialogManager.DialogID.ADD_ALL_MODULES);
        return true;
      case R.id.action_addAllFont:
        dialogManager.createDialog(DialogManager.DialogID.ADD_ALL_FONT);
        return true;
      case R.id.action_save:
        dialogManager.saveSandbox(false);
        return true;
      case R.id.action_partInfo:
        startActivity(new Intent(this, PartInfoActivity.class));
        return true;
      case R.id.action_fuelInfo:
        startTask(new SbxEditConfig(Sandbox.Mode.FUEL_INFO));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_staInfo:
        dialogManager.stationInfo(selected.get(0));
        return true;
      case R.id.action_staCopy:
        dialogManager.stationCopy(selected);
        return true;
      case R.id.action_staEdit:
        Storage.setEditConfig(new SbxEditConfig(Sandbox.Mode.EDIT, selected));
        Intent intent = new Intent(this, SbxEditActivity.class);
        startActivityForResult(intent, Request.EDIT_STATION);
        return true;
      case R.id.action_staDelete:
        dialogManager.stationDelete(selected);
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == Request.EDIT_NAVICOMP) {
      if (Storage.getSandbox().isNaviCompModified())
        startTask(new SbxEditConfig(Sandbox.Mode.UPDATE_NAV));
    } else if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case Request.EDIT_STATION:
          MyLog.d(LOG_TAG, "Station edit configuration received");
          startTask(Storage.getEditConfig());
          break;
        case Request.PICK_MODULE:
          int partID = intent.getIntExtra(SAT.EXTRA_PART_ID, 0);
          if (partID != 0) dialogManager.addModuleDialog(partID);
          break;
        case Request.PICK_MODULE_FOR_COLONY:
          int colonyPartID = intent.getIntExtra(SAT.EXTRA_PART_ID, 0);
          if (colonyPartID != 0) dialogManager.addColonyDialog(colonyPartID);
          break;
        case Request.SBX_SAVE_INTERNAL:
          Uri data = intent.getData();
          if (data != null)
            startTask(new SbxEditConfig(Sandbox.Mode.SEND, data.getPath()));
          break;
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (actionMode != null) {
      actionMode.finish();
      return;
    }

    if (!uiLocked) {
      if (Storage.getSandbox().isModified()) {
        dialogManager.exitDialog();
      } else
        super.onBackPressed();
    }
  }

  @Override
  public void onItemClick(View view, Station station) {
    if (station != null) {
      selected = new ArrayList<>(2);
      selected.add(station);
      openContextMenu(view);
    }
  }

  @Override
  public void onMultiSelectionEvent(int event, int count) {
    switch (event) {
      case MultiSelectAdapter.MSEvent.START:
        actionMode = startSupportActionMode(actionModeCallback);
        updateActionMode(count, false);
        break;
      case MultiSelectAdapter.MSEvent.UPDATE:
        updateActionMode(count, false);
        break;
      case MultiSelectAdapter.MSEvent.ALL_SELECTED:
        updateActionMode(count, true);
        break;
      case MultiSelectAdapter.MSEvent.END:
        actionMode.finish();
        break;
    }
  }

  private void updateActionMode(int count, boolean allSelected) {
    actionMode.setTitle(getString(R.string.amStationList_selectedCount, count));
    actionMode.getMenu().findItem(R.id.action_selectAll).setVisible(!allSelected);
  }

  private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      mode.getMenuInflater().inflate(R.menu.am_menu_station, menu);
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      switch (item.getItemId()) {
        case R.id.action_selectAll:
          adapter.selectAll();
          break;
        case R.id.action_staCopy:
        case R.id.action_staEdit:
        case R.id.action_staDelete:
          selected = new ArrayList<>(adapter.getSelected());
          onContextItemSelected(item);
          actionMode.finish();
          break;
      }
      return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      adapter.multiSelectionCancel();
      actionMode = null;
    }
  };

  @Override
  public void onTaskEvent(int event, boolean success) {
    switch (event) {
      case SbxEditTask.Event.START:
        uiLock(true);
        break;
      case SbxEditTask.Event.FINISH:
        uiLock(false);
        if (success)
          uiSet();
        else if (Storage.getSandbox() == null) {
          MyLog.e(LOG_TAG, "Unable to load external file. Finishing Activity.");
          finish();
        }
        break;
    }
  }

  @Override
  public void onDialogEvent(int dialog, int event, SbxEditConfig config) {
    switch (dialog) {
      case DialogManager.DialogID.SBX_EDIT:
        if (event == DialogManager.Event.OK)
          updateActionBar();
        break;
      case DialogManager.DialogID.SBX_SAVE:
        switch (event) {
          case DialogManager.Event.OK:
            startTask(config);
            break;
          case DialogManager.Event.CANCEL:
            finish();
            break;
        }
        break;
      case DialogManager.DialogID.EXIT:
        switch (event) {
          case DialogManager.Event.OK:
            dialogManager.saveSandbox(true);
            exitRequired = true;
            break;
          case DialogManager.Event.CANCEL:
            finish();
            break;
        }
        break;
      case DialogManager.DialogID.SEND_FAILURE:
        sendSandboxInternal();
        break;
      case DialogManager.DialogID.SBX_OPTIMIZE:
      case DialogManager.DialogID.ADD_MODULE:
      case DialogManager.DialogID.ADD_COLONY:
      case DialogManager.DialogID.ADD_ALL_MODULES:
      case DialogManager.DialogID.ADD_TEXT:
      case DialogManager.DialogID.ADD_ALL_FONT:
      case DialogManager.DialogID.STATION_COPY:
      case DialogManager.DialogID.STATION_DELETE:
        if (event == DialogManager.Event.OK)
          startTask(config);
        break;
    }
  }

  private void sendSandbox() {
    if (Settings.isUseInternalSender()) {
      sendSandboxInternal();
      return;
    }
    MyLog.d(LOG_TAG, "Sending sandbox...");
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("file/*");
    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
      new File(Storage.getSandbox().getFileName())));
    if (Utils.isIntentSase(this, intent)) {
      startActivity(intent);
      MyLog.d(LOG_TAG, "Sandbox sent");
    }
    else {
      MyLog.e(LOG_TAG, "No any external sender found");
      dialogManager.sendSandboxFailure();
    }
  }

  private void sendSandboxInternal() {
    MyLog.d(LOG_TAG, "Saving Sandbox with internal explorer...");
    Intent intent = new Intent(this, DirPickerActivity.class);
    intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.MODE_DIRECTORY);
    startActivityForResult(intent, Request.SBX_SAVE_INTERNAL);
  }

  private void startTask (SbxEditConfig config) {
    task = new SbxEditTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(config);
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    if (actionSave != null)
      actionSave.setVisible(!state);
    if (actionSbxInfo != null)
      actionSbxInfo.setVisible(!state);
    if (actionOptimize != null)
      actionOptimize.setVisible(!state);
    if (actionNav != null)
      actionNav.setVisible(!state);
    if (actionDevTools != null)
      actionDevTools.setVisible(!state);
    if (Settings.isDbLoaded()) {
      if (actionAdd != null)
        actionAdd.setVisible(!state);
      if (actionPartInfo != null)
        actionPartInfo.setVisible(!state);
    }

    if (state) {
      rvStationList.removeAllViewsInLayout();
      sbxProgressBar.setVisibility(ProgressBar.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    }
    else {
      sbxProgressBar.setVisibility(ProgressBar.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  void uiSet() {
    MyLog.d(LOG_TAG, "Setting up UI...");
    adapter.refreshData(Storage.getSandbox());
    if (task.isSuccess()) {
      if (task.isNewFileCreated())
        sendSandbox();
      else if (exitRequired)
        finish();
    }
    updateActionBar();
    MyLog.d(LOG_TAG, "UI setup completed");
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void updateActionBar() {
    if (Settings.isSbxNameInHeader()) {
      ActionBar bar = getSupportActionBar();
      if (bar != null)
        bar.setTitle(Storage.getSandbox().getInfo().getSbxName());
    }
  }

  private static class SavedState {
    private MultiSelectAdapter.State adapterState;
    private SbxEditTask task;
    private int viewMode;
  }
}
