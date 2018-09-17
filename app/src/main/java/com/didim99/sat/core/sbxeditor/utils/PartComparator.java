package com.didim99.sat.core.sbxeditor.utils;

import com.didim99.sat.core.sbxeditor.wrapper.Part;
import java.util.Comparator;

/**
 * Double level Part comparator
 * Created by didim99 on 16.06.18.
 */
public class PartComparator {

  private static final Comparator<Part> comparePartId = (o1, o2)
    -> Integer.compare(o1.getPartId(), o2.getPartId());

  private static final Comparator<Part> comparePartName = (o1, o2)
    -> o1.getPartName().compareTo(o2.getPartName());

  private static final Comparator<Part> compareFuelMain = (o1, o2)
    -> Integer.compare(o1.getFuelMain(), o2.getFuelMain());

  private static final Comparator<Part> compareFuelThr = (o1, o2)
    -> Integer.compare(o1.getFuelThr(), o2.getFuelThr());

  private static final Comparator<Part> comparePowerGen = (o1, o2)
    -> Integer.compare(o1.getPowerGen(), o2.getPowerGen());

  private static final Comparator<Part> comparePowerProfit = (o1, o2) -> {
    int profit1 = o1.getPowerGen() - o1.getPowerUse();
    int profit2 = o2.getPowerGen() - o2.getPowerUse();
    return Integer.compare(profit1, profit2);
  };

  private static final Comparator<Part> compareCargoCount = (o1, o2)
    -> Integer.compare(o1.getCargoCount(), o2.getCargoCount());

  public static Comparator<Part> create(int mainMethod, int secondMethod, boolean reverse) {
    return (p1, p2) -> {
      int direction = reverse ? -1 : 1;
      Comparator<Part> second;
      switch (secondMethod) {
        case Method.PART_ID:
          second = comparePartId;
          break;
        case Method.PART_MANE:
          second = comparePartName;
          break;
        default:
          throw new IllegalArgumentException("Incorrect second comparison method");
      }

      switch (mainMethod) {
        case Method.PART_ID:
          return direction * comparePartId.compare(p1, p2);
        case Method.PART_MANE:
          return direction * comparePartName.compare(p1, p2);
        case Method.FUEL_MAIN:
          return compare(p1, p2, compareFuelMain, second, direction);
        case Method.FUEL_THR:
          return compare(p1, p2, compareFuelThr, second, direction);
        case Method.POWER_GEN:
          return compare(p1, p2, comparePowerGen, second, direction);
        case Method.POWER_PROFIT:
          return compare(p1, p2, comparePowerProfit, second, direction);
        case Method.CARGO_COUNT:
          return compare(p1, p2, compareCargoCount, second, direction);
        default:
          throw new IllegalArgumentException("Incorrect main comparison method");
      }
    };
  }

  private static int compare(Part p1, Part p2, Comparator<Part> c1,
                             Comparator<Part> c2, int direction) {
    int i = direction * c1.compare(p1, p2);
    return i == 0 ? c2.compare(p1, p2) : i;
  }

  public static boolean hasSecondLevel(int firstLevel) {
    switch (firstLevel) {
      case Method.PART_ID:
      case Method.PART_MANE:
        return false;
      case Method.FUEL_MAIN:
      case Method.FUEL_THR:
      case Method.POWER_GEN:
      case Method.POWER_PROFIT:
      case Method.CARGO_COUNT:
        return true;
      default:
        throw new IllegalArgumentException("Incorrect main comparison method");
    }
  }

  public static final class Method {
    public static final int PART_ID = 0;
    public static final int PART_MANE = 1;
    public static final int FUEL_MAIN = 2;
    public static final int FUEL_THR = 3;
    public static final int POWER_GEN = 4;
    public static final int POWER_PROFIT = 5;
    public static final int CARGO_COUNT = 6;
  }
}
