package com.didim99.sat.ui.sbxeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.didim99.sat.event.GlobalEvent;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.network.NetworkManager;
import com.didim99.sat.network.WebAPI;
import com.didim99.sat.core.sbxeditor.Sandbox;
import com.didim99.sat.core.sbxeditor.SbxEditConfig;
import com.didim99.sat.core.sbxeditor.Station;
import com.didim99.sat.core.sbxeditor.Storage;
import com.didim99.sat.core.sbxeditor.utils.InputValidator;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.ui.sbxeditor.view.RatioBar;
import com.didim99.sat.ui.sbxeditor.view.ValueBar;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Dialog manager class
 * Created by didim99 on 01.06.18.
 */
public class DialogManager {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DialogManager";

  private static final DialogManager ourInstance = new DialogManager();
  public static DialogManager getInstance() {
    return ourInstance;
  }

  private static SparseIntArray cargoViewIDs;
  private static final int NUMBER_DECIMAL_SIGNED = InputType.TYPE_CLASS_NUMBER
    | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
  private static final int NUMBER_DECIMAL = InputType.TYPE_CLASS_NUMBER
    | InputType.TYPE_NUMBER_FLAG_DECIMAL;

  static class DialogID {
    static final int EXIT = 1;
    static final int SBX_EDIT = 2;
    static final int SBX_SAVE = 3;
    static final int SBX_OPTIMIZE = 4;
    static final int ADD_MODULE = 5;
    static final int ADD_COLONY = 6;
    static final int ADD_ALL_MODULES = 7;
    static final int ADD_TEXT = 8;
    static final int ADD_ALL_FONT = 9;
    static final int STATION_COPY = 10;
    static final int STATION_DELETE = 11;
    static final int SEND_FAILURE = 12;
  }

  static class Event {
    static final int OK = 1;
    static final int CANCEL = 2;
  }

  private WeakReference<Context> contextRef;
  private LayoutInflater inflater;
  private EventListener listener;
  private UIManager uiManager;
  private InputValidator inputValidator;
  private Resources res;
  private Toast toastMsg;
  private ArrayList<Station> selected;
  private int addModulePID;

  private DialogManager() {
    uiManager = UIManager.getInstance();
    inputValidator = InputValidator.getInstance();
    cargoViewIDs = new SparseIntArray();
    cargoViewIDs.append(SBML.CargoID.O2, R.id.oxygenBar);
    cargoViewIDs.append(SBML.CargoID.CO2, R.id.carbonBar);
    cargoViewIDs.append(SBML.CargoID.H2O, R.id.waterBar);
    cargoViewIDs.append(SBML.CargoID.BAT, R.id.batteryBar);
  }

  public void updateContext(Context context, EventListener listener) {
    this.contextRef = new WeakReference<>(context);
    this.listener = listener;
    toastMsg = Toast.makeText(context, "", Toast.LENGTH_LONG);
    inflater = LayoutInflater.from(context);
    res = context.getResources();
  }

