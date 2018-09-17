package com.didim99.sat.core.sbxeditor.wrapper;

/**
 * SBML constants
 * Created by didim99 on 14.02.18.
 */

public final class SBML {
  public enum DistanceUnit { NCU, PERCENT }
  //default parameters
  public static final int FORMAT_VERSION = 0;
  public static final int FILE_TYPE_SANDBOX = 1;
  static final float PLANET_SCALE = 0.25f;
  public static final float MARKER_SCALE = 1f;
  public static final String DEFAULT_SANDBOX_NAME = "My Sandbox";
  //service constants
  public static final String INVALID_SBX_NAME = ".*[^A-z0-9_\\- ].*";
  public static final String INVALID_MARKER_NAME = ".*[^A-Z0-9].*";
  public static final String FILE_MASK_SASBX = ".sasbx";
  public static final String VAL_SEP = ",";
  public static final String FS_PATH_SEP = "/";
  public static final String SANDBOX_TMP_DIR = "sandbox_tmp";
  public static final String RESOURCES_TMP_DIR = "resources_tmp";
  public static final String FUEL_INFO_FIRST_LINE = "PID: MAIN     THR";
  public static final String FUEL_INFO_FORMAT = "%3d: %7.2f, %7.2f";
  public static final String FUEL_INFO_FILENAME = "fuel_info.txt";
  public static final int TIMESTAMP_UNDEFINED = 1;
  public static final int POSITION_FACTOR = 100;
  public static final int PREC_DEFAULT = 2;
  static final int PREC_COORDS = 6;
  public static final int DOCK_STATE_UNDOCKED = -1;
  public static final int SAVE_CARGO_REGULAR = 1;
  public static final int SAVE_CARGO_ROOT = 2;
  //MIN/MAX values
  static final float CARGO_FULL_VALUE = 1f;
  public static final float MAX_SPEED_SUB_ORBITAL = 3.5f;
  public static final float MAX_SPEED_ORBITAL = 8f;
  public static final int OPACITY_MIN_VALUE = 0;
  public static final int OPACITY_MAX_VALUE = 100;
  public static final float DIRECTION_MIN_VALUE = 0f;
  public static final float DIRECTION_MAX_VALUE = 360f;
  public static final float ROTATION_SPEED_MIN_VALUE = -8f;
  public static final float ROTATION_SPEED_MAX_VALUE = 8f;
  public static final float ROTATION_SPEED_HIGH = 3.5f;
  public static final double DISTANCE_1000_NCU = POSITION_FACTOR * 1000;
  public static final double DISTANCE_500_NCU = POSITION_FACTOR * 500;
  public static final double DISTANCE_200_NCU = POSITION_FACTOR * 200;
  public static final double DISTANCE_100_NCU = POSITION_FACTOR * 100;

  public static final class PartID {
    public static final int SPY = 103;
    public static final int HUB = 108;
    public static final int LOK_SERVICE = 125;
    public static final int SHUTTLE = 140;
    public static final int SOYUZ_SERVICE = 222;
  }

  public static final class CargoID {
    public static final int O2 = 500;
    public static final int CO2 = 501;
    public static final int H2O = 502;
    public static final int BAT = 503;
  }

  public static final class Section {
    public static final String SYSTEM = "system";
    public static final String SANDBOX = "sandbox";
    public static final String MODSPACE = "modspace";
    public static final String NAVICOMP = "navicomp";
  }

