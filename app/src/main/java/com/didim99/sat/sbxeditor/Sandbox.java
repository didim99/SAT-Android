package com.didim99.sat.sbxeditor;

import android.content.Context;
import android.graphics.PointF;
import android.util.SparseArray;
import com.didim99.sat.MyLog;
import com.didim99.sat.Utils;
import com.didim99.sat.sbxconverter.SbxConverter;
import com.didim99.sat.sbxeditor.model.Module;
import com.didim99.sat.sbxeditor.model.NaviCompMarker;
import com.didim99.sat.sbxeditor.model.Part;
import com.didim99.sat.sbxeditor.model.Planet;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.sbxeditor.TextGenerator.IllegalCharException;
import com.didim99.sat.settings.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

/**
 * Sandbox container
 * Created by didim99 on 12.02.18.
 */

public class Sandbox {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_sandbox";

  public static class Mode {
    public static final int CREATE = 1;
    public static final int OPEN = 2;
    public static final int UPDATE_NAV = 3;
    public static final int ADD_MODULE = 4;
    public static final int ADD_COLONY = 5;
    public static final int ADD_TEXT = 6;
    public static final int ADD_ALL = 7;
    public static final int ADD_ALL_FONT = 8;
    public static final int COPY = 9;
    public static final int EDIT = 10;
    public static final int DELETE = 11;
    public static final int OPTIMIZE = 12;
    public static final int SAVE = 13;
    public static final int SEND = 14;
    public static final int FUEL_INFO = 15;
  }

  private WeakReference<Context> appContextRef;
  //standard SA fields
  private int formatVersion;
  private int fileType;
  private int sbxVersion;
  private String sbxName;
  private Integer sbxHighMission;
  private Integer sbxUid;
  //data set
  private ArrayList<Station> space;
  private ArrayList<Module> alone;
  private ArrayList<NaviCompMarker> naviComp;
  //system fields
  private boolean modified = false;
  private boolean naviCompModified = false;
  private String inputFileName;
  private String fileName;
  private Info info;

  Sandbox(Context context, SbxEditConfig config) {
    this.appContextRef = new WeakReference<>(context.getApplicationContext());
    this.formatVersion = SBML.FORMAT_VERSION;
    this.fileType = SBML.FILE_TYPE_SANDBOX;
    this.sbxName = config.getSbxName();
    this.fileName = Settings.getSbxTempDir() + sbxName + SBML.FILE_MASK_SASBX;
    this.sbxUid = config.getSbxUid();
    this.space = new ArrayList<>();
    this.alone = new ArrayList<>();
    this.naviComp = new ArrayList<>();
    this.info = new Info();
    if (config.isAddMarkers()) {
      for (Planet planet : Storage.getPlanetInfo())
        naviComp.add(new NaviCompMarker(planet));
    }
    MyLog.d(LOG_TAG, this.toString());
  }

  Sandbox(Context context, String inputFileName)
    throws IOException, SBMLParserException {
    this.appContextRef = new WeakReference<>(context.getApplicationContext());
    this.info = new Info();
    this.inputFileName = inputFileName;
    this.fileName = Settings.getSbxTempDir() + new File(inputFileName).getName();
    Utils.copyFile(inputFileName, fileName);
    if (isCompressed(fileName)) {
      SbxConverter converter = new SbxConverter(context,
        new SbxConverter.Config(fileName, SbxConverter.ACTION_UNCOMPRESS, 0));
      converter.convert();
      if (converter.getStatusCode() != SbxConverter.STATUS_CODE_OK)
        throw new IOException("Can't decompress sandbox");
    }
    splitStations(parseData(readFile(fileName)));
    MyLog.d(LOG_TAG, this.toString());
  }

