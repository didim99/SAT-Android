package com.didim99.sat.ui.sbxeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.annotation.AttrRes;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.didim99.sat.R;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.core.sbxeditor.Station;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;

/**
 * Created by didim99 on 09.05.18.
 */

public class UIManager {
  private static final UIManager ourInstance = new UIManager();
  public static UIManager getInstance() {
    return ourInstance;
  }
  private UIManager() {}

  private Resources res;
  private Resources.Theme theme;
  private TypedValue typedValue;

  public void init(Context appContext) {
    this.res = appContext.getResources();
    this.theme = appContext.getTheme();
    this.typedValue = new TypedValue();
  }

  public void applyTheme(Resources.Theme theme) {
    this.theme = theme;
  }

  void setMovementIcon(ImageView view, Station.Info info) {
    view.setRotationY(0);
    if (info.hasMovement()) {
      int colorId;
      if (info.getMovementSpeed() > SBML.MAX_SPEED_ORBITAL)
        colorId = resolveAttr(R.attr.clr_SARed);
      else if (info.getMovementSpeed() > SBML.MAX_SPEED_SUB_ORBITAL)
        colorId = resolveAttr(R.attr.clr_SAGreen);
      else
        colorId = resolveAttr(R.attr.clr_SAOrange);
      view.setImageResource(resolveAttr(R.attr.ic_direction));
      view.setRotation(180 - info.getMovementDirection());
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.setImageResource(resolveAttr(R.attr.ic_cross));
      view.setRotation(0);
    }
  }

  void setRotationIcon(ImageView view, Station.Info info) {
    view.setRotation(0);
    if (info.hasRotation()) {
      float rotationAbs = Math.abs(info.getRotationSpeed());
      int colorId;
      if (!(rotationAbs < SBML.ROTATION_SPEED_MAX_VALUE))
        colorId = resolveAttr(R.attr.clr_SARed);
      else if (rotationAbs > SBML.ROTATION_SPEED_HIGH)
        colorId = resolveAttr(R.attr.clr_SAGreen);
      else
        colorId = resolveAttr(R.attr.clr_SAOrange);
      if (info.getObjType() == Station.Type.COLONY)
        view.setImageResource(resolveAttr(R.attr.ic_orbiting));
      else
        view.setImageResource(resolveAttr(R.attr.ic_rotation));
      view.setRotationY(info.getRotationSpeed() < 0 ? 180 : 0);
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.setImageResource(resolveAttr(R.attr.ic_cross));
      view.setRotationY(0);
    }
  }

  void setNavDistanceIcon(ImageView view, Station.Info info) {
    if (info.getObjType() == Station.Type.COLONY) {
      view.setVisibility(View.GONE);
    } else if (info.hasNavDistance()) {
      view.setVisibility(View.VISIBLE);
      view.setRotation(45 - info.getNavDirection());
      int colorId;
      double distance = info.getNavDistance();
      if (distance > SBML.DISTANCE_1000_NCU)
        colorId = resolveAttr(R.attr.clr_textActive);
      else if (distance > SBML.DISTANCE_500_NCU)
        colorId = resolveAttr(R.attr.clr_SALightGreen);
      else if (distance > SBML.DISTANCE_200_NCU)
        colorId = resolveAttr(R.attr.clr_SAGreen);
      else if (distance > SBML.DISTANCE_100_NCU)
        colorId = resolveAttr(R.attr.clr_SAOrange);
      else
        colorId = resolveAttr(R.attr.clr_SARed);
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.getDrawable().clearColorFilter();
      view.setVisibility(View.INVISIBLE);
      view.setRotation(0);
    }
  }

  void setTimeString(Integer launchTime, View textView) {
    CharSequence launchTimeStr;
    if (launchTime == null)
      launchTimeStr = res.getString(R.string.NA);
    else if (launchTime == SBML.TIMESTAMP_UNDEFINED)
      launchTimeStr = res.getString(R.string.longTimeAgo);
    else {
      launchTimeStr = DateFormat.format(Utils.DATE_FORMAT,
        Utils.timestampToMillis(launchTime));
    }
    ((TextView) textView).setText(launchTimeStr);
  }

  public int resolveAttr(@AttrRes int attr) {
    theme.resolveAttribute(attr, typedValue, true);
    return typedValue.resourceId;
  }
}
