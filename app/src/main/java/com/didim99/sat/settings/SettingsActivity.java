package com.didim99.sat.settings;

import android.os.Bundle;
import com.didim99.sat.ui.BaseActivity;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;

/**
 * Settings Activity
 * Created by didim99 on 30.01.18.
 */
public class SettingsActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_setAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "Settings activity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_settings);

    SettingsFragment fragment;
    if (savedInstanceState == null) {
      fragment = new SettingsFragment();
      getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.prefs_content, fragment)
        .commit();
    }

    MyLog.d(LOG_TAG, "Settings activity started");
  }
}