  private void analyze(boolean onChange) {
    MyLog.d(LOG_TAG, "Updating sandbox info...");
    if (onChange) modified = true;
    //reset max values
    info.largestStationSize = 0;
    info.maxSaveId = 0;
    info.minVer = SBML.VER_CODE_14;

    info.modulesCount = alone.size();
    for (Station station : space) {
      Station.Info staInfo = station.getInfo();
      int size = staInfo.getSize();
      info.modulesCount += size;
      if (size > info.largestStationSize)
        info.largestStationSize = size;
      if (staInfo.getMaxSaveId() > info.maxSaveId)
        info.maxSaveId = staInfo.getMaxSaveId();
      if (staInfo.getMinVer() > info.minVer)
        info.minVer = staInfo.getMinVer();
    }
    for (Module module : alone) {
      if (Settings.isDbLoaded()) {
        int ver = Storage.getPartInfo().get(module.getPartId()).getMinVer();
        if (ver > info.minVer)
          info.minVer = ver;
      }
      if (module.getSaveId() > info.maxSaveId)
        info.maxSaveId = module.getSaveId();
    }
    MyLog.d(LOG_TAG, "Sandbox info updated");
  }

  void optimize(boolean optSaveId, boolean refreshCargo, boolean refreshFuel) {
    MyLog.d(LOG_TAG, "Optimizing sandbox (SID: " + optSaveId
      + ", cargo: " + refreshCargo + ", fuel: " + refreshFuel + ")");
    if (optSaveId) {
      info.maxSaveId = -1;
      for (Station station : space) {
        station.saveIdChange(info.maxSaveId + 1);
        station.analyze(naviComp);
        info.maxSaveId = station.getInfo().getMaxSaveId();
      }
      for (Module module : alone)
        module.setSaveId(++info.maxSaveId);
    }
    if (refreshCargo || refreshFuel) {
      for (Station station : space) {
        if (refreshCargo) station.refreshCargo();
        if (refreshFuel) station.setFuel(true, false, 0);
      }
      for (Module module : alone) {
        if (refreshCargo) module.refreshCargo();
        if (refreshFuel) module.setFuel(true, false, 0);
      }
    }
    modified = true;
  }

  void addModule(SbxEditConfig config) {
    int partId = config.getPartId();
    int count = config.getCount();
    MyLog.d(LOG_TAG, "Adding module(s): " + partId + " x " + count);
    int timestamp = Utils.getTimestamp();
    Part part = Storage.getPartInfo().get(partId);
    if (count > 1) {
      Station group = new Station(count, Station.Type.GROUP);
      float startPosX = config.getPositionX(), currPosX = startPosX;
      float currPosY = config.getPositionY();
      float offset = config.getOffset();
      int saveId = info.maxSaveId + 1;

      for (int i = 0; i < count; i++) {
        Module module = new Module(saveId++, partId);
        module.setPosition(currPosX, currPosY, 0);
        module.setTimes(timestamp);
        if (part.isHasCargo()) {
          for (int id = 0; id < part.getCargoCount(); id++)
            module.addCargo(id, SBML.CARGO_ID_BAT);
        }

        group.addModule(module);
        currPosX += offset;
        if ((i + 1) % config.getInLine() == 0) {
          currPosX = startPosX;
          currPosY += offset;
        }
      }

      space.add(group.init(naviComp));
    } else {
      Module module = new Module(info.maxSaveId + 1, partId);
      module.setPosition(config.getPositionX(), config.getPositionY(), 0);
      module.setTimes(timestamp);
      if (part.isHasCargo()) {
        for (int i = 0; i < part.getCargoCount(); i++)
          module.addCargo(i, SBML.CARGO_ID_BAT);
      }
      alone.add(module);
    }
    analyze(true);
  }

