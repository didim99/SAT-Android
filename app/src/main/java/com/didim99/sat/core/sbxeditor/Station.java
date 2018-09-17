package com.didim99.sat.core.sbxeditor;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.core.sbxeditor.wrapper.Module;
import com.didim99.sat.core.sbxeditor.wrapper.NaviCompMarker;
import com.didim99.sat.core.sbxeditor.wrapper.Part;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.settings.Settings;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Space station container
 * Created by didim99 on 16.02.18.
 */

public class Station implements Cloneable {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_station";

  public static class Type {
    public static final int UNKNOWN = 0;
    public static final int MULTIPLE_OBJECTS = 1;
    public static final int STATION = 2;
    public static final int COLONY = 3;
    public static final int GROUP = 4;
    public static final int TEXT = 5;
  }

  //edit mode constants
  public static final int EDIT_MODE_REPLACE = 1;
  public static final int EDIT_MODE_CREATE_NEW = 2;
  public static final int MOVE_MODE_OFFSET = 1;
  public static final int MOVE_MODE_NEW_CENTER = 2;
  public static final int MOVEMENT_MODE_STOP = 1;
  public static final int MOVEMENT_MODE_EDIT = 2;
  //visibility
  public static final int VISIBLE = SBML.Visibility.VISIBLE;
  public static final int INVISIBLE = SBML.Visibility.INVISIBLE;
  public static final int VISIBILITY_UNKNOWN = -1;

  private int id;
  private int type;
  private ArrayList<Module> moduleSet;
  private Info info;
  private Statistics stat;

  Station(int size) {
    moduleSet = new ArrayList<>(size);
    type = Type.STATION;
  }

  Station(int size, int type) {
    moduleSet = new ArrayList<>(size);
    this.type = type;
  }

  public Station init(ArrayList<NaviCompMarker> naviComp) {
    MyLog.d(LOG_TAG, "New station init");
    moduleSet.trimToSize();
    analyze(naviComp);
    return this;
  }

  void analyze(ArrayList<NaviCompMarker> naviComp) {
    MyLog.d(LOG_TAG, "Collecting station info...");
    int minSaveId, maxSaveId, saveId, visibility, minVer, ver;
    boolean hasMovement = false, hasRotation = false,
      allVisible = true, allInvisible = true, hasTime = false;
    Float movementDirection = null, movementSpeed = null, rotationSpeed = null;
    ArrayList<String> names = new ArrayList<>();
    SparseArray<Part> partInfo = Storage.getPartInfo();
    Integer launchTime = Integer.MAX_VALUE, time;

    boolean hasDb = Settings.isDbLoaded();
    Module initModule = moduleSet.get(0);
    minSaveId = maxSaveId = initModule.getSaveId();
    minVer = SBML.VerCode.V14;

    for (Module module : moduleSet) {
      saveId = module.getSaveId();

      if (saveId < minSaveId)
        minSaveId = saveId;
      else if (saveId > maxSaveId)
        maxSaveId = saveId;
      if (hasDb) {
        ver = partInfo.get(module.getPartId()).getMinVer();
        if (ver > minVer)
          minVer = ver;
      }
      if (!hasMovement) {
        if (module.hasMovement()) {
          hasMovement = true;
          movementDirection = module.getMovementDirection();
          movementSpeed = module.getMovementSpeed();
        }
      }
      if (!hasRotation) {
        if (module.hasRotation()) {
          hasRotation = true;
          rotationSpeed = module.getMovementRotation();
        }
      }

      time = module.getLaunchTimestamp();
      if (time != null) {
        if (time == SBML.TIMESTAMP_UNDEFINED)
          hasTime = true;
        else if (time < launchTime)
          launchTime = time;
      }

      if (allVisible && !module.isVisible())
        allVisible = false;
      if (allInvisible && module.isVisible())
        allInvisible = false;

      if (module.hasTransponderName())
        names.add(module.getTransponderName());
    }

    if (launchTime == Integer.MAX_VALUE)
      launchTime = hasTime ? SBML.TIMESTAMP_UNDEFINED : null;

    if (allVisible)
      visibility = VISIBLE;
    else if (allInvisible)
      visibility = INVISIBLE;
    else
      visibility = VISIBILITY_UNKNOWN;

    if (info != null && type == Type.TEXT)
      names = info.names;

    if (type == Type.COLONY && hasMovement) {
      rotationSpeed = Float.compare(movementDirection, 90f) == 0
        ? movementSpeed : -movementSpeed;
      movementSpeed = null;
      hasMovement = false;
      hasRotation = true;
    }

    id = minSaveId;
    info = new Info(
      minSaveId, maxSaveId, launchTime,
      hasMovement, movementDirection, movementSpeed,
      hasRotation, rotationSpeed, visibility, minVer
    );

    analyzePosition();
    analyzeMap(naviComp);
    if (!names.isEmpty())
      info.names = names;
    if (hasDb)
      collectStatistics();
    MyLog.d(LOG_TAG, info.toString());
  }

