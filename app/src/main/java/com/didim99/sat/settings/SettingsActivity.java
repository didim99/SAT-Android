package com.didim99.sat.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;

/**
 * Settings Activity
 * Created by didim99 on 30.01.18.
 */

public class SettingsActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_setAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "Settings activity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_settings);
    setupActionBar();

    SettingsFragment fragment;
    if (savedInstanceState == null) {
      fragment = new SettingsFragment();
      getFragmentManager()
        .beginTransaction()
        .add(R.id.prefs_content, fragment)
        .commit();
    }

    MyLog.d(LOG_TAG, "Settings activity started");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }
}