package com.didim99.sat.network;

import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Web-server API wrapper for Retrofit
 * Created by didim99 on 24.07.18.
 */

public interface WebAPI {
  String URL_GET_APK = "https://didim.eclabs.ru/dl/SAT.apk";
  String URL_BASE = "https://didim.eclabs.ru/";

  final class Action {
    public static final String LAST_APP_VER = "last_app_ver";
    public static final String GET_DB_VER = "get_db_ver";
    public static final String GET_DB = "get_db";
  }

  final class LogEvent {
    public static final String DB_IGNORE = "db_ignore";
    public static final String DB_CREATE = "db_create";
    public static final String DB_UPDATE = "db_update";
    public static final String DB_USE_DAMAGED = "db_use_damaged";
    public static final String DEV_MODE = "dev_mode_enable";
    public static final String UNKNOWN = "unknown";
  }

  @GET("/?id=sagency.android")
  Call<ResponseBody> getData(@Query("action") String action);

  @FormUrlEncoded
  @POST("/?id=android.log&agent=sat")
  Call<Void> sendLog(@FieldMap Map<String, String> data);
}
