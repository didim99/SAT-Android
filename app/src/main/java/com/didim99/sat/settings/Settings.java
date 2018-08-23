package com.didim99.sat.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.didim99.sat.MyLog;

/**
 * Settings manager and storage class
 * Created by didim99 on 19.02.18.
 */

public class Settings {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Settings";

  private static SharedPreferences settings;
  private Settings() {}

  //Internal
  public static final String VALUE_DEFAULT = "default";
  public static final String VALUE_CUSTOM = "custom";
  public static final String VALUE_DATE = "date";

  //Settings names
  public static final String KEY_DIR_PICKER_LAST_PATH = "dirPicker_lastPath";
  private static final String KEY_DEVICE_VENDOR = "devVendor";
  private static final String KEY_DEVICE_MODEL = "devMod";
  private static final String KEY_DEVICE_RESOLUTION = "devRes";
  private static final String KEY_DEVICE_OS_VERSION = "devOs";
  private static final String KEY_DEVICE_ABI = "devAbi";
  private static final String KEY_DEVICE_ID = "devId";
  private static final String KEY_LAST_APP_VERSION = "lastAppVersion";
  private static final String KEY_FIRST_START = "firstStart";
  private static final String KEY_IGNORE_DB = "ignoreDB";
  private static final String KEY_HAS_DB = "hasDB";
  private static final String KEY_DB_VER = "DBVer";
  private static final String KEY_DB_GAME_VER = "DBGameVer";
  private static final String KEY_HAS_ICONS_DIR = "hasIconsDir";
  private static final String KEY_DEVELOPER_MODE = "devMode";
  private static final String KEY_REQUEST_ROOT = "requestRoot";
  private static final String KEY_CONFIRM_EXIT_EDIT_MODE = "confirm_exitEditMode";
  private static final String KEY_SBX_NAME_IN_HEADER = "sbxNameInHeader";
  private static final String KEY_CREATE_WITH_MARKERS = "createWithMarkers";
  private static final String KEY_USE_INTERNAL_SENDER = "internalSender";
  private static final String KEY_SBX_OPT_SID = "sbxOpt_saveId";
  private static final String KEY_SBX_OPT_CARGO = "sbxOpt_cargo";
  private static final String KEY_SBX_OPT_FUEL = "sbxOpt_fuel";
  static final String KEY_RES_CONV_DEFAULT_TYPE = "resConverter.defaultType";
  private static final String KEY_RES_CONV_SAVE_ORIGINAL_FILES = "resConverter.saveOriginalFiles";
  private static final String KEY_RES_CONV_INTERNAL_EXPLORER = "resConverter.internalExplorer";
  static final String KEY_SBX_EDITOR_DEFAULT_NAME = "sbxEditor.defaultSbxName";
  static final String KEY_SBX_EDITOR_CUSTOM_NAME = "sbxEditor.customSbxName";
  private static final String KEY_PART_INFO_SORT_MAIN = "partInfo_sortMain";
  private static final String KEY_PART_INFO_SORT_SECOND = "partInfo_sortSecond";
  private static final String KEY_PART_INFO_SORT_REVERSE = "partInfo_sortReverse";
  private static final String KEY_SYSTEM_CACHE_DIR = "system.cacheDir";
  private static final String KEY_SYSTEM_SBX_TMP_DIR = "system.sbxTmpDir";
  private static final String KEY_SYSTEM_RES_TMP_DIR = "system.resTmpDir";
  static final String KEY_LANGUAGE = "language";
  static final String KEY_UPDATE_DB = "updateDb";
  static final String KEY_UPDATE_APP = "updateApp";
  static final String KEY_FEEDBACK = "feedback";
  static final String KEY_ABOUT = "about";

