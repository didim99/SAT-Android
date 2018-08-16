package com.didim99.sat.sbxeditor.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.Utils;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.network.NetworkManager;
import com.didim99.sat.network.WebAPI;
import com.didim99.sat.sbxeditor.Sandbox;
import com.didim99.sat.sbxeditor.SbxEditConfig;
import com.didim99.sat.sbxeditor.Station;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.InputValidator;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.settings.Settings;
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

  static class DialogID {
    static final int EXIT = 1;
    static final int SBX_EDIT = 2;
    static final int SBX_SAVE = 3;
    static final int SBX_OPTIMIZE = 4;
    static final int ADD_MODULE = 5;
    static final int ADD_ALL_MODULES = 6;
    static final int ADD_TEXT = 7;
    static final int ADD_ALL_FONT = 8;
    static final int STATION_COPY = 9;
    static final int STATION_DELETE = 10;
    static final int SEND_FAILURE = 11;
  }

  static class Event {
    static final int OK = 1;
    static final int CANCEL = 2;
  }

  private WeakReference<Context> contextRef;
  private LayoutInflater inflater;
  private EventListener listener;
  private UIManager uiManager;
  private Resources res;
  private Toast toastMsg;
  private ArrayList<Station> selected;
  private int addModulePID;

  private DialogManager() {
    uiManager = UIManager.getInstance();
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
      new DBTask(contextRef.get(), app, DBTask.Mode.UPDATE).execute());
    adb.setNegativeButton(R.string.notUse, (dialog, which) -> {
      Settings.setDbLoaded(false);
      Settings.setIgnoreDb(true);
      Settings.setHasDB(false);
      app.dispatchGlobalEvent(SAT.GlobalEvent.UI_RELOAD);
    });
    if (Settings.isDevMode()) {
     adb.setNeutralButton(R.string.continueAnyway, (dialog, which) ->
       new NetworkManager(WebAPI.LOG_EVENT_DB_USE_DAMAGED).execute());
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
    private int verCode = SBML.VER_CODE_21;

    @Override
    public void onShow(final DialogInterface dialog) {
      MyLog.d(LOG_TAG, "Dialog shown");
      CheckBox cbOverwrite = ((AlertDialog) dialog).findViewById(R.id.cbOverwrite);
      CheckBox cbCompress = ((AlertDialog) dialog).findViewById(R.id.cbCompress);
      final View compressLayout = ((AlertDialog) dialog).findViewById(R.id.compressLayout);
      selectorActive =
        (Storage.getSandbox().getInfo().getMinVer() < SBML.VER_CODE_22) || Settings.isDevMode();
      if (!Storage.getSandbox().isImported())
        cbOverwrite.setVisibility(View.GONE);
      overwrite = cbOverwrite.isChecked();
      compress = cbCompress.isChecked();

      ((RadioGroup) ((AlertDialog) dialog).findViewById(R.id.verCodeGroup))
        .setOnCheckedChangeListener((radioGroup, currVer) -> {
          switch (currVer) {
            case R.id.verCode20:
              verCode = SBML.VER_CODE_20;
              break;
            case R.id.verCode21:
              verCode = SBML.VER_CODE_21;
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
              new SbxEditConfig(Sandbox.Mode.SAVE, overwrite, compress, verCode));
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
          String uidStr = sbxUid.getText().toString();

          if (!InputValidator.checkSbxName(name, false))
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
    ((TextView) dialogView.findViewById(R.id.tvSaveIdRange))
      .setText(staInfo.getSaveIdRange());
    ((TextView) dialogView.findViewById(R.id.tvCenterPos))
      .setText(staInfo.getCenterPosStr(2));
    ((TextView) dialogView.findViewById(R.id.tvSize))
      .setText(staInfo.getSizeStr(2));
    ((TextView) dialogView.findViewById(R.id.tvModCount))
      .setText(Utils.intToString(staInfo.getSize()));
    if (Settings.isDbLoaded()) {
      ((TextView) dialogView.findViewById(R.id.tvMinVer))
        .setText(Storage.getSAVerInfo().get(staInfo.getMinVer()));
    } else
      ((TextView) dialogView.findViewById(R.id.tvMinVer)).setText(R.string.needDB);

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
      case Station.Type.GROUP: titleId = R.string.diaTitle_groupInfo; break;
      case Station.Type.TEXT: titleId = R.string.diaTitle_textInfo; break;
    }

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(titleId);
    adb.setView(dialogView);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "StationInfo dialog created");
    dialog.show();
  }

  void createDialog(int dialog) {
    int titleId = 0, viewId = 0;
    DialogInterface.OnShowListener listener = null;

    switch (dialog) {
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
          float posX, posY, offset = 0;
          int count = 1, inLine = 0;
          String strPosX = positionX.getText().toString();
          String strPosY = positionY.getText().toString();
          if (strPosX.isEmpty() || strPosY.isEmpty()) {
            MyLog.w(LOG_TAG, "Position is empty");
            toastMsg.setText(R.string.editErr_emptyPosition);
            toastMsg.show();
            return;
          }
          try {
            posX = (float) ((Double.parseDouble(strPosX) * SBML.POSITION_FACTOR));
            posY = (float) ((Double.parseDouble(strPosY) * SBML.POSITION_FACTOR));
          } catch (NumberFormatException e) {
            MyLog.w(LOG_TAG, "Incorrect position");
            toastMsg.setText(R.string.editErr_incorrectPosition);
            toastMsg.show();
            return;
          }

          if (isMultiple) {
            offset = res.getInteger(R.integer.addModule_defaultOffset) * SBML.POSITION_FACTOR;
            inLine = res.getInteger(R.integer.addModule_defaultInLine);
            String strCount = quantity.getText().toString();
            if (strCount.isEmpty()) {
              count = res.getInteger(R.integer.addModule_defaultQuantity);
            } else {
              try {
                count = Integer.parseInt(strCount);
                if (count < 2)
                  throw new NumberFormatException();
              } catch (NumberFormatException e) {
                MyLog.w(LOG_TAG, "Incorrect count");
                toastMsg.setText(R.string.editErr_incorrectCount);
                toastMsg.show();
                return;
              }
            }

            if (isAdvanced) {
              String strOffset = etOffset.getText().toString();
              String strInLine = etInLine.getText().toString();
              if (strOffset.isEmpty() && strInLine.isEmpty()) {
                MyLog.w(LOG_TAG, "Advanced settings is empty");
                toastMsg.setText(R.string.editErr_emptyAdvanced);
                toastMsg.show();
                return;
              }

              if (!strOffset.isEmpty()){
                try {
                  offset = (float) ((Double.parseDouble(strOffset) * SBML.POSITION_FACTOR));
                } catch (NumberFormatException e) {
                  MyLog.w(LOG_TAG, "Incorrect offset");
                  toastMsg.setText(R.string.editErr_incorrectDistance);
                  toastMsg.show();
                  return;
                }
              }

              if (!strInLine.isEmpty()) {
                try {
                  inLine = Integer.parseInt(strInLine);
                  if (inLine < 1)
                    throw new NumberFormatException();
                } catch (NumberFormatException e) {
                  MyLog.w(LOG_TAG, "Incorrect line length");
                  toastMsg.setText(R.string.editErr_incorrectInLine);
                  toastMsg.show();
                  return;
                }
              }
            }
          }

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_MODULE, Event.OK,
            new SbxEditConfig(Sandbox.Mode.ADD_MODULE,
              addModulePID, posX, posY, count, offset, inLine));
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

  private DialogInterface.OnShowListener addAllDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddAll";
    private float posX, posY;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      posX = res.getInteger(R.integer.addAll_defaultPosX);
      posY = res.getInteger(R.integer.addAll_defaultPosY);
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final Spinner verSelector = dialog.findViewById(R.id.verSelector);
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      verSelector.setAdapter(new ArrayAdapter<>(contextRef.get(),
        android.R.layout.simple_spinner_item, Storage.getSAVerNames()));
      positionX.setHint(String.valueOf((int) posX));
      positionY.setHint(String.valueOf((int) posY));
      posX *= SBML.POSITION_FACTOR;
      posY *= SBML.POSITION_FACTOR;

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          int verCode = Storage.getSAVerInfo().keyAt(verSelector.getSelectedItemPosition());
          String strPosX = positionX.getText().toString();
          String strPosY = positionY.getText().toString();
          if (!strPosX.isEmpty() || !strPosY.isEmpty()) {
            try {
              if (!strPosX.isEmpty())
                posX = (float) ((Double.parseDouble(strPosX) * SBML.POSITION_FACTOR));
              if (!strPosY.isEmpty())
                posY = (float) ((Double.parseDouble(strPosY) * SBML.POSITION_FACTOR));
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect position");
              toastMsg.setText(R.string.editErr_incorrectPosition);
              toastMsg.show();
              return;
            }
          }

          float offset = res.getInteger(R.integer.addModule_defaultOffset) * SBML.POSITION_FACTOR;
          int inLine = res.getInteger(R.integer.addModule_defaultInLine);
          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_ALL_MODULES, Event.OK,
            new SbxEditConfig(Sandbox.Mode.ADD_ALL, verCode, posX, posY, offset, inLine));
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
          float posX, posY;
          int align = alignSelector.getSelectedItemPosition();
          String text = etText.getText().toString();
          if (text.isEmpty()) {
            MyLog.w(LOG_TAG, "Text is empty");
            toastMsg.setText(R.string.editErr_emptyText);
            toastMsg.show();
            return;
          }

          String strPosX = positionX.getText().toString();
          String strPosY = positionY.getText().toString();
          if (strPosX.isEmpty() || strPosY.isEmpty()) {
            MyLog.w(LOG_TAG, "Position is empty");
            toastMsg.setText(R.string.editErr_emptyPosition);
            toastMsg.show();
            return;
          }
          try {
            posX = (float) ((Double.parseDouble(strPosX) * SBML.POSITION_FACTOR));
            posY = (float) ((Double.parseDouble(strPosY) * SBML.POSITION_FACTOR));
          } catch (NumberFormatException e) {
            MyLog.w(LOG_TAG, "Incorrect position");
            toastMsg.setText(R.string.editErr_incorrectPosition);
            toastMsg.show();
            return;
          }

          int margin = res.getInteger(R.integer.addText_defaultMargin);
          if (isAdvanced) {
            String strMargin = etMargin.getText().toString();
            if (strMargin.isEmpty()) {
              MyLog.w(LOG_TAG, "Margin is empty");
              toastMsg.setText(R.string.editErr_emptyMargin);
              toastMsg.show();
              return;
            }
            try {
              margin = Integer.parseInt(strMargin);
              if (!Settings.isDevMode()) {
                if (margin < 1)
                  throw new NumberFormatException();
              }
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect margin");
              toastMsg.setText(R.string.editErr_incorrectMargin);
              toastMsg.show();
              return;
            }
          }

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_TEXT, Event.OK,
            new SbxEditConfig(Sandbox.Mode.ADD_TEXT, posX, posY, text, align, margin));
        });
    }
  };

  private DialogInterface.OnShowListener addAllFontDialog_showListener
    = new DialogInterface.OnShowListener() {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AddAllFont";
    private float posX, posY;

    @Override
    public void onShow(final DialogInterface dialogInterface) {
      MyLog.d(LOG_TAG, "Dialog shown");
      posX = res.getInteger(R.integer.addAll_defaultPosX);
      posY = res.getInteger(R.integer.addAll_defaultPosY);
      AlertDialog dialog = (AlertDialog) dialogInterface;
      final EditText positionX = dialog.findViewById(R.id.etPositionX);
      final EditText positionY = dialog.findViewById(R.id.etPositionY);
      final EditText etInLine = dialog.findViewById(R.id.etInLine);
      positionX.setHint(String.valueOf((int) posX));
      positionY.setHint(String.valueOf((int) posY));
      posX *= SBML.POSITION_FACTOR;
      posY *= SBML.POSITION_FACTOR;

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
        v -> {
          MyLog.d(LOG_TAG, "Checking values...");
          String strPosX = positionX.getText().toString();
          String strPosY = positionY.getText().toString();
          if (!strPosX.isEmpty() || !strPosY.isEmpty()) {
            try {
              if (!strPosX.isEmpty())
                posX = (float) ((Double.parseDouble(strPosX) * SBML.POSITION_FACTOR));
              if (!strPosY.isEmpty())
                posY = (float) ((Double.parseDouble(strPosY) * SBML.POSITION_FACTOR));
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect position");
              toastMsg.setText(R.string.editErr_incorrectPosition);
              toastMsg.show();
              return;
            }
          }

          int inLine = res.getInteger(R.integer.addAllFont_defaultInLine);
          String strInLine = etInLine.getText().toString();
          if (!strInLine.isEmpty()) {
            try {
              inLine = Integer.parseInt(strInLine);
              if (inLine < 1)
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect line length");
              toastMsg.setText(R.string.editErr_incorrectInLineFont);
              toastMsg.show();
              return;
            }
          }

          dialogInterface.dismiss();
          listener.onDialogEvent(DialogID.ADD_ALL_FONT, Event.OK,
            new SbxEditConfig(Sandbox.Mode.ADD_ALL_FONT, posX, posY, inLine));
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
          if (!(optSaveId || refreshCargo || refreshFuel)) {
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
            new SbxEditConfig(Sandbox.Mode.OPTIMIZE, optSaveId, refreshCargo, refreshFuel));
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
            String strPosX = positionX.getText().toString();
            String strPosY = positionY.getText().toString();
            if (strPosX.isEmpty() || strPosY.isEmpty()) {
              MyLog.w(LOG_TAG, "Offset is empty");
              toastMsg.setText(R.string.editErr_emptyOffset);
              toastMsg.show();
              return;
            }
            try {
              float deltaX = (float) (Double.parseDouble(strPosX) * SBML.POSITION_FACTOR);
              float deltaY = (float) (Double.parseDouble(strPosY) * SBML.POSITION_FACTOR);
              dialog.dismiss();
              listener.onDialogEvent(DialogID.STATION_COPY, Event.OK,
                new SbxEditConfig(Sandbox.Mode.COPY, selected, movementMode, deltaX, deltaY));
            } catch (NumberFormatException e) {
              MyLog.w(LOG_TAG, "Incorrect offset");
              toastMsg.setText(R.string.editErr_incorrectOffset);
              toastMsg.show();
            }
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
      case Station.Type.GROUP: titleId = R.string.deleteGroup; break;
      case Station.Type.TEXT: titleId = R.string.deleteText; break;
    }

    AlertDialog.Builder adb = new AlertDialog.Builder(contextRef.get());
    adb.setTitle(titleId);
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which)
      -> listener.onDialogEvent(DialogID.STATION_DELETE, Event.OK,
        new SbxEditConfig(Sandbox.Mode.DELETE, stations)));
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