  void createColony(SbxEditConfig config) {
    int planetId = config.getPlanetId();
    int state = config.getOrbitalState();
    int count = config.getCount();
    float posRotation = config.getPositionAnge();
    float movementSpeed = config.getMovementSpeed();
    int timestamp = Utils.getTimestamp();
    int saveId = info.maxSaveId + 1;
    Planet planet = Storage.getPlanetInfo().get(planetId);
    Part part = Storage.getPartInfo().get(config.getPartId());

    Float gap = config.getGap();
    if (gap == null)
      gap = 360f / count;
    Float height = config.getOrbHeight();
    if (config.getUnits() == SBML.DistanceUnit.PERCENT)
      height = planet.getObjectRadius() + planet.getOrbitRadius() * (height / 100);

    boolean setMovement = false;
    float movementDirection = 270f;
    if (state == SBML.ORBITAL_STATE_ORBITING) {
      setMovement = true;
      if (movementSpeed < 0) {
        movementSpeed = Math.abs(movementSpeed);
        movementDirection = 90f;
      }
    }

    MyLog.d(LOG_TAG, "Creating colony\n  planet: " + planetId + " state: " + state
      + " partId: " + part.getPartId() + " count: " + count + " height: " + height
      + " gap: " + gap + " rotation: " + posRotation + " speed: " + movementSpeed);

    float posX, posY, orbitAngle = 0;
    double baseX = planet.getPositionX(), baseY = planet.getPositionY();
    Station colony = new Station(count, Station.Type.COLONY);
    for (int i = 0; i < count; i++) {
      double phi = Math.toRadians(orbitAngle - 90);
      posX = (float) (baseX + height * Math.cos(phi));
      posY = (float) (baseY - height * Math.sin(phi));

      Module module = new Module(saveId++, part);
      module.setOrbitalState(planetId, state, height, orbitAngle);
      if (setMovement)
        module.setMovement(movementDirection, movementSpeed, 0);
      module.setPosition(posX, posY, posRotation);
      module.setTimes(timestamp);
      colony.addModule(module);

      orbitAngle += gap;
      posRotation += gap;
      movementDirection += gap;
    }

    space.add(colony.init(naviComp));
    analyze(true);
  }

  void addAllModules(SbxEditConfig config) {
    int verCode = config.getVerCode();
    float startPosX = config.getPositionX();
    float currPosY = config.getPositionY();
    MyLog.d(LOG_TAG, "Adding all modules ("
      + verCode + ": " + startPosX + "; " + currPosY + ")");
    int timestamp = Utils.getTimestamp();
    int saveId = info.maxSaveId + 1;
    float offset = config.getOffset();
    float currPosX = startPosX;

    int pos = 1;
    int size = Storage.getPartInfo().size();
    Station group = new Station(size, Station.Type.GROUP);
    for (int i = 0; i < size; i++) {
      Part part = Storage.getPartInfo().valueAt(i);
      int partId = part.getPartId();

      if (part.getMinVer() > verCode
        || partId == SBML.PART_ID_SOYUZ_SERVICE)
        continue;

      Module module = new Module(saveId++, part);
      module.setPosition(currPosX, currPosY, 0);
      module.setTimes(timestamp);

      group.addModule(module);
      currPosX += offset;
      if (pos++ % config.getInLine() == 0) {
        currPosX = startPosX;
        currPosY += offset;
      }
    }

    space.add(group.init(naviComp));
    analyze(true);
  }

  void addAllFont(SbxEditConfig config) throws IOException {
    config.setStartSaveId(info.maxSaveId + 1);
    space.add(new TextGenerator(appContextRef.get()).createAllFont(config, naviComp));
    analyze(true);
  }

  void addText(SbxEditConfig config)
    throws IOException, IllegalCharException {
    config.setStartSaveId(info.maxSaveId + 1);
    space.add(new TextGenerator(appContextRef.get()).createText(config, naviComp));
    analyze(true);
  }

  void stationCopy(SbxEditConfig config)
    throws CloneNotSupportedException {
    int mode = config.getMode();
    float deltaX = config.getPositionX();
    float deltaY = config.getPositionY();

    ArrayList<Station> stations = config.getStations();
    if (stations.size() > 1 && mode == Station.MOVE_MODE_NEW_CENTER) {
      PointF center = findCenter(stations);
      mode = Station.MOVE_MODE_OFFSET;
      deltaX -= center.x;
      deltaY -= center.y;
    }

    for (Station station : stations) {
      MyLog.d(LOG_TAG, "Copying station (" + station.getObjId() + ")");
      Station newStation = station.copy(
        info.maxSaveId + 1, mode, deltaX, deltaY, naviComp);
      info.maxSaveId = newStation.getInfo().getMaxSaveId();
      space.add(newStation);
    }
    analyze(true);
  }

