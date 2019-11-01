package com.didim99.sat.ui.sbxeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.didim99.sat.ui.BaseActivity;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.core.sbxeditor.Storage;
import com.didim99.sat.core.sbxeditor.wrapper.Part;
import com.didim99.sat.core.sbxeditor.utils.PartComparator;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.settings.Settings;

public class PartInfoActivity extends BaseActivity
  implements PartListAdapter.OnItemClickListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PartAct";
  
  private static final int MODE_DANGEROUS_PID = 1;
  private static final int MODE_NOT_STANDALONE = 2;
  
  private PartListAdapter adapter;
  private boolean pickerEnabled;
  private int choosedPID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "PartInfoActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_sbx_main);

    String intentAction = getIntent().getAction();
    if (intentAction != null && intentAction.equals(SAT.ACTION_PICK_MODULE))
      pickerEnabled = true;
    setupActionBar();

    boolean screenLarge = (getResources().getConfiguration().screenLayout
      & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

    //View components init
    MyLog.d(LOG_TAG, "View components init...");
    adapter = new PartListAdapter(this, Storage.getPartInfo(), screenLarge, this);
    RecyclerView partList = findViewById(R.id.rvStationList);
    if (screenLarge)
      partList.setLayoutManager(new GridLayoutManager(this, 2));
    else
      partList.setLayoutManager(new LinearLayoutManager(this));
    partList.setHasFixedSize(true);
    partList.setAdapter(adapter);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "PartInfoActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu...");
    getMenuInflater().inflate(R.menu.menu_part_info, menu);
    MyLog.d(LOG_TAG, "menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_help:
        helpDialog();
        return true;
      case R.id.action_sort:
        sortDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(Part part) {
    if (!pickerEnabled) return;
    choosedPID = part.getPartId();
    if (choosedPID == SBML.PartID.SOYUZ_SERVICE)
      sureDialog(MODE_DANGEROUS_PID);
    else if (!part.isStandalone())
      sureDialog(MODE_NOT_STANDALONE);
    else
      sendResult();
  }

  private void helpDialog() {
    MyLog.d(LOG_TAG, "Help dialog called");
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_part_info_help, null);
    ((TextView) dialogView.findViewById(R.id.tvPower))
      .setText(Html.fromHtml(getString(R.string.HelpDialog_power)));
    ((TextView) dialogView.findViewById(R.id.tvSaveCargo))
      .setText(Html.fromHtml(getString(R.string.HelpDialog_cargoSaver)));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.mTitle_actionHelp);
    adb.setView(dialogView);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "Help dialog created");
    dialog.show();
  }

  private void sortDialog() {
    MyLog.d(LOG_TAG, "Sort dialog called");
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_part_info_sort, null);
    Spinner mainSelector = dialogView.findViewById(R.id.mainSelector);
    Spinner secondSelector = dialogView.findViewById(R.id.secondSelector);
    mainSelector.setSelection(Settings.PartInfo.getSortMain());
    secondSelector.setSelection(Settings.PartInfo.getSortSecond());
    secondSelector.setEnabled(PartComparator.hasSecondLevel(
      Settings.PartInfo.getSortMain()));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.mTitle_actionSort);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(dialogView);
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(sortDialog_showListener);
    MyLog.d(LOG_TAG, "Sort dialog created");
    dialog.show();
  }

  private DialogInterface.OnShowListener sortDialog_showListener =
    new DialogInterface.OnShowListener() {
    private boolean reverse;
    private ImageView ivReverse;

    private void onReverseChanged() {
      ivReverse.setRotationX(reverse ? 0f : 180f);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
      AlertDialog dialog = (AlertDialog) dialogInterface;
      Spinner mainSelector = dialog.findViewById(R.id.mainSelector);
      Spinner secondSelector = dialog.findViewById(R.id.secondSelector);
      ivReverse = dialog.findViewById(R.id.ivReverse);
      reverse = Settings.PartInfo.isSortReverse();
      onReverseChanged();

      ivReverse.setOnClickListener(v -> {
        reverse = !reverse;
        onReverseChanged();
      });

      mainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          secondSelector.setEnabled(PartComparator.hasSecondLevel(position));
          switch (position) {
            case PartComparator.Method.PART_ID:
            case PartComparator.Method.PART_MANE:
              reverse = false;
              break;
            case PartComparator.Method.FUEL_MAIN:
            case PartComparator.Method.FUEL_THR:
            case PartComparator.Method.POWER_GEN:
            case PartComparator.Method.POWER_PROFIT:
            case PartComparator.Method.CARGO_COUNT:
              reverse = true;
              break;
          }
          onReverseChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
      });

      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
        updateSortMethod(mainSelector.getSelectedItemPosition(),
          secondSelector.getSelectedItemPosition(), reverse);
        dialogInterface.dismiss();
      });
    }
  };

  private void sureDialog(int mode) {
    MyLog.d(LOG_TAG, "Warning dialog called");
    int msgId = 0;
    switch (mode) {
      case MODE_DANGEROUS_PID:
        msgId = R.string.modulePicker_dangerousPID;
        break;
      case MODE_NOT_STANDALONE:
        msgId = R.string.modulePicker_notStandalone;
        break;
    }
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.diaTitle_warning);
    adb.setMessage(msgId);
    adb.setPositiveButton(R.string.dialogButtonOk, (dialog, which) -> sendResult());
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    AlertDialog dialog = adb.create();
    MyLog.d(LOG_TAG, "Warning dialog created");
    dialog.show();
  }

  private void sendResult() {
    MyLog.d(LOG_TAG, "Picked module: " + choosedPID);
    Intent intent = new Intent();
    intent.putExtra(SAT.EXTRA_PART_ID, choosedPID);
    setResult(RESULT_OK, intent);
    finish();
  }

  void updateSortMethod(int newSortMain, int newSortSecond, boolean newReverse) {
    int sortMain = Settings.PartInfo.getSortMain();
    int sortSecond = Settings.PartInfo.getSortSecond();
    boolean reverse = Settings.PartInfo.isSortReverse();
    if (newSortMain == sortMain && newSortSecond == sortSecond
      && newReverse == reverse) return;
    MyLog.d(LOG_TAG, "Updating sort method: "
      + newSortMain + "/" + newSortSecond + " (" + newReverse + ")");
    if (newSortMain != sortMain)
      Settings.PartInfo.setSortMain(newSortMain);
    if (newSortSecond != sortSecond)
      Settings.PartInfo.setSortSecond(newSortSecond);
    if (newReverse != reverse)
      Settings.PartInfo.setSortReverse(newReverse);
    adapter.updateSortMethod(newSortMain, newSortSecond, newReverse);
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
      if (pickerEnabled)
        bar.setTitle(R.string.actLabel_pickModule);
    }
  }
}
