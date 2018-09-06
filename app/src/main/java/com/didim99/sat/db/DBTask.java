package com.didim99.sat.db;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Base64;
import android.util.SparseArray;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.Utils;
import com.didim99.sat.network.NetworkManager;
import com.didim99.sat.network.WebAPI;
import com.didim99.sat.sbxeditor.model.Storage;
import com.didim99.sat.sbxeditor.model.wrapper.Part;
import com.didim99.sat.sbxeditor.model.wrapper.Planet;
import com.didim99.sat.settings.Settings;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Database management wrapper
 * Created by didim99 on 08.03.18.
 */

public class DBTask extends AsyncTask<Void, Void, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DBTask";

  public static final class Mode {
    public static final int CHECK_DB_UPDATES = 1;
    public static final int CREATE = 2;
    public static final int UPDATE = 3;
    public static final int LOAD = 4;
  }

  public static final class Event {
    public static final int TASK_FAILED = -1;
    public static final int DATA_RECEIVED = 1;
    public static final int DB_LOADED = 2;
  }

  public static final class Error {
    private static final int NO_ERRORS = 0;
    private static final int WEB_UNAVAILABLE = -1;
    private static final int DB_UNWRITABLE = -2;
    public static final int DB_DAMAGED = -3;
  }

  private static final int ID_LOADING_DB = 100;

  private WeakReference<Context> contextRef;
  private NotificationCompat.Builder nBuilder;
  private NotificationManagerCompat nManager;
  private EventListener listener;
  private DbHelper dbHelper;
  private SQLiteDatabase db;
  private int mode, statusCode;

  public DBTask(Context context, EventListener listener, int mode) {
    if (mode == Mode.CREATE || mode == Mode.UPDATE) {
      nManager = NotificationManagerCompat.from(context);
      nBuilder = new NotificationCompat.Builder(context);
      if (mode == Mode.CREATE)
        Settings.setHasDB(true);
    }
    this.statusCode = Error.NO_ERRORS;
    this.contextRef = new WeakReference<>(context);
    this.listener = listener;
    this.mode = mode;
  }

  public void unregisterEventListener() { listener = null; }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    if (mode == Mode.CREATE || mode == Mode.UPDATE)
      Toast.makeText(contextRef.get(), R.string.downloadingStarted, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected Void doInBackground(Void... voids) {
    switch (mode) {
      case Mode.CHECK_DB_UPDATES:
        try {
          String rawData = NetworkManager.getContentFromWeb(WebAPI.Action.GET_DB_VER);
          statusCode = Integer.parseInt(rawData.trim());
        } catch (Exception e) {
          MyLog.e(LOG_TAG, "Can't check for DB updates\n  " + e.toString());
          statusCode = Error.WEB_UNAVAILABLE;
        }
        return null;
    }

    MyLog.d(LOG_TAG, "Connecting to local database...");
    dbHelper = new DbHelper(contextRef.get());
    if (mode == Mode.UPDATE || mode == Mode.CREATE) {
      try {
        db = dbHelper.getWritableDatabase();
      } catch (SQLiteException ex) {
        MyLog.e(LOG_TAG, "Can't open local database for writing\n  " + ex.getMessage());
        statusCode = Error.DB_UNWRITABLE;
        if (mode == Mode.CREATE)
          Settings.setHasDB(false);
        return null;
      }
    } else if (mode == Mode.LOAD)
      db = dbHelper.getReadableDatabase();
    MyLog.d(LOG_TAG, "Connecting to local database completed");

    if (mode == Mode.UPDATE || mode == Mode.CREATE) {
      try {
        notifyStart();
        updateDbFromWeb();
        String action = WebAPI.LogEvent.UNKNOWN;
        switch (mode) {
          case Mode.CREATE: action = WebAPI.LogEvent.DB_CREATE; break;
          case Mode.UPDATE: action = WebAPI.LogEvent.DB_UPDATE; break;
        }
        NetworkManager.sendLog(action);
      } catch (IOException e) {
        MyLog.e(LOG_TAG, "Can't load data from web\n  " + e.getMessage());
        statusCode = Error.WEB_UNAVAILABLE;
        if (mode == Mode.CREATE)
          Settings.setHasDB(false);
        return null;
      }
    }
    else if (mode == Mode.LOAD) {
      try {
        loadLocalData();
      } catch (DBDamagedException e) {
        MyLog.e(LOG_TAG, "DB damaged: " + e.getMessage());
        statusCode = Error.DB_DAMAGED;
      }
    }

    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    if (mode == Mode.CHECK_DB_UPDATES) {
      if (listener != null)
        listener.onTaskEvent(Event.DATA_RECEIVED, statusCode);
      contextRef.clear();
      listener = null;
      return;
    }

    Integer msgId = null;
    Integer iconId = null;
    switch (statusCode) {
      case Error.DB_UNWRITABLE:
        msgId = R.string.dbTask_DbUnwritable;
        iconId = R.drawable.ic_error_24dp;
        break;
      case Error.WEB_UNAVAILABLE:
        msgId = R.string.dbTask_webUnavailable;
        iconId = R.drawable.ic_error_24dp;
        break;
    }
    if (msgId != null)
      notifyFinish(msgId, iconId, true);
    else if (listener != null) {
      listener.onTaskEvent(statusCode == Error.NO_ERRORS ?
        Event.DB_LOADED : Event.TASK_FAILED, statusCode);
    }

    db.close();
    dbHelper.close();
    contextRef.clear();
    listener = null;
  }

  private void loadLocalData() throws DBDamagedException {
    MyLog.d(LOG_TAG, "Loading parts info from local db...");
    Cursor cursor = db.query(DbHelper.TABLE_MODULES,
      null, null, null,
      null, null, DbHelper.KEY_PART_ID);
    SparseArray<Part> partInfo = new SparseArray<>(cursor.getCount());
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        int partId = cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_PART_ID));
        Part newPart = new Part(partId);
        newPart.setMinVer(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_VER)));
        newPart.setPartSize(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_SIZE)));
        newPart.setPartName(cursor.getString(cursor.getColumnIndex(DbHelper.KEY_NAME)));
        newPart.setPowerGen(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_POWER_GEN)));
        newPart.setPowerUse(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_POWER_USE)));
        newPart.setCargoCount(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_CARGO)));
        newPart.setFuelMain(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_FUEL_MAIN)));
        newPart.setFuelThr(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_FUEL_THR)));
        newPart.setStandalone(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_STANDALONE)));
        newPart.setHasNaviComp(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_HAS_NAV)));
        newPart.setSaveCargo(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_SAVE_CARGO)));
        partInfo.put(partId, newPart);
        cursor.moveToNext();
      }
      cursor.close();
      Storage.setPartInfo(partInfo);
      MyLog.d(LOG_TAG, "Parts info loaded (" + partInfo.size() + ")");
    } else {
      cursor.close();
      Storage.setPartInfo(partInfo);
      throw new DBDamagedException("Can't load parts data.");
    }
    MyLog.d(LOG_TAG, "Loading planets info from local db...");
    cursor = db.query(DbHelper.TABLE_PLANETS,
      null, null, null,
      null, null, DbHelper.KEY_ID);
    ArrayList<Planet> planetInfo = new ArrayList<>(cursor.getCount());
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        Planet planet = new Planet();
        planet.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_ID)));
        planet.setLabel(cursor.getString(cursor.getColumnIndex(DbHelper.KEY_NAME)));
        planet.setPositionX(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_POS_X)));
        planet.setPositionY(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_POS_Y)));
        planet.setObjectRadius(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_OBJECT_R)));
        planet.setOrbitRadius(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_ORBIT_R)));
        planet.setRescaleRadius(cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_RESCALE_R)));
        planetInfo.add(planet);
        cursor.moveToNext();
      }
      cursor.close();
      Storage.setPlanetInfo(planetInfo);
      MyLog.d(LOG_TAG, "Planets info loaded (" + planetInfo.size() + ")");
    } else {
      cursor.close();
      Storage.setPlanetInfo(planetInfo);
      throw new DBDamagedException("Can't load planets data.");
    }
    MyLog.d(LOG_TAG, "Loading SA versions info from local db...");
    cursor = db.query(DbHelper.TABLE_VERSIONS,
      null, null, null,
      null, null, DbHelper.KEY_VER_CODE);
    ArrayList<String> saVerNames = new ArrayList<>(cursor.getCount());
    SparseArray<String> saVerInfo = new SparseArray<>(cursor.getCount());
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        int verCode = cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_VER_CODE));
        String verName = cursor.getString(cursor.getColumnIndex(DbHelper.KEY_VER_NAME));
        saVerInfo.put(verCode, verName);
        saVerNames.add(verName);
        cursor.moveToNext();
      }
      cursor.close();
      Storage.setSAVerInfo(saVerInfo);
      Storage.setSAVerNames(saVerNames);
      MyLog.d(LOG_TAG, "SA Versions info loaded (" + saVerInfo.size() + ")");
    } else {
      cursor.close();
      Storage.setSAVerInfo(saVerInfo);
      Storage.setSAVerNames(saVerNames);
      throw new DBDamagedException("Can't load versions data.");
    }
    Settings.setDbLoaded(true);
    MyLog.d(LOG_TAG, "Database loaded");
  }

  private void updateDbFromWeb() throws IOException {
    String data = NetworkManager.getContentFromWeb(WebAPI.Action.GET_DB);
    DataBuffer buffer = new Gson().fromJson(data, DataBuffer.class);
    if (buffer == null || buffer.parts == null || buffer.parts.length == 0)
      throw new IOException("Incorrect parts data from server:\n  " + data);
    if (buffer.planets == null || buffer.planets.length == 0)
      throw new IOException("Incorrect planets data from server:\n  " + data);
    if (buffer.saVerInfo == null || buffer.saVerInfo.length == 0)
      throw new IOException("Incorrect SA versions data from server:\n  " + data);
    MyLog.d(LOG_TAG, "Database loaded from web (" + buffer.version + ")");

    int progress = 0;
    int maxProgress = buffer.parts.length + buffer.planets.length + buffer.saVerInfo.length;
    nBuilder.setProgress(maxProgress, progress, false);
    nBuilder.setContentTitle(contextRef.get().getString(R.string.dbIsUnpacking));
    nManager.notify(ID_LOADING_DB, nBuilder.build());

    MyLog.d(LOG_TAG, "Writing data into local db...");
    db.delete(DbHelper.TABLE_MODULES, null, null);
    SparseArray<Part> partInfo = new SparseArray<>(buffer.parts.length);
    for (Part part : buffer.parts) {
      cachePartData(part);
      partInfo.put(part.getPartId(), part);
      nBuilder.setProgress(maxProgress, ++progress, false);
      nManager.notify(ID_LOADING_DB, nBuilder.build());
    }
    Storage.setPartInfo(partInfo);

    db.delete(DbHelper.TABLE_PLANETS, null, null);
    ArrayList<Planet> planetInfo = new ArrayList<>(buffer.planets.length);
    for (Planet planet : buffer.planets) {
      cachePlanetData(planet);
      planetInfo.add(planet);
      nBuilder.setProgress(maxProgress, ++progress, false);
      nManager.notify(ID_LOADING_DB, nBuilder.build());
    }
    Storage.setPlanetInfo(planetInfo);

    ArrayList<String> saVerNames = new ArrayList<>(buffer.saVerInfo.length);
    SparseArray<String> saVerInfo = new SparseArray<>(buffer.saVerInfo.length);
    db.delete(DbHelper.TABLE_VERSIONS, null, null);
    for (SAVerInfo verInfo : buffer.saVerInfo) {
      cacheSAVerInfo(verInfo);
      saVerNames.add(verInfo.verName);
      saVerInfo.put(verInfo.verCode, verInfo.verName);
      nBuilder.setProgress(maxProgress, ++progress, false);
      nManager.notify(ID_LOADING_DB, nBuilder.build());
    }
    Storage.setSAVerInfo(saVerInfo);
    Storage.setSAVerNames(saVerNames);

    notifyFinish(R.string.dbTask_dbDownloaded, null, false);
    MyLog.d(LOG_TAG, "Data was successfully written into local db");
    Settings.setDbGameVer(buffer.gameVersion);
    Settings.setDbVer(buffer.version);
    Settings.setDbLoaded(true);
    if (mode == Mode.CREATE)
      Settings.setHasDB(true);
  }

  private void cachePartData (Part part) throws IOException {
    ContentValues newRow = new ContentValues();
    newRow.put(DbHelper.KEY_PART_ID, part.getPartId());
    newRow.put(DbHelper.KEY_VER, part.getMinVer());
    newRow.put(DbHelper.KEY_SIZE, part.getPartSize());
    newRow.put(DbHelper.KEY_NAME, part.getPartName());
    newRow.put(DbHelper.KEY_POWER_GEN, part.getPowerGen());
    newRow.put(DbHelper.KEY_POWER_USE, part.getPowerUse());
    newRow.put(DbHelper.KEY_CARGO, part.getCargoCount());
    newRow.put(DbHelper.KEY_FUEL_MAIN, part.getFuelMain());
    newRow.put(DbHelper.KEY_FUEL_THR, part.getFuelThr());
    newRow.put(DbHelper.KEY_STANDALONE, part.getStandalone());
    newRow.put(DbHelper.KEY_HAS_NAV, part.getHasNaviComp());
    newRow.put(DbHelper.KEY_SAVE_CARGO, part.getSaveCargo());
    MyLog.d(LOG_TAG, "Creating new row (" + part.getPartId() + ")");
    db.insert(DbHelper.TABLE_MODULES, null, newRow);
    if (Settings.isHasIconsDir() && part.isHasIcon()) {
      Utils.writeFile(String.format(SAT.ICONS_PATH, part.getPartId()),
        Base64.decode(part.getIcon(), Base64.DEFAULT));
    }
  }

  private void cachePlanetData(Planet planet) {
    ContentValues newRow = new ContentValues();
    newRow.put(DbHelper.KEY_ID, planet.getId());
    newRow.put(DbHelper.KEY_NAME, planet.getLabel());
    newRow.put(DbHelper.KEY_POS_X, planet.getPositionX());
    newRow.put(DbHelper.KEY_POS_Y, planet.getPositionY());
    newRow.put(DbHelper.KEY_OBJECT_R, planet.getObjectRadius());
    newRow.put(DbHelper.KEY_ORBIT_R, planet.getOrbitRadius());
    newRow.put(DbHelper.KEY_RESCALE_R, planet.getRescaleRadius());
    MyLog.d(LOG_TAG, "Creating new row (" + planet.getId() + ")");
    db.insert(DbHelper.TABLE_PLANETS, null, newRow);
  }

  private void cacheSAVerInfo(SAVerInfo verInfo) {
    ContentValues newRow = new ContentValues();
    newRow.put(DbHelper.KEY_VER_CODE, verInfo.getVerCode());
    newRow.put(DbHelper.KEY_VER_NAME, verInfo.getVerName());
    MyLog.d(LOG_TAG, "Creating new row (" + verInfo.getVerCode() + ")");
    db.insert(DbHelper.TABLE_VERSIONS, null, newRow);
  }

  private void notifyStart() {
    MyLog.d(LOG_TAG, "Creating notification...");
    nBuilder.setAutoCancel(false)
      .setWhen(System.currentTimeMillis())
      .setSmallIcon(R.drawable.ic_download_24dp)
      .setContentTitle(contextRef.get().getString(R.string.dbIsLoading))
      .setProgress(100, 0, true);
    nManager.notify(ID_LOADING_DB, nBuilder.build());
    MyLog.d(LOG_TAG, "Notification created");
  }

  private void notifyFinish(int msgId, Integer iconId, boolean bigText) {
    nBuilder.setProgress(0, 0, false);
    nBuilder.setContentTitle(contextRef.get().getString(R.string.app_name));
    nBuilder.setContentText(contextRef.get().getString(msgId));
    nBuilder.setDefaults(Notification.DEFAULT_ALL);
    if (bigText) {
      nBuilder.setStyle(
        new NotificationCompat.BigTextStyle().bigText(contextRef.get().getString(msgId)));
    }
    if (iconId != null)
      nBuilder.setSmallIcon(iconId);
    nManager.notify(ID_LOADING_DB, nBuilder.build());
  }

  private class DataBuffer {
    @SerializedName("version")
    private int version;
    @SerializedName("game_version")
    private int gameVersion;
    @SerializedName("parts")
    private Part[] parts;
    @SerializedName("planets")
    private Planet[] planets;
    @SerializedName("SA_versions")
    private SAVerInfo[] saVerInfo;
  }

  private class SAVerInfo {
    @SerializedName("ver_code")
    private int verCode;
    @SerializedName("ver_name")
    private String verName;

    int getVerCode() {
      return verCode;
    }
    String getVerName() {
      return verName;
    }
  }

  public interface EventListener {
    void onTaskEvent(int event, int statusCode);
  }

  private class DBDamagedException extends Exception {
    DBDamagedException(String message) { super(message); }
  }
}