  void stationEdit(SbxEditConfig config)
    throws CloneNotSupportedException {
    ArrayList<Station> stations = config.getStations();

    boolean isChangePosition = config.isChangePosition();
    boolean isChangeAngle = config.isChangeAngle();

    if (stations.size() > 1 && (isChangePosition || isChangeAngle)) {
      PointF center = findCenter(stations);
      if (isChangePosition && config.getPositionMode() == Station.MOVE_MODE_NEW_CENTER) {
        config.setPositionMode(Station.MOVE_MODE_OFFSET);
        config.setPositionX(config.getPositionX() - center.x);
        config.setPositionY(config.getPositionY() - center.y);
      }
      if (isChangeAngle && config.isRotationCommonBase() && !config.isRotationCustomBase()) {
        config.setRotationBaseX(center.x);
        config.setRotationBaseY(center.y);
      }
    }

    for (Station station : stations) {
      if (config.getEditMode() == Station.EDIT_MODE_CREATE_NEW) {
        station = station.copy(info.maxSaveId + 1,
          Station.MOVE_MODE_OFFSET, 0, 0, naviComp);
        info.maxSaveId = station.getInfo().getMaxSaveId();
        space.add(station);
      }

      if (config.isChangeSaveId())
        station.saveIdChange(config.getStartSaveId());
      if (isChangePosition)
        station.move(config.getPositionMode(),
          config.getPositionX(), config.getPositionY());
      if (isChangeAngle)
        station.rotate(config.getPositionAnge(),
          config.getRotationBaseX(), config.getRotationBaseY());
      if (config.isChangeMovement())
        station.movementChange(config);
      if (config.isChangeVisibility())
        station.setVisible(config.getHideMode());
      if (config.isChangeAlpha())
        station.setTextureAlpha(config.getAlphaValue());
      if (config.isRefreshCargo())
        station.refreshCargo();
      if (config.isRefreshFuel() || config.isExtendFuel())
        station.setFuel(config.isRefreshFuel(),
          config.isExtendFuel(), config.getExtendFuelValue());
      station.analyze(naviComp);

      config.setStartSaveId(station.getInfo().getMaxSaveId() + 1);
    }

    analyze(true);
  }

  void stationDelete(ArrayList<Station> stations) {
    for (Station station : stations) {
      MyLog.d(LOG_TAG, "Deleting station (" + station.getObjId() + ")");
      space.remove(station);
    }
    analyze(true);
  }

  public void addMarker(NaviCompMarker marker) {
    MyLog.d(LOG_TAG, "Adding marker: " + marker.getLabel()
      + " (" + marker.getCenterStr(2) + ")");
    naviComp.add(marker);
    naviCompModified = true;
  }

  public void markerReplace(NaviCompMarker oldMarker, NaviCompMarker newMarker) {
    int markerId = naviComp.indexOf(oldMarker);
    MyLog.d(LOG_TAG, "Replacing marker (" + markerId + ")");
    naviComp.set(markerId, newMarker);
    naviCompModified = true;
  }

  public void markerDelete (ArrayList<NaviCompMarker> markers) {
    for (NaviCompMarker marker : markers) {
      MyLog.d(LOG_TAG, "Deleting marker (" + marker.getLabel() + ")");
      naviComp.remove(marker);
    }
    naviCompModified = true;
  }

  void updateNaviComp() {
    MyLog.d(LOG_TAG, "Updating NaviComp");
    for (Station station : space)
      station.analyzeMap(naviComp);
    naviCompModified = false;
    modified = true;
  }

  private PointF findCenter(ArrayList<Station> stations) {
    Station.Info info = stations.get(0).getInfo();
    float minX = info.getCenterPosX();
    float maxX = info.getCenterPosX();
    float minY = info.getCenterPosY();
    float maxY = info.getCenterPosY();
    for (Station station : stations) {
      info = station.getInfo();
      if (info.getCenterPosX() < minX)
        minX = info.getCenterPosX();
      else if (info.getCenterPosX() > maxX)
        maxX = info.getCenterPosX();
      if (info.getCenterPosY() < minY)
        minY = info.getCenterPosY();
      else if (info.getCenterPosY() > maxY)
        maxY = info.getCenterPosY();
    }
    return new PointF((minX + maxX) / 2, (minY + maxY) / 2);
  }

