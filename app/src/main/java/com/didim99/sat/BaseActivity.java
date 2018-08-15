package com.didim99.sat;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.didim99.sat.sbxeditor.ui.DialogManager;

/**
 * Basic Activity wrapper for this application
 * Created by didim99 on 26.07.18.
 */
public abstract class BaseActivity extends AppCompatActivity
  implements SAT.GlobalEventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_BaseAct";

  protected DialogManager dialogManager;

  @Override
  @CallSuper
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dialogManager = DialogManager.getInstance();
  }

  @Override
  @CallSuper
  protected void onResume() {
    super.onResume();
    ((SAT) getApplication()).registerEventListener(this);
  }

  @Override
  @CallSuper
  protected void onPause() {
    ((SAT) getApplication()).unregisterEventListener();
    super.onPause();
  }

  @Override
  @CallSuper
  public void onGlobalEvent(SAT.GlobalEvent event) {
    MyLog.d(LOG_TAG, "Global event received");
    switch (event) {
      case DB_DAMAGED:
        dialogManager.dbDamaged((SAT) getApplication());
        break;
      case UI_RELOAD:
        this.recreate();
        break;
    }
  }
}
