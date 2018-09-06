package com.didim99.sat.sbxeditor.model.wrapper;

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
  //Internal service fields
  private int cargoECounter = 0;
  private int dockECounter = 0;

  public Module() {}

  public Module(int saveId, int partId) {
    this.saveId = saveId;
    this.partId = partId;
    this.position = new float[3];
    this.showInSelector = SBML.Visibility.VISIBLE;
  }

  public Module(int saveId, Part part) {
    this(saveId, part.getPartId());
    if (part.isHasCargo()) {
      for (int id = 0; id < part.getCargoCount(); id++)
        addCargo(id, SBML.CargoID.BAT);
    }
  }

  public void setValue(String key, ArrayList<String> args) {
    String value = null;

    if (args.size() == 1)
      value = args.get(0);

    switch (key) {
      case SBML.Key.SAVE_ID:
        this.saveId = Integer.parseInt(value);
        break;
      case SBML.Key.PART_ID:
        this.partId = Integer.parseInt(value);
        break;
      case SBML.Key.DEBUG_ID:
        this.debugId = Integer.parseInt(value);
        break;
      case SBML.Key.STATE:
        this.state = Integer.parseInt(value);
        break;
      case SBML.Key.EFFECT_COUNTER:
        this.effectCounter = Integer.parseInt(value);
        break;
      case SBML.Key.APPLY_GRAVITY:
        this.applyGravity = Integer.parseInt(value);
        break;
      case SBML.Key.TEXTURE_ALPHA:
        this.textureAlpha = Float.parseFloat(value);
        break;
      case SBML.Key.TEMPERATURE:
        this.temperature = Float.parseFloat(value);
        break;
      case SBML.Key.SHOW_IN_SELECTOR:
        this.showInSelector = Integer.parseInt(value);
        break;
      case SBML.Key.CARGO_ITEM:
        if (cargo == null)
          cargo = new ArrayList<>();
        cargo.add(new CargoItem(args));
        break;
      case SBML.Key.AIR:
        this.air = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.POWER_SATATE:
        this.powerState = stringArrayToIntArray(args);
        break;
      case SBML.Key.NAVICOMP_SATATE:
        this.navicompState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.Key.COLLISION_SATATE:
        this.collisionState = stringArrayToIntArray(args);
        break;
      case SBML.Key.POSITION:
        this.position = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.MOVEMENT:
        this.movement = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.LAUNCH_TIMESTAMP:
        this.launchTimestamp = Integer.parseInt(value);
        break;
      case SBML.Key.LAST_USED_TIMESTAMP:
        this.lastUsedTimestamp = Integer.parseInt(value);
        break;
      case SBML.Key.ORBITAL_SATATE:
        this.orbitalState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.Key.FUEL_LEVELS:
        this.fuelLevels = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.SOLAR_PANEL_SATATE:
        this.solarPanelState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.Key.SIDE_PANEL_SATATE:
        this.sidePanelState = Utils.joinStr(SBML.VAL_SEP, args);
        break;
      case SBML.Key.PARENT_MODULE:
        this.parentModule = Integer.parseInt(value);
        break;
      case SBML.Key.PAYLOAD_PARENT:
        this.payloadParent = Integer.parseInt(value);
        break;
      case SBML.Key.TRANSPONDER_ID:
        this.transponderId = value;
        break;
      case SBML.Key.TRANSPONDER_NAME:
        this.transponderName = value;
        break;
      case SBML.Key.TRANSPONDER_SELECTED:
        this.transponderSelected = Integer.parseInt(value);
        break;
      case SBML.Key.DOCK_POINT:
        if (dock == null)
          dock = new ArrayList<>();
        dock.add(new DockPoint(args));
        break;
    }
  }

  public String getValue(String key) {
    switch (key) {
      case SBML.Key.SAVE_ID:
        return String.valueOf(saveId);
      case SBML.Key.PART_ID:
        return String.valueOf(partId);
      case SBML.Key.DEBUG_ID:
        return Utils.intToString(debugId);
      case SBML.Key.STATE:
        return Utils.intToString(state);
      case SBML.Key.EFFECT_COUNTER:
        return Utils.intToString(effectCounter);
      case SBML.Key.APPLY_GRAVITY:
        return Utils.intToString(applyGravity);
      case SBML.Key.TEXTURE_ALPHA:
        return Utils.floatToString(textureAlpha, SBML.PREC_DEFAULT);
      case SBML.Key.TEMPERATURE:
        return Utils.floatToString(temperature, SBML.PREC_DEFAULT);
      case SBML.Key.SHOW_IN_SELECTOR:
        return Utils.intToString(showInSelector);
      case SBML.Key.CARGO_ITEM:
        if (cargo == null || cargo.isEmpty())
          return null;
        if (cargoECounter == cargo.size()) {
          cargoECounter = 0;
          return null;
        }
        return cargo.get(cargoECounter++).export();
      case SBML.Key.AIR:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(air, SBML.PREC_DEFAULT));
      case SBML.Key.POWER_SATATE:
        return Utils.joinStr(SBML.VAL_SEP, IntArrayToStringArray(powerState));
      case SBML.Key.NAVICOMP_SATATE:
        return navicompState;
      case SBML.Key.COLLISION_SATATE:
        return Utils.joinStr(SBML.VAL_SEP, IntArrayToStringArray(collisionState));
      case SBML.Key.POSITION:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(position, SBML.PREC_COORDS));
      case SBML.Key.MOVEMENT:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(movement, SBML.PREC_COORDS));
      case SBML.Key.LAUNCH_TIMESTAMP:
        return Utils.intToString(launchTimestamp);
      case SBML.Key.LAST_USED_TIMESTAMP:
        return Utils.intToString(lastUsedTimestamp);
      case SBML.Key.ORBITAL_SATATE:
        return orbitalState;
      case SBML.Key.FUEL_LEVELS:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(fuelLevels, SBML.PREC_DEFAULT));
      case SBML.Key.SOLAR_PANEL_SATATE:
        return solarPanelState;
      case SBML.Key.SIDE_PANEL_SATATE:
        return sidePanelState;
      case SBML.Key.PARENT_MODULE:
        return Utils.intToString(parentModule);
      case SBML.Key.PAYLOAD_PARENT:
        return Utils.intToString(payloadParent);
      case SBML.Key.TRANSPONDER_ID:
        return transponderId;
      case SBML.Key.TRANSPONDER_NAME:
        return transponderName;
      case SBML.Key.TRANSPONDER_SELECTED:
        return Utils.intToString(transponderSelected);
      case SBML.Key.DOCK_POINT:
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
        fuelLevels[SBML.FuelIndex.MAIN_CAP] *= extendArg;
        fuelLevels[SBML.FuelIndex.THR_CAP] *= extendArg;
      }
      if (refresh) {
        fuelLevels[SBML.FuelIndex.MAIN_VAL] = fuelLevels[SBML.FuelIndex.MAIN_CAP];
        fuelLevels[SBML.FuelIndex.THR_VAL] = fuelLevels[SBML.FuelIndex.THR_CAP];
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

  public int getCargoCount() {
    return cargo == null ? 0 : cargo.size();
  }

  public boolean isVisible() {
    return showInSelector == SBML.Visibility.VISIBLE;
  }

  public boolean hasMovement() {
    return movement != null && movement[SBML.MovementIndex.SPEED] > 0;
  }

  public boolean hasRotation() {
    return movement != null && movement[SBML.MovementIndex.ROTATION_REAL] != 0;
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

  public ArrayList<CargoItem> getCargo() {
    return cargo;
  }

  public float getPositionX() {
    return position[SBML.PosIndex.X];
  }

  public float getPositionY() {
    return position[SBML.PosIndex.Y];
  }

  public float getPositionAngle() {
    return position[SBML.PosIndex.ANGLE];
  }

  public float getMovementDirection() {
    return movement[SBML.MovementIndex.DIRECTION];
  }

  public float getMovementSpeed() {
    return movement[SBML.MovementIndex.SPEED];
  }

  public float getMovementRotation() {
    return movement[SBML.MovementIndex.ROTATION_REAL];
  }

  public float getMainFuelCapacity() {
    return fuelLevels[SBML.FuelIndex.MAIN_CAP];
  }

  public float getMainFuelValue() {
    return fuelLevels[SBML.FuelIndex.MAIN_VAL];
  }

  public float getThrFuelCapacity() {
    return fuelLevels[SBML.FuelIndex.THR_CAP];
  }

  public float getThrFuelValue() {
    return fuelLevels[SBML.FuelIndex.THR_VAL];
  }

  public Integer getLaunchTimestamp() {
    return launchTimestamp;
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
    position[SBML.PosIndex.X] = posX;
    position[SBML.PosIndex.Y] = posY;
    position[SBML.PosIndex.ANGLE] = angle;
  }

  public void setPositionX(float posX) {
    position[SBML.PosIndex.X] = posX;
  }

  public void setPositionY(float posY) {
    position[SBML.PosIndex.Y] = posY;
  }

  public void setPositionAngle(float angle) {
    position[SBML.PosIndex.ANGLE] = angle;
  }

  public void setMovement(float[] movement) {
    this.movement = movement;
  }

  public void setMovement(float direction, float speed, float rotation) {
    this.movement = new float[4];
    movement[SBML.MovementIndex.DIRECTION] = direction;
    movement[SBML.MovementIndex.SPEED] = speed;
    movement[SBML.MovementIndex.ROTATION_VISUAL] = rotation;
    movement[SBML.MovementIndex.ROTATION_REAL] = rotation;
  }

  public void setMovementDirection(float direction) {
    movement[SBML.MovementIndex.DIRECTION] = direction;
  }

  public void setMovementSpeed(float speed) {
    movement[SBML.MovementIndex.SPEED] = speed;
  }

  public void setMovementRotation(float rotation) {
    movement[SBML.MovementIndex.ROTATION_VISUAL] = rotation;
    movement[SBML.MovementIndex.ROTATION_REAL] = rotation;
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

  public static class CargoItem implements Cloneable {
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

    public int getResId() {
      return resId;
    }

    public float getResValue() {
      return value;
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
        state[SBML.DockIndex.SLAVE_PORT] = slavePort;
      }
      state[SBML.DockIndex.ID] = id;
      state[SBML.DockIndex.POWER] = power;
      state[SBML.DockIndex.FUEL] = fuel;
      state[SBML.DockIndex.DOOR] = door;
      state[SBML.DockIndex.SLAVE_ID] = slaveId;
    }

    public boolean isDocked() {
      return state[SBML.DockIndex.SLAVE_ID] != SBML.DOCK_STATE_UNDOCKED;
    }

    public int getSlaveId() {
      return state[SBML.DockIndex.SLAVE_ID];
    }

    public void setSlaveId(int slaveId) {
      state[SBML.DockIndex.SLAVE_ID] = slaveId;
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