  private void analyzePosition() {
    MyLog.d(LOG_TAG, "Collecting position info...");
    float minPosX, maxPosX, posX, minPosY, maxPosY, posY;
    Module initModule = moduleSet.get(0);
    minPosX = maxPosX = initModule.getPositionX();
    minPosY = maxPosY = initModule.getPositionY();

    for (Module module : moduleSet) {
      posX = module.getPositionX();
      posY = module.getPositionY();
      if (posX < minPosX)
        minPosX = posX;
      else if (posX > maxPosX)
        maxPosX = posX;
      if (posY < minPosY)
        minPosY = posY;
      else if (posY > maxPosY)
        maxPosY = posY;
    }

    info.updatePosition(minPosX, minPosY, maxPosX, maxPosY);
    MyLog.d(LOG_TAG, "Position info collected");
  }

  void analyzeMap(ArrayList<NaviCompMarker> naviComp) {
    MyLog.d(LOG_TAG, "Collecting navigation info...");
    double centerX = (double) info.centerPosX;
    double centerY = (double) info.centerPosY;
    double minDistance = Double.MAX_VALUE;
    NaviCompMarker nearMarker = null;
    for (NaviCompMarker marker : naviComp) {
      double distance = Math.sqrt(
        Math.pow(centerX - (double) marker.getCenterX(), 2)
          + Math.pow(centerY - (double) marker.getCenterY(), 2)
      );
      if (distance < minDistance) {
        minDistance = distance;
        info.distance = minDistance;
        nearMarker = marker;
      }
    }

    if (nearMarker != null) {
      info.nearestMarker = nearMarker.getLabel();
      double navDistanceX = nearMarker.getCenterX() - info.centerPosX;
      double navDistanceY = info.centerPosY - nearMarker.getCenterY();
      info.navDirection = (float) Math.toDegrees(Math.atan2(navDistanceY, navDistanceX));
    } else {
      info.nearestMarker = null;
      info.navDirection = 0;
      info.distance = null;
    }

    MyLog.d(LOG_TAG, "Navigation info collected");
  }

  private void collectStatistics() {
    MyLog.d(LOG_TAG, "Collecting station statistics...");
    SparseArray<Part> partInfo = Storage.getPartInfo();
    this.stat = new Statistics();

    int partID, cargoID, NOT_EXISTS = -1;
    for (Module module : moduleSet) {
      partID = module.getPartId();
      Part part = partInfo.get(partID);

      if (stat.partCount.get(partID, NOT_EXISTS) == NOT_EXISTS)
        stat.partCount.append(partID, 1);
      else
        stat.partCount.put(partID, stat.partCount.get(partID) + 1);

      if (module.hasFuel()) {
        stat.mainFuelCap += module.getMainFuelCapacity();
        stat.mainFuelVal += module.getMainFuelValue();
        stat.thrFuelCap += module.getThrFuelCapacity();
        stat.thrFuelVal += module.getThrFuelValue();
      }

      stat.powerGen += part.getPowerGen();
      stat.powerUse += part.getPowerUse();
      stat.cargoTotal += part.getCargoCount();
      stat.cargoUsed += module.getCargoCount();
      if (module.getCargoCount() > 0) {
        for (Module.CargoItem cargoItems : module.getCargo()) {
          cargoID = cargoItems.getResId();
          ResourceState cargoState = stat.resState.get(cargoID);

          if (cargoState == null)
            stat.resState.put(cargoID,
              new ResourceState(cargoItems.getResValue()));
          else {
            cargoState.updateTotal();
            cargoState.updateUsed(cargoItems.getResValue());
          }
        }
      }
    }

    MyLog.d(LOG_TAG, "Statistics collected");
    MyLog.d(LOG_TAG, stat.toString());
  }

