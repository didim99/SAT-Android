package com.didim99.sat.sbxeditor.model;

import android.support.annotation.NonNull;
import com.didim99.sat.MyLog;
import com.didim99.sat.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Module container class
 * Created by didim99 on 12.02.18.
 */

public class Module implements Comparable, Cloneable {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_module";

  //standard module fields
  private int saveId;
  private int partId;
  private Integer debugId;
  private Integer state;
  private Integer effectCounter;
  private Integer applyGravity;
  private Float textureAlpha;
  private Float temperature;
  private Integer showInSelector;
  private ArrayList<CargoItem> cargo;
  private float[] air;
  private int[] powerState;
  private String navicompState;
  private int[] collisionState;
  private float[] position;
  private float[] movement;
  private Integer launchTimestamp;
  private Integer lastUsedTimestamp;
  private String orbitalState;
  private float[] fuelLevels;
  private String solarPanelState;
  private String sidePanelState;
  private Integer parentModule;
  private Integer payloadParent;
  private String transponderId;
  private String transponderName;
  private Integer transponderSelected;
  private ArrayList<DockPoint> dock;
  private int cargoECounter = 0;
  private int dockECounter = 0;

  public Module() {}

  public Module(int saveId, int partId) {
    this.saveId = saveId;
    this.partId = partId;
    this.position = new float[3];
    this.showInSelector = SBML.VISIBILITY_MODE_VISIBLE;
  }

  public Module(int saveId, Part part) {
    this(saveId, part.getPartId());
    if (part.isHasCargo()) {
      for (int id = 0; id < part.getCargoCount(); id++)
        addCargo(id, SBML.CARGO_ID_BAT);
    }
  }

  public void setValue(String key, ArrayList<String> args) {
    String value = null;

    if (args.size() == 1)
      value = args.get(SBML.START_INDEX);

    switch (key) {
      case SBML.KEY_SAVE_ID:
        this.saveId = Integer.parseInt(value);
        break;
      case SBML.KEY_PART_ID:
        this.partId = Integer.parseInt(value);
        break;
      case SBML.KEY_DEBUG_ID:
        this.debugId = Integer.parseInt(value);
        break;
      case SBML.KEY_STATE:
        this.state = Integer.parseInt(value);
        break;
      case SBML.KEY_EFFECT_COUNTER:
        this.effectCounter = Integer.parseInt(value);
        break;
      case SBML.KEY_APPLY_GRAVITY:
        this.applyGravity = Integer.parseInt(value);
        break;
      case SBML.KEY_TEXTURE_ALPHA:
        this.textureAlpha = Float.parseFloat(value);
        break;
      case SBML.KEY_TEMPERATURE:
        this.temperature = Float.parseFloat(value);
        break;
      case SBML.KEY_SHOW_IN_SELECTOR:
        this.showInSelector = Integer.parseInt(value);
        break;
      case SBML.KEY_CARGO_ITEM:
        if (cargo == null)
          cargo = new ArrayList<>();
        cargo.add(new CargoItem(args));
        break;
      case SBML.KEY_AIR:
        this.air = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.KEY_POWER_SATATE:
        this.powerState = stringArrayToIntArray(args);
        break;
      case SBML.KEY_NAVICOMP_SATATE:
        this.navicompState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.KEY_COLLISION_SATATE:
        this.collisionState = stringArrayToIntArray(args);
        break;
      case SBML.KEY_POSITION:
        this.position = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.KEY_MOVEMENT:
        this.movement = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.KEY_LAUNCH_TIMESTAMP:
        this.launchTimestamp = Integer.parseInt(value);
        break;
      case SBML.KEY_LAST_USED_TIMESTAMP:
        this.lastUsedTimestamp = Integer.parseInt(value);
        break;
      case SBML.KEY_ORBITAL_SATATE:
        this.orbitalState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.KEY_FUEL_LEVELS:
        this.fuelLevels = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.KEY_SOLAR_PANEL_SATATE:
        this.solarPanelState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.KEY_SIDE_PANEL_SATATE:
        this.sidePanelState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.KEY_PARENT_MODULE:
        this.parentModule = Integer.parseInt(value);
        break;
      case SBML.KEY_PAYLOAD_PARENT:
        this.payloadParent = Integer.parseInt(value);
        break;
      case SBML.KEY_TRANSPONDER_ID:
        this.transponderId = value;
        break;
      case SBML.KEY_TRANSPONDER_NAME:
        this.transponderName = value;
        break;
      case SBML.KEY_TRANSPONDER_SELECTED:
        this.transponderSelected = Integer.parseInt(value);
        break;
      case SBML.KEY_DOCK_POINT:
        if (dock == null)
          dock = new ArrayList<>();
        dock.add(new DockPoint(args));
        break;
    }
  }

