package com.didim99.sat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.didim99.sat.utils.MyLog;

/*
 * Created by didim99 on 08.03.18.
 */

class DbHelper extends SQLiteOpenHelper {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DbHelper";

  private static final int DB_VERSION = 2;
  private static final String DB_NAME = "offline";

  //table names
  static final String TABLE_MODULES = "modules";
  static final String TABLE_PLANETS = "planets";
  static final String TABLE_VERSIONS = "versions";

  //column names
  static final String KEY_PART_ID = "part_id";
  static final String KEY_VER = "min_ver";
  static final String KEY_SIZE = "size";
  static final String KEY_NAME = "name";
  static final String KEY_POWER_GEN = "power_gen";
  static final String KEY_POWER_USE = "power_use";
  static final String KEY_CARGO = "cargo_count";
  static final String KEY_FUEL_MAIN = "fuel_main";
  static final String KEY_FUEL_THR = "fuel_thr";
  static final String KEY_STANDALONE = "standalone";
  static final String KEY_HAS_NAV = "has_navicomp";
  static final String KEY_SAVE_CARGO = "save_cargo";
  static final String KEY_ID = "id";
  static final String KEY_POS_X = "pos_x";
  static final String KEY_POS_Y = "pos_y";
  static final String KEY_OBJECT_R = "object_r";
  static final String KEY_ORBIT_R = "orbit_r";
  static final String KEY_RESCALE_R = "rescale_r";
  static final String KEY_VER_CODE = "ver_code";
  static final String KEY_VER_NAME = "ver_name";

  //SQL queries
  private static final String CREATE_TABLE_VERSIONS =
    "CREATE TABLE " + TABLE_VERSIONS + "("
      + KEY_VER_CODE + " integer primary key, "
      + KEY_VER_NAME + " varchar(16)"
      + ");";

  DbHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    MyLog.d(LOG_TAG, "Creating new database...");
    db.execSQL(
      "CREATE TABLE " + TABLE_MODULES + "("
        + KEY_PART_ID + " integer primary key, "
        + KEY_VER + " integer, "
        + KEY_SIZE + " integer, "
        + KEY_NAME + " varchar(64), "
        + KEY_POWER_GEN + " integer, "
        + KEY_POWER_USE + " integer, "
        + KEY_CARGO + " integer, "
        + KEY_FUEL_MAIN + " integer, "
        + KEY_FUEL_THR + " integer, "
        + KEY_STANDALONE + " integer, "
        + KEY_HAS_NAV + " integer, "
        + KEY_SAVE_CARGO + " integer"
        + ");"
    );
    db.execSQL(
      "CREATE TABLE " + TABLE_PLANETS + "("
        + KEY_ID + " integer primary key, "
        + KEY_NAME + " varchar(8), "
        + KEY_POS_X + " integer, "
        + KEY_POS_Y + " integer, "
        + KEY_OBJECT_R + " integer, "
        + KEY_ORBIT_R + " integer, "
        + KEY_RESCALE_R + " integer"
        + ");"
    );
    db.execSQL(CREATE_TABLE_VERSIONS);
    MyLog.d(LOG_TAG, "Database created");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (newVersion == 2) {
      db.execSQL(CREATE_TABLE_VERSIONS);
    }
  }
}
