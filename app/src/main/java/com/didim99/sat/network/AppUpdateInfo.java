package com.didim99.sat.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 18.06.18.
 */
public class AppUpdateInfo {
  @SerializedName("app_ver_code")
  private int verCode;
  @SerializedName("app_ver_name")
  private String verName;

  public int getVerCode() {
    return verCode;
  }
  public String getVerName() {
    return verName;
  }
}
