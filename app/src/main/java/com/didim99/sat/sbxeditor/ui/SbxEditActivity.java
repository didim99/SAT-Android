package com.didim99.sat.sbxeditor.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.sbxeditor.SbxEditConfig;
import com.didim99.sat.sbxeditor.Station;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.settings.Settings;

public class SbxEditActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SbxEditAct";

  private CheckBox rotationCommonBase,
    rotationCustomBase, changeMovementDirection,
    changeMovementSpeed, changeMovementRotation;
  private EditText
    etAlpha, etExtendFuel, etStartSaveId, etPositionX, etPositionY,
    etRotateAngle, etRotationBaseX, etRotationBaseY,
    etMovementDirection, etMovementSpeed, etMovementRotation;
  private RadioButton
    hideModeHide, hideModeShow,
    moveModeOffset, moveModeNewCenter,
    movementModeStop, movementModeEdit;
  private TextView enterOffset;
  private SbxEditConfig config;
  private boolean isMultiple = false;
  private boolean isConfigModified = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SbxEditActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_sbx_edit);
    setupActionBar();

    config = Storage.getEditConfig();
    isMultiple = config.getStations().size() > 1;

    //view components init
    MyLog.d(LOG_TAG, "View components init...");
    CheckBox sectionHide = findViewById(R.id.sectionHide);
    CheckBox sectionAlpha = findViewById(R.id.sectionAlpha);
    CheckBox sectionExtendFuel = findViewById(R.id.sectionExtendFuel);
    CheckBox sectionSaveId = findViewById(R.id.sectionSaveId);
    CheckBox sectionPosition = findViewById(R.id.sectionPosition);
    CheckBox sectionRotate = findViewById(R.id.sectionRotate);
    CheckBox sectionMovement = findViewById(R.id.sectionMovement);
    CheckBox refreshCargo = findViewById(R.id.refreshCargo);
    CheckBox refreshFuel = findViewById(R.id.refreshFuel);
    rotationCustomBase = findViewById(R.id.rotationCustomBase);
    changeMovementDirection = findViewById(R.id.changeMovementDirection);
    changeMovementSpeed = findViewById(R.id.changeMovementSpeed);
    changeMovementRotation = findViewById(R.id.changeMovementRotation);
    RadioGroup rgHideMode = findViewById(R.id.rgHideMode);
    RadioGroup rgMoveMode = findViewById(R.id.rgMoveMode);
    RadioGroup rgMovementMode = findViewById(R.id.rgMovementMode);
    etAlpha = findViewById(R.id.etAlpha);
    etExtendFuel = findViewById(R.id.etExtendFuel);
    etStartSaveId = findViewById(R.id.etStartSaveId);
    etPositionX = findViewById(R.id.etPositionX);
    etPositionY = findViewById(R.id.etPositionY);
    etRotateAngle = findViewById(R.id.etRotateAngle);
    etRotationBaseX = findViewById(R.id.etRotationBaseX);
    etRotationBaseY = findViewById(R.id.etRotationBaseY);
    etMovementDirection = findViewById(R.id.etMovementDirection);
    etMovementSpeed = findViewById(R.id.etMovementSpeed);
    etMovementRotation = findViewById(R.id.etMovementRotation);
    hideModeHide = findViewById(R.id.hideModeHide);
    hideModeShow = findViewById(R.id.hideModeShow);
    moveModeOffset = findViewById(R.id.moveModeOffset);
    moveModeNewCenter = findViewById(R.id.moveModeNewCenter);
    movementModeStop = findViewById(R.id.movementModeStop);
    movementModeEdit = findViewById(R.id.movementModeEdit);
    enterOffset = findViewById(R.id.tvEnterOffset);
    MyLog.d(LOG_TAG, "View components init completed");

    sectionHide.setOnCheckedChangeListener(sectionListener);
    sectionAlpha.setOnCheckedChangeListener(sectionListener);
    sectionExtendFuel.setOnCheckedChangeListener(sectionListener);
    sectionSaveId.setOnCheckedChangeListener(sectionListener);
    sectionPosition.setOnCheckedChangeListener(sectionListener);
    sectionRotate.setOnCheckedChangeListener(sectionListener);
    sectionMovement.setOnCheckedChangeListener(sectionListener);
    refreshCargo.setOnCheckedChangeListener(sectionListener);
    refreshFuel.setOnCheckedChangeListener(sectionListener);
    rotationCustomBase.setOnCheckedChangeListener(sectionListener);
    changeMovementDirection.setOnCheckedChangeListener(sectionListener);
    changeMovementSpeed.setOnCheckedChangeListener(sectionListener);
    changeMovementRotation.setOnCheckedChangeListener(sectionListener);
    rgHideMode.setOnCheckedChangeListener(modeListener);
    rgMoveMode.setOnCheckedChangeListener(modeListener);
    rgMovementMode.setOnCheckedChangeListener(modeListener);

    if (isMultiple) {
      MyLog.d(LOG_TAG, "Multiple edit, enable additional views");
      rotationCommonBase = findViewById(R.id.rotationCommonBase);
      rotationCommonBase.setVisibility(View.VISIBLE);
      rotationCommonBase.setOnCheckedChangeListener(sectionListener);
      config.setRotationCommonBase(getResources()
        .getBoolean(R.bool.default_rotate_commonBase));
    }

    MyLog.d(LOG_TAG, "SbxEditActivity started");
  }

  CompoundButton.OnCheckedChangeListener sectionListener =
    new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
          case R.id.sectionHide:
            hideModeHide.setEnabled(isChecked);
            hideModeShow.setEnabled(isChecked);
            config.setChangeVisibility(isChecked);
            if (isChecked) {
              if (hideModeHide.isChecked())
                config.setHideMode(SBML.VISIBILITY_MODE_INVISIBLE);
              else if (hideModeShow.isChecked())
                config.setHideMode(SBML.VISIBILITY_MODE_VISIBLE);
            }
            break;
          case R.id.sectionAlpha:
            etAlpha.setEnabled(isChecked);
            config.setChangeAlpha(isChecked);
            break;
          case R.id.sectionExtendFuel:
            etExtendFuel.setEnabled(isChecked);
            config.setExtendFuel(isChecked);
            break;
          case R.id.sectionSaveId:
            etStartSaveId.setEnabled(isChecked);
            config.setChangeSaveId(isChecked);
            break;
          case R.id.sectionPosition:
            etPositionX.setEnabled(isChecked);
            etPositionY.setEnabled(isChecked);
            moveModeOffset.setEnabled(isChecked);
            moveModeNewCenter.setEnabled(isChecked);
            config.setChangePosition(isChecked);
            if (isChecked) {
              if (moveModeOffset.isChecked())
                config.setPositionMode(Station.MOVE_MODE_OFFSET);
              else if (moveModeNewCenter.isChecked())
                config.setPositionMode(Station.MOVE_MODE_NEW_CENTER);
            }
            break;
          case R.id.sectionRotate:
            etRotateAngle.setEnabled(isChecked);
            boolean commonBase = isChecked;
            if (isMultiple) {
              commonBase &= rotationCommonBase.isChecked();
              rotationCommonBase.setEnabled(isChecked);
            }
            rotationCustomBase.setEnabled(commonBase);
            if (commonBase) {
              etRotationBaseX.setEnabled(rotationCustomBase.isChecked());
              etRotationBaseY.setEnabled(rotationCustomBase.isChecked());
            } else {
              etRotationBaseX.setEnabled(false);
              etRotationBaseY.setEnabled(false);
            }
            config.setChangeAngle(isChecked);
            break;
          case R.id.rotationCommonBase:
            rotationCustomBase.setEnabled(isChecked);
            if (isChecked) {
              etRotationBaseX.setEnabled(rotationCustomBase.isChecked());
              etRotationBaseY.setEnabled(rotationCustomBase.isChecked());
            } else {
              etRotationBaseX.setEnabled(false);
              etRotationBaseY.setEnabled(false);
            }
            config.setRotationCommonBase(isChecked);
            break;
          case R.id.rotationCustomBase:
            etRotationBaseX.setEnabled(isChecked);
            etRotationBaseY.setEnabled(isChecked);
            config.setRotationCustomBase(isChecked);
            break;
          case R.id.sectionMovement:
            movementModeStop.setEnabled(isChecked);
            movementModeEdit.setEnabled(isChecked);
            if (isChecked && movementModeEdit.isChecked()) {
              changeMovementDirection.setEnabled(true);
              changeMovementSpeed.setEnabled(true);
              changeMovementRotation.setEnabled(true);
              etMovementDirection.setEnabled(changeMovementDirection.isChecked());
              etMovementSpeed.setEnabled(changeMovementSpeed.isChecked());
              etMovementRotation.setEnabled(changeMovementRotation.isChecked());
            } else {
              changeMovementDirection.setEnabled(false);
              changeMovementSpeed.setEnabled(false);
              changeMovementRotation.setEnabled(false);
              etMovementDirection.setEnabled(false);
              etMovementSpeed.setEnabled(false);
              etMovementRotation.setEnabled(false);
            }
            config.setChangeMovement(isChecked);
            if (isChecked) {
              if (movementModeStop.isChecked())
                config.setMovementMode(Station.MOVEMENT_MODE_STOP);
              else if (movementModeEdit.isChecked())
                config.setMovementMode(Station.MOVE_MODE_NEW_CENTER);
            }
            break;
          case R.id.changeMovementDirection:
            etMovementDirection.setEnabled(isChecked);
            config.setChangeMovementDirection(isChecked);
            break;
          case R.id.changeMovementSpeed:
            etMovementSpeed.setEnabled(isChecked);
            config.setChangeMovementSpeed(isChecked);
            break;
          case R.id.changeMovementRotation:
            etMovementRotation.setEnabled(isChecked);
            config.setChangeMovementRotation(isChecked);
            break;
          case R.id.refreshCargo:
            config.setRefreshCargo(isChecked);
            break;
          case R.id.refreshFuel:
            config.setRefreshFuel(isChecked);
            break;
        }
        setConfigModified();
      }
  };

  RadioGroup.OnCheckedChangeListener modeListener = new RadioGroup.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
      switch (group.getId()) {
        case R.id.rgHideMode:
          switch (checkedId) {
            case R.id.hideModeHide:
              config.setHideMode(SBML.VISIBILITY_MODE_INVISIBLE);
              break;
            case R.id.hideModeShow:
              config.setHideMode(SBML.VISIBILITY_MODE_VISIBLE);
              break;
          }
          break;
        case R.id.rgMoveMode:
          switch (checkedId) {
            case R.id.moveModeOffset:
              enterOffset.setText(R.string.diaStaCopy_tvText_enterOffset);
              config.setPositionMode(Station.MOVE_MODE_OFFSET);
              break;
            case R.id.moveModeNewCenter:
              enterOffset.setText(R.string.diaStaCopy_tvText_enterCenterPos);
              config.setPositionMode(Station.MOVE_MODE_NEW_CENTER);
              break;
          }
          break;
        case R.id.rgMovementMode:
          switch (checkedId) {
            case R.id.movementModeStop:
              changeMovementDirection.setEnabled(false);
              changeMovementSpeed.setEnabled(false);
              changeMovementRotation.setEnabled(false);
              etMovementDirection.setEnabled(false);
              etMovementSpeed.setEnabled(false);
              etMovementRotation.setEnabled(false);
              config.setMovementMode(Station.MOVEMENT_MODE_STOP);
              break;
            case R.id.movementModeEdit:
              changeMovementDirection.setEnabled(true);
              changeMovementSpeed.setEnabled(true);
              changeMovementRotation.setEnabled(true);
              etMovementDirection.setEnabled(changeMovementDirection.isChecked());
              etMovementSpeed.setEnabled(changeMovementSpeed.isChecked());
              etMovementRotation.setEnabled(changeMovementRotation.isChecked());
              config.setMovementMode(Station.MOVEMENT_MODE_EDIT);
              break;
          }
          break;
      }
      setConfigModified();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_sbx_edit, menu);
    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_done:
        if (checkValues())
          editModeDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (isConfigModified && Settings.isConfirmExitEditMode())
      exitDialog();
    else
      super.onBackPressed();
  }

  private boolean checkValues() {
    Toast toastMsg = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
    if (config.isChangeAlpha()) {
      String opacityStr = etAlpha.getText().toString();
      if (opacityStr.isEmpty()) {
        MyLog.w(LOG_TAG, "Texture alpha value is empty");
        toastMsg.setText(R.string.actSbxEdit_alpha_isEmpty);
        toastMsg.show();
        return false;
      }
      int opacity = Integer.parseInt(opacityStr);
      if (opacity > SBML.OPACITY_MAX_VALUE || opacity < SBML.OPACITY_MIN_VALUE) {
        MyLog.w(LOG_TAG, "Texture alpha value incorrect (" + opacity + ")");
        toastMsg.setText(R.string.actSbxEdit_alpha_incorrect);
        toastMsg.show();
        return false;
      }
      config.setAlphaValue(
        (float) (SBML.OPACITY_MAX_VALUE - opacity) / SBML.OPACITY_MAX_VALUE);
    }
    if (config.isExtendFuel()) {
      String extendFuelStr = etExtendFuel.getText().toString();
      if (extendFuelStr.isEmpty()) {
        MyLog.w(LOG_TAG, "Extend fuel value is empty");
        toastMsg.setText(R.string.actSbxEdit_extend_fuel_isEmpty);
        toastMsg.show();
        return false;
      }
      try {
        config.setExtendFuelValue(Float.parseFloat(extendFuelStr));
      } catch (NumberFormatException e) {
        MyLog.w(LOG_TAG, "Extend fuel value incorrect");
        toastMsg.setText(R.string.actSbxEdit_extend_fuel_incorrect);
        toastMsg.show();
        return false;
      }
    }
    if (config.isChangeSaveId()) {
      String saveIdStr = etStartSaveId.getText().toString();
      if (saveIdStr.isEmpty())
        config.setStartSaveId(Storage.getSandbox().getInfo().getMaxSaveId() + 1);
      else
        config.setStartSaveId(Integer.parseInt(saveIdStr));
    }
    if (config.isChangePosition()) {
      String strPosX = etPositionX.getText().toString();
      String strPosY = etPositionY.getText().toString();
      if (strPosX.isEmpty() || strPosY.isEmpty()) {
        MyLog.w(LOG_TAG, "Offset is empty");
        toastMsg.setText(R.string.editErr_emptyOffset);
        toastMsg.show();
        return false;
      }
      try {
        float deltaX = (float) (Double.parseDouble(strPosX) * SBML.POSITION_FACTOR);
        float deltaY = (float) (Double.parseDouble(strPosY) * SBML.POSITION_FACTOR);
        config.setPositionX(deltaX);
        config.setPositionY(deltaY);
      } catch (NumberFormatException e) {
        MyLog.w(LOG_TAG, "Offset incorrect");
        toastMsg.setText(R.string.editErr_incorrectOffset);
        toastMsg.show();
        return false;
      }
    }
    if (config.isChangeAngle()) {
      String angleStr = etRotateAngle.getText().toString();
      if (angleStr.isEmpty()) {
        MyLog.w(LOG_TAG, "Rotation angle is empty");
        toastMsg.setText(R.string.actSbxEdit_rotate_emptyAngle);
        toastMsg.show();
        return false;
      }
      try {
        config.setPositionAnge(Float.parseFloat(angleStr));
      } catch (NumberFormatException e) {
        MyLog.w(LOG_TAG, "Rotation angle incorrect");
        toastMsg.setText(R.string.actSbxEdit_rotate_incorrectAngle);
        toastMsg.show();
        return false;
      }
      if (isMultiple && !config.isRotationCommonBase())
        config.setRotationCustomBase(false);
      if (config.isRotationCustomBase()) {
        String strBaseX = etRotationBaseX.getText().toString();
        String strBaseY = etRotationBaseY.getText().toString();
        if (strBaseX.isEmpty() && strBaseY.isEmpty()) {
          MyLog.w(LOG_TAG, "Rotation base is empty");
          toastMsg.setText(R.string.actSbxEdit_rotate_emptyBase);
          toastMsg.show();
          return false;
        }
        try {
          if (!strBaseX.isEmpty()) {
            float baseX = (float) (Double.parseDouble(strBaseX) * SBML.POSITION_FACTOR);
            config.setRotationBaseX(baseX);
          }
          if (!strBaseY.isEmpty()) {
            float baseY = (float) (Double.parseDouble(strBaseY) * SBML.POSITION_FACTOR);
            config.setRotationBaseY(baseY);
          }
        } catch (NumberFormatException e) {
          MyLog.w(LOG_TAG, "Offset incorrect");
          toastMsg.setText(R.string.actSbxEdit_rotate_incorrectBase);
          toastMsg.show();
          return false;
        }
      }
    }
    if (config.isChangeMovement() && config.getMovementMode() == Station.MOVEMENT_MODE_EDIT) {
      if (config.isChangeMovementDirection()) {
        String directionStr = etMovementDirection.getText().toString();
        if (directionStr.isEmpty()) {
          MyLog.w(LOG_TAG, "Movement direction is empty");
          toastMsg.setText(R.string.actSbxEdit_movement_direction_isEmpty);
          toastMsg.show();
          return false;
        }
        try {
          float direction = Float.parseFloat(directionStr);
          if (direction < SBML.DIRECTION_MIN_VALUE
            || direction > SBML.DIRECTION_MAX_VALUE)
            throw new NumberFormatException();
          config.setMovementDirection(direction);
        } catch (NumberFormatException e) {
          MyLog.w(LOG_TAG, "Movement direction incorrect");
          toastMsg.setText(R.string.actSbxEdit_movement_direction_incorrect);
          toastMsg.show();
          return false;
        }
      }
      if (config.isChangeMovementSpeed()) {
        String speedStr = etMovementSpeed.getText().toString();
        if (speedStr.isEmpty()) {
          MyLog.w(LOG_TAG, "Movement speed is empty");
          toastMsg.setText(R.string.actSbxEdit_movement_speed_isEmpty);
          toastMsg.show();
          return false;
        }
        try {
          config.setMovementSpeed(Float.parseFloat(speedStr));
        } catch (NumberFormatException e) {
          MyLog.w(LOG_TAG, "Movement speed incorrect");
          toastMsg.setText(R.string.actSbxEdit_movement_speed_incorrect);
          toastMsg.show();
          return false;
        }
      }
      if (config.isChangeMovementRotation()) {
        String rotationStr = etMovementRotation.getText().toString();
        if (rotationStr.isEmpty()) {
          MyLog.w(LOG_TAG, "Movement rotation is empty");
          toastMsg.setText(R.string.actSbxEdit_movement_rotation_isEmpty);
          toastMsg.show();
          return false;
        }
        try {
          float rotation = Float.parseFloat(rotationStr);
          if (rotation < SBML.ROTATION_SPEED_MIN_VALUE
            || rotation > SBML.ROTATION_SPEED_MAX_VALUE)
            throw new NumberFormatException();
          config.setMovementRotation(rotation);
        } catch (NumberFormatException e) {
          MyLog.w(LOG_TAG, "Movement rotation incorrect");
          toastMsg.setText(R.string.actSbxEdit_movement_rotation_incorrect);
          toastMsg.show();
          return false;
        }
      }
    }
    return true;
  }

  private void editModeDialog() {
    MyLog.d(LOG_TAG, "EditMode dialog called");
    int count = isMultiple ? 5 : 1;
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setMessage(getResources().getQuantityString(R.plurals.chooseEditMode, count));
    adb.setPositiveButton(R.string.editMode_replace, (dialog, which) -> {
      config.setEditMode(Station.EDIT_MODE_REPLACE);
      sendResult();
    });
    adb.setNeutralButton(getResources().getQuantityString(R.plurals.editMode_createNew, count),
      (dialog, which) -> {
        config.setEditMode(Station.EDIT_MODE_CREATE_NEW);
        sendResult();
    });
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "EditMode dialog created");
    dialog.show();
  }

  private void sendResult() {
    config.log();
    Storage.setEditConfig(config);
    setResult(RESULT_OK);
    finish();
  }

  private void exitDialog() {
    MyLog.d(LOG_TAG, "Exit dialog called");
    LayoutInflater inflater = LayoutInflater.from(this);
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.exitFromEditing);
    adb.setPositiveButton(R.string.dialogButtonExit, (dialog, which) -> finish());
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(inflater.inflate(R.layout.dialog_no_ask_more, null));
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(dialog1 -> {
      CheckBox confirmExit = ((AlertDialog) dialog1).findViewById(R.id.cbNoAskMore);
      confirmExit.setChecked(!Settings.isConfirmExitEditMode());
      confirmExit.setOnCheckedChangeListener((buttonView, isChecked) -> {
        MyLog.d(LOG_TAG, "Exit from edit mode confirmation: " + !isChecked);
        Settings.setConfirmExitEditMode(!isChecked);
      });
    });
    MyLog.d(LOG_TAG, "Exit dialog created");
    dialog.show();
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setConfigModified() {
    if (!isConfigModified)
      isConfigModified = true;
  }
}