  public static final class Key {
    //system attrs
    public static final String FORMAT_VERSION = "format version";
    public static final String FILE_TYPE = "file type";
    public static final String VERSION = "version";
    public static final String HIGH_MISSION = "high mission";
    public static final String NAME = "name";
    public static final String UID = "uid";
    //modules attrs
    public static final String RECORD_COUNT = "record count 01";
    public static final String SAVE_ID = "module save id 01";
    public static final String CARGO_ITEM = "cargo item 01";
    public static final String DOCK_POINT = "dock point 01";
    static final String PART_ID = "part id 01";
    static final String DEBUG_ID = "debug id 01";
    static final String STATE = "state 01";
    static final String EFFECT_COUNTER = "effect counter 01";
    static final String APPLY_GRAVITY = "apply gravity 01";
    static final String TEXTURE_ALPHA = "texture alpha 01";
    static final String TEMPERATURE = "temperature 01";
    static final String SHOW_IN_SELECTOR = "show in selector 01";
    static final String AIR = "air 01";
    static final String POWER_SATATE = "power state 01";
    static final String NAVICOMP_SATATE = "navicomp state 01";
    static final String COLLISION_SATATE = "collision state 01";
    static final String POSITION = "position 01";
    static final String MOVEMENT = "movement 01";
    static final String LAUNCH_TIMESTAMP = "launch timestamp 01";
    static final String LAST_USED_TIMESTAMP = "last used timestamp 01";
    static final String ORBITAL_SATATE = "orbital state 01";
    static final String FUEL_LEVELS = "fuel levels 01";
    static final String SOLAR_PANEL_SATATE = "solar panel state 01";
    static final String SIDE_PANEL_SATATE = "side panel state 01";
    static final String PARENT_MODULE = "parent module 01";
    static final String PAYLOAD_PARENT = "payload parent 01";
    static final String TRANSPONDER_ID = "transponder id 01";
    static final String TRANSPONDER_NAME = "transponder name 01";
    static final String TRANSPONDER_SELECTED = "transponder selected 01";
    //NaviComp attrs
    public static final String NAV_LABEL = "label";
    public static final String NAV_END = "end";
    static final String NAV_CENTER = "center";
    static final String NAV_POSITION = "position";
    static final String NAV_OBJECT_RADIUS = "object radius";
    static final String NAV_ORBIT_RADIUS = "orbit radius";
    static final String NAV_RESCALE_RADIUS = "rescale radius";
    static final String NAV_SCALE = "scale";
  }

  public static final class Size {
    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int LARGE = 3;
  }

  public static final class Visibility {
    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 1;
  }

  //parameters indexes
  static final class PosIndex {
    static final int X = 0;
    static final int Y = 1;
    static final int ANGLE = 2;
  }

  static final class MovementIndex {
    static final int DIRECTION = 0;
    static final int SPEED = 1;
    static final int ROTATION_VISUAL = 2;
    static final int ROTATION_REAL = 3;
  }

  static final class FuelIndex {
    static final int MAIN_CAP = 0;
    static final int MAIN_VAL = 1;
    static final int THR_CAP = 2;
    static final int THR_VAL = 3;
  }

  static final class DockIndex {
    static final int ID = 0;
    static final int POWER = 1;
    static final int FUEL = 2;
    static final int DOOR = 3;
    static final int SLAVE_ID = 4;
    static final int SLAVE_PORT = 5;
  }

  //parameter values
  public static final class OrbitalState {
    public static final int ORBITING = 2;
    public static final int LANDED = 4;
  }

  public static final class VerCode {
    public static final int V14 = 14;
    public static final int V20 = 20;
    public static final int V21 = 21;
    public static final int V22 = 22;
  }

  //attributes order
  public static final String[] systemAttrSet =
    { Key.FORMAT_VERSION, Key.FILE_TYPE };
  public static final String[] sandboxAttrSet =
    { Key.VERSION, Key.HIGH_MISSION, Key.NAME, Key.UID };
  public static final String[] moduleAttrSet = {
    Key.SAVE_ID, Key.PART_ID, Key.DEBUG_ID, Key.STATE, Key.EFFECT_COUNTER,
    Key.APPLY_GRAVITY, Key.TEXTURE_ALPHA, Key.TEMPERATURE, Key.SHOW_IN_SELECTOR,
    Key.CARGO_ITEM, Key.AIR, Key.POWER_SATATE, Key.NAVICOMP_SATATE,
    Key.COLLISION_SATATE, Key.POSITION, Key.MOVEMENT, Key.LAUNCH_TIMESTAMP,
    Key.LAST_USED_TIMESTAMP, Key.ORBITAL_SATATE, Key.FUEL_LEVELS,
    Key.SOLAR_PANEL_SATATE, Key.SIDE_PANEL_SATATE, Key.PARENT_MODULE,
    Key.PAYLOAD_PARENT, Key.TRANSPONDER_ID, Key.TRANSPONDER_NAME,
    Key.TRANSPONDER_SELECTED, Key.DOCK_POINT
  };
  public static final String[] naviCompAttrSet = {
    Key.NAV_LABEL, Key.NAV_CENTER, Key.NAV_POSITION, Key.NAV_OBJECT_RADIUS,
    Key.NAV_ORBIT_RADIUS, Key.NAV_RESCALE_RADIUS, Key.NAV_SCALE, Key.NAV_END
  };
  //others
  public static final int[] ORBITAL_STATES_COLONY =
    { OrbitalState.ORBITING, OrbitalState.LANDED };
}