  void saveIdChange(int startSid) {
    MyLog.d(LOG_TAG, "Changing save id (" + startSid + ")");
    SparseIntArray sidMap = new SparseIntArray(moduleSet.size());
    for (Module module : moduleSet) {
      sidMap.put(module.getSaveId(), startSid);
      module.setSaveId(startSid++);
    }
    Integer oldId;
    ArrayList<Module.DockPoint> dock;
    for (Module module : moduleSet) {
      if ((oldId = module.getParentModule()) != null)
        module.setParentModule(sidMap.get(oldId));
      if ((oldId = module.getPayloadParent()) != null)
        module.setPayloadParent(sidMap.get(oldId));
      if ((dock = module.getDock()) != null) {
        for (Module.DockPoint dockPoint : dock) {
          if (dockPoint.isDocked())
            dockPoint.setSlaveId(sidMap.get(dockPoint.getSlaveId()));
        }
        module.setDock(dock);
      }
    }
    MyLog.d(LOG_TAG, "Save id changed");
  }

  void setTextureAlpha(float textureAlpha) {
    MyLog.d(LOG_TAG, "Setting opacity (" + textureAlpha + ")");
    for (Module module : moduleSet)
      module.setTextureAlpha(textureAlpha);
  }

  void setVisible(int mode) {
    MyLog.d(LOG_TAG, "Hiding station (" + mode + ")");
    for (Module module : moduleSet)
      module.setShowInSelector(mode);
  }

  void refreshCargo() {
    MyLog.d(LOG_TAG, "Refreshing cargo");
    for (Module module : moduleSet)
      module.refreshCargo ();
  }

  void setFuel(boolean refresh, boolean extend, float extendArg) {
    if (extend)
      MyLog.d(LOG_TAG, "Extending fuel (" + extendArg + ")");
    if (refresh)
    MyLog.d(LOG_TAG, "Refreshing fuel");
    for (Module module : moduleSet)
      module.setFuel(refresh, extend, extendArg);
  }

  void move(int mode, float deltaX, float deltaY) {
    if (mode == MOVE_MODE_NEW_CENTER) {
      deltaX -= info.centerPosX;
      deltaY -= info.centerPosY;
    }
    MyLog.d(LOG_TAG, "Moving station (x: " + deltaX + ", y: " + deltaY + ")");
    for (Module module : moduleSet) {
      module.setPositionX(module.getPositionX() + deltaX);
      module.setPositionY(module.getPositionY() + deltaY);
    }
    MyLog.d(LOG_TAG, "Station moved");
    colonyToGroup();
    analyzePosition();
  }

  void rotate(float angle, Float baseX, Float baseY) {
    if (baseX == null) baseX = info.centerPosX;
    if (baseY == null) baseY = info.centerPosY;
    MyLog.d(LOG_TAG, "Rotating station (angle: " + angle
      + ", x: " + baseX + ", y: " + baseY + ")");
    double offsetX, offsetY,
      phi = Math.toRadians(angle),
      sinPhi = Math.sin(phi),
      cosPhi = Math.cos(phi);
    for (Module module : moduleSet) {
      offsetX = module.getPositionX() - baseX;
      offsetY = baseY - module.getPositionY();
      module.setPositionX((float) (baseX + (offsetX * cosPhi - offsetY * sinPhi)));
      module.setPositionY((float) (baseY - (offsetX * sinPhi + offsetY * cosPhi)));
      module.setPositionAngle(module.getPositionAngle() + angle);
    }
    MyLog.d(LOG_TAG, "Station rotated");
    colonyToGroup();
  }

