package com.didim99.sat.sbxeditor.model;

import android.util.SparseArray;

import com.didim99.sat.sbxeditor.model.wrapper.Part;
import com.didim99.sat.sbxeditor.model.wrapper.Planet;

import java.util.ArrayList;
import java.util.Map;

/**
 * Internal data storage
 * Created by didim99 on 11.05.18.
 */
public class Storage {
  private static SparseArray<Part> partInfo;
  private static ArrayList<Planet> planetInfo;
  private static ArrayList<String> planetNames;
  private static Map<Character, TextGenerator.PixelChar> font;
  private static SparseArray<String> saVerInfo;
  private static ArrayList<String> saVerNames;
  private static SbxEditConfig editConfig;
  private static Sandbox sandbox;

  public static SparseArray<Part> getPartInfo() {
    return partInfo;
  }

  public static ArrayList<Planet> getPlanetInfo() {
    return planetInfo;
  }

  public static ArrayList<String> getPlanetNames() {
    if (planetNames == null) initPlanetNames();
    return planetNames;
  }

  public static Map<Character, TextGenerator.PixelChar> getFont() {
    return font;
  }

  public static SparseArray<String> getSAVerInfo() {
    return saVerInfo;
  }

  public static ArrayList<String> getSAVerNames() {
    return saVerNames;
  }

  public static SbxEditConfig getEditConfig() {
    return editConfig;
  }

  public static Sandbox getSandbox() {
    return sandbox;
  }

  public static void setPartInfo(SparseArray<Part> partInfo) {
    Storage.partInfo = partInfo;
  }

  public static void setPlanetInfo(ArrayList<Planet> planetInfo) {
    Storage.planetInfo = planetInfo;
  }

  public static void setFont(Map<Character, TextGenerator.PixelChar> font) {
    Storage.font = font;
  }

  public static void setEditConfig(SbxEditConfig editConfig) {
    Storage.editConfig = editConfig;
  }

  public static void setSandbox(Sandbox sandbox) {
    Storage.sandbox = sandbox;
  }

  public static void setSAVerInfo(SparseArray<String> saVerInfo) {
    Storage.saVerInfo = saVerInfo;
  }

  public static void setSAVerNames(ArrayList<String> saVerNames) {
    Storage.saVerNames = saVerNames;
  }

  private static void initPlanetNames() {
    planetNames = new ArrayList<>();
    for (Planet planet : planetInfo)
      planetNames.add(planet.getLabel());
    planetNames.trimToSize();
  }
}
