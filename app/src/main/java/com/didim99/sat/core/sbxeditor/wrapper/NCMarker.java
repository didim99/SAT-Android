package com.didim99.sat.core.sbxeditor.wrapper;

import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * NaviComp Marker container class
 * Created by didim99 on 14.02.18.
 */

public class NCMarker {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NCMarker";

  private String label;
  private float[] center;
  private float[] position;
  private float objectRadius;
  private float orbitRadius;
  private float rescaleRadius;
  private float scale;

  public NCMarker() {}

  public NCMarker(String label, float centerX, float centerY,
                  float objR, float orbR, float rescaleR, float scale) {
    this.label = label;
    this.center = new float[] {centerX, centerY};
    this.position = new float[] {centerX, centerY};
    this.objectRadius = objR;
    this.orbitRadius = orbR;
    this.rescaleRadius = rescaleR;
    this.scale = scale;
  }

  public NCMarker(Planet planet) {
    this.label = planet.getLabel();
    this.center = new float[] {planet.getPositionX(), planet.getPositionY()};
    this.position = new float[] {planet.getPositionX(), planet.getPositionY()};
    this.objectRadius = planet.getObjectRadius();
    this.orbitRadius = planet.getOrbitRadius();
    this.rescaleRadius = planet.getRescaleRadius();
    this.scale = planet.getScale();
  }

  public void setValue(String key, ArrayList<String> args) {
    String value = null;

    if (args.size() == 1)
      value = args.get(0);

    switch (key) {
      case SBML.Key.NAV_LABEL:
        this.label = value;
        break;
      case SBML.Key.NAV_CENTER:
        this.center = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.NAV_POSITION:
        this.position = Utils.stringArrayToFloatArray(args);
        break;
      case SBML.Key.NAV_OBJECT_RADIUS:
        this.objectRadius = Float.parseFloat(value);
        break;
      case SBML.Key.NAV_ORBIT_RADIUS:
        this.orbitRadius = Float.parseFloat(value);
        break;
      case SBML.Key.NAV_RESCALE_RADIUS:
        this.rescaleRadius = Float.parseFloat(value);
        break;
      case SBML.Key.NAV_SCALE:
        this.scale = Float.parseFloat(value);
        break;
    }
  }

  public String getValue(String key) {
    switch (key) {
      case SBML.Key.NAV_LABEL:
        return label;
      case SBML.Key.NAV_CENTER:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(center, SBML.PREC_DEFAULT));
      case SBML.Key.NAV_POSITION:
        return Utils.joinStr(SBML.VAL_SEP,
          Utils.FloatArrayToStringArray(position, SBML.PREC_COORDS));
      case SBML.Key.NAV_OBJECT_RADIUS:
        return Utils.floatToString(objectRadius, SBML.PREC_COORDS);
      case SBML.Key.NAV_ORBIT_RADIUS:
        return Utils.floatToString(orbitRadius, SBML.PREC_COORDS);
      case SBML.Key.NAV_RESCALE_RADIUS:
        return Utils.floatToString(rescaleRadius, SBML.PREC_COORDS);
      case SBML.Key.NAV_SCALE:
        return Utils.floatToString(scale, SBML.PREC_DEFAULT);
      default:
        return null;
    }
  }

  public String getLabel() {
    return label;
  }

  public float getCenterX() {
    return center[SBML.PosIndex.X];
  }

  public float getCenterY() {
    return center[SBML.PosIndex.Y];
  }

  public String getCenterStr(int precision) {
    return Utils.joinStr ("; ",
      Utils.floatToString(center[SBML.PosIndex.X] / SBML.POSITION_FACTOR, precision),
      Utils.floatToString(center[SBML.PosIndex.Y] / SBML.POSITION_FACTOR, precision)
    );
  }

  public float getObjectRadius() {
    return objectRadius;
  }

  public float getOrbitRadius() {
    return orbitRadius;
  }

  public float getRescaleRadius() {
    return rescaleRadius;
  }

  public float getScale() {
    return scale;
  }

  public void log() {
    MyLog.d(LOG_TAG, "NaviComp marker (" + hashCode() + ")"
      + "\n  label: " + label
      + "\n  center: " + Arrays.toString(center)
      + "\n  position: " + Arrays.toString(position)
      + "\n  object radius: " + objectRadius
      + "\n  orbit radius: " + orbitRadius
      + "\n  rescale radius: " + rescaleRadius
      + "\n  scale: " + scale
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NCMarker))
      return false;
    NCMarker other = (NCMarker) obj;
    return other.label.equals(label)
      && Arrays.equals(other.center, center)
      && other.objectRadius == objectRadius
      && other.orbitRadius == orbitRadius
      && other.rescaleRadius == rescaleRadius
      && other.scale == scale;
  }
}