  void movementChange(SbxEditConfig config) {
    switch (config.getMovementMode()) {
      case MOVEMENT_MODE_STOP:
        MyLog.d(LOG_TAG, "Stopping station");
        for (Module module : moduleSet) {
          module.setMovement(null);
          module.setOrbitalState(null);
        }
        break;
      case MOVEMENT_MODE_EDIT:
        if (!info.hasMovement && !info.hasRotation) {
          MyLog.d(LOG_TAG, "Starting station movement ("
            + config.getMovementDirection() + ", " + config.getMovementSpeed()
            + ", " + config.getMovementRotation() + ")");
          float[] movement = {
            config.getMovementDirection(),
            config.getMovementSpeed(),
            config.getMovementRotation(),
            config.getMovementRotation()
          };
          for (Module module : moduleSet)
            module.setMovement(movement);
        } else {
          MyLog.d(LOG_TAG, "Editing station movement ("
            + config.getMovementDirection() + ", " + config.getMovementSpeed()
            + ", " + config.getMovementRotation() + ")");
          for (Module module : moduleSet) {
            if (module.hasMovement() || module.hasRotation()) {
              if (config.isChangeMovementDirection())
                module.setMovementDirection(config.getMovementDirection());
              if (config.isChangeMovementSpeed())
                module.setMovementSpeed(config.getMovementSpeed());
              if (config.isChangeMovementRotation())
                module.setMovementRotation(config.getMovementRotation());
            }
          }
        }
        break;
    }
  }

  Station copy(int newStartSid, int moveMode, float deltaX, float deltaY,
                ArrayList<NaviCompMarker> navCiomp)
    throws CloneNotSupportedException {
    Station copy = this.clone();
    copy.saveIdChange(newStartSid);
    copy.move(moveMode, deltaX, deltaY);
    copy.analyze(navCiomp);
    return copy;
  }

  void addModule(Module module) {
    moduleSet.add(module);
  }

  public void addModules(ArrayList<Module> modules) {
    moduleSet.addAll(modules);
  }

  ArrayList<Module> getModules() {
    return moduleSet;
  }

  public int getObjId() {
    return id;
  }

  public Info getInfo() {
    return info;
  }

  public Statistics getStat() {
    return stat;
  }

  @Override
  public String toString() {
    return type + "." + id;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Station && ((Station) obj).id == this.id;
  }

  @Override
  protected Station clone() throws CloneNotSupportedException {
    Station clone = (Station) super.clone();
    clone.moduleSet = new ArrayList<>(this.moduleSet.size());
    for (Module module : this.moduleSet)
      clone.moduleSet.add(module.clone());
    clone.info = this.info.clone();
    clone.stat = this.stat.clone();
    return clone;
  }

  public static int getObjType(ArrayList<Station> stations) {
    if (stations != null) {
      if (stations.size() > 1)
        return Type.MULTIPLE_OBJECTS;
      else
        return stations.get(0).getInfo().getObjType();
    } else return Type.UNKNOWN;
  }

  private void colonyToGroup() {
    if (type == Type.COLONY) {
      for (Module module : moduleSet)
        module.setOrbitalState(null);
      type = Type.GROUP;
    }
  }

  public class Info implements Cloneable {
    private int minSaveId;
    private int maxSaveId;
    private float minPosX;
    private float minPosY;
    private float maxPosX;
    private float maxPosY;
    private float centerPosX;
    private float centerPosY;
    private boolean hasMovement;
    private Float movementDirection;
    private Float movementSpeed;
    private boolean hasRotation;
    private Float rotationSpeed;
    private Integer launchTimestamp;
    private int visibility;
    private int minVer;
    private ArrayList<String> names;
    private String nearestMarker;
    private float navDirection;
    private Double distance;