  //Settings default values
  private static final int DEFVALUE_LAST_APP_VERSION = 0;
  private static final boolean DEFVALUE_FIRST_START = true;
  private static final boolean DEFVALUE_IGNORE_DB = false;
  private static final boolean DEFVALUE_HAS_DB = false;
  private static final boolean DEFVALUE_HAS_ICONS_DIR = false;
  private static final int DEFVALUE_DB_VER = 0;
  private static final int DEFVALUE_DB_GAME_VER = 0;
  private static final boolean DEFVALUE_DEVELOPER_MODE = false;
  private static final boolean DEFVALUE_REQUEST_ROOT = false;
  private static final boolean DEFVALUE_CONFIRM_EXIT_EDIT_MODE = true;
  private static final boolean DEFVALUE_CREATE_WITH_MARKERS = false;
  private static final boolean DEFVALUE_USE_INTERNAL_SENDER = false;
  private static final String DEFVALUE_SBX_EDITOR_DEFAULT_NAME = "default";
  private static final String DEFVALUE_SBX_EDITOR_CUSTOM_NAME = "";
  private static final boolean DEFVALUE_SBX_NAME_IN_HEADER = true;
  private static final boolean DEFVALUE_SBX_OPT_SID = true;
  private static final boolean DEFVALUE_SBX_OPT_CARGO = true;
  private static final boolean DEFVALUE_SBX_OPT_FUEL = true;
  private static final String DEFVALUE_RES_CONV_DEFAULT_TYPE = "1";
  private static final boolean DEFVALUE_RES_CONV_SAVE_ORIGINAL_FILES = true;
  private static final boolean DEFVALUE_RES_CONV_INTERNAL_EXPLORER = false;
  private static final int DEFVALUE_PART_INFO_SORT_MAIN = 0;
  private static final int DEFVALUE_PART_INFO_SORT_SECOND = 0;
  private static final boolean DEFVALUE_PART_INFO_SORT_REVERSE = false;
  public static final String DEFVALUE_LANGUAGE = "default";
  private static final String DEFVALUE_SYSTEM_CACHE_DIR = "";
  private static final String DEFVALUE_SYSTEM_SBX_TMP_DIR = "";
  private static final String DEFVALUE_SYSTEM_RES_TMP_DIR = "";

  //device info
  private static String devVendor;
  private static String devModel;
  private static String devRes;
  private static String devOsVer;
  private static String devAbi;
  private static String devId;
  //Settings fields
  private static int lastAppVersion;
  private static String language;
  private static boolean firstStart;
  private static boolean ignoreDb;
  private static boolean hasDB;
  private static int dbVer;
  private static int dbGemeVer;
  private static boolean hasIconsDir;
  private static boolean dbLoaded;
  private static boolean devMode;
  private static boolean requestRoot;
  private static boolean confirmExitEditMode;
  private static String defaultSbxName;
  private static String customSbxName;
  private static boolean createWithMarkers;
  private static boolean useInternalSender;
  private static boolean sbxNameInHeader;
  private static boolean sbxOptSID;
  private static boolean sbxOptCargo;
  private static boolean sbxOptFuel;
  //Internal settings
  private static boolean fontLoaded;
  private static String sysCacheDir;
  private static String sbxTempDir;
  private static String resTempDir;