  public void dbDamaged(SAT app) {
    MyLog.d(LOG_TAG, "DBDamaged dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.oops);
    adb.setMessage(R.string.databaseDamaged);
    adb.setPositiveButton(R.string.downloadAgain, (dialog, which) ->
      new DBTask(contextRef.get(), app::onDBTaskEvent, DBTask.Mode.UPDATE).execute());
    adb.setNegativeButton(R.string.notUse, (dialog, which) -> {
      Settings.setDbLoaded(false);
      Settings.setIgnoreDb(true);
      Settings.setHasDB(false);
      app.getEventDispatcher().dispatchGlobalEvent(GlobalEvent.UI_RELOAD);
    });
    if (Settings.isDevMode()) {
     adb.setNeutralButton(R.string.continueAnyway, (dialog, which) ->
       new NetworkManager(WebAPI.LogEvent.DB_USE_DAMAGED).execute());
    }
    adb.setCancelable(false);
    MyLog.d(LOG_TAG, "Dialog created");
    adb.create().show();
  }

  void saveSandbox(boolean exitRequired) {
    MyLog.d(LOG_TAG, "SaveSandbox dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.diaSbxSave_title);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNeutralButton(R.string.dialogButtonCancel, null);
    if (exitRequired)
      adb.setNeutralButton(R.string.dialogButtonCancel, (dialog, which)
        -> listener.onDialogEvent(DialogID.SBX_SAVE, Event.CANCEL, null));
    adb.setView(inflater.inflate(R.layout.dialog_sandbox_save, null));
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(saveDialog_showListener);
    if (exitRequired)
      dialog.setCanceledOnTouchOutside(false);
    MyLog.d(LOG_TAG, "SaveSandbox dialog created");
    dialog.show();
  }

  private DialogInterface.OnShowListener saveDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SaveSandbox";
    private boolean overwrite, compress, selectorActive;
    private int verCode = SBML.VerCode.V21;

    @Override
    public void onShow(final DialogInterface dialog) {
      MyLog.d(LOG_TAG, "Dialog shown");
      CheckBox cbOverwrite = ((AlertDialog) dialog).findViewById(R.id.cbOverwrite);
      CheckBox cbCompress = ((AlertDialog) dialog).findViewById(R.id.cbCompress);
      final View compressLayout = ((AlertDialog) dialog).findViewById(R.id.compressLayout);
      selectorActive =
        (Storage.getSandbox().getInfo().getMinVer() < SBML.VerCode.V22) || Settings.isDevMode();
      if (!Storage.getSandbox().isImported())
        cbOverwrite.setVisibility(View.GONE);
      overwrite = cbOverwrite.isChecked();
      compress = cbCompress.isChecked();

      ((RadioGroup) ((AlertDialog) dialog).findViewById(R.id.verCodeGroup))
        .setOnCheckedChangeListener((radioGroup, currVer) -> {
          switch (currVer) {
            case R.id.verCode20:
              verCode = SBML.VerCode.V20;
              break;
            case R.id.verCode21:
              verCode = SBML.VerCode.V21;
              break;
          }
          MyLog.d(LOG_TAG, "Current version code: " + verCode);
        });

      cbOverwrite.setOnCheckedChangeListener((buttonView, isChecked) -> {
        MyLog.d(LOG_TAG, "Overwrite sandbox: " + isChecked);
        overwrite = isChecked;
      });

      cbCompress.setOnCheckedChangeListener((buttonView, isChecked) -> {
        MyLog.d(LOG_TAG, "Compress sandbox: " + isChecked);
        compress = isChecked;
        if (selectorActive) {
          if (isChecked)
            compressLayout.setVisibility(View.VISIBLE);
          else
            compressLayout.setVisibility(View.GONE);
        }
      });

      ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> {
            dialog.dismiss();
            listener.onDialogEvent(DialogID.SBX_SAVE, Event.OK,
              new SbxEditConfig.Builder(Sandbox.Mode.SAVE)
                .setOverwrite(overwrite).setCompress(compress)
                .setVerCode(verCode).build());
          }
        );
    }
  };

  void sendSandboxFailure() {
    MyLog.d(LOG_TAG, "SendSandboxFailure dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.oops);
    adb.setMessage(R.string.diaTitle_sendFailure);
    adb.setPositiveButton(R.string.dialogButtonNow, (dialog, which) ->
      listener.onDialogEvent(DialogID.SEND_FAILURE, Event.OK, null));
    adb.setNegativeButton(R.string.dialogButtonAlways, (dialog, which) -> {
      Settings.setUseInternalSender(true);
      listener.onDialogEvent(DialogID.SEND_FAILURE, Event.OK, null);
    });
    adb.setCancelable(false);
    MyLog.d(LOG_TAG, "SendSandboxFailure dialog created");
    adb.create().show();
  }

  void sandboxInfo() {
    MyLog.d(LOG_TAG, "SandboxInfo dialog called");
    View dialogView = inflater.inflate(R.layout.dialog_sandbox_info, null);

    Sandbox.Info sbxInfo = Storage.getSandbox().getInfo();
    ((TextView) dialogView.findViewById(R.id.tvSbxName))
      .setText(sbxInfo.getSbxName());
    ((TextView) dialogView.findViewById(R.id.tvSbxUid))
      .setText(sbxInfo.getSbxUid());
    if (Settings.isDbLoaded()) {
      ((TextView) dialogView.findViewById(R.id.tvMinVer))
        .setText(Storage.getSAVerInfo().get(sbxInfo.getMinVer()));
    } else
      ((TextView) dialogView.findViewById(R.id.tvMinVer)).setText(R.string.needDB);
    ((TextView) dialogView.findViewById(R.id.tvModCount))
      .setText(Utils.intToString(sbxInfo.getModulesCount()));
    ((TextView) dialogView.findViewById(R.id.tvObjCount))
      .setText(Utils.intToString(sbxInfo.getObjectsCount()));
    ((TextView) dialogView.findViewById(R.id.tvStationCount))
      .setText(Utils.intToString(sbxInfo.getStationCount()));
    ((TextView) dialogView.findViewById(R.id.tvAloneCount))
      .setText(Utils.intToString(sbxInfo.getAloneCount()));
    ((TextView) dialogView.findViewById(R.id.tvMarkerCount))
      .setText(Utils.intToString(sbxInfo.getMarkerCount()));

    uiManager.setTimeString(sbxInfo.getCreationTime(),
      dialogView.findViewById(R.id.tvCreationTime));

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.mTitle_actionSbxInfo);
    adb.setView(dialogView);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNeutralButton(R.string.dialogButtonEdit, (dialog, which) ->
      createDialog(R.string.actStart_btnText_sbxEdit,
        R.layout.dialog_sandbox_create, sbxEditDialog_showListener)
    );
    MyLog.d(LOG_TAG, "SandboxInfo dialog created");
    adb.create().show();
  }