    Info(int minSaveId, int maxSaveId, Integer launchTimestamp,
         boolean hasMovement, Float movementDirection, Float movementSpeed,
         boolean hasRotation, Float rotationSpeed, int visibility, int minVer) {
      this.minSaveId = minSaveId;
      this.maxSaveId = maxSaveId;
      this.hasMovement = hasMovement;
      this.movementDirection = movementDirection;
      this.movementSpeed = movementSpeed;
      this.hasRotation = hasRotation;
      this.rotationSpeed = rotationSpeed;
      this.launchTimestamp = launchTimestamp;
      this.visibility = visibility;
      this.minVer = minVer;
    }

    void updatePosition(float minPosX, float minPosY,
                        float maxPosX, float maxPosY) {
      this.minPosX = minPosX;
      this.minPosY = minPosY;
      this.maxPosX = maxPosX;
      this.maxPosY = maxPosY;
      centerPosX = (minPosX + maxPosX) / 2;
      centerPosY = (minPosY + maxPosY) / 2;
    }

    public boolean hasMovement() {
      return hasMovement;
    }

    public boolean hasRotation() {
      return hasRotation;
    }

    public boolean hasName() {
      return names != null && !names.isEmpty();
    }

    public boolean hasNavDistance() {
      return distance != null;
    }

    public int getSize() {
      return moduleSet.size();
    }

    public String getNearestMarker() {
      return nearestMarker;
    }

    public String getStationName() {
      if (hasName())
        return Utils.joinStr(", ", names);
      else return null;
    }

    public String getSaveIdRange () {
      return minSaveId + " ... " + maxSaveId;
    }

    public String getCenterPosStr (int precision) {
      return Utils.joinStr ("; ",
        Utils.floatToString(centerPosX / SBML.POSITION_FACTOR, precision),
        Utils.floatToString(centerPosY / SBML.POSITION_FACTOR, precision)
      );
    }

    public String getSizeStr (int precision) {
      return Utils.joinStr (" × ",
        Utils.floatToString((maxPosX - minPosX) / SBML.POSITION_FACTOR, precision),
        Utils.floatToString((maxPosY - minPosY) / SBML.POSITION_FACTOR, precision)
      );
    }

    public String getDistanceStr(Context ctx) {
      if (distance == null)
        return null;
      if (type == Type.COLONY)
        return ctx.getString(R.string.colonyNear, nearestMarker);
      String formatStr;
      ctx = ctx.getApplicationContext();
      double distance = this.distance / SBML.POSITION_FACTOR;
      if (distance > 1000000.0) {
        formatStr = ctx.getString(R.string.distanceTo_M);
        distance /= 1000000;
      }
      else if (distance > 1000.0) {
        formatStr = ctx.getString(R.string.distanceTo_K);
        distance /= 1000;
      }
      else {
        formatStr = ctx.getString(R.string.distanceTo_U);
      }
      return String.format(Locale.US, formatStr, distance, nearestMarker);
    }

    public int getObjType() {
      return type;
    }

    public int getMinSaveId() {
      return minSaveId;
    }

    public int getMaxSaveId() {
      return maxSaveId;
    }

    public float getMinPosX() {
      return minPosX;
    }

    public float getMinPosY() {
      return minPosY;
    }

    public float getMaxPosX() {
      return maxPosX;
    }

    public float getMaxPosY() {
      return maxPosY;
    }

    public float getCenterPosX() {
      return centerPosX;
    }

    public float getCenterPosY() {
      return centerPosY;
    }

    public float getMovementDirection() {
      return movementDirection;
    }

    public float getMovementSpeed() {
      return movementSpeed;
    }

    public float getRotationSpeed() {
      return rotationSpeed;
    }

    public Integer getLaunchTimestamp() {
      return launchTimestamp;
    }

    public int getVisibility() {
      return visibility;
    }

