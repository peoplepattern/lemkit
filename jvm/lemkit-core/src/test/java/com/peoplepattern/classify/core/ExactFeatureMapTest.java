package com.peoplepattern.classify.core;

import com.eclipsesource.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExactFeatureMapTest {

  final String[] feats1 = new String[] {"A", "B", "C"};
  final String[] feats1c = Arrays.copyOf(feats1, feats1.length);
  final String[] feats2 = new String[] {"B", "D", "E"};

  final long sig1 = 1L;
  final long sig2 = 2L;

  final ExactFeatureMap m11 = new ExactFeatureMap(sig1, feats1);
  final ExactFeatureMap m11c = new ExactFeatureMap(sig1, feats1c);
  final ExactFeatureMap m12 = new ExactFeatureMap(sig1, feats2);
  final ExactFeatureMap m21 = new ExactFeatureMap(sig2, feats1);

  @Test
  public void testIndexOfFeature() {
    assertEquals(0, m11.indexOfFeature("A"));
    assertEquals(1, m11.indexOfFeature("B"));
    assertEquals(-1, m11.indexOfFeature("foo"));
  }

  @Test
  public void testFunctionSig() {
    assertEquals(sig1, m11.functionSig());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgToConstructor() {
    new ExactFeatureMap(1L, null);
  }

  @Test
  public void testEquals() {
    assertNotEquals(m11, null);
    assertEquals(m11, m11);
    assertEquals(m11, m11c);
    assertNotEquals(m11, m12);
    assertNotEquals(m11, m21);
  }

  @Test
  public void testWorksInSets() {
    HashSet<ExactFeatureMap> s = new HashSet<ExactFeatureMap>();
    assertEquals(0, s.size());
    s.add(m11);
    assertEquals(1, s.size());
    s.add(m11c);
    assertEquals(1, s.size());
    s.add(m21);
    assertEquals(2, s.size());
    s.add(m12);
    assertEquals(3, s.size());
  }

  @Test
  public void testSize() {
    assertEquals(3, m11.size());
  }

  @Test
  public void testStreamIo() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] bytes;
    try {
      m11.writeToStream(new DataOutputStream(baos));
      bytes = baos.toByteArray();
    } finally {
      baos.close();
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    try {
      final DataInputStream dis = new DataInputStream(bais);
      final FeatureMap output = FeatureMap.binaryReader(sig1).readFromStream(dis);
      assertEquals(m11, output);
    } finally {
      bais.close();
    }
  }

  @Test
  public void testJsonIo() {
    final JsonValue json = m11.toJson();
    final FeatureMap output = FeatureMap.jsonReader(sig1).fromJson(json);
    assertEquals(m11, output);
  }
}