  public String getFuelInfo() throws IOException {
    ArrayList<String> fuelInfo = new ArrayList<>();
    fuelInfo.add(SBML.FUEL_INFO_FIRST_LINE);
    for (Module module : alone) {
      if (module.hasFuel()) {
        fuelInfo.add(String.format(Locale.US, SBML.FUEL_INFO_FORMAT, module.getPartId(),
          module.getMainFuelCapacity(), module.getThtFuelCapacity()));
      }
    }

    String fileName = Settings.getSysCacheDir() + SBML.FUEL_INFO_FILENAME;
    writeFile(fileName, fuelInfo);
    return fileName;
  }

  private void splitStations(ArrayList<Module> space) {
    MyLog.d(LOG_TAG, "Searching stations...");
    SparseArray<ArrayList<Integer>> rels = new SparseArray<>();
    ArrayList<Integer> aloneModules = new ArrayList<>(space.size());
    SparseArray<Module> newSpace = new SparseArray<>(space.size());

    //get all relations
    ArrayList<Module.DockPoint> dock;
    Integer slaveId;
    for (Module module : space) {
      int masterId = module.getSaveId();
      newSpace.put(masterId, module);
      aloneModules.add(masterId);

      if ((dock = module.getDock()) != null) {
        if (rels.get(masterId) == null)
          rels.append(masterId, new ArrayList<>());
        for (Module.DockPoint dockPoint : dock) {
          if (dockPoint.isDocked())
            rels.get(masterId).add(dockPoint.getSlaveId());
        }
        if (rels.get(masterId).isEmpty())
          rels.delete(masterId);
      }

      if ((slaveId = module.getParentModule()) != null) {
        if (rels.get(masterId) == null)
          rels.append(masterId, new ArrayList<>());
        rels.get(masterId).add(slaveId);
        if (rels.get(slaveId) == null)
          rels.append(slaveId, new ArrayList<>());
        rels.get(slaveId).add(masterId);
      }

      if ((slaveId = module.getPayloadParent()) != null) {
        if (rels.get(masterId) == null)
          rels.append(masterId, new ArrayList<>());
        rels.get(masterId).add(slaveId);
        if (rels.get(slaveId) == null)
          rels.append(slaveId, new ArrayList<>());
        rels.get(slaveId).add(masterId);
      }
    }
    space.clear();
    space = null;
    //getting all relations completed

    //get stand-alone modules
    for (int i = 0; i < rels.size(); i++)
      aloneModules.remove((Integer) rels.keyAt(i));
    aloneModules.trimToSize();
    this.alone = new ArrayList<>(aloneModules.size());
    for (Integer aloneId : aloneModules) {
      this.alone.add(newSpace.get(aloneId));
      newSpace.delete(aloneId);
    }

    ArrayList<HashSet<Integer>> stations = new ArrayList<>();
    int stationCount = 0, count1, count2;
    boolean flag;

    if (rels.size() > 0) {
      //search stations
      while (rels.size() > 0) {
        //new station init
        stations.add(new HashSet<>());
        stations.get(stationCount).add(rels.keyAt(0));
        flag = true;
        //station init completed

        //included modules search
        while (flag) {
          flag = false;
          count1 = stations.get(stationCount).size();

          //add current module relations
          Integer[] station = stations.get(stationCount).toArray(new Integer[0]);
          for (Integer master : station)
            stations.get(stationCount).addAll(rels.get(master));

          count2 = stations.get(stationCount).size();
          if (count2 > count1)
            flag = true;
        }
        //included modules search completed

        for (Integer processed : stations.get(stationCount))
          rels.delete(processed);
        stationCount++;
      }
      //stations search completed
    }

    int cnt = 1;
    this.space = new ArrayList<>(stations.size());
    StringBuilder sb = new StringBuilder();
    sb.append("  Stations: ").append(stations.size());
    for (HashSet<Integer> station : stations) {
      sb.append("\n    Station ").append(cnt++).append(" -> ")
        .append(station.size());

      Integer[] moduleSet = station.toArray(new Integer[0]);
      Arrays.sort(moduleSet);
      Station newStation = new Station(station.size());
      for (Integer id : moduleSet)
        newStation.addModule(newSpace.get(id));
      this.space.add(newStation.init(naviComp));
    }

    sb.append("\n  Alone: ").append(aloneModules.size());
    MyLog.d(LOG_TAG, "Search completed:\n" + sb.toString());
    analyze(false);
  }

