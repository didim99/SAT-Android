package com.didim99.sat;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
  private static final String LOG_TAG = "SAT_test";

  @Test
  public void useAppContext() throws Exception {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();

    assertEquals("com.didim99.sat", appContext.getPackageName());
  }

  /*public static void sortingTest() {
    MyLog.d(LOG_TAG, "Creating collection");
    long genStart = System.currentTimeMillis();

    *//*SparseArray<Part> partStorage = Storage.getPartInfo();
    ArrayList<Part> parts = new ArrayList<>(partStorage.size());
    for (int i = 0; i < partStorage.size(); i++)
      parts.add(partStorage.valueAt(i));*//*

    int size = 10000;
    Random random = new Random();
    ArrayList<Part> parts = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Part part = new Part(Math.abs(random.nextInt()) % size);
      part.setFuelMain(Math.abs(random.nextInt()) % 10000);
      part.setFuelThr(Math.abs(random.nextInt()) % 10000);
      part.setCargoCount(Math.abs(random.nextInt()) % 50);
      part.setPowerGen(Math.abs(random.nextInt()) % 1000);
      part.setPowerUse(Math.abs(random.nextInt()) % 1000);
      part.setPartName(Utils.md5(part.toString()));
      parts.add(part);
    }

    long genEnd = System.currentTimeMillis();
    MyLog.d(LOG_TAG, "Starting test");
    long start = System.currentTimeMillis();

    Collections.sort(parts, PartComparator.create(
      PartComparator.Method.CARGO_COUNT, PartComparator.Method.PART_MANE));
    Collections.sort(parts, PartComparator.create(
      PartComparator.Method.CARGO_COUNT, PartComparator.Method.PART_ID));

    long end = System.currentTimeMillis();
    MyLog.d(LOG_TAG, "Completed");
    MyLog.d(LOG_TAG, "Generating: " + (genEnd - genStart));
    MyLog.d(LOG_TAG, "Sorting: " + (end - start));
  }*/
}