  public String getValue(String key) {
    switch (key) {
      case SBML.KEY_SAVE_ID:
        return String.valueOf(saveId);
      case SBML.KEY_PART_ID:
        return String.valueOf(partId);
      case SBML.KEY_DEBUG_ID:
        return Utils.intToString(debugId);
      case SBML.KEY_STATE:
        return Utils.intToString(state);
      case SBML.KEY_EFFECT_COUNTER:
        return Utils.intToString(effectCounter);
      case SBML.KEY_APPLY_GRAVITY:
        return Utils.intToString(applyGravity);
      case SBML.KEY_TEXTURE_ALPHA:
        return Utils.floatToString(textureAlpha, SBML.PREC_DEFAULT);
      case SBML.KEY_TEMPERATURE:
        return Utils.floatToString(temperature, SBML.PREC_DEFAULT);
      case SBML.KEY_SHOW_IN_SELECTOR:
        return Utils.intToString(showInSelector);
      case SBML.KEY_CARGO_ITEM:
        if (cargo == null || cargo.isEmpty())
          return null;
        if (cargoECounter == cargo.size()) {
          cargoECounter = 0;
          return null;
        }
        return cargo.get(cargoECounter++).export();
      case SBML.KEY_AIR:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(air, SBML.PREC_DEFAULT));
      case SBML.KEY_POWER_SATATE:
        return Utils.joinStr(SBML.VAL_SEP, IntArrayToStringArray(powerState));
      case SBML.KEY_NAVICOMP_SATATE:
        return navicompState;
      case SBML.KEY_COLLISION_SATATE:
        return Utils.joinStr(SBML.VAL_SEP, IntArrayToStringArray(collisionState));
      case SBML.KEY_POSITION:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(position, SBML.PREC_COORDS));
      case SBML.KEY_MOVEMENT:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(movement, SBML.PREC_COORDS));
      case SBML.KEY_LAUNCH_TIMESTAMP:
        return Utils.intToString(launchTimestamp);
      case SBML.KEY_LAST_USED_TIMESTAMP:
        return Utils.intToString(lastUsedTimestamp);
      case SBML.KEY_ORBITAL_SATATE:
        return orbitalState;
      case SBML.KEY_FUEL_LEVELS:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(fuelLevels, SBML.PREC_DEFAULT));
      case SBML.KEY_SOLAR_PANEL_SATATE:
        return solarPanelState;
      case SBML.KEY_SIDE_PANEL_SATATE:
        return sidePanelState;
      case SBML.KEY_PARENT_MODULE:
        return Utils.intToString(parentModule);
      case SBML.KEY_PAYLOAD_PARENT:
        return Utils.intToString(payloadParent);
      case SBML.KEY_TRANSPONDER_ID:
        return transponderId;
      case SBML.KEY_TRANSPONDER_NAME:
        return transponderName;
      case SBML.KEY_TRANSPONDER_SELECTED:
        return Utils.intToString(transponderSelected);
      case SBML.KEY_DOCK_POINT:
        if (dock == null || dock.isEmpty())
          return null;
        if (dockECounter == dock.size()) {
          dockECounter = 0;
          return null;
        }
        return dock.get(dockECounter++).toString();
      default:
        return null;
    }
  }

  public void addCargo(int id, int resId) {
    if (cargo == null)
      cargo = new ArrayList<>();
    cargo.add(new CargoItem(id, resId, SBML.CARGO_FULL_VALUE));
  }

  public void refreshCargo() {
    if (cargo != null) {
      for (CargoItem item : cargo)
        item.setFull();
    }
  }

  public void setFuel(boolean refresh, boolean extend, float extendArg) {
    if (fuelLevels != null) {
      if (extend) {
        fuelLevels[SBML.FUEL_INDEX_MAIN_CAP] *= extendArg;
        fuelLevels[SBML.FUEL_INDEX_THR_CAP] *= extendArg;
      }
      if (refresh) {
        fuelLevels[SBML.FUEL_INDEX_MAIN_VAL] = fuelLevels[SBML.FUEL_INDEX_MAIN_CAP];
        fuelLevels[SBML.FUEL_INDEX_THR_VAL] = fuelLevels[SBML.FUEL_INDEX_THR_CAP];
      }
    }
  }

  public void setTimes(int timestamp) {
    launchTimestamp = timestamp;
    lastUsedTimestamp = timestamp;
  }

  public void addDock(DockPoint point) {
    if (dock == null)
      dock = new ArrayList<>(4);
    dock.add(point);
  }

  public void log() {
    MyLog.d(LOG_TAG, "Module (" + hashCode() + ")"
      + "\n  save id: " + saveId
      + "\n  part id: " + partId
      + "\n  debug id: " + debugId
      + "\n  state: " + state
      + "\n  effect counter: " + effectCounter
      + "\n  apply gravity: " + applyGravity
      + "\n  texture alpha: " + textureAlpha
      + "\n  temperature: " + temperature
      + "\n  show in selector: " + showInSelector
      + "\n  cargo item: " + cargo
      + "\n  air: " + Arrays.toString(air)
      + "\n  power state: " + Arrays.toString(powerState)
      + "\n  navicomp state: " + navicompState
      + "\n  collision state: " + Arrays.toString(collisionState)
      + "\n  position: " + Arrays.toString(position)
      + "\n  movement: " + Arrays.toString(movement)
      + "\n  launch timestamp: " + launchTimestamp
      + "\n  last used timestamp: " + lastUsedTimestamp
      + "\n  orbital state: " + orbitalState
      + "\n  fuel levels: " + Arrays.toString(fuelLevels)
      + "\n  solar panel state: " + solarPanelState
      + "\n  side panel state: " + sidePanelState
      + "\n  parent module: " + parentModule
      + "\n  payload parent: " + payloadParent
      + "\n  transponder id: " + transponderId
      + "\n  transponder name: " + transponderName
      + "\n  transponder selected: " + transponderSelected
      + "\n  dock point: " + dock
    );
  }

  public boolean isVisible() {
    return showInSelector == SBML.VISIBILITY_MODE_VISIBLE;
  }

  public boolean hasMovement() {
    return movement != null && movement[SBML.MOVEMENT_INDEX_SPEED] > 0;
  }

  public boolean hasRotation() {
    return movement != null && movement[SBML.MOVEMENT_INDEX_ROTATION_REAL] != 0;
  }

  public boolean hasFuel() {
    return fuelLevels != null && fuelLevels.length > 0;
  }

  public boolean hasTransponderName() {
    return transponderName != null && !transponderName.isEmpty();
  }

  public boolean isDocked() {
    if (dock != null && !dock.isEmpty()) {
      for (DockPoint point : dock)
        if (point.isDocked()) return true;
    }
    return false;
  }

  //Getters
  public int getSaveId() {
    return saveId;
  }

  public int getPartId() {
    return partId;
  }

  public float getPositionX() {
    return position[SBML.POS_INDEX_X];
  }

  public float getPositionY() {
    return position[SBML.POS_INDEX_Y];
  }

  public float getPositionAngle() {
    return position[SBML.POS_INDEX_ANGLE];
  }

  public float getMovementDirection() {
    return movement[SBML.MOVEMENT_INDEX_DIRECTION];
  }

  public float getMovementSpeed() {
    return movement[SBML.MOVEMENT_INDEX_SPEED];
  }

  public float getMovementRotation() {
    return movement[SBML.MOVEMENT_INDEX_ROTATION_REAL];
  }

  public float getMainFuelCapacity() {
    return fuelLevels[SBML.FUEL_INDEX_MAIN_CAP];
  }

  public float getThtFuelCapacity() {
    return fuelLevels[SBML.FUEL_INDEX_THR_CAP];
  }

  public Integer getParentModule() {
    return parentModule;
  }

  public Integer getPayloadParent() {
    return payloadParent;
  }

  public String getTransponderName() {
    return transponderName;
  }

  public ArrayList<DockPoint> getDock() {
    return dock;
  }

  //Setters
  public void setSaveId(int newSaveId) {
    this.saveId = newSaveId;
  }

  public void setTextureAlpha(float alpha) {
    this.textureAlpha = alpha;
  }

  public void setShowInSelector(int show) {
    this.showInSelector = show;
  }

  public void setPosition(float posX, float posY, float angle) {
    position[SBML.POS_INDEX_X] = posX;
    position[SBML.POS_INDEX_Y] = posY;
    position[SBML.POS_INDEX_ANGLE] = angle;
  }

  public void setPositionX(float posX) {
    position[SBML.POS_INDEX_X] = posX;
  }

  public void setPositionY(float posY) {
    position[SBML.POS_INDEX_Y] = posY;
  }

  public void setPositionAngle(float angle) {
    position[SBML.POS_INDEX_ANGLE] = angle;
  }

  public void setMovement(float[] movement) {
    this.movement = movement;
  }

  public void setMovement(float direction, float speed, float rotation) {
    this.movement = new float[4];
    movement[SBML.MOVEMENT_INDEX_DIRECTION] = direction;
    movement[SBML.MOVEMENT_INDEX_SPEED] = speed;
    movement[SBML.MOVEMENT_INDEX_ROTATION_VISUAL] = rotation;
    movement[SBML.MOVEMENT_INDEX_ROTATION_REAL] = rotation;
  }

  public void setMovementDirection(float direction) {
    movement[SBML.MOVEMENT_INDEX_DIRECTION] = direction;
  }

  public void setMovementSpeed(float speed) {
    movement[SBML.MOVEMENT_INDEX_SPEED] = speed;
  }

  public void setMovementRotation(float rotation) {
    movement[SBML.MOVEMENT_INDEX_ROTATION_VISUAL] = rotation;
    movement[SBML.MOVEMENT_INDEX_ROTATION_REAL] = rotation;
  }

  public void setOrbitalState(String orbitalState) {
    this.orbitalState = orbitalState;
  }

  public void setOrbitalState(int planetId, int state, float height, float angle) {
    this.orbitalState = String.format(Locale.US,
      "%d,%d,%.2f,%.2f", state, planetId, height, angle);
  }

  public void setParentModule(Integer parentModule) {
    this.parentModule = parentModule;
  }

  public void setPayloadParent(Integer payloadParent) {
    this.payloadParent = payloadParent;
  }

  public void setDock(ArrayList<DockPoint> dock) {
    this.dock = dock;
  }

  private static int[] stringArrayToIntArray (ArrayList<String> args) {
    if (args == null || args.isEmpty())
      return null;
    int[] result = new int[args.size()];
    for (int i = 0; i < args.size(); i++) {
      result[i] = Integer.parseInt(args.get(i));
    }
    return result;
  }

  private static String[] IntArrayToStringArray (int[] args) {
    if (args == null || args.length == 0)
      return null;
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = String.valueOf(args[i]);
    }
    return result;
  }

  @Override
  public int compareTo(@NonNull Object o) {
    return saveId - ((Module) o).saveId;
  }

  @Override
  public Module clone() throws CloneNotSupportedException {
    Module clone = (Module) super.clone();
    if (this.air != null)
      clone.air = this.air.clone();
    if (this.powerState != null)
      clone.powerState = this.powerState.clone();
    if (this.collisionState != null)
      clone.collisionState = this.collisionState.clone();
    if (this.position != null)
      clone.position = this.position.clone();
    if (this.movement != null)
      clone.movement = this.movement.clone();
    if (this.fuelLevels != null)
      clone.fuelLevels = this.fuelLevels.clone();
    if (this.cargo != null) {
      clone.cargo = new ArrayList<>(this.cargo.size());
      for (CargoItem item : this.cargo)
        clone.cargo.add(item.clone());
    }
    if (this.dock != null) {
      clone.dock = new ArrayList<>(this.dock.size());
      for (DockPoint item : this.dock)
        clone.dock.add(item.clone());
    }
    return clone;
  }

  static class CargoItem implements Cloneable {
    int id;
    int resId;
    float value;

    CargoItem(ArrayList<String> args) {
      this.id = Integer.parseInt(args.get(0));
      this.resId = Integer.parseInt(args.get(1));
      this.value = Float.parseFloat(args.get(2));
    }

    CargoItem(int id, int resId, float value) {
      this.id = id;
      this.resId = resId;
      this.value = value;
    }

    void setFull() {
      this.value = SBML.CARGO_FULL_VALUE;
    }

    String export() {
      return id + SBML.VAL_SEP + resId + SBML.VAL_SEP
        + Utils.floatToString(value, SBML.PREC_DEFAULT);
    }

    @Override
    public String toString() {
      return "[" + id + "," + resId + "," + value + "]";
    }

    @Override
    protected CargoItem clone() throws CloneNotSupportedException {
      return (CargoItem) super.clone();
    }
  }

  public static class DockPoint implements Cloneable {
    private int[] state;

    DockPoint(ArrayList<String> args) {
      this.state = stringArrayToIntArray(args);
    }

    public DockPoint(int id, int power, int fuel, int door, int slaveId, Integer slavePort) {
      if (slavePort == null)
        state = new int[5];
      else {
        state = new int[6];
        state[SBML.DOCK_INDEX_SLAVE_PORT] = slavePort;
      }
      state[SBML.DOCK_INDEX_ID] = id;
      state[SBML.DOCK_INDEX_POWER] = power;
      state[SBML.DOCK_INDEX_FUEL] = fuel;
      state[SBML.DOCK_INDEX_DOOR] = door;
      state[SBML.DOCK_INDEX_SLAVE_ID] = slaveId;
    }

    public boolean isDocked() {
      return state[SBML.DOCK_INDEX_SLAVE_ID] != SBML.DOCK_STATE_UNDOCKED;
    }

    public int getSlaveId() {
      return state[SBML.DOCK_INDEX_SLAVE_ID];
    }

    public void setSlaveId(int slaveId) {
      state[SBML.DOCK_INDEX_SLAVE_ID] = slaveId;
    }

    @Override
    public String toString() {
      return Utils.joinStr(SBML.VAL_SEP, IntArrayToStringArray(state));
    }

    @Override
    protected DockPoint clone() throws CloneNotSupportedException {
      DockPoint clone = (DockPoint) super.clone();
      clone.state = this.state.clone();
      return clone;
    }
  }

  public static class DefaultComparator implements Comparator<Module> {
    @Override
    public int compare(Module m1, Module m2) {
      return Integer.compare(m1.saveId, m2.saveId);
    }
  }
}