  private ArrayList<Module> mergeStations() {
    MyLog.d(LOG_TAG, "Merging stations...");
    ArrayList<Module> space = new ArrayList<>(alone);
    for (Station station : this.space)
      space.addAll(station.getModules());
    Collections.sort(space, new Module.DefaultComparator());
    MyLog.d(LOG_TAG, "Stations merged (" + this.space.size()
      + "|" + alone.size() + " -> " + space.size() + ")");
    return space;
  }

  private ArrayList<Module> parseData(ArrayList<String> data)
    throws SBMLParserException {
    MyLog.d(LOG_TAG, "Parsing sandbox data...");
    ArrayList<Module> space = new ArrayList<>();
    naviComp = new ArrayList<>();

    int lineNum = 0;
    int modspacePosition = -1, naviCompPosition = 0;
    ArrayList<String> args = new ArrayList<>();

    for (String line : data) {
      lineNum++;
      if (line.isEmpty()) continue;
      args.clear();
      args.addAll(Arrays.asList(line.split(SBML.VAL_SEP)));

      if (args.size() < 2)
        throw new SBMLParserException("Incorrect SBML statement", lineNum);

      String section = args.get(SBML.START_INDEX);
      args.remove(SBML.START_INDEX);
      String key = args.get(SBML.START_INDEX);
      args.remove(SBML.START_INDEX);

      switch (section) {
        case SBML.SECTION_SYSTEM:
        case SBML.SECTION_SANDBOX:
          try { setValue(key, args); }
          catch (IllegalArgumentException e) {
            throw new SBMLParserException(e.getMessage(), lineNum);
          }
          break;
        case SBML.SECTION_MODSPACE:
          if (key.equals(SBML.KEY_RECORD_COUNT)) {
            continue;
          } else if (key.equals(SBML.KEY_SAVE_ID)) {
            space.add(new Module());
            modspacePosition++;
          }
          space.get(modspacePosition).setValue(key, args);
          break;
        case SBML.SECTION_NAVICOMP:
          if (key.equals(SBML.KEY_NAV_END)) {
            naviCompPosition++;
            continue;
          } else if (key.equals(SBML.KEY_NAV_LABEL)) {
            naviComp.add(new NaviCompMarker());
          }
          naviComp.get(naviCompPosition).setValue(key, args);
          break;
        default:
          throw new SBMLParserException("Unknown SBML section (" + section + ")", lineNum);
      }
    }

    MyLog.d(LOG_TAG, "Parsing completed");
    return space;
  }

  private ArrayList<String> exportData(ArrayList<Module> space) {
    MyLog.d(LOG_TAG, "Exporting sandbox data...");
    ArrayList<String> data = new ArrayList<>();

    //system information
    MyLog.d(LOG_TAG, "\"system\" section");
    for (String key : SBML.systemAttrSet) {
      data.add(Utils.joinStr(SBML.VAL_SEP, SBML.SECTION_SYSTEM, key, getValue(key)));
    }

    //sandbox information
    MyLog.d(LOG_TAG, "\"sandbox\" section");
    for (String key : SBML.sandboxAttrSet) {
      String value = getValue(key);
      if (value != null)
        data.add(Utils.joinStr(SBML.VAL_SEP, SBML.SECTION_SANDBOX, key, value));
    }

    //modules set
    if (space != null && !space.isEmpty()) {
      MyLog.d(LOG_TAG, "\"modspace\" section");
      data.add(Utils.joinStr(SBML.VAL_SEP,
        SBML.SECTION_MODSPACE, SBML.KEY_RECORD_COUNT, String.valueOf(space.size())));
      for (Module module : space) {
        for (String key : SBML.moduleAttrSet) {
          String value = module.getValue(key);
          if (value != null) {
            if (key.equals(SBML.KEY_CARGO_ITEM) || key.equals(SBML.KEY_DOCK_POINT)) {
              do {
                data.add(Utils.joinStr(SBML.VAL_SEP, SBML.SECTION_MODSPACE, key, value));
              } while ((value = module.getValue(key)) != null);
            } else
              data.add(Utils.joinStr(SBML.VAL_SEP, SBML.SECTION_MODSPACE, key, value));
          }
        }
      }
    }

    //markers set
    if (!naviComp.isEmpty()) {
      MyLog.d(LOG_TAG, "\"navicomp\" section");
      for (NaviCompMarker marker : naviComp) {
        for (String key : SBML.naviCompAttrSet) {
          if (key.equals(SBML.KEY_NAV_END))
            data.add(Utils.joinStr(SBML.VAL_SEP, SBML.SECTION_NAVICOMP, key));
          else
            data.add(Utils.joinStr(
              SBML.VAL_SEP, SBML.SECTION_NAVICOMP, key, marker.getValue(key)));
        }
      }
    }

    MyLog.d(LOG_TAG, "Export completed (" + data.size() +  ")");
    return data;
  }