  public static void init(Context appContext) {
    MyLog.d(LOG_TAG, "Loading settings...");

    dbLoaded = false;
    fontLoaded = false;
    settings = PreferenceManager.getDefaultSharedPreferences(appContext);
    lastAppVersion = settings.getInt(KEY_LAST_APP_VERSION, DEFVALUE_LAST_APP_VERSION);
    firstStart = settings.getBoolean(KEY_FIRST_START, DEFVALUE_FIRST_START);
    ignoreDb = settings.getBoolean(KEY_IGNORE_DB, DEFVALUE_IGNORE_DB);
    hasDB = settings.getBoolean(KEY_HAS_DB, DEFVALUE_HAS_DB);
    dbVer = settings.getInt(KEY_DB_VER, DEFVALUE_DB_VER);
    dbGemeVer = settings.getInt(KEY_DB_GAME_VER, DEFVALUE_DB_GAME_VER);
    hasIconsDir = settings.getBoolean(KEY_HAS_ICONS_DIR, DEFVALUE_HAS_ICONS_DIR);
    devMode = settings.getBoolean(KEY_DEVELOPER_MODE, DEFVALUE_DEVELOPER_MODE);
    requestRoot = settings.getBoolean(KEY_REQUEST_ROOT, DEFVALUE_REQUEST_ROOT);
    confirmExitEditMode = settings.getBoolean(
      KEY_CONFIRM_EXIT_EDIT_MODE, DEFVALUE_CONFIRM_EXIT_EDIT_MODE);
    defaultSbxName = settings.getString(
      KEY_SBX_EDITOR_DEFAULT_NAME, DEFVALUE_SBX_EDITOR_DEFAULT_NAME);
    customSbxName = settings.getString(
      KEY_SBX_EDITOR_CUSTOM_NAME, DEFVALUE_SBX_EDITOR_CUSTOM_NAME);
    createWithMarkers = settings.getBoolean(KEY_CREATE_WITH_MARKERS, DEFVALUE_CREATE_WITH_MARKERS);
    useInternalSender = settings.getBoolean(KEY_USE_INTERNAL_SENDER, DEFVALUE_USE_INTERNAL_SENDER);
    sbxNameInHeader = settings.getBoolean(KEY_SBX_NAME_IN_HEADER, DEFVALUE_SBX_NAME_IN_HEADER);
    sbxOptSID = settings.getBoolean(KEY_SBX_OPT_SID, DEFVALUE_SBX_OPT_SID);
    sbxOptCargo = settings.getBoolean(KEY_SBX_OPT_CARGO, DEFVALUE_SBX_OPT_CARGO);
    sbxOptFuel = settings.getBoolean(KEY_SBX_OPT_FUEL, DEFVALUE_SBX_OPT_FUEL);
    language = settings.getString(KEY_LANGUAGE, DEFVALUE_LANGUAGE);
    sysCacheDir = settings.getString(KEY_SYSTEM_CACHE_DIR, DEFVALUE_SYSTEM_CACHE_DIR);
    sbxTempDir = settings.getString(KEY_SYSTEM_SBX_TMP_DIR, DEFVALUE_SYSTEM_SBX_TMP_DIR);
    resTempDir = settings.getString(KEY_SYSTEM_RES_TMP_DIR, DEFVALUE_SYSTEM_RES_TMP_DIR);
    ResConverter.defaultType = Integer.parseInt(settings.getString(
      KEY_RES_CONV_DEFAULT_TYPE, DEFVALUE_RES_CONV_DEFAULT_TYPE));
    ResConverter.currentType = ResConverter.defaultType;
    ResConverter.saveOriginal = settings.getBoolean(
      KEY_RES_CONV_SAVE_ORIGINAL_FILES, DEFVALUE_RES_CONV_SAVE_ORIGINAL_FILES);
    ResConverter.useInternalExplorer = settings.getBoolean(
      KEY_RES_CONV_INTERNAL_EXPLORER, DEFVALUE_RES_CONV_INTERNAL_EXPLORER);
    PartInfo.sortMain = settings.getInt(KEY_PART_INFO_SORT_MAIN, DEFVALUE_PART_INFO_SORT_MAIN);
    PartInfo.sortSecond = settings.getInt(
      KEY_PART_INFO_SORT_SECOND, DEFVALUE_PART_INFO_SORT_SECOND);
    PartInfo.sortReverse = settings.getBoolean(
      KEY_PART_INFO_SORT_REVERSE, DEFVALUE_PART_INFO_SORT_REVERSE);

    MyLog.v(LOG_TAG, "Current configuration:"
      + "\n  lastAppVersion: " + lastAppVersion
      + "\n  firstStart: " + firstStart
      + "\n  language: " + language
      + "\n  ignoreDb: " + ignoreDb
      + "\n  hasDB: " + hasDB
      + "\n  dbVer: " + dbVer + " [" + dbGemeVer + "]"
      + "\n  hasIconsDir: " + hasIconsDir
      + "\n  devMode: " + devMode
      + "\n  requestRoot: " + requestRoot
      + "\n  ResConverter.defaultType: " + ResConverter.defaultType
      + "\n  ResConverter.saveOriginalFiles: " + ResConverter.saveOriginal
      + "\n  confirmExitEditMode: " + confirmExitEditMode
      + "\n  defaultSbxName: " + defaultSbxName
      + "\n  customSbxName: " + customSbxName
      + "\n  createWithMarkers: " + createWithMarkers
      + "\n  useInternalSender: " + useInternalSender
      + "\n  sbxNameInHeader: " + sbxNameInHeader
      + "\n  sbxOptSID: " + sbxOptSID
      + "\n  sbxOptCargo: " + sbxOptCargo
      + "\n  sbxOptFuel: " + sbxOptFuel
      + "\n  partInfo.sortMain: " + PartInfo.sortMain
      + "\n  partInfo.sortSecond: " + PartInfo.sortSecond
      + "\n  partInfo.sortReverse: " + PartInfo.sortReverse
      + "\n  sysCacheDir: " + sysCacheDir
      + "\n  sbxTempDir: " + sbxTempDir
      + "\n  resTempDir: " + resTempDir
    );
    MyLog.d(LOG_TAG, "Settings loaded");
  }