  private DialogInterface.OnShowListener sbxEditDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_sbxEdit";

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final EditText sbxName = dialog.findViewById(R.id.etSbxName);
      final EditText sbxUid = dialog.findViewById(R.id.etSbxUid);
      final Sandbox.Info info = Storage.getSandbox().getInfo();
      sbxName.setText(info.getSbxName());
      sbxName.setSelection(sbxName.getText().length());
      if (info.getSbxUid() != null) {
        sbxUid.setText(info.getSbxUid());
        sbxUid.setSelection(sbxUid.getText().length());
      }

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          String name = sbxName.getText().toString();
          if (!inputValidator.checkSbxName(name, false))
            return;

          Integer uid;
          try {
            uid = inputValidator.checkInteger(sbxUid, 0,
              0, R.string.editErr_incorrectUID, "UID");
          } catch (InputValidator.ValidationException e) {
            return;
          }

          info.setSbxUid(uid);
          info.setSbxName(name);
          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.SBX_EDIT, Event.OK, null);
        }
      );
    }
  };

  void stationInfo(Station station) {
    MyLog.d(LOG_TAG, "StationInfo dialog called");
    View dialogView = inflater.inflate(R.layout.dialog_station_info, null);

    Station.Info staInfo = station.getInfo();
    if (staInfo.getObjType() == Station.Type.COLONY) {
      ((TextView) dialogView.findViewById(R.id.tvPosition))
        .setText(R.string.diaSbxInfo_planet);
      ((TextView) dialogView.findViewById(R.id.tvCenterPos))
        .setText(staInfo.getNearestMarker());
    } else {
      ((TextView) dialogView.findViewById(R.id.tvCenterPos))
        .setText(staInfo.getCenterPosStr(2));
    }
    ((TextView) dialogView.findViewById(R.id.tvSaveIdRange))
      .setText(staInfo.getSaveIdRange());
    ((TextView) dialogView.findViewById(R.id.tvSize))
      .setText(staInfo.getSizeStr(2));
    ((TextView) dialogView.findViewById(R.id.tvModCount))
      .setText(Utils.intToString(staInfo.getSize()));
    if (Settings.isDbLoaded()) {
      ((TextView) dialogView.findViewById(R.id.tvMinVer))
        .setText(Storage.getSAVerInfo().get(staInfo.getMinVer()));
    } else
      ((TextView) dialogView.findViewById(R.id.tvMinVer)).setText(R.string.needDB);

    uiManager.setTimeString(staInfo.getLaunchTimestamp(),
      dialogView.findViewById(R.id.tvLaunchTime));

    ImageView ivMovement = dialogView.findViewById(R.id.ivMovement);
    TextView tvMovement = dialogView.findViewById(R.id.tvMovement);
    ImageView ivRotation = dialogView.findViewById(R.id.ivRotation);
    TextView tvRotation = dialogView.findViewById(R.id.tvRotation);

    if (staInfo.hasMovement()) {
      uiManager.setMovementIcon(ivMovement, staInfo);
      tvMovement.setText(Utils.floatToString(
        staInfo.getMovementSpeed(), SBML.PREC_DEFAULT));
      if (!staInfo.hasRotation())
        ivRotation.setVisibility(View.GONE);
    } else {
      tvMovement.setVisibility(View.GONE);
      ivMovement.setVisibility(View.GONE);
    }

    if (staInfo.hasRotation()) {
      uiManager.setRotationIcon(ivRotation, staInfo);
      tvRotation.setText(Utils.floatToString(
        Math.abs(staInfo.getRotationSpeed()), SBML.PREC_DEFAULT));
    } else {
      tvRotation.setVisibility(View.GONE);
    }

    int titleId = 0;
    switch (staInfo.getObjType()) {
      case Station.Type.STATION: titleId = R.string.diaTitle_staInfo; break;
      case Station.Type.COLONY: titleId = R.string.diaTitle_colonyInfo; break;
      case Station.Type.GROUP: titleId = R.string.diaTitle_groupInfo; break;
      case Station.Type.TEXT: titleId = R.string.diaTitle_textInfo; break;
    }

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(titleId);
    adb.setView(dialogView);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    if (Settings.isDbLoaded()) {
      adb.setNeutralButton(R.string.dialogButtonStat,
        (dialog, which) -> StationStatDialog(station));
    }

    MyLog.d(LOG_TAG, "StationInfo dialog created");
    adb.create().show();
  }

  private void StationStatDialog(Station station) {
    MyLog.d(LOG_TAG, "StationStat dialog called");
    View dialogView = inflater.inflate(R.layout.dialog_station_stat, null);
    Station.Statistics stat = station.getStat();

    ((RatioBar) dialogView.findViewById(R.id.powerTotalBar))
      .setValueInteger(stat.getPowerGen(), stat.getPowerUse());
    ((ValueBar) dialogView.findViewById(R.id.mainFuelBar))
      .setValueDouble(stat.getMainFuelCap(), stat.getMainFuelVal());
    ((ValueBar) dialogView.findViewById(R.id.thrFuelBar))
      .setValueDouble(stat.getThrFuelCap(), stat.getThrFuelVal());
    ((ValueBar) dialogView.findViewById(R.id.cargoTotalBar))
      .setValueInteger(stat.getCargoTotal(), stat.getCargoUsed());

    Station.ResourceState resState;
    SparseArray<Station.ResourceState> resStates = stat.getResState();
    for (int i = 0; i < cargoViewIDs.size(); i++) {
      if ((resState = resStates.get(cargoViewIDs.keyAt(i))) != null) {
        ((ValueBar) dialogView.findViewById(cargoViewIDs.valueAt(i)))
          .setValueDouble(resState.getTotal(), resState.getUsed());
      } else {
        ((ValueBar) dialogView.findViewById(cargoViewIDs.valueAt(i)))
          .setValueInteger(0, 0);
      }
    }

    RecyclerView rvPartList = dialogView.findViewById(R.id.rvPartList);
    rvPartList.setLayoutManager(new LinearLayoutManager(contextRef.get()));
    rvPartList.setAdapter(new PartStatAdapter(contextRef.get(), stat.getPartCount()));
    rvPartList.setHasFixedSize(true);

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.dialogButtonStat);
    adb.setView(dialogView);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    MyLog.d(LOG_TAG, "StationInfo dialog created");
    adb.create().show();
  }

  void createDialog(int dialogId) {
    int titleId = 0, viewId = 0;
    DialogInterface.OnShowListener listener = null;

    switch (dialogId) {
      case DialogID.SBX_OPTIMIZE:
        titleId = R.string.mTitle_actionOptimize;
        viewId = R.layout.dialog_optimize;
        listener = optimizeDialog_showListener;
        break;
      case DialogID.ADD_MODULE:
        MyLog.d(LOG_TAG, "AddModule dialog called");
        titleId = R.string.diaTitle_addModule;
        viewId = R.layout.dialog_add_module;
        listener = addModuleDialog_showListener;
        break;
      case DialogID.ADD_COLONY:
        MyLog.d(LOG_TAG, "AddColony dialog called");
        titleId = R.string.diaTitle_addColony;
        viewId = R.layout.dialog_add_colony;
        listener = addColonyDialog_showListener;
        break;
      case DialogID.ADD_ALL_MODULES:
        MyLog.d(LOG_TAG, "AddAll dialog called");
        titleId = R.string.diaTitle_addAll;
        viewId = R.layout.dialog_add_all_modules;
        listener = addAllDialog_showListener;
        break;
      case DialogID.ADD_TEXT:
        MyLog.d(LOG_TAG, "AddText dialog called");
        titleId = R.string.diaTitle_addText;
        viewId = R.layout.dialog_add_text;
        listener = addTextDialog_showListener;
        break;
      case DialogID.ADD_ALL_FONT:
        MyLog.d(LOG_TAG, "AddAllFont dialog called");
        titleId = R.string.diaTitle_addAllFont;
        viewId = R.layout.dialog_add_all_font;
        listener = addAllFontDialog_showListener;
        break;
    }

    createDialog(titleId, viewId, listener);
  }

  private void createDialog(int titleId, int viewId, DialogInterface.OnShowListener listener) {
    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(titleId);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(inflater.inflate(viewId, null));
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(listener);
    MyLog.d(LOG_TAG, "Dialog created");
    dialog.show();
  }

  void addModuleDialog(int partId) {
    addModulePID = partId;
    createDialog(DialogID.ADD_MODULE);
  }

  void addColonyDialog(int partId) {
    addModulePID = partId;
    createDialog(DialogID.ADD_COLONY);
  }

  private DialogInterface.OnShowListener addModuleDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddModule";
    private boolean isMultiple, isAdvanced;
    private View countLayout, advancedLayout;
    private TextView tvEnterPosition;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      countLayout = dialog.findViewById(R.id.countLayout);
      advancedLayout = dialog.findViewById(R.id.advancedLayout);
      tvEnterPosition = dialog.findViewById(R.id.tvEnterPosition);
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      final EditText quantity = dialog.findViewById(R.id.etCount);
      final EditText etOffset = dialog.findViewById(R.id.etDistance);
      final EditText etInLine = dialog.findViewById(R.id.etInLine);
      ((CheckBox) dialog.findViewById(R.id.cbMultiple)).setOnCheckedChangeListener(cbListener);
      ((CheckBox) dialog.findViewById(R.id.cbAdvanced)).setOnCheckedChangeListener(cbListener);
      ((TextView) dialog.findViewById(R.id.tvModName))
        .setText(Storage.getPartInfo().get(addModulePID).getPartName());

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          Float posX, posY, offset = null;
          Integer count = 1, inLine = null;

          try {
            posX = inputValidator.checkFloat(positionX,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "X-position");
            posY = inputValidator.checkFloat(positionY,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "Y-position");
          } catch (InputValidator.ValidationException e) {
            return;
          }

          if (isMultiple) {
            try {
              count = inputValidator.checkInteger(quantity, 2,
                0, R.string.editErr_incorrectCount, "modules count");
            } catch (InputValidator.ValidationException e) {
              return;
            }

            if (isAdvanced) {
              try {
                offset = inputValidator.checkFloat(etOffset, SBML.POSITION_FACTOR,
                  R.string.editErr_incorrectDistance, "offset");
                inLine = inputValidator.checkInteger(etInLine, 1,
                  0, R.string.editErr_incorrectInLine, "line length");

                if (offset == null && inLine == null) {
                  MyLog.w(LOG_TAG, "Advanced settings is empty");
                  toastMsg.setText(R.string.editErr_emptyAdvanced);
                  toastMsg.show();
                  return;
                }
              } catch (InputValidator.ValidationException e) {
                return;
              }
            }

            if (count == null)
              count = res.getInteger(R.integer.addModule_defaultQuantity);
          }

          if (offset == null) {
            offset = (float) (res.getInteger(
              R.integer.addModule_defaultOffset) * SBML.POSITION_FACTOR);
          }
          if (inLine == null)
            inLine = res.getInteger(R.integer.addModule_defaultInLine);

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_MODULE, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.ADD_MODULE)
              .setPartId(addModulePID).setPositionX(posX).setPositionY(posY)
              .setCount(count).setOffset(offset).setInLine(inLine).build());
        }
      );
    }

    private CompoundButton.OnCheckedChangeListener cbListener
      = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        switch (button.getId()) {
          case R.id.cbMultiple:
            isMultiple = isChecked;
            if (isChecked) {
              countLayout.setVisibility(View.VISIBLE);
              tvEnterPosition.setText(R.string.diaAddModule_enterPositionFirst);
            } else {
              countLayout.setVisibility(View.GONE);
              tvEnterPosition.setText(R.string.diaAddModule_enterPosition);
            }
            break;
          case R.id.cbAdvanced:
            isAdvanced = isChecked;
            if (isChecked)
              advancedLayout.setVisibility(View.VISIBLE);
            else
              advancedLayout.setVisibility(View.GONE);
            break;
        }
      }
    };
  };

  private DialogInterface.OnShowListener addColonyDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddColony";
    private SBML.DistanceUnit hUnits = SBML.DistanceUnit.PERCENT;
    private boolean checkSpeed = false;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      ((TextView) dialog.findViewById(R.id.tvModName))
        .setText(Storage.getPartInfo().get(addModulePID).getPartName());
      final View speedLayout = dialog.findViewById(R.id.speedLayout);
      final Spinner planetSelector = dialog.findViewById(R.id.planetSelector);
      final Spinner stateSelector = dialog.findViewById(R.id.stateSelector);
      final EditText etCount = dialog.findViewById(R.id.etModCount);
      final EditText etHeight = dialog.findViewById(R.id.etOrbHeight);
      final EditText etGap = dialog.findViewById(R.id.etModGap);
      final EditText etRotate = dialog.findViewById(R.id.etModRotate);
      final EditText etSpeed = dialog.findViewById(R.id.etModSpeed);

      planetSelector.setAdapter(new ArrayAdapter<>(contextRef.get(),
        android.R.layout.simple_spinner_item, Storage.getPlanetNames()));
      planetSelector.setSelection(res.getInteger(
        R.integer.addColony_defaultPlanet));
      stateSelector.setSelection(res.getInteger(
        R.integer.addColony_defaultState));

      stateSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          if (SBML.ORBITAL_STATES_COLONY[position] == SBML.OrbitalState.ORBITING) {
            speedLayout.setVisibility(View.VISIBLE);
            checkSpeed = true;
          } else {
            speedLayout.setVisibility(View.GONE);
            checkSpeed = false;
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
      });

      (dialog.findViewById(R.id.heightUnits)).setOnClickListener(v -> {
        switch (hUnits) {
          case NCU:
            hUnits = SBML.DistanceUnit.PERCENT;
            ((TextView) v).setText(R.string.valuePercent);
            etHeight.setHint(String.valueOf(res.getInteger(
              R.integer.addColony_defaultHeightPercent)));
            etHeight.setInputType(NUMBER_DECIMAL_SIGNED);
            break;
          case PERCENT:
            hUnits = SBML.DistanceUnit.NCU;
            ((TextView) v).setText(R.string.valueNCU);
            etHeight.setInputType(NUMBER_DECIMAL);
            etHeight.setHint(null);
            break;
        }
      });

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          int planetId = planetSelector.getSelectedItemPosition();
          int state = SBML.ORBITAL_STATES_COLONY[stateSelector.getSelectedItemPosition()];
          Float orbHeight, gap, rotate, speed = 0f;
          Integer count;

          int heightEmptyRes = 0;
          Number heightFactor = null, heightMin = -100;
          if (hUnits == SBML.DistanceUnit.NCU) {
            heightEmptyRes = R.string.editErr_emptyOrbHeight;
            heightFactor = SBML.POSITION_FACTOR;
            heightMin = 0;
          }

          try {
            count = inputValidator.checkInteger(etCount, 2,
              0, R.string.editErr_incorrectCount, "modules count");
            orbHeight = inputValidator.checkFloat(etHeight,
              heightFactor, heightMin, null, heightEmptyRes,
              R.string.editErr_incorrectOrbHeight, "orbit height");
            gap = inputValidator.checkFloat(etGap, null,
              0, 360, 0,
              R.string.editErr_incorrectModGap, "modules gap");
            rotate = inputValidator.checkFloat(etRotate, null,
              -360, 360, 0,
              R.string.actSbxEdit_rotate_incorrectAngle, "modules rotate");
            if (checkSpeed) {
              speed = inputValidator.checkFloat(etSpeed, null,
                R.string.actSbxEdit_movement_speed_incorrect, "modules speed");
              if (speed == null)
                speed = (float) res.getInteger(R.integer.addColony_defaultSpeed);
            }

            if (count == null)
              count = res.getInteger(R.integer.addColony_defaultCount);
            if (orbHeight == null)
              orbHeight = (float) res.getInteger(R.integer.addColony_defaultHeightPercent);
            if (rotate == null)
              rotate = (float) res.getInteger(R.integer.addColony_defaultRotate);
          } catch (InputValidator.ValidationException e) {
            return;
          }

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_COLONY, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.ADD_COLONY).setPlanetId(planetId)
              .setPartId(addModulePID).setOrbitalState(state).setCount(count)
              .setOrbHeight(orbHeight).setUnits(hUnits).setGap(gap)
              .setPositionAngle(rotate).setMovementSpeed(speed).build());
        }
      );
    }
  };

  private DialogInterface.OnShowListener addAllDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddAll";

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final Spinner verSelector = dialog.findViewById(R.id.verSelector);
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      verSelector.setAdapter(new ArrayAdapter<>(contextRef.get(),
        android.R.layout.simple_spinner_item, Storage.getSAVerNames()));
      positionX.setHint(String.valueOf(res.getInteger(R.integer.addAll_defaultPosX)));
      positionY.setHint(String.valueOf(res.getInteger(R.integer.addAll_defaultPosY)));

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          int verCode = Storage.getSAVerInfo().keyAt(verSelector.getSelectedItemPosition());
          Float posX, posY;

          try {
            posX = inputValidator.checkFloat(positionX, SBML.POSITION_FACTOR,
              R.string.editErr_incorrectPosition, "X-position");
            posY = inputValidator.checkFloat(positionY, SBML.POSITION_FACTOR,
              R.string.editErr_incorrectPosition, "Y-position");
          } catch (InputValidator.ValidationException e) {
            return;
          }

          if (posX == null) {
            posX = (float) (res.getInteger(
              R.integer.addAll_defaultPosX) * SBML.POSITION_FACTOR);
          }
          if (posY == null) {
            posY = (float) (res.getInteger(
              R.integer.addAll_defaultPosY) * SBML.POSITION_FACTOR);
          }

          float offset = res.getInteger(R.integer.addModule_defaultOffset) * SBML.POSITION_FACTOR;
          int inLine = res.getInteger(R.integer.addModule_defaultInLine);
          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_ALL_MODULES, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.ADD_ALL)
              .setVerCode(verCode).setPositionX(posX).setPositionY(posY)
              .setOffset(offset).setInLine(inLine).build());
        });
    }
  };

  private DialogInterface.OnShowListener addTextDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddText";
    private boolean isAdvanced;
    private View advancedLayout;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      advancedLayout = dialog.findViewById(R.id.advancedLayout);
      final Spinner alignSelector = dialog.findViewById(R.id.alignSelector);
      final EditText etText = dialog.findViewById(R.id.etText);
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      final EditText etMargin = dialog.findViewById(R.id.etMargin);

      ((CheckBox) dialog.findViewById(R.id.cbAdvanced)).setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked)
            advancedLayout.setVisibility(View.VISIBLE);
          else
            advancedLayout.setVisibility(View.GONE);
          isAdvanced = isChecked;
        }
      );

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          int align = alignSelector.getSelectedItemPosition();
          Float posX, posY;
          Integer margin;
          String text;

          try {
            text = inputValidator.checkEmptyStr(etText,
              R.string.editErr_emptyText, "text");
            posX = inputValidator.checkFloat(positionX,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "X-position");
            posY = inputValidator.checkFloat(positionY,
              SBML.POSITION_FACTOR, R.string.editErr_emptyPosition,
              R.string.editErr_incorrectPosition, "Y-position");
            margin = inputValidator.checkInteger(etMargin,
              (Settings.isDevMode() ? null : 1),
              (isAdvanced ? R.string.editErr_emptyMargin : 0),
              R.string.editErr_incorrectMargin, "margin");
          } catch (InputValidator.ValidationException e) {
            return;
          }

          if (margin == null)
            margin = res.getInteger(R.integer.addText_defaultMargin);

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_TEXT, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.ADD_TEXT)
              .setPositionX(posX).setPositionY(posY).setText(text)
              .setAlign(align).setMargin(margin).build());
        });
    }
  };

  private DialogInterface.OnShowListener addAllFontDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddAllFont";

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      final EditText etInLine = dialog.findViewById(R.id.etInLine);
      positionX.setHint(String.valueOf(res.getInteger(R.integer.addAll_defaultPosX)));
      positionY.setHint(String.valueOf(res.getInteger(R.integer.addAll_defaultPosY)));

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          Float posX, posY;
          Integer inLine;

          try {
            posX = inputValidator.checkFloat(positionX, SBML.POSITION_FACTOR,
              R.string.editErr_incorrectPosition, "X-position");
            posY = inputValidator.checkFloat(positionY, SBML.POSITION_FACTOR,
              R.string.editErr_incorrectPosition, "Y-position");
            inLine = inputValidator.checkInteger(etInLine, 1, 0,
              R.string.editErr_incorrectInLineFont, "line length");

          } catch (InputValidator.ValidationException e) {
            return;
          }

          if (posX == null) {
            posX = (float) (res.getInteger(
              R.integer.addAll_defaultPosX) * SBML.POSITION_FACTOR);
          }
          if (posY == null) {
            posY = (float) (res.getInteger(
              R.integer.addAll_defaultPosY) * SBML.POSITION_FACTOR);
          }
          if (inLine == null)
            inLine = res.getInteger(R.integer.addAllFont_defaultInLine);

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_ALL_FONT, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.ADD_ALL_FONT)
              .setPositionX(posX).setPositionY(posY)
              .setInLine(inLine).build());
        }
      );
    }
  };

  private DialogInterface.OnShowListener optimizeDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Optimize";
    private boolean optSaveId, refreshCargo, refreshFuel;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      AlertDialog dialog = (AlertDialog) dialogInterface;

      optSaveId = Settings.isSbxOptSID();
      refreshCargo = Settings.isSbxOptCargo();
      refreshFuel = Settings.isSbxOptFuel();

      CheckBox cbOptSaveId = dialog.findViewById(R.id.optSaveId);
      CheckBox cbRefreshCargo = dialog.findViewById(R.id.refreshCargo);
      CheckBox cbRefreshFuel = dialog.findViewById(R.id.refreshFuel);
      cbOptSaveId.setChecked(optSaveId);
      cbRefreshCargo.setChecked(refreshCargo);
      cbRefreshFuel.setChecked(refreshFuel);
      cbOptSaveId.setOnCheckedChangeListener(cbListener);
      cbRefreshCargo.setOnCheckedChangeListener(cbListener);
      cbRefreshFuel.setOnCheckedChangeListener(cbListener);

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          if (!(optSaveId | refreshCargo | refreshFuel)) {
            MyLog.w(LOG_TAG, "Incorrect optimization parameters");
            toastMsg.setText(R.string.editErr_incorrectOptParams);
            toastMsg.show();
            return;
          }

          Settings.setSbxOptSID(optSaveId);
          Settings.setSbxOptCargo(refreshCargo);
          Settings.setSbxOptFuel(refreshFuel);

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.SBX_OPTIMIZE, Event.OK,
            new SbxEditConfig.Builder(Sandbox.Mode.OPTIMIZE).setOptSaveId(optSaveId)
              .setRefreshCargo(refreshCargo).setRefreshFuel(refreshFuel).build());
        }
      );
    }

    private CompoundButton.OnCheckedChangeListener cbListener
      = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
          case R.id.optSaveId: optSaveId = isChecked; break;
          case R.id.refreshCargo: refreshCargo = isChecked; break;
          case R.id.refreshFuel: refreshFuel = isChecked; break;
        }
      }
    };
  };

  void stationCopy(ArrayList<Station> stations) {
    MyLog.d(LOG_TAG, "StationCopy dialog called");
    selected = stations;

    int titleId = 0;
    switch (Station.getObjType(stations)) {
      case Station.Type.MULTIPLE_OBJECTS: titleId = R.string.diaTitle_multipleCopy; break;
      case Station.Type.STATION: titleId = R.string.diaTitle_staCopy; break;
      case Station.Type.COLONY: titleId = R.string.diaTitle_colonyCopy; break;
      case Station.Type.GROUP: titleId = R.string.diaTitle_groupCopy; break;
      case Station.Type.TEXT: titleId = R.string.diaTitle_textCopy; break;
    }

    createDialog(titleId, R.layout.dialog_station_copy, staCopyDialog_showListener);
  }

  private DialogInterface.OnShowListener staCopyDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StationCopy";
    private int movementMode = Station.MOVE_MODE_OFFSET;

    @Override
    public void onShow(final DialogInterface dialog) {
      MyLog.d(LOG_TAG, "Dialog shown");
      final EditText positionX = ((AlertDialog) dialog).findViewById(R.id.etPositionX);
      final EditText positionY = ((AlertDialog) dialog).findViewById(R.id.etPositionY);
      final TextView enterOffset = ((AlertDialog) dialog).findViewById(R.id.tvEnterOffset);
      RadioGroup modeGroup = ((AlertDialog) dialog).findViewById(R.id.moveModeGroup);

      modeGroup.setOnCheckedChangeListener((radioGroup, currVer) -> {
        switch (currVer) {
          case R.id.modeOffset:
            movementMode = Station.MOVE_MODE_OFFSET;
            enterOffset.setText(R.string.diaStaCopy_tvText_enterOffset);
            break;
          case R.id.modeNewCenter:
            movementMode = Station.MOVE_MODE_NEW_CENTER;
            enterOffset.setText(R.string.diaStaCopy_tvText_enterCenterPos);
            break;
        }
      });

      ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(
          view -> {
            Float deltaX, deltaY;

            try {
              deltaX = inputValidator.checkFloat(positionX,
                SBML.POSITION_FACTOR, R.string.editErr_emptyOffset,
                R.string.editErr_incorrectOffset, "X-offset");
              deltaY = inputValidator.checkFloat(positionY,
                SBML.POSITION_FACTOR, R.string.editErr_emptyOffset,
                R.string.editErr_incorrectOffset, "Y-offset");
            } catch (InputValidator.ValidationException e) {
              return;
            }

            dialog.dismiss();
            listener.onDialogEvent(DialogID.STATION_COPY, Event.OK,
              new SbxEditConfig.Builder(Sandbox.Mode.COPY)
                .setStations(selected).setPositionMode(movementMode)
                .setPositionX(deltaX).setPositionY(deltaY).build());
          }
        );
    }
  };

  void stationDelete(ArrayList<Station> stations) {
    MyLog.d(LOG_TAG, "stationDelete dialog called");

    int titleId = 0;
    switch (Station.getObjType(stations)) {
      case Station.Type.MULTIPLE_OBJECTS: titleId = R.string.deleteMultiple; break;
      case Station.Type.STATION: titleId = R.string.deleteStation; break;
      case Station.Type.COLONY: titleId = R.string.deleteColony; break;
      case Station.Type.GROUP: titleId = R.string.deleteGroup; break;
      case Station.Type.TEXT: titleId = R.string.deleteText; break;
    }

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(titleId);
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which)
      -> listener.onDialogEvent(DialogID.STATION_DELETE, Event.OK,
        new SbxEditConfig.Builder(Sandbox.Mode.DELETE).setStations(stations).build()));
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "stationDelete dialog created");
    dialog.show();
  }

  void exitDialog() {
    MyLog.d(LOG_TAG, "Exit dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(R.string.dialogButtonExit);
    adb.setMessage(R.string.saveBeforeExit);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setNeutralButton(R.string.dialogButtonExit, (dialog, which)
      -> listener.onDialogEvent(DialogID.EXIT, Event.CANCEL, null));
    adb.setPositiveButton(R.string.dialogButtonSave, (dialog, which)
      -> listener.onDialogEvent(DialogID.EXIT, Event.OK, null));
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "Exit dialog created");
    dialog.show();
  }

  interface EventListener {
    void onDialogEvent(int dialog, int event, SbxEditConfig config);
  }
}
