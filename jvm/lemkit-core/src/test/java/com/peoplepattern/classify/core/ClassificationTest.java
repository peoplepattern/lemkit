package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

import static java.lang.Math.exp;
import static org.junit.Assert.*;

public class ClassificationTest {

  final String[] labels1 = new String[] {"one", "two", "three"};
  final String[] labels1c = Arrays.copyOf(labels1, labels1.length);
  final String[] labels1v = new String[] {"three", "two", "one"};
  final String[] labels2 = new String[] {"a", "b", "c"};
  final double[] scores1 = new double[] {0.1, 0.2, 0.7};
  final double[] scores1c = Arrays.copyOf(scores1, scores1.length);
  final double[] scores1v = new double[] {0.7, 0.2, 0.1};
  final double[] scores2 = new double[] {0.2, 0.5, 0.3};

  final Classification cls11 = new Classification(labels1, scores1);
  final Classification cls11c = new Classification(labels1c, scores1c);
  final Classification cls11v = new Classification(labels1v, scores1v);
  final Classification cls12 = new Classification(labels1, scores2);
  final Classification cls21 = new Classification(labels2, scores1);

  final String cls11json =
      "{\"best\":\"three\",\"scores\":{\"one\":0.1,\"three\":0.7,\"two\":0.2}}";

  final double[] expectedScores = new double[] {0.1, 0.7, 0.2};
  {
    for (int i = 0; i < 3; i++)
      expectedScores[i] = exp(expectedScores[i]);
    double sum = 0.0;
    for (int i = 0; i < 3; i++)
      sum += expectedScores[i];
    for (int i = 0; i < 3; i++)
      expectedScores[i] /= sum;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoNullArgs() {
    new Classification(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoNullLabel() {
    new Classification(null, scores1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoNullScores() {
    new Classification(labels1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonEqualLabelsAndScoresSizes() {
    final String[] labels = new String[] {"a", "b", "c"};
    final double[] scores = new double[] {0.4, 0.6};
    new Classification(labels, scores);
  }

  @Test
  public void testEquals() {
    assertEquals(cls11, cls11);
    assertNotEquals(cls11, null);
    assertEquals(cls11, cls11c);
    assertNotEquals(cls11, cls12);
    assertNotEquals(cls11, cls21);
    assertEquals(cls11, cls11v);
  }

  @Test
  public void testWorksInSets() {
    HashSet<Classification> s = new HashSet<Classification>();
    assertEquals(0, s.size());
    s.add(cls11);
    assertEquals(1, s.size());
    s.add(cls11c);
    assertEquals(1, s.size());
    s.add(cls11v);
    assertEquals(1, s.size());
    s.add(cls11);
    assertEquals(1, s.size());
    s.add(cls21);
    assertEquals(2, s.size());
  }

  @Test
  public void testToJson() {
    assertEquals(Json.parse(cls11json), cls11.toJson());
  }

  @Test
  public void testFromJson() {
    assertEquals(cls11, Classification.JPARSER.fromJsonString(cls11json));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNull() {
    Classification.JPARSER.fromJson(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject1() {
    Classification.JPARSER.fromJsonString("[]");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject2() {
    Classification.JPARSER.fromJsonString("1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject3() {
    Classification.JPARSER.fromJsonString("\"foo\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnMissingFunctionScoresField() {
    Classification.JPARSER.fromJsonString("{\"best\":\"foo\"}");
  }

  @Test
  public void testBest() {
    assertEquals("three", cls11.best());
  }

  @Test
  public void testProbabilityOfBest() {
    assertEquals(expectedScores[1], cls11.probabilityOfBest(), 1E-1);
  }

  @Test
  public void testLabels() {
    assertArrayEquals(new String[] {"one", "three", "two"}, cls11.labels());
  }

  @Test
  public void testScores() {
    assertArrayEquals(expectedScores, cls11.probabilities(), 1E-7);
  }
}
