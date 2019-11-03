package com.didim99.sat.core.sbxeditor;

import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.utils.MyLog;
import java.util.ArrayList;

/**
 * Sandbox editor configuration container
 * Created by didim99 on 21.02.18.
 */

public class SbxEditConfig {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SbxEditConfig";

  private int mode;
  //Create Sandbox
  private String fileName;
  private String sbxName;
  private Integer sbxUid;
  private boolean addMarkers;
  //Add items to Sandbox
  private int partId;
  private int count;
  private float offset;
  private int inLine;
  private int planetId;
  private int orbitalState;
  private float orbHeight;
  private Float gap;
  private String text;
  private int align;
  private Integer margin;
  private SBML.DistanceUnit units;
  //Edit Sandbox
  private ArrayList<Station> stations;
  private int editMode;
  private int startSaveId;
  private int hideMode;
  private float alphaValue;
  private float extendFuelValue;
  private int positionMode;
  private float positionX;
  private float positionY;
  private float positionAnge;
  private Float rotationBaseX;
  private Float rotationBaseY;
  private int movementMode;
  private float movementDirection;
  private float movementSpeed;
  private float movementRotation;
  //Editable parameters
  private boolean changeSaveId;
  private boolean changeVisibility;
  private boolean changeAlpha;
  private boolean changePosition;
  private boolean changeAngle;
  private boolean rotationCommonBase;
  private boolean rotationCustomBase;
  private boolean changeMovement;
  private boolean changeMovementDirection;
  private boolean changeMovementSpeed;
  private boolean changeMovementRotation;
  private boolean refreshCargo;
  private boolean refreshFuel;
  private boolean extendFuel;
  private boolean optSaveId;
  //Save Sandbox
  private boolean overwrite;
  private boolean compress;
  private int verCode;

  public SbxEditConfig(int mode) {
    this.mode = mode;
  }

  public void log() {
    MyLog.d(LOG_TAG, "Current config: "
      + "\n  mode: " + mode
      + "\n -----"
      + "\n  changeSaveId: " + changeSaveId
      + "\n  changeVisibility: " + changeVisibility
      + "\n  changeAlpha: " + changeAlpha
      + "\n  changePosition: " + changePosition
      + "\n  changeAngle: " + changeAngle
      + "\n  rotationCommonBase: " + rotationCommonBase
      + "\n  rotationCustomBase: " + rotationCustomBase
      + "\n  changeMovement: " + changeMovement
      + "\n  changeMovementDirection: " + changeMovementDirection
      + "\n  changeMovementSpeed: " + changeMovementSpeed
      + "\n  changeMovementRotation: " + changeMovementRotation
      + "\n  refreshCargo: " + refreshCargo
      + "\n  refreshFuel: " + refreshFuel
      + "\n  extendFuel: " + extendFuel
      + "\n  optSaveId: " + optSaveId
      + "\n -----"
      + "\n  partId: " + partId
      + "\n  count: " + count
      + "\n  offset: " + offset
      + "\n  inLine: " + inLine
      + "\n  planetId: " + planetId
      + "\n  orbitalState: " + orbitalState
      + "\n  orbHeight: " + orbHeight
      + "\n  gap: " + gap
      + "\n  text: " + text
      + "\n  align: " + align
      + "\n  margin: " + margin
      + "\n  units: " + units
      + "\n -----"
      + "\n  stations: " + stations.size() + " " + stations
      + "\n  editMode: " + editMode
      + "\n  startSaveId: " + startSaveId
      + "\n  hideMode: " + hideMode
      + "\n  alphaValue: " + alphaValue
      + "\n  extendFuelValue: " + extendFuelValue
      + "\n  positionMode: " + positionMode
      + "\n  position: " + positionX + "; " + positionY
      + "\n  positionAnge: " + positionAnge
      + "\n  rotationBase: " + rotationBaseX + "; " + rotationBaseY
      + "\n  movementMode: " + movementMode
      + "\n  movementDirection: " + movementDirection
      + "\n  movementSpeed: " + movementSpeed
      + "\n  movementRotation: " + movementRotation
    );
  }

  //getters
  int getMode() {
    return mode;
  }

  String getFileName() {
    return fileName;
  }

  String getSbxName() {
    return sbxName;
  }

  Integer getSbxUid() {
    return sbxUid;
  }

  boolean isAddMarkers() {
    return addMarkers;
  }

  public boolean isChangeSaveId() {
    return changeSaveId;
  }