  void save(Context context, boolean overwrite, boolean compress, int verCode)
    throws IOException {
    String fileName = this.fileName;
    if (overwrite)
      fileName = this.inputFileName;
    writeFile(fileName, exportData(mergeStations()));
    if (compress) {
      SbxConverter converter = new SbxConverter(context,
        new SbxConverter.Config(fileName, SbxConverter.ACTION_COMPRESS, verCode));
      converter.convert();
      if (converter.getStatusCode() != SbxConverter.STATUS_CODE_OK)
        throw new IOException("Can't compress sandbox");
    }
    modified = false;
  }

  private ArrayList<String> readFile(String fileName)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading:\n  " + fileName);
    File file = new File(fileName);
    MyLog.d(LOG_TAG, "File size: " + file.length());
    BufferedReader reader = new BufferedReader(new FileReader(file));
    ArrayList<String> data = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null)
      if (!line.isEmpty()) data.add(line);
    MyLog.d(LOG_TAG, "Reading completed");
    return data;
  }

  private void writeFile(String fileName, ArrayList<String> data)
    throws IOException {
    MyLog.d(LOG_TAG, "Writing:\n  " + fileName);
    File file = new File(fileName);
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    for (String line : data) {
      writer.append(line);
      writer.newLine();
    }
    writer.close();
    MyLog.d(LOG_TAG, "Writing completed (" + file.length() + ")");
  }

  private boolean isCompressed(String fileName)
    throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    char[] buff = new char[SBML.SECTION_SYSTEM.length()];
    reader.read(buff, SBML.START_INDEX, buff.length);
    reader.close();
    boolean isCompressed = !(new String(buff).equals(SBML.SECTION_SYSTEM));
    if (isCompressed)
      MyLog.w(LOG_TAG, "Sandbox file compressed");
    return isCompressed;
  }

  private void setValue(String key, ArrayList<String> args) {
    String value = null;

    if (args.size() == 1)
      value = args.get(SBML.START_INDEX);

    switch (key) {
      case SBML.KEY_FORMAT_VERSION:
        this.formatVersion = Integer.parseInt(value);
        break;
      case SBML.KEY_FILE_TYPE:
        this.fileType = Integer.parseInt(value);
        break;
      case SBML.KEY_VERSION:
        this.sbxVersion = Integer.parseInt(value);
        break;
      case SBML.KEY_NAME:
        this.sbxName = value;
        break;
      case SBML.KEY_HIGH_MISSION:
        this.sbxHighMission = Integer.parseInt(value);
        break;
      case SBML.KEY_UID:
        this.sbxUid = value == null ? null : Integer.parseInt(value);
        break;
      default:
        throw new IllegalArgumentException("Unknown SBML field (" + key + ")");
    }
  }

  private String getValue(String key) {
    switch (key) {
      case SBML.KEY_FORMAT_VERSION:
        return String.valueOf(formatVersion);
      case SBML.KEY_FILE_TYPE:
        return String.valueOf(fileType);
      case SBML.KEY_VERSION:
        return String.valueOf(sbxVersion);
      case SBML.KEY_NAME:
        return sbxName;
      case SBML.KEY_HIGH_MISSION:
        return Utils.intToString(sbxHighMission);
      case SBML.KEY_UID:
        return sbxUid == null ? "" : String.valueOf(sbxUid);
      default:
        return null;
    }
  }

  public Station getStation(int position) {
    if (position < 0 || position >= space.size())
      return null;
    else
      return space.get(position);
  }

  public Info getInfo() {
    return info;
  }

  public String getFileName() {
    return fileName;
  }

  public ArrayList<NaviCompMarker> getNaviComp() {
    return naviComp;
  }

  public boolean isImported() {
    return inputFileName != null;
  }

  public boolean isModified() {
    return modified;
  }

  public boolean isNaviCompModified() {
    return naviCompModified;
  }

  public void setNaviCompModified() {
    naviCompModified = true;
  }

  @Override
  public String toString() {
    return "Sandbox info (" + hashCode() + ")"
      + "\n  inputFileName: " + inputFileName
      + "\n  fileName: " + fileName
      + "\n -----"
      + "\n  format version: " + formatVersion
      + "\n  file type: " + fileType
      + "\n  version: " + sbxVersion
      + "\n  name: " + sbxName
      + "\n  high mission: " + sbxHighMission
      + "\n  uid: " + sbxUid
      + "\n -----"
      + "\n  modules: " + info.modulesCount
      + "\n  stations: " + space.size()
      + "\n  stand-alone: " + alone.size()
      + "\n  markers: " + naviComp.size();
  }

  public class Info {
    private int largestStationSize = 0;
    private int modulesCount = 0;
    private int maxSaveId = 0;
    private int minVer = SBML.VER_CODE_14;

    public String getSbxName() {
      return sbxName;
    }

    public String getSbxUid() {
      return sbxUid == null ? null : String.valueOf(sbxUid);
    }

    public int getModulesCount() {
      return modulesCount;
    }

    public int getObjectsCount() {
      return space.size() + alone.size();
    }

    public int getStationCount() {
      return space.size();
    }

    public int getAloneCount() {
      return alone.size();
    }

    public int getMarkerCount() {
      return naviComp.size();
    }

    public int getLargestStationSize() {
      return largestStationSize;
    }

    public int getMaxSaveId() {
      return maxSaveId;
    }

    public int getMinVer() {
      return minVer;
    }

    public void setSbxName(String name) {
      sbxName = name;
      modified = true;
    }

    public void setSbxUid(Integer uid) {
      sbxUid = uid;
      modified = true;
    }
  }

  // TESTING
  private Sandbox() {}

  static String parserTest(Context context, String inputFileName) {
    ArrayList<String> data;
    Sandbox sbx = new Sandbox();

    try {
      String fileName = context.getCacheDir().getAbsolutePath()
        + "/" + new File(inputFileName).getName();
      Utils.copyFile(inputFileName, fileName);
      if (sbx.isCompressed(fileName)) {
        SbxConverter converter = new SbxConverter(context,
          new SbxConverter.Config(fileName, SbxConverter.ACTION_UNCOMPRESS, 0));
        converter.convert();
        if (converter.getStatusCode() != SbxConverter.STATUS_CODE_OK)
          throw new IOException("Can't decompress sandbox");
      }
      data = sbx.readFile(fileName);
      new File(fileName).delete();
    } catch (IOException e) {
      return e.toString();
    }

    try {
      sbx.splitStations(sbx.parseData(data));
      ArrayList<String> data2 = sbx.exportData(sbx.mergeStations());

      int count = 0;
      ArrayList<String> test = new ArrayList<>();
      for (int i = 0; i < data.size(); i++) {
        if (!data.get(i).equals(data2.get(i))) {
          MyLog.v(LOG_TAG, data.get(i) + " --> " + data2.get(i));
          test.add(Utils.joinStr("  -->  ", data.get(i), data2.get(i)));
          count++;
        }
      }

      MyLog.d(LOG_TAG, data.size() + " --> " + data2.size() + " [" + count + "]");
      return Utils.joinStr("\n", test);
    } catch (SBMLParserException e) {
      e.printStackTrace();
      return e.toString();
    }
  }

  class SBMLParserException extends Exception {
    private int lineNum;

    SBMLParserException(String msg, int line) {
      super(msg);
      lineNum = line;
    }

    public int getLineNum() {
      return lineNum;
    }
  }
}
