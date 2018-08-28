package com.didim99.sat.sbxeditor;

import com.didim99.sat.MyLog;
import com.didim99.sat.sbxeditor.model.SBML;
import java.util.ArrayList;

/**
 * Sandbox editor configuration container
 * Created by didim99 on 21.02.18.
 */

public class SbxEditConfig {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SbxEditConfig";

  private int mode;
  //for creating
  private String fileName;
  private String sbxName;
  private Integer sbxUid;
  private boolean addMarkers;
  //for adding
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
  //for editing
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
  //editable parameters
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
  //for saving
  private boolean overwrite;
  private boolean compress;
  private int verCode;

  //open/copy existing file
  public SbxEditConfig(int mode, String fileName) {
    this.mode = mode;
    this.fileName = fileName;
  }

  //create new sandbox
  public SbxEditConfig(int mode, String sbxName, Integer sbxUid, boolean addMarkers) {
    this.mode = mode;
    this.sbxName = sbxName;
    this.sbxUid = sbxUid;
    this.addMarkers = addMarkers;
  }

  //save sandbox
  public SbxEditConfig(int mode, boolean overwrite, boolean compress, int verCode) {
    this.mode = mode;
    this.overwrite = overwrite;
    this.compress = compress;
    this.verCode = verCode;
  }

  //edit/delete station
  public SbxEditConfig(int mode, ArrayList<Station> stations) {
    this.mode = mode;
    this.stations = stations;
  }

  //copy station
  public SbxEditConfig(int mode, ArrayList<Station> stations, int positionMode,
                       float positionX, float positionY) {
    this.mode = mode;
    this.stations = stations;
    this.positionMode = positionMode;
    this.positionX = positionX;
    this.positionY = positionY;
  }

  //add module(s)
  public SbxEditConfig(int mode, int partId, float positionX, float positionY,
                       int count, float offset, int inLine) {
    this.mode = mode;
    this.partId = partId;
    this.positionX = positionX;
    this.positionY = positionY;
    this.count = count;
    this.offset = offset;
    this.inLine = inLine;
  }

  //add colony
  public SbxEditConfig(int mode, int planetId, int partId, int orbitalState,
                       int count, float orbHeight, SBML.DistanceUnit hUnits,
                       Float gap, float rotationAnge, Float movementSpeed) {
    this.mode = mode;
    this.planetId = planetId;
    this.partId = partId;
    this.orbitalState = orbitalState;
    this.count = count;
    this.orbHeight = orbHeight;
    this.units = hUnits;
    this.gap = gap;
    this.positionAnge = rotationAnge;
    this.movementSpeed = movementSpeed;
  }

  //add all modules
  public SbxEditConfig(int mode, int verCode, float positionX, float positionY,
                       float offset, int inLine) {
    this.mode = mode;
    this.verCode = verCode;
    this.positionX = positionX;
    this.positionY = positionY;
    this.offset = offset;
    this.inLine = inLine;
  }

  //add text
  public SbxEditConfig(int mode, float positionX, float positionY,
                       String text, int align, int margin) {
    this.mode = mode;
    this.positionX = positionX;
    this.positionY = positionY;
    this.text = text;
    this.align = align;
    this.margin = margin;
  }

  //add all font
  public SbxEditConfig(int mode, float positionX, float positionY, int inLine) {
    this.mode = mode;
    this.positionX = positionX;
    this.positionY = positionY;
    this.inLine = inLine;
  }

  //optimize sandbox
  public SbxEditConfig(int mode, boolean optSaveId, boolean refreshCargo, boolean refreshFuel) {
    this.mode = mode;
    this.optSaveId = optSaveId;
    this.refreshCargo = refreshCargo;
    this.refreshFuel = refreshFuel;
  }

  //play/update NaviComp/get fuel info
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
  void setText(String text) {
    this.text = text;
  }

  void setAlign(int align) {
    this.align = align;
  }

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
}