    public int getMinVer() {
      return minVer;
    }

    public float getNavDirection() {
      return navDirection;
    }

    public double getNavDistance() {
      return distance;
    }

    public void setName(String name) {
      if (names != null) return;
      names = new ArrayList<>();
      names.add(name);
    }

    @Override
    public String toString() {
      return "\nStation info:"
        + "\n  saveIdRange: " + getSaveIdRange()
        + "\n  Position X: " + minPosX + "/" + maxPosX
        + "\n  Position Y: " + minPosY + "/" + maxPosY
        + "\n  Center position: " + centerPosX + ", " + centerPosY
        + "\n  movementDirection: " + movementDirection
        + "\n  movementSpeed: " + movementSpeed
        + "\n  rotationSpeed: " + rotationSpeed
        + "\n  launchTimestamp: " + launchTimestamp
        + "\n  visibility: " + visibility
        + "\n  minVer: " + minVer
        + "\n  names: " + (names == null ? null : names.toString())
        + "\n -----"
        + "\n  nearestMarker: " + nearestMarker
        + "\n  navDirection: " + navDirection
        + "\n  distance: " + distance;
    }

    @Override
    protected Info clone() throws CloneNotSupportedException {
      Info clone = (Info) super.clone();
      if (this.names != null)
        clone.names = new ArrayList<>(this.names);
      return clone;
    }
  }

  public static class Statistics implements Cloneable {
    private float
      mainFuelCap = 0, mainFuelVal = 0,
      thrFuelCap = 0, thrFuelVal = 0;
    private int
      cargoTotal = 0, cargoUsed = 0,
      powerGen = 0, powerUse = 0;
    private SparseArray<ResourceState> resState;
    private SparseIntArray partCount;

    private Statistics() {
      resState = new SparseArray<>(4);
      partCount = new SparseIntArray();
    }

    public float getMainFuelCap() {
      return mainFuelCap;
    }

    public float getMainFuelVal() {
      return mainFuelVal;
    }

    public float getThrFuelCap() {
      return thrFuelCap;
    }

    public float getThrFuelVal() {
      return thrFuelVal;
    }

    public int getPowerGen() {
      return powerGen;
    }

    public int getPowerUse() {
      return powerUse;
    }

    public int getCargoTotal() {
      return cargoTotal;
    }

    public int getCargoUsed() {
      return cargoUsed;
    }

    public SparseIntArray getPartCount() {
      return partCount;
    }

    public SparseArray<ResourceState> getResState() {
      return resState;
    }

    @Override
    public String toString() {
      return "\nStation statistics:"
        + "\n  Main fuel: " + mainFuelCap + "/" + mainFuelVal
        + "\n  Thr fuel: " + thrFuelCap + "/" + thrFuelVal
        + "\n  Power: " + powerGen + "/" + powerUse
        + "\n  Cargo: " + cargoTotal + "/" + cargoUsed
        + "\n  Cargo state: " + resState.toString()
        + "\n  Part count: " + partCount.toString();
    }

    @Override
    protected Statistics clone() throws CloneNotSupportedException {
      Statistics clone = (Statistics) super.clone();
      clone.partCount = this.partCount.clone();
      clone.resState = new SparseArray<>(this.resState.size());
      for (int i = 0; i < this.resState.size(); i++) {
        clone.resState.put(this.resState.keyAt(i),
          this.resState.valueAt(i).clone());
      }
      return clone;
    }
  }

  public static class ResourceState implements Cloneable {
    private float total, used;

    ResourceState(float used) {
      this.total = 1f;
      this.used = used;
    }

    void updateTotal() {
      this.total += 1f;
    }

    void updateUsed(float used) {
      this.used += used;
    }

    public float getTotal() { return total; }
    public float getUsed() { return used; }

    @Override
    public String toString() {
      return total + "/" + used;
    }

    @Override
    protected ResourceState clone() throws CloneNotSupportedException {
      return (ResourceState) super.clone();
    }
  }
}
