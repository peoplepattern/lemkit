package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import java.util.HashSet;
import org.junit.Test;

import static java.lang.Math.sqrt;
import static org.junit.Assert.*;

public class VecTest {

  final Vec d1 = new Vec(new double[] {1.0, 0.0, 2.1, 0.0, 3.0});
  final Vec d1copy = new Vec(new double[] {1.0, 0.0, 2.1, 0.0, 3.0});
  final Vec d2 = new Vec(new double[] {2.1, 1.0, 0.0, 5.0, 5.0});
  final Vec s1 = new Vec(5, new int[] {0, 2, 4}, new double[] {1.0, 2.1, 3.0});
  final Vec s1copy = new Vec(5, new int[] {0, 2, 4}, new double[] {1.0, 2.1, 3.0});
  final Vec s2 = new Vec(5, new int[] {0, 1, 3, 4}, new double[] {2.1, 1.0, 5.0, 5.0});

  final String d1json = "[1, 0, 2.1, 0, 3]";
  final String s1json = "{\"size\": 5, \"indices\": [0, 2, 4], \"values\": [1, 2.1, 3]}";

  @Test
  public void denseDotProd() {
    assertEquals(17.1, d1.dot(d2), 1e-7);
  }

  @Test
  public void sparseDotProd() {
    assertEquals(17.1, s1.dot(s2), 1e-7);
  }

  @Test
  public void denseSparseDotProduct() {
    assertEquals(17.1, d1.dot(s2), 1e-7);
  }

  @Test
  public void sparseDenseDotProduct() {
    assertEquals(17.1, s1.dot(d2), 1e-7);
  }

  @Test
  public void denseEquals() {
    assertEquals(d1, d1copy);
    assertNotEquals(d1, d2);
  }

  @Test
  public void sparseEquals() {
    assertEquals(s1, s1copy);
    assertNotEquals(s1, s2);
  }

  @Test
  public void denseSparseEquals() {
    assertEquals(d1, s1);
    assertNotEquals(d1, s2);
  }

  @Test
  public void sparseDenseEquals() {
    assertEquals(s1, d1);
    assertNotEquals(s1, d2);
  }

  @Test
  public void denseHashCodeSane() {
    assertEquals(d1.hashCode(), d1copy.hashCode());
  }

  @Test
  public void sparseHashCodeSane() {
    assertEquals(s1.hashCode(), s1copy.hashCode());
  }

  @Test(expected = IllegalArgumentException.class)
  public void noNullSparseIndices() {
    Vec v = new Vec(4, null, new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noNullSparseValues() {
    Vec v = new Vec(4, new int[0], null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noNullDenseValues() {
    Vec v = new Vec(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noIndicesGreaterThanSize() {
    Vec v = new Vec(4, new int[] {0, 1, 4}, new double[] {1.0, 2.1, 3.0});
  }

  @Test(expected = IllegalArgumentException.class)
  public void indicesSameSizeAsValues1() {
    Vec v = new Vec(4, new int[] {0, 1, 2}, new double[] {1.0, 2.1});
  }

  @Test(expected = IllegalArgumentException.class)
  public void indicesSameSizeAsValues2() {
    Vec v = new Vec(4, new int[] {0, 1, 2}, new double[] {1.0, 2.1, 3.0, 4.0});
  }

  @Test
  public void vecHashCodeWorksInSets() {
    HashSet<Vec> s = new HashSet<Vec>();
    s.add(d1);
    assertEquals("After d1", 1, s.size());
    s.add(d1copy);
    assertEquals("After d1copy", 1, s.size());
    s.add(s1);
    assertEquals("After s1", 1, s.size());
    s.add(s1copy);
    assertEquals("After s1 copy", 1, s.size());
    s.add(d2);
    assertEquals("After d2", 2, s.size());
    s.add(s2);
    assertEquals("After s2", 2, s.size());
  }

  @Test
  public void vecHashCode() {
    assertEquals("d1 / d1copy", d1.hashCode(), d1copy.hashCode());
    assertEquals("d1 / s1", d1.hashCode(), s1.hashCode());
  }

  @Test
  public void testToJson() {
    assertEquals(Json.parse(d1json), d1.toJson());
    assertEquals(Json.parse(s1json), s1.toJson());
  }

  @Test
  public void testFromJson() {
    assertEquals(d1, Vec.JPARSER.fromJsonString(d1json));
    assertEquals(s1, Vec.JPARSER.fromJsonString(s1json));
  }
}
