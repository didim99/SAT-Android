package com.didim99.sat.sbxeditor.model;

import com.google.gson.annotations.SerializedName;

/**
 * Planet information container
 * Created by didim99 on 09.03.18.
 */

public class Planet {
  @SerializedName("id")
  private int id;
  @SerializedName("label")
  private String label;
  @SerializedName("positionX")
  private int positionX;
  @SerializedName("positionY")
  private int positionY;
  @SerializedName("objectRadius")
  private int objectRadius;
  @SerializedName("orbitRadius")
  private int orbitRadius;
  @SerializedName("rescaleRadius")
  private int rescaleRadius;
  @SerializedName("scale")
  private float scale;

  public Planet() {
    this.scale = SBML.PLANET_SCALE;
  }

  //getters
  public int getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public int getPositionX() {
    return positionX;
  }

  public int getPositionY() {
    return positionY;
  }

  public int getObjectRadius() {
    return objectRadius;
  }

  public int getOrbitRadius() {
    return orbitRadius;
  }

  public int getRescaleRadius() {
    return rescaleRadius;
  }

  public float getScale() {
    return scale;
  }

  //setters
  public void setId(int id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setPositionX(int positionX) {
    this.positionX = positionX;
  }

  public void setPositionY(int positionY) {
    this.positionY = positionY;
  }

  public void setObjectRadius(int objectRadius) {
    this.objectRadius = objectRadius;
  }

  public void setOrbitRadius(int orbitRadius) {
    this.orbitRadius = orbitRadius;
  }

  public void setRescaleRadius(int rescaleRadius) {
    this.rescaleRadius = rescaleRadius;
  }

  @Override
  public String toString() {
    return "Planet info: (" + id + ")"
      + "\n  label: " + label
      + "\n  position: " + positionX + "; " + positionY
      + "\n  objectRadius: " + objectRadius
      + "\n  orbitRadius: " + orbitRadius
      + "\n  rescaleRadius: " + rescaleRadius
      + "\n  scale: " + scale;
  }
}
