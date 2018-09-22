package com.didim99.sat.core.sbxeditor;

import android.content.Context;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.core.sbxeditor.wrapper.Module;
import com.didim99.sat.core.sbxeditor.wrapper.NCMarker;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.settings.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Text generator class
 * Created by didim99 on 17.03.18.
 */

public class TextGenerator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_textGen";

  //text formatting
  private static final int ALIGN_LEFT = 0;
  private static final int ALIGN_CENTER = 1;
  private static final int ALIGN_RIGHT = 2;
  //font format
  private static final String COMMENT_HEADER = "//";
  private static final char DOT_ACTIVE = '1';
  private static final int START_OFFSET = 1;
  private static final int HEADER_LENGTH = 1;
  private static final int FONT_WIDTH = 5;
  private static final int FONT_HEIGHT = 7;
  private static final int DOT_SIZE = 36;
  private static final int DEFAULT_LINE_MARGIN = 2;
  private static final int DEFAULT_LETTER_MARGIN = 1;
  private static final int DEFAULT_NAME_LENGTH = 15;
  private static final int[][] DOCK_PATTERN = {
    {0, -1, 0, 1}, //соседний модуль сверху
    {1, 1, 0, 0},  //соседний модуль снизу
    {2, 0, -1, 3}, //соседний модуль слева
    {3, 0, 1, 2}   //соседний модуль справа
  };

  private Map<Character, PixelChar> font;

  TextGenerator(Context context) throws IOException {
    if (!Settings.isFontLoaded())
      loadFont(context);
    else font = Storage.getFont();
  }

  public Station createText(SbxEditConfig config, ArrayList<NCMarker> naviComp) {
    int startSaveId = config.getStartSaveId();
    String text = config.getText();
    int align = config.getAlign();
    Integer margin = config.getMargin();
    if (margin == null)
      margin = DEFAULT_LETTER_MARGIN;
    float posX = config.getPositionX();
    float posY = config.getPositionY();
    MyLog.d(LOG_TAG, "Creating text (align: " + align + ", margin: " + margin
      + ", SID: " + startSaveId + ", posX: " + posX + "; posY: " + posY + ")\n" + text);
    Station station = new Station(text.length(), Station.Type.TEXT);
    station.addModules(makeText(text, posX, posY, startSaveId, align, margin));
    station.init(naviComp);

    String name;
    int nameLength = DEFAULT_NAME_LENGTH;
    if (text.length() <= nameLength)
      name = text;
    else {
      int spacePos = text.substring(0, DEFAULT_NAME_LENGTH - 1).lastIndexOf(' ');
      if (spacePos > 5)
        nameLength = spacePos;
      name = text.substring(0, nameLength) + "...";
    }
    name = name.replaceAll("[\t\n\r\\x00\\x0B]", " ").toUpperCase();
    station.getInfo().setName(name);

    MyLog.d(LOG_TAG, "Text created");
    return station;
  }

  public Station createAllFont(SbxEditConfig config, ArrayList<NCMarker> naviComp) {
    StringBuilder builder = new StringBuilder(font.size());
    int inLine = config.getInLine();
    Character[] chars = font.keySet().toArray(new Character[0]);
    Arrays.sort(chars);
    int count = 0;
    for (Character c : chars) {
      builder.append(c);
      if (++count == inLine) {
        builder.append("\n");
        count = 0;
      }
    }
    config.setAlign(ALIGN_LEFT);
    config.setText(builder.toString());
    return createText(config, naviComp);
  }

  private ArrayList<Module> makeText(String text, float startPosX, float posY,
                                     int startSaveId, int align, int margin) {
    ArrayList<Module> group = new ArrayList<>();
    String[] lines = text.split("\n");
    int[] len = new int[lines.length];

    for (int i = 0; i < lines.length; i++) {
      lines[i] = lines[i].replaceAll("[\t\r\\x00\\x0B]", "");
      len[i] = lines[i].length();
    }

    int maxLength = Utils.arrayMax(len);
    if (maxLength <= 0)
      throw new IllegalArgumentException("All lines is empty");

    float posX = 0;
    for (int i = 0; i < lines.length; i++) {
      switch (align) {
        case ALIGN_LEFT:
          posX = startPosX;
          break;
        case ALIGN_RIGHT:
          posX = startPosX + strOffset((maxLength - len[i]), margin);
          break;
        case ALIGN_CENTER:
          posX = startPosX + strOffset((maxLength - len[i]) / 2f, margin);
          break;
      }

      if (!lines[i].isEmpty())
        group.addAll(makeLine(lines[i].toUpperCase(), posX, posY, startSaveId, margin));
      if (group.size() > 0)
        startSaveId = group.get(group.size() - 1).getSaveId() + 1;
      posY += DOT_SIZE * (FONT_HEIGHT + DEFAULT_LINE_MARGIN);
    }

    return group;
  }

  private ArrayList<Module> makeLine (String line, float posX, float posY,
                                      int startSaveId, int margin) {
    ArrayList<Module> group = new ArrayList<>();
    for (char c : line.toCharArray()) {
      PixelChar pixelChar = font.get(c);
      if (pixelChar == null)
        throw new IllegalCharException("Invalid character \'" + c + "\'", c);
      group.addAll(makeChar(c, posX, posY, startSaveId));
      posX += DOT_SIZE * (FONT_WIDTH + margin);
      startSaveId += pixelChar.maxOffset;
    }
    return group;
  }

  private ArrayList<Module> makeChar(char c, float startPosX, float startPosY, int startSaveId) {
    PixelChar pixelChar = font.get(c);
    ArrayList<Module> group = new ArrayList<>(pixelChar.maxOffset);
    int[][] matrix = pixelChar.matrix;
    int time = Utils.getTimestamp();
    int saveId = startSaveId;
    float posX;

    for (int lineId = 0; lineId < matrix.length; lineId++) {
      posX = startPosX;

      for (int dotId = 0; dotId < matrix[lineId].length; dotId++) {
        if (matrix[lineId][dotId] > 0) {
          Module module = new Module(saveId++, SBML.PartID.HUB);
          module.setPosition(posX, startPosY, 0);
          module.addCargo(0, SBML.CargoID.BAT);
          module.setTimes(time);

          for (int[] dockState : DOCK_PATTERN) {
            //координаты соседнего модуля на карте символа
            int slaveId = 0;
            try { slaveId = matrix[lineId + dockState[1]][dotId + dockState[2]]; }
            catch (IndexOutOfBoundsException ignored) {}

            Module.DockPoint point;
            if (slaveId > 0)
              point = new Module.DockPoint(dockState[0], 1, 1, 1,
                startSaveId + slaveId - 1, dockState[3]);
            else
              point = new Module.DockPoint(dockState[0], 0, 0, 0,
                SBML.DOCK_STATE_UNDOCKED, null);
            module.addDock(point);
          }

          group.add(module);
        }

        posX += DOT_SIZE;
      }

      startPosY += DOT_SIZE;
    }

    return group;
  }

  private float strOffset(float chars, float margin) {
    return chars * DOT_SIZE * (FONT_WIDTH + margin);
  }

  private void loadFont(Context context) throws IOException {
    MyLog.d(LOG_TAG, "Loading font...");
    BufferedReader reader = new BufferedReader(new InputStreamReader(
      context.getResources().openRawResource(R.raw.font)));

    String line;
    font = new HashMap<>();
    Character index = null;
    PixelChar pixelChar = new PixelChar();
    int strPos = 0, offset = START_OFFSET;

    while ((line = reader.readLine()) != null) {
      if (line.isEmpty() || line.startsWith(COMMENT_HEADER))
        continue; //comment or empty string
      switch (line.length()) {
        case HEADER_LENGTH:
          if (index != null)
            font.put(index, pixelChar.init());
          index = line.charAt(0);
          pixelChar = new PixelChar();
          offset = START_OFFSET;
          strPos = 0;
          break;
        case FONT_WIDTH:
          if (strPos > FONT_HEIGHT)
            throw new IllegalArgumentException("Incorrect font file format");
          int[] dots = new int[FONT_WIDTH];
          for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == DOT_ACTIVE)
              dots[i] = offset++;
          }
          pixelChar.addLine(strPos++, dots);
          break;
        default:
          throw new IllegalArgumentException("Incorrect font file format");
      }
    }

    if (index != null)
      font.put(index, pixelChar.init());
    Settings.setFontLoaded(true);
    Storage.setFont(font);
    MyLog.d(LOG_TAG, "Font loaded (" + font.size() + ")");
  }

  public class PixelChar {
    private int[][] matrix;
    private int maxOffset = 0;

    PixelChar() {
      matrix = new int[FONT_HEIGHT][FONT_WIDTH];
    }

    void addLine(int str, int[] dots) {
      matrix[str] = dots;
    }

    PixelChar init() {
      for (int[] line : matrix) {
        for (int value : line) {
          if (value > maxOffset)
            maxOffset = value;
        }
      }
      return this;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < FONT_HEIGHT; i++) {
        for (int k = 0; k < FONT_WIDTH; k++)
          sb.append(matrix[i][k] > 0 ? 1 : 0);
        if (i < FONT_HEIGHT - 1)
          sb.append("\n");
      }
      return sb.toString();
    }
  }

  class IllegalCharException extends IllegalArgumentException {
    private char illegalChar;

    IllegalCharException(String msg, char c) {
      super(msg);
      illegalChar = c;
    }

    char getIllegalChar() {
      return illegalChar;
    }
  }
}