  boolean isChangeVisibility() {
    return changeVisibility;
  }

  public boolean isChangeAlpha() {
    return changeAlpha;
  }

  public boolean isChangePosition() {
    return changePosition;
  }

  public boolean isChangeAngle() {
    return changeAngle;
  }

  public boolean isRotationCommonBase() {
    return rotationCommonBase;
  }

  public boolean isRotationCustomBase() {
    return rotationCustomBase;
  }

  public boolean isChangeMovement() {
    return changeMovement;
  }

  public boolean isChangeMovementDirection() {
    return changeMovementDirection;
  }

  public boolean isChangeMovementSpeed() {
    return changeMovementSpeed;
  }

  public boolean isChangeMovementRotation() {
    return changeMovementRotation;
  }

  boolean isRefreshCargo() {
    return refreshCargo;
  }

  boolean isRefreshFuel() {
    return refreshFuel;
  }

  public boolean isExtendFuel() {
    return extendFuel;
  }

  int getPartId() {
    return partId;
  }

  int getCount() {
    return count;
  }

  float getOffset() {
    return offset;
  }

  int getInLine() {
    return inLine;
  }

  int getPlanetId() {
    return planetId;
  }

  int getOrbitalState() {
    return orbitalState;
  }

  float getOrbHeight() {
    return orbHeight;
  }

  Float getGap() {
    return gap;
  }

  String getText() {
    return text;
  }

  int getAlign() {
    return align;
  }

  Integer getMargin() {
    return margin;
  }

  SBML.DistanceUnit getUnits() {
    return units;
  }

  int getEditMode() {
    return editMode;
  }

  public ArrayList<Station> getStations() {
    return stations;
  }

  int getStartSaveId() {
    return startSaveId;
  }

  int getHideMode() {
    return hideMode;
  }

  float getAlphaValue() {
    return alphaValue;
  }

  float getExtendFuelValue() {
    return extendFuelValue;
  }

  int getPositionMode() {
    return positionMode;
  }

  float getPositionX() {
    return positionX;
  }

  float getPositionY() {
    return positionY;
  }

  float getPositionAnge() {
    return positionAnge;
  }

  Float getRotationBaseX() {
    return rotationBaseX;
  }

  Float getRotationBaseY() {
    return rotationBaseY;
  }

  public int getMovementMode() {
    return movementMode;
  }

  float getMovementDirection() {
    return movementDirection;
  }

  float getMovementSpeed() {
    return movementSpeed;
  }

  float getMovementRotation() {
    return movementRotation;
  }

  boolean getOptSaveId() {
    return optSaveId;
  }

  boolean getOverwrite() {
    return overwrite;
  }

  boolean getCompress() {
    return compress;
  }

  int getVerCode() {
    return verCode;
  }

  //setters
  public void setEditMode(int editMode) {
    this.editMode = editMode;
  }

  public void setStartSaveId(int startSaveId) {
    this.startSaveId = startSaveId;
  }

  public void setHideMode(int hideMode) {
    this.hideMode = hideMode;
  }

  public void setAlphaValue(float alphaValue) {
    this.alphaValue = alphaValue;
  }

  public void setExtendFuelValue(float extendFuelValue) {
    this.extendFuelValue = extendFuelValue;
  }

  public void setPositionMode(int positionMode) {
    this.positionMode = positionMode;
  }

  public void setPositionX(float positionX) {
    this.positionX = positionX;
  }

  public void setPositionY(float positionY) {
    this.positionY = positionY;
  }

  public void setPositionAnge(float positionAnge) {
    this.positionAnge = positionAnge;
  }

  public void setRotationBaseX(float rotationBaseX) {
    this.rotationBaseX = rotationBaseX;
  }

  public void setRotationBaseY(float rotationBaseY) {
    this.rotationBaseY = rotationBaseY;
  }

  public void setMovementMode(int movementMode) {
    this.movementMode = movementMode;
  }

  public void setMovementDirection(float movementDirection) {
    this.movementDirection = movementDirection;
  }

  public void setMovementSpeed(float movementSpeed) {
    this.movementSpeed = movementSpeed;
  }

  public void setMovementRotation(float movementRotation) {
    this.movementRotation = movementRotation;
  }

  public void setChangeSaveId(boolean changeSaveId) {
    this.changeSaveId = changeSaveId;
  }

  public void setChangeVisibility(boolean changeVisibility) {
    this.changeVisibility = changeVisibility;
  }

  public void setChangeAlpha(boolean changeAlpha) {
    this.changeAlpha = changeAlpha;
  }

