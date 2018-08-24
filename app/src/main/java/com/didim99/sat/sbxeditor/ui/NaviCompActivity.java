package com.didim99.sat.sbxeditor.ui;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils;
import com.didim99.sat.sbxeditor.Sandbox;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.InputValidator;
import com.didim99.sat.sbxeditor.model.NaviCompMarker;
import com.didim99.sat.sbxeditor.model.Planet;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.settings.Settings;
import java.util.ArrayList;

public class NaviCompActivity extends AppCompatActivity
  implements NavListAdapter.EventListener<NaviCompMarker> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NavAct";

  //marker processing modes
  private static final int MODE_ADD_CUSTOM = 1;
  private static final int MODE_EDIT = 2;

  private Resources resources;
  private ArrayList<Planet> planets;
  private ArrayList<String> planetNames;
  private NavListAdapter adapter;
  private ActionMode actionMode;
  private Toast toastMsg;
  private int mode;
  private ArrayList<NaviCompMarker> selected;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "NaviCompActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_sbx_main);
    resources = getResources();
    setupActionBar();

    boolean screenLarge = (resources.getConfiguration()
      .orientation == Configuration.ORIENTATION_LANDSCAPE);

    if (Settings.isDbLoaded()) {
      planets = Storage.getPlanetInfo();
      planetNames = Storage.getPlanetNames();
    }

    MyLog.d(LOG_TAG, "Loading saved instance...");
    NavListAdapter.State adapterState =
      (NavListAdapter.State) getLastCustomNonConfigurationInstance();

    //View components init
    MyLog.d(LOG_TAG, "View components init...");
    adapter = new NavListAdapter(this, getMenuInflater(),
      screenLarge, Storage.getSandbox(), planetNames, this);
    if (adapterState != null) {
      MyLog.d(LOG_TAG, "Selected list found, enable multi-selection mode");
      adapter.initSelection(Storage.getSandbox(), adapterState);
    }
    RecyclerView navList = findViewById(R.id.rvStationList);
    if (screenLarge)
      navList.setLayoutManager(new GridLayoutManager(this, 2));
    else
      navList.setLayoutManager(new LinearLayoutManager(this));
    navList.setHasFixedSize(true);
    navList.setAdapter(adapter);
    toastMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "NaviCompActivity started");
  }

  @Override
  public NavListAdapter.State onRetainCustomNonConfigurationInstance() {
    if (adapter.inMultiSelectionMode())
      return adapter.getState();
    else return null;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_navicomp, menu);

    if (Settings.isDbLoaded()) {
      menu.findItem(R.id.action_addStdMarker).setVisible(true);
      menu.findItem(R.id.action_addAllMarkers).setVisible(true);
    }

    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_addStdMarker:
        MyLog.d(LOG_TAG, "markerAddStd dialog called");
        createDialog(R.string.diaTitle_markerAddStd,
          R.layout.dialog_add_marker_std, addMarkerStd_showListener);
        return true;
      case R.id.action_addAllMarkers:
        markerAddAllStd();
        return true;
      case R.id.action_addCustomMarker:
        mode = MODE_ADD_CUSTOM;
        MyLog.d(LOG_TAG, "markerAddCustom dialog called");
        createDialog(R.string.diaTitle_markerAddCustom,
          R.layout.dialog_add_marker_custom, addMarkerCustom_showListener);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_navInfo:
        markerInfo();
        return true;
      case R.id.action_navEdit:
        markerEdit();
        return true;
      case R.id.action_navDelete:
        markerDelete();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (actionMode != null) {
      actionMode.finish();
      return;
    }

    super.onBackPressed();
  }

  @Override
  public void onItemClick(View view, NaviCompMarker marker) {
    selected = new ArrayList<>(2);
    selected.add(marker);
    openContextMenu(view);
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

  private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      mode.getMenuInflater().inflate(R.menu.am_menu_navicomp, menu);
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
        case R.id.action_navDelete:
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

  private void updateActionMode(int count, boolean allSelected) {
    actionMode.setTitle(getString(R.string.amStationList_selectedCount, count));
    actionMode.getMenu().findItem(R.id.action_selectAll).setVisible(!allSelected);
  }

  private void createDialog(int titleId, int viewId, DialogInterface.OnShowListener listener) {
    LayoutInflater inflater = LayoutInflater.from(this);
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(inflater.inflate(viewId, null));
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(listener);
    MyLog.d(LOG_TAG, "Dialog created");
    dialog.show();
  }

  DialogInterface.OnShowListener addMarkerStd_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddMarkerStd";

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final Spinner markerSelector = dialog.findViewById(R.id.markerSelector);

      markerSelector.setAdapter(new ArrayAdapter<>(
        NaviCompActivity.this, android.R.layout.simple_spinner_item, planetNames));

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          Sandbox sandbox = Storage.getSandbox();
          Planet planet = planets.get(markerSelector.getSelectedItemPosition());
          NaviCompMarker existingMarker = null;

          for (NaviCompMarker marker : sandbox.getNaviComp()) {
            if (marker.getLabel().equals(planet.getLabel())) {
              existingMarker = marker;
              break;
            }
          }

          if (existingMarker != null)
            alreadyExistsDialog(existingMarker, planet);
          else {
            sandbox.addMarker(new NaviCompMarker(planet));
            adapter.refreshData(sandbox);
            toastMsg.setText(R.string.sbxProcessing_markerAdd_success);
            toastMsg.show();
          }

          dialogInterface.dismiss();
        }
      );
    }
  };

  DialogInterface.OnShowListener addMarkerCustom_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddMarkerCustom";
    private boolean isAdvanced;
    private View advancedLayout;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      advancedLayout = dialog.findViewById(R.id.advancedLayout);
      final EditText etLabel = ((AlertDialog) dialog).findViewById(R.id.etLabel);
      final EditText positionX = ((AlertDialog) dialog).findViewById(R.id.etPositionX);
      final EditText positionY = ((AlertDialog) dialog).findViewById(R.id.etPositionY);
      final EditText etObjRadius = ((AlertDialog) dialog).findViewById(R.id.etObjRadius);
      final EditText etOrbRadius = ((AlertDialog) dialog).findViewById(R.id.etOrbRadius);
      final EditText etRescaleRadius = ((AlertDialog) dialog).findViewById(R.id.etRescaleRadius);
      final EditText etScale = ((AlertDialog) dialog).findViewById(R.id.etScale);
      etLabel.setSelection(etLabel.getText().length());

      ((CheckBox) dialog.findViewById(R.id.cbAdvanced))
        .setOnCheckedChangeListener((buttonView, isChecked) -> {
          isAdvanced = isChecked;
          if (isChecked)
            advancedLayout.setVisibility(View.VISIBLE);
          else
            advancedLayout.setVisibility(View.GONE);
        });

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          InputValidator inputValidator = InputValidator.getInstance();
          Float posX, posY, objR = null, orbR = null, rescaleR = null, scale = null;
          String label;

          if (mode == Sandbox.Mode.EDIT)
            isAdvanced = true;

          try {
            label = inputValidator.checkEmptyStr(etLabel,
              R.string.editErr_emptyLabel, "marker label");
            if (label.matches(SBML.INVALID_MARKER_NAME)) {
              toastMsg.setText(R.string.editErr_incorrectLabel);
              toastMsg.show();
              return;
            }

            posX = inputValidator.checkFloat(positionX,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "X-position");
            posY = inputValidator.checkFloat(positionY,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "Y-position");

            if (isAdvanced) {
              objR = inputValidator.checkFloat(etObjRadius, SBML.POSITION_FACTOR,
                R.string.editErr_incorrectObjectRadius, "object radius");
              orbR = inputValidator.checkFloat(etOrbRadius, SBML.POSITION_FACTOR,
                R.string.editErr_incorrectOrbitRadius, "orbit radius");
              rescaleR = inputValidator.checkFloat(etRescaleRadius, SBML.POSITION_FACTOR,
                R.string.editErr_incorrectRescaleRadius, "rescale radius");
              scale = inputValidator.checkFloat(etScale, null,
                R.string.editErr_incorrectScale, "scale");

              if (objR == null && orbR == null && rescaleR == null && scale == null) {
                MyLog.w(LOG_TAG, "Advanced settings is empty");
                toastMsg.setText(R.string.editErr_emptyAdvanced);
                toastMsg.show();
                return;
              }
            }
          } catch (InputValidator.ValidationException e) {
            return;
          }

          float defValue = resources.getInteger(
            R.integer.addMarker_defaultRadius) * SBML.POSITION_FACTOR;
          objR = setDefaultIfNull(objR, defValue);
          orbR = setDefaultIfNull(orbR, defValue);
          rescaleR = setDefaultIfNull(rescaleR, defValue);
          scale = setDefaultIfNull(scale, SBML.MARKER_SCALE);

          Sandbox sandbox = Storage.getSandbox();
          NaviCompMarker newMarker = new NaviCompMarker(
            label, posX, posY, objR, orbR, rescaleR, scale);
          switch (mode) {
            case MODE_ADD_CUSTOM:
              sandbox.addMarker(newMarker);
              toastMsg.setText(R.string.sbxProcessing_markerAdd_success);
              break;
            case MODE_EDIT:
              sandbox.markerReplace(selected.get(0), newMarker);
              toastMsg.setText(R.string.sbxProcessing_markerEdit_success);
              break;
          }
          toastMsg.show();
          adapter.refreshData(sandbox);
          dialogInterface.dismiss();
        }
      );
    }
  };

  private void alreadyExistsDialog(final NaviCompMarker marker, final Planet planet) {
    MyLog.d(LOG_TAG, "alreadyExists dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(getString(R.string.diaTitle_markerExists, planet.getLabel()));
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> {
      Storage.getSandbox().markerReplace(marker, new NaviCompMarker(planet));
      adapter.refreshData(Storage.getSandbox());
      toastMsg.setText(R.string.sbxProcessing_markerReplace_success);
      toastMsg.show();
    });
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "alreadyExists dialog created");
    dialog.show();
  }

  private void markerInfo() {
    MyLog.d(LOG_TAG, "markerInfo dialog called");
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_marker_info, null);

    NaviCompMarker marker = selected.get(0);
    ((TextView) dialogView.findViewById(R.id.tvMarkerLabel))
      .setText(marker.getLabel());
    ((TextView) dialogView.findViewById(R.id.tvPosition))
      .setText(marker.getCenterStr(SBML.PREC_DEFAULT));
    ((TextView) dialogView.findViewById(R.id.tvObjectRadius)).setText(Utils
      .floatToString(marker.getObjectRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((TextView) dialogView.findViewById(R.id.tvOrbitRadius)).setText(Utils
      .floatToString(marker.getOrbitRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((TextView) dialogView.findViewById(R.id.tvRescaleRadius)).setText(Utils
      .floatToString(marker.getRescaleRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((TextView) dialogView.findViewById(R.id.tvScale))
      .setText(Utils.floatToString(marker.getScale(), SBML.PREC_DEFAULT));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.diaTitle_markerInfo);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setView(dialogView);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "markerInfo dialog created");
    dialog.show();
  }

  private void markerEdit() {
    MyLog.d(LOG_TAG, "markerEdit dialog called");
    View dialogView = LayoutInflater.from(this)
      .inflate(R.layout.dialog_add_marker_custom, null);

    NaviCompMarker marker = selected.get(0);
    ((EditText) dialogView.findViewById(R.id.etLabel))
      .setText(marker.getLabel());
    ((EditText) dialogView.findViewById(R.id.etPositionX)).setText(Utils
      .floatToString(marker.getCenterX() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((EditText) dialogView.findViewById(R.id.etPositionY)).setText(Utils
      .floatToString(marker.getCenterY() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((EditText) dialogView.findViewById(R.id.etObjRadius)).setText(Utils
      .floatToString(marker.getObjectRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((EditText) dialogView.findViewById(R.id.etOrbRadius)).setText(Utils
      .floatToString(marker.getOrbitRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((EditText) dialogView.findViewById(R.id.etRescaleRadius)).setText(Utils
      .floatToString(marker.getRescaleRadius() / SBML.POSITION_FACTOR, SBML.PREC_DEFAULT));
    ((EditText) dialogView.findViewById(R.id.etScale))
      .setText(Utils.floatToString(marker.getScale(), SBML.PREC_DEFAULT));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.diaTitle_markerEdit);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(dialogView);
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(addMarkerCustom_showListener);
    MyLog.d(LOG_TAG, "markerEdit dialog created");
    mode = MODE_EDIT;
    dialog.show();
  }

  private void markerDelete() {
    MyLog.d(LOG_TAG, "markerDelete dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    int count = selected.size();
    String label = count > 1 ? "" : selected.get(0).getLabel();
    adb.setTitle(resources.getQuantityString(R.plurals.deleteMarker, count, label));
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> {
      Storage.getSandbox().markerDelete(selected);
      toastMsg.setText(resources.getQuantityString(
        R.plurals.sbxProcessing_markerDelete_success, count));
      toastMsg.show();
      adapter.refreshData(Storage.getSandbox());
    });
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "markerDelete dialog created");
    dialog.show();
  }

  private void markerAddAllStd() {
    MyLog.d(LOG_TAG, "Adding all standard markers");
    ArrayList<NaviCompMarker> naviComp = Storage.getSandbox().getNaviComp();

    int count = planets.size();
    for (Planet planet : planets) {
      boolean exists = false;
      for (NaviCompMarker marker : naviComp) {
        if (planet.getLabel().equals(marker.getLabel())) {
          exists = true;
          count--;
          break;
        }
      }

      if (!exists) {
        NaviCompMarker marker = new NaviCompMarker(planet);
        if (naviComp.size() > planet.getId())
          naviComp.add(planet.getId(), marker);
        else
          naviComp.add(marker);
      }
    }

    if (count > 0) {
      toastMsg.setText(R.string.sbxProcessing_markerAddAll_success);
      adapter.refreshData(Storage.getSandbox());
    } else
      toastMsg.setText(R.string.sbxProcessing_markerAddAll_exists);
    toastMsg.show();
    if (count > 0) Storage.getSandbox().setNaviCompModified();
    MyLog.d(LOG_TAG, "Markers added (" + count + ")");
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private Float setDefaultIfNull(Float oldValue, Float defaultValue) {
    return oldValue == null ? defaultValue : oldValue;
  }
}
