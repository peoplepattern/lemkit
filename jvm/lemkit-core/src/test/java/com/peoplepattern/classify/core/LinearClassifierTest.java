package com.peoplepattern.classify.core;

import org.junit.Test;

import static java.lang.Math.log;
import static org.junit.Assert.*;

public class LinearClassifierTest {

  @Test(expected = IllegalArgumentException.class)
  public void testVoidEverything() {
    new LinearClassifier(1L, null, null);
  }

  final Vec[] params1;
  {
    Vec v1 = new Vec(new double[] {-1.0, 5.0, 5.0});
    Vec v2 = new Vec(new double[] {5.0, 5.0, -1.0});
    params1 = new Vec[] {v1, v2};
  }

  final String[] labels1 = new String[] {"A", "B"};

  final Vec a1 = new Vec(new double[] {0.0, 1.0, 1.0});
  final Vec b1 = new Vec(new double[] {1.0, 1.0, 0.0});

  final Datum da = new Datum(1L, a1);
  final Datum db = new Datum(1L, b1);

  final LinearClassifier lc1 = new LinearClassifier(1L, labels1, params1);

  @Test(expected = IllegalArgumentException.class)
  public void testVoidParams() {
    new LinearClassifier(1L, labels1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVoidLabels() {
    new LinearClassifier(1L, null, params1);
  }

  @Test
  public void testBasicClassification() {
    assertEquals("A", lc1.predict(da).best());
    assertEquals("B", lc1.predict(db).best());
  }
}
