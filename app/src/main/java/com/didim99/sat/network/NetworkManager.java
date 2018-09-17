package com.didim99.sat.network;

import android.os.AsyncTask;
import com.didim99.sat.BuildConfig;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.settings.Settings;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * synchronous/asynchronous network access manager
 * Created by didim99 on 26.07.18.
 */
public class NetworkManager extends AsyncTask<Void, Void, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NetMgr";

  public static final class Status {
    public static final int OK = 0;
    private static final int WEB_UNAVAILABLE = -1;
  }

  private enum Mode { GET_DATA, SEND_LOG }

  private static boolean initCompleted = false;
  private static WebAPI webAPI;

  private Mode mode;
  private EventListener listener;
  private String action, data;
  private int status;

  public NetworkManager(EventListener listener, String action) {
    this.listener = listener;
    this.action = action;
    this.mode = Mode.GET_DATA;
    this.status = Status.OK;
  }

  public NetworkManager(String logEvent) {
    this.action = logEvent;
    this.mode = Mode.SEND_LOG;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    switch (mode) {
      case GET_DATA:
        try {
          data = NetworkManager.getContentFromWeb(action);
        } catch (IOException e) {
          MyLog.e(LOG_TAG, "Can't load data from web\n  " + e.toString());
          status = Status.WEB_UNAVAILABLE;
        }
        break;
      case SEND_LOG:
        sendLog(action);
        break;
    }

    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    if (mode == Mode.GET_DATA)
      listener.onDataReceived(action, status, data);
    listener = null;
  }

  public static void init() {
    OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(5, TimeUnit.SECONDS).build();
    webAPI = new Retrofit.Builder().baseUrl(WebAPI.URL_BASE)
      .client(client).build().create(WebAPI.class);
    initCompleted = true;
  }

  public static String getContentFromWeb(String action) throws IOException {
    if (!initCompleted) init();
    MyLog.d(LOG_TAG, "Trying to get data from web-server ("+ action +")...");
    Response<ResponseBody> response = webAPI.getData(action).execute();
    MyLog.d(LOG_TAG, response.toString());
    if (response.isSuccessful()) {
      ResponseBody body = response.body();
      if (body != null) {
        String data = body.string();
        MyLog.d(LOG_TAG, "Data loaded (" + data.length() + " bytes)");
        return data;
      }
      else throw new IOException("Response body is null");
    } else {
      throw new IOException("Connection error ("
        + response.code() + " " + response.message() + ")");
    }
  }

  public static void sendLog(String event) {
    try {
      if (!initCompleted) init();
      MyLog.d(LOG_TAG, "Sending log... ");
      Settings.loadDeviceInfo();
      Map<String, String> data = new HashMap<>();
      data.put("vendor", Settings.getDevVendor());
      data.put("model", Settings.getDevModel());
      data.put("res", Settings.getDevRes());
      data.put("osver", Settings.getDevOsVer());
      data.put("abi", Settings.getDevAbi());
      data.put("app_ver", Utils.base64Encode(BuildConfig.VERSION_NAME));
      data.put("uuid", Settings.getDevId());
      data.put("action", Utils.base64Encode(event));

      Response<Void> response = webAPI.sendLog(data).execute();
      MyLog.d(LOG_TAG, response.toString());
      if (response.isSuccessful())
        MyLog.d(LOG_TAG, "Log sent successful");
      else {
        throw new IOException("Connection error ("
          + response.code() + " " + response.message() + ")");
      }
    } catch (IOException e) {
      MyLog.w(LOG_TAG, "can't send log\n  " + e.toString());
    }
  }

  public interface EventListener {
    void onDataReceived(String action, int status, String data);
  }
}
