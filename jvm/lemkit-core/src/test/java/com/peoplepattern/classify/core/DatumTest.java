package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatumTest {

  final double[] scores1 = new double[] {0.1, 0.2, 0.7};
  final double[] scores1c = Arrays.copyOf(scores1, scores1.length);
  final double[] scores2 = new double[] {0.2, 0.5, 0.3};

  final Vec vec1 = new Vec(scores1);
  final Vec vec1c = new Vec(scores1c);
  final Vec vec2 = new Vec(scores2);

  final long sig1 = 1L;
  final long sig2 = 2L;

  final Datum d11 = new Datum(sig1, vec1);
  final Datum d11c = new Datum(sig1, vec1c);
  final Datum d12 = new Datum(sig1, vec2);
  final Datum d21 = new Datum(sig2, vec1);

  final String d11json = "{\"function_sig\":1,\"vector\":[0.1,0.2,0.7]}";
  final String d11jsonSparse =
      "{\"function_sig\":1,\"vector\":{\"indices\":[0,1,2],\"values\":[0.1,0.2,0.7],\"size\":3}}";

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgToConstructor() {
    new Datum(1L, null);
  }

  @Test
  public void testEquals() {
    assertNotEquals(d11, null);
    assertEquals(d11, d11);
    assertEquals(d11, d11c);
    assertNotEquals(d11, d12);
    assertNotEquals(d11, d21);
  }

  @Test
  public void testWorksInSets() {
    HashSet<Datum> s = new HashSet<Datum>();
    assertEquals(0, s.size());
    s.add(d11);
    assertEquals(1, s.size());
    s.add(d11c);
    assertEquals(1, s.size());
    s.add(d21);
    assertEquals(2, s.size());
    s.add(d12);
    assertEquals(3, s.size());
  }

  @Test
  public void testToJson() {
    assertEquals(Json.parse(d11json), d11.toJson());
  }

  @Test
  public void testFromJson() {
    assertEquals(d11, Datum.JPARSER.fromJsonString(d11json));
    assertEquals(d11, Datum.JPARSER.fromJsonString(d11jsonSparse));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNull() {
    Datum.JPARSER.fromJson(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject1() {
    Datum.JPARSER.fromJsonString("[]");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject2() {
    Datum.JPARSER.fromJsonString("1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsOnNonObject3() {
    Datum.JPARSER.fromJsonString("\"foo\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsMissingFunctionSig() {
    Datum.JPARSER.fromJsonString("{\"vector\":[0.1,0.9]}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsFunctionSigNotNumber() {
    Datum.JPARSER.fromJsonString("{\"vector\":[0.1,0.9],\"function_sig\":\"foo\"}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsMissingVector() {
    Datum.JPARSER.fromJsonString("{\"function_sig\":1234567}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromJsonFailsVectorValueNotValid() {
    Datum.JPARSER.fromJsonString("{\"vector\":\"foo\",\"function_sig\":123456}");
  }
}
