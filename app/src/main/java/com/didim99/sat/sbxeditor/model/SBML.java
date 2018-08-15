package com.didim99.sat.sbxeditor.model;

/**
 * SBML constants
 * Created by didim99 on 14.02.18.
 */

public final class SBML {
  //default parameters
  public static final int FORMAT_VERSION = 0;
  public static final int FILE_TYPE_SANDBOX = 1;
  static final float PLANET_SCALE = 0.25f;
  public static final float MARKER_SCALE = 1f;
  //part & cargo IDs
  public static final int PART_ID_SPY = 103;
  public static final int PART_ID_HUB = 108;
  public static final int PART_ID_LOK_SERVICE = 125;
  public static final int PART_ID_SHUTTLE = 140;
  public static final int PART_ID_SOYUZ_SERVICE = 222;
  public static final int CARGO_ID_O2 = 500;
  public static final int CARGO_ID_CO2 = 501;
  public static final int CARGO_ID_H2O = 502;
  public static final int CARGO_ID_BAT = 503;
  //sections
  public static final String SECTION_SYSTEM = "system";
  public static final String SECTION_SANDBOX = "sandbox";
  public static final String SECTION_MODSPACE = "modspace";
  public static final String SECTION_NAVICOMP = "navicomp";
  //system attrs
  public static final String KEY_FORMAT_VERSION = "format version";
  public static final String KEY_FILE_TYPE = "file type";
  public static final String KEY_VERSION = "version";
  public static final String KEY_HIGH_MISSION = "high mission";
  public static final String KEY_NAME = "name";
  public static final String KEY_UID = "uid";
  //modules attrs
  public static final String KEY_RECORD_COUNT = "record count 01";
  public static final String KEY_SAVE_ID = "module save id 01";
  static final String KEY_PART_ID = "part id 01";
  static final String KEY_DEBUG_ID = "debug id 01";
  static final String KEY_STATE = "state 01";
  static final String KEY_EFFECT_COUNTER = "effect counter 01";
  static final String KEY_APPLY_GRAVITY = "apply gravity 01";
  static final String KEY_TEXTURE_ALPHA = "texture alpha 01";
  static final String KEY_TEMPERATURE = "temperature 01";
  static final String KEY_SHOW_IN_SELECTOR = "show in selector 01";
  public static final String KEY_CARGO_ITEM = "cargo item 01";
  static final String KEY_AIR = "air 01";
  static final String KEY_POWER_SATATE = "power state 01";
  static final String KEY_NAVICOMP_SATATE = "navicomp state 01";
  static final String KEY_COLLISION_SATATE = "collision state 01";
  static final String KEY_POSITION = "position 01";
  static final String KEY_MOVEMENT = "movement 01";
  static final String KEY_LAUNCH_TIMESTAMP = "launch timestamp 01";
  static final String KEY_LAST_USED_TIMESTAMP = "last used timestamp 01";
  static final String KEY_ORBITAL_SATATE = "orbital state 01";
  static final String KEY_FUEL_LEVELS = "fuel levels 01";
  static final String KEY_SOLAR_PANEL_SATATE = "solar panel state 01";
  static final String KEY_SIDE_PANEL_SATATE = "side panel state 01";
  static final String KEY_PARENT_MODULE = "parent module 01";
  static final String KEY_PAYLOAD_PARENT = "payload parent 01";
  static final String KEY_TRANSPONDER_ID = "transponder id 01";
  static final String KEY_TRANSPONDER_NAME = "transponder name 01";
  static final String KEY_TRANSPONDER_SELECTED = "transponder selected 01";
  public static final String KEY_DOCK_POINT = "dock point 01";
  //NaviComp attrs
  public static final String KEY_NAV_LABEL = "label";
  static final String KEY_NAV_CENTER = "center";
  static final String KEY_NAV_POSITION = "position";
  static final String KEY_NAV_OBJECT_RADIUS = "object radius";
  static final String KEY_NAV_ORBIT_RADIUS = "orbit radius";
  static final String KEY_NAV_RESCALE_RADIUS = "rescale radius";
  static final String KEY_NAV_SCALE = "scale";
  public static final String KEY_NAV_END = "end";
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
  public static final int START_INDEX = 0;
  public static final int PREC_DEFAULT = 2;
  static final int PREC_COORDS = 6;
  public static final int SIZE_SMALL = 1;
  public static final int SIZE_MEDIUM = 2;
  public static final int SIZE_LARGE = 3;
  public static final int VISIBILITY_MODE_VISIBLE = 0;
  public static final int VISIBILITY_MODE_INVISIBLE = 1;
  public static final int POSITION_FACTOR = 100;
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
  //parameters indexes
  static final int POS_INDEX_X = 0;
  static final int POS_INDEX_Y = 1;
  static final int POS_INDEX_ANGLE = 2;
  static final int MOVEMENT_INDEX_DIRECTION = 0;
  static final int MOVEMENT_INDEX_SPEED = 1;
  static final int MOVEMENT_INDEX_ROTATION_VISUAL = 2;
  static final int MOVEMENT_INDEX_ROTATION_REAL = 3;
  static final int FUEL_INDEX_MAIN_CAP = 0;
  static final int FUEL_INDEX_MAIN_VAL = 1;
  static final int FUEL_INDEX_THR_CAP = 2;
  static final int FUEL_INDEX_THR_VAL = 3;
  static final int DOCK_INDEX_ID = 0;
  static final int DOCK_INDEX_POWER = 1;
  static final int DOCK_INDEX_FUEL = 2;
  static final int DOCK_INDEX_DOOR = 3;
  static final int DOCK_INDEX_SLAVE_ID = 4;
  static final int DOCK_INDEX_SLAVE_PORT = 5;
  //version codes
  public static final int VER_CODE_14 = 14;
  public static final int VER_CODE_20 = 20;
  public static final int VER_CODE_21 = 21;
  public static final int VER_CODE_22 = 22;
  //attributes order
  public static final String[] systemAttrSet = {
    KEY_FORMAT_VERSION, KEY_FILE_TYPE
  };
  public static final String[] sandboxAttrSet = {
    KEY_VERSION, KEY_HIGH_MISSION, KEY_NAME, KEY_UID
  };
  public static final String[] moduleAttrSet = {
    KEY_SAVE_ID, KEY_PART_ID, KEY_DEBUG_ID, KEY_STATE, KEY_EFFECT_COUNTER,
    KEY_APPLY_GRAVITY, KEY_TEXTURE_ALPHA, KEY_TEMPERATURE, KEY_SHOW_IN_SELECTOR,
    KEY_CARGO_ITEM, KEY_AIR, KEY_POWER_SATATE, KEY_NAVICOMP_SATATE,
    KEY_COLLISION_SATATE, KEY_POSITION, KEY_MOVEMENT, KEY_LAUNCH_TIMESTAMP,
    KEY_LAST_USED_TIMESTAMP, KEY_ORBITAL_SATATE, KEY_FUEL_LEVELS,
    KEY_SOLAR_PANEL_SATATE, KEY_SIDE_PANEL_SATATE, KEY_PARENT_MODULE,
    KEY_PAYLOAD_PARENT, KEY_TRANSPONDER_ID, KEY_TRANSPONDER_NAME,
    KEY_TRANSPONDER_SELECTED, KEY_DOCK_POINT
  };
  public static final String[] naviCompAttrSet = {
    KEY_NAV_LABEL, KEY_NAV_CENTER, KEY_NAV_POSITION, KEY_NAV_OBJECT_RADIUS,
    KEY_NAV_ORBIT_RADIUS, KEY_NAV_RESCALE_RADIUS, KEY_NAV_SCALE, KEY_NAV_END
  };
}