  public void setChangePosition(boolean changePosition) {
    this.changePosition = changePosition;
  }

  public void setChangeAngle(boolean changeAngle) {
    this.changeAngle = changeAngle;
  }

  public void setRotationCommonBase(boolean rotationCommonBase) {
    this.rotationCommonBase = rotationCommonBase;
  }

  public void setRotationCustomBase(boolean rotationCustomBase) {
    this.rotationCustomBase = rotationCustomBase;
  }

  public void setChangeMovement(boolean changeMovement) {
    this.changeMovement = changeMovement;
  }

  public void setChangeMovementDirection(boolean changeMovementDirection) {
    this.changeMovementDirection = changeMovementDirection;
  }

  public void setChangeMovementSpeed(boolean changeMovementSpeed) {
    this.changeMovementSpeed = changeMovementSpeed;
  }

  public void setChangeMovementRotation(boolean changeMovementRotation) {
    this.changeMovementRotation = changeMovementRotation;
  }

  public void setRefreshCargo(boolean refreshCargo) {
    this.refreshCargo = refreshCargo;
  }

  public void setRefreshFuel(boolean refreshFuel) {
    this.refreshFuel = refreshFuel;
  }

  public void setExtendFuel(boolean extendFuel) {
    this.extendFuel = extendFuel;
  }

  public static class Builder {
    private SbxEditConfig config;

    public Builder(int mode) {
      config = new SbxEditConfig(mode);
    }

    public Builder(SbxEditConfig src) {
      config = src;
    }

    public Builder setFileName(String fileName) {
      config.fileName = fileName;
      return this;
    }

    public Builder setSbxName(String sbxName) {
      config.sbxName = sbxName;
      return this;
    }

    public Builder setSbxUid(Integer sbxUid) {
      config.sbxUid = sbxUid;
      return this;
    }

    public Builder setAddMarkers(boolean addMarkers) {
      config.addMarkers = addMarkers;
      return this;
    }

    public Builder setOverwrite(boolean overwrite) {
      config.overwrite = overwrite;
      return this;
    }

    public Builder setCompress(boolean compress) {
      config.compress = compress;
      return this;
    }

    public Builder setVerCode(int verCode) {
      config.verCode = verCode;
      return this;
    }

    public Builder setStations(ArrayList<Station> stations) {
      config.stations = stations;
      return this;
    }

    public Builder setPositionMode(int positionMode) {
      config.positionMode = positionMode;
      return this;
    }

    public Builder setPositionX(float positionX) {
      config.positionX = positionX;
      return this;
    }

    public Builder setPositionY(float positionY) {
      config.positionY = positionY;
      return this;
    }

    public Builder setPositionAngle(float positionAnge) {
      config.positionAnge = positionAnge;
      return this;
    }

    public Builder setMovementSpeed(Float movementSpeed) {
      config.movementSpeed = movementSpeed;
      return this;
    }

    public Builder setPartId(int partId) {
      config.partId = partId;
      return this;
    }

    public Builder setPlanetId(int planetId) {
      config.planetId = planetId;
      return this;
    }

    public Builder setOrbitalState(int orbitalState) {
      config.orbitalState = orbitalState;
      return this;
    }

    public Builder setOrbHeight(float orbHeight) {
      config.orbHeight = orbHeight;
      return this;
    }

    public Builder setUnits(SBML.DistanceUnit units) {
      config.units = units;
      return this;
    }

    public Builder setGap(Float gap) {
      config.gap = gap;
      return this;
    }

    public Builder setCount(int count) {
      config.count = count;
      return this;
    }

    public Builder setOffset(float offset) {
      config.offset = offset;
      return this;
    }

    public Builder setInLine(int inLine) {
      config.inLine = inLine;
      return this;
    }

    public Builder setText(String text) {
      config.text = text;
      return this;
    }

    public Builder setAlign(int align) {
      config.align = align;
      return this;
    }

    public Builder setMargin(int margin) {
      config.margin = margin;
      return this;
    }

    public Builder setOptSaveId(boolean optSaveId) {
      config.optSaveId = optSaveId;
      return this;
    }

    public Builder setRefreshCargo(boolean refreshCargo) {
      config.refreshCargo = refreshCargo;
      return this;
    }

    public Builder setRefreshFuel(boolean refreshFuel) {
      config.refreshFuel = refreshFuel;
      return this;
    }

    public SbxEditConfig build() {
      return config;
    }
  }
}