  static void updateSettings(String key) {
    MyLog.d(LOG_TAG, "Updating setting state (" + key + ")");
    switch (key) {
      case KEY_RES_CONV_DEFAULT_TYPE:
        ResConverter.defaultType = Integer.parseInt(settings.getString(
          key, DEFVALUE_RES_CONV_DEFAULT_TYPE));
        break;
      case KEY_RES_CONV_SAVE_ORIGINAL_FILES:
        ResConverter.saveOriginal = settings.getBoolean(
          key, DEFVALUE_RES_CONV_SAVE_ORIGINAL_FILES);
        break;
      case KEY_RES_CONV_INTERNAL_EXPLORER:
        ResConverter.useInternalExplorer = settings.getBoolean(
          key, DEFVALUE_RES_CONV_INTERNAL_EXPLORER);
        break;
      case KEY_SBX_EDITOR_DEFAULT_NAME:
        defaultSbxName = settings.getString(key, DEFVALUE_SBX_EDITOR_DEFAULT_NAME);
        break;
      case KEY_SBX_EDITOR_CUSTOM_NAME:
        customSbxName = settings.getString(key, DEFVALUE_SBX_EDITOR_CUSTOM_NAME);
        break;
      case KEY_SBX_NAME_IN_HEADER:
        sbxNameInHeader = settings.getBoolean(key, DEFVALUE_SBX_NAME_IN_HEADER);
        break;
      case KEY_CONFIRM_EXIT_EDIT_MODE:
        confirmExitEditMode = settings.getBoolean(key, DEFVALUE_CONFIRM_EXIT_EDIT_MODE);
        break;
      case KEY_USE_INTERNAL_SENDER:
        useInternalSender = settings.getBoolean(key, DEFVALUE_USE_INTERNAL_SENDER);
        break;
    }
  }

  public static void loadDeviceInfo() {
    if (devId != null) return;
    MyLog.d(LOG_TAG, "Loading device info...");
    devVendor = settings.getString(KEY_DEVICE_VENDOR, "");
    devModel = settings.getString(KEY_DEVICE_MODEL, "");
    devRes = settings.getString(KEY_DEVICE_RESOLUTION, "");
    devOsVer = settings.getString(KEY_DEVICE_OS_VERSION, "");
    devAbi = settings.getString(KEY_DEVICE_ABI, "");
    devId = settings.getString(KEY_DEVICE_ID, "");
  }

  //getters
  public static SharedPreferences getSettings() {
    return settings;
  }

  public static String getDevVendor() {
    return devVendor;
  }

  public static String getDevModel() {
    return devModel;
  }

  public static String getDevRes() {
    return devRes;
  }

