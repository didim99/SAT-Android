package com.didim99.sat.sbxeditor.model.wrapper;

import com.google.gson.annotations.SerializedName;

/**
 * Part information container
 * Created by didim99 on 08.03.18.
 */

public class Part {
  @SerializedName("partId")
  private int partId;
  @SerializedName("minVer")
  private int minVer;
  @SerializedName("size")
  private int size;
  @SerializedName("name")
  private String name;
  @SerializedName("powerGen")
  private int powerGen;
  @SerializedName("powerUse")
  private int powerUse;
  @SerializedName("cargoCount")
  private int cargoCount;
  @SerializedName("fuelMain")
  private int fuelMain;
  @SerializedName("fuelThr")
  private int fuelThr;
  @SerializedName("standalone")
  private int standalone;
  @SerializedName("hasNaviComp")
  private int hasNaviComp;
  @SerializedName("saveCargo")
  private int saveCargo;
  @SerializedName("icon")
  private String icon;

  public Part() {}

  public Part(int partId) {
    this.partId = partId;
  }

  public boolean isHasCargo() {
    return cargoCount > 0;
  }

  public boolean isStandalone() {
    return standalone > 0;
  }

  public boolean isHasNaviComp() {
    return hasNaviComp > 0;
  }

  public boolean isHasIcon() {
    return icon != null;
  }

  //getters
  public int getPartId() {
    return partId;
  }

  public int getMinVer() {
    return minVer;
  }

  public int getPartSize() {
    return size;
  }

  public String getPartName() {
    return name;
  }

  public int getPowerGen() {
    return powerGen;
  }

  public int getPowerUse() {
    return powerUse;
  }

  public int getCargoCount() {
    return cargoCount;
  }

  public int getFuelMain() {
    return fuelMain;
  }

  public int getFuelThr() {
    return fuelThr;
  }

  public int getStandalone() {
    return standalone;
  }

  public int getHasNaviComp() {
    return hasNaviComp;
  }

  public int getSaveCargo() {
    return saveCargo;
  }

  public String getIcon() {
    return icon;
  }

  //setters
  public void setPartName(String name) {
    this.name = name;
  }

  public void setMinVer(int minVer) {
    this.minVer = minVer;
  }

  public void setPartSize(int size) {
    this.size = size;
  }

  public void setPowerGen(int powerGen) {
    this.powerGen = powerGen;
  }

  public void setPowerUse(int powerUse) {
    this.powerUse = powerUse;
  }

  public void setCargoCount(int cargoCount) {
    this.cargoCount = cargoCount;
  }

  public void setFuelMain(int fuelMain) {
    this.fuelMain = fuelMain;
  }

  public void setFuelThr(int fuelThr) {
    this.fuelThr = fuelThr;
  }

  public void setStandalone(int standalone) {
    this.standalone = standalone;
  }

  public void setHasNaviComp(int hasNaviComp) {
    this.hasNaviComp = hasNaviComp;
  }

  public void setSaveCargo(int saveCargo) {
    this.saveCargo = saveCargo;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  @Override
  public String toString() {
    return "Part info (" + partId + ")"
      + "\n  minVer: " + minVer
      + "\n  size: " + size
      + "\n  name: " + name
      + "\n  powerGen: " + powerGen
      + "\n  powerUse: " + powerUse
      + "\n  cargoCount: " + cargoCount
      + "\n  fuelMain: " + fuelMain
      + "\n  fuelThr: " + fuelThr
      + "\n  standalone: " + standalone
      + "\n  hasNaviComp: " + hasNaviComp
      + "\n  saveCargo: " + saveCargo;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Part && this.partId == ((Part) obj).partId;
  }
}
