package com.didim99.sat.sbxeditor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import com.didim99.sat.R;
import com.didim99.sat.sbxeditor.Station;
import com.didim99.sat.sbxeditor.model.SBML;

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

  public void init(Context appContext) {
    this.res = appContext.getResources();
  }

  void setMovementIcon(ImageView view, Station.Info info) {
    view.setRotationY(0);
    if (info.hasMovement()) {
      int colorId;
      if (info.getMovementSpeed() > SBML.MAX_SPEED_ORBITAL)
        colorId = R.color.speedOverOrbital;
      else if (info.getMovementSpeed() > SBML.MAX_SPEED_SUB_ORBITAL)
        colorId = R.color.speedOrbital;
      else
        colorId = R.color.speedSubOrbital;
      view.setImageResource(R.drawable.ic_direction_24dp);
      view.setRotation(180 - info.getMovementDirection());
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.setImageResource(R.drawable.ic_cross_24dp);
      view.setRotation(0);
    }
  }

  void setRotationIcon(ImageView view, Station.Info info) {
    view.setRotation(0);
    if (info.hasRotation()) {
      float rotationAbs = Math.abs(info.getRotationSpeed());
      int colorId;
      if (!(rotationAbs < SBML.ROTATION_SPEED_MAX_VALUE))
        colorId = R.color.speedOverOrbital;
      else if (rotationAbs > SBML.ROTATION_SPEED_HIGH)
        colorId = R.color.speedOrbital;
      else
        colorId = R.color.speedSubOrbital;
      if (info.getObjType() == Station.Type.COLONY)
        view.setImageResource(R.drawable.ic_orbiting_24dp);
      else
        view.setImageResource(R.drawable.ic_rotation_24dp);
      view.setRotationY(info.getRotationSpeed() < 0 ? 180 : 0);
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.setImageResource(R.drawable.ic_cross_24dp);
      view.setRotationY(0);
    }
  }

  void setNavDistanceIcon(ImageView view, Station.Info info) {
    if (info.hasNavDistance()) {
      view.setVisibility(View.VISIBLE);
      view.setRotation(45 - info.getNavDirection());
      int colorId;
      double distance = info.getNavDistance();
      if (distance > SBML.DISTANCE_1000_NCU)
        colorId = R.color.distanceOver1000;
      else if (distance > SBML.DISTANCE_500_NCU)
        colorId = R.color.distanceOver500;
      else if (distance > SBML.DISTANCE_200_NCU)
        colorId = R.color.distanceOver200;
      else if (distance > SBML.DISTANCE_100_NCU)
        colorId = R.color.distanceOver100;
      else
        colorId = R.color.distanceUnder100;
      view.getDrawable().setColorFilter(
        res.getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    } else {
      view.getDrawable().clearColorFilter();
      view.setVisibility(View.INVISIBLE);
      view.setRotation(0);
    }
  }
}