  public static String getDevOsVer() {
    return devOsVer;
  }

  public static String getDevAbi() {
    return devAbi;
  }

  public static String getDevId() {
    return devId;
  }

  public static int getLastAppVersion() {
    return lastAppVersion;
  }

  public static String getLanguage() {
    return language;
  }

  public static boolean isFirstStart() {
    return firstStart;
  }

  public static boolean isIgnoreDb() {
    return ignoreDb;
  }

  public static boolean isHasDB() {
    return hasDB;
  }

  static int getDbVer() {
    return dbVer;
  }

  static int getDbGameVer() {
    return dbGemeVer;
  }

  public static boolean isHasIconsDir() {
    return hasIconsDir;
  }

  public static boolean isDbLoaded() {
    return dbLoaded;
  }

  public static boolean isDevMode() {
    return devMode;
  }

  public static boolean getRequestRoot() {
    return requestRoot;
  }

  public static boolean isConfirmExitEditMode() {
    return confirmExitEditMode;
  }

  public static String getDefaultSbxName() {
    return defaultSbxName;
  }

  public static String getCustomSbxName() {
    return customSbxName;
  }

  public static boolean isCreateWithMarkers() {
    return createWithMarkers;
  }

  public static boolean isUseInternalSender() {
    return useInternalSender;
  }

  public static boolean isSbxNameInHeader() {
    return sbxNameInHeader;
  }

  public static boolean isSbxOptSID() {
    return sbxOptSID;
  }

  public static boolean isSbxOptCargo() {
    return sbxOptCargo;
  }

  public static boolean isSbxOptFuel() {
    return sbxOptFuel;
  }

  public static boolean isFontLoaded() {
    return fontLoaded;
  }

  public static String getSysCacheDir() {
    return sysCacheDir;
  }

  public static String getSbxTempDir() {
    return sbxTempDir;
  }

  public static String getResTempDir() {
    return resTempDir;
  }

  //setters
  public static void setDevVendor (String vendor) {
    settings.edit().putString(KEY_DEVICE_VENDOR, vendor).apply();
  }

  public static void setDevModel (String model) {
    settings.edit().putString(KEY_DEVICE_MODEL, model).apply();
  }

  public static void setDevRes (String res) {
    settings.edit().putString(KEY_DEVICE_RESOLUTION, res).apply();
  }

  public static void setDevOsVer (String osVer) {
    settings.edit().putString(KEY_DEVICE_OS_VERSION, osVer).apply();
  }

  public static void setDevAbi (String abi) {
    settings.edit().putString(KEY_DEVICE_ABI, abi).apply();
  }

  public static void setDevId(String id) {
    settings.edit().putString(KEY_DEVICE_ID, id).apply();
  }

  public static void setLastAppVersion (int version) {
    lastAppVersion = version;
    settings.edit().putInt(KEY_LAST_APP_VERSION, version).apply();
  }

  public static void setFirstStart(boolean state) {
    firstStart = state;
    settings.edit().putBoolean(KEY_FIRST_START, state).apply();
  }

  public static void setIgnoreDb(boolean state) {
    ignoreDb = state;
    settings.edit().putBoolean(KEY_IGNORE_DB, state).apply();
  }

  public static void setHasDB(boolean state) {
    hasDB = state;
    settings.edit().putBoolean(KEY_HAS_DB, state).apply();
  }

  public static void setDbVer(int ver) {
    dbVer = ver;
    settings.edit().putInt(KEY_DB_VER, ver).apply();
  }

  public static void setDbGameVer(int ver) {
    dbGemeVer = ver;
    settings.edit().putInt(KEY_DB_GAME_VER, ver).apply();
  }

  public static void setHasIconsDir(boolean state) {
    hasIconsDir = state;
    settings.edit().putBoolean(KEY_HAS_ICONS_DIR, state).apply();
  }

  public static void setDbLoaded(boolean state) {
    dbLoaded = state;
  }

  static void setDevMode(boolean state) {
    devMode = state;
    settings.edit().putBoolean(KEY_DEVELOPER_MODE, state).apply();
  }

  public static void setRequestRoot(boolean state) {
    requestRoot = state;
    settings.edit().putBoolean(KEY_REQUEST_ROOT, state).apply();
  }

  public static void setConfirmExitEditMode(boolean state) {
    confirmExitEditMode = state;
    settings.edit().putBoolean(KEY_CONFIRM_EXIT_EDIT_MODE, state).apply();
  }

  public static void setCustomSbxName(String newName) {
    Settings.customSbxName = newName;
    settings.edit().putString(KEY_SBX_EDITOR_CUSTOM_NAME, newName).apply();
  }

  public static void setCreateWithMarkers(boolean state) {
    Settings.createWithMarkers = state;
    settings.edit().putBoolean(KEY_CREATE_WITH_MARKERS, state).apply();
  }

  public static void setUseInternalSender(boolean state) {
    Settings.useInternalSender = state;
    settings.edit().putBoolean(KEY_USE_INTERNAL_SENDER, state).apply();
  }

  public static void setSbxOptSID(boolean state) {
    Settings.sbxOptSID = state;
    settings.edit().putBoolean(KEY_SBX_OPT_SID, state).apply();
  }

  public static void setSbxOptCargo(boolean state) {
    Settings.sbxOptCargo = state;
    settings.edit().putBoolean(KEY_SBX_OPT_CARGO, state).apply();
  }

  public static void setSbxOptFuel(boolean state) {
    Settings.sbxOptFuel = state;
    settings.edit().putBoolean(KEY_SBX_OPT_FUEL, state).apply();
  }

  public static void setFontLoaded(boolean state) {
    fontLoaded = state;
  }

  public static void setSystemDirs(String sysCacheDir, String sbxTempDir, String resTempDir) {
    Settings.sysCacheDir = sysCacheDir;
    Settings.sbxTempDir = sbxTempDir;
    Settings.resTempDir = resTempDir;
    settings.edit().putString(KEY_SYSTEM_CACHE_DIR, sysCacheDir)
      .putString(KEY_SYSTEM_SBX_TMP_DIR, sbxTempDir)
      .putString(KEY_SYSTEM_RES_TMP_DIR, resTempDir).apply();
  }

  public static class ResConverter {
    private static int defaultType;
    private static int currentType;
    private static boolean saveOriginal;
    private static boolean useInternalExplorer;

    public static int getCurrentType() {
      return currentType;
    }

    public static boolean isSaveOriginal() {
      return saveOriginal;
    }

    public static boolean isUseInternalExplorer() {
      return useInternalExplorer;
    }

    public static void setCurrentType(int currentType) {
      ResConverter.currentType = currentType;
    }

    public static void setUseInternalExplorer(boolean useInternalExplorer) {
      ResConverter.useInternalExplorer = useInternalExplorer;
      settings.edit().putBoolean(KEY_RES_CONV_INTERNAL_EXPLORER, useInternalExplorer).apply();
    }
  }

  public static class PartInfo {
    private static int sortMain;
    private static int sortSecond;
    private static boolean sortReverse;

    public static int getSortMain() {
      return sortMain;
    }

    public static int getSortSecond() {
      return sortSecond;
    }

    public static boolean isSortReverse() {
      return sortReverse;
    }

    public static void setSortMain(int sortMain) {
      PartInfo.sortMain = sortMain;
      settings.edit().putInt(KEY_PART_INFO_SORT_MAIN, sortMain).apply();
    }

    public static void setSortSecond(int sortSecond) {
      PartInfo.sortSecond = sortSecond;
      settings.edit().putInt(KEY_PART_INFO_SORT_MAIN, sortSecond).apply();
    }

    public static void setSortReverse(boolean sortReverse) {
      PartInfo.sortReverse = sortReverse;
      settings.edit().putBoolean(KEY_PART_INFO_SORT_REVERSE, sortReverse).apply();
    }
  }
}
