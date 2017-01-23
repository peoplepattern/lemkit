package com.peoplepattern.classify.core;

import com.eclipsesource.json.JsonValue;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;

import static com.eclipsesource.json.Json.parse;
import static com.peoplepattern.classify.core.Scored.fromJsonObject;
import static org.junit.Assert.*;

public class FeatureBundleTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNoNullArgsToConstructor() {
    new FeatureBundle(1L, null);
  }

  final Collection<Scored<String>> ss1, ss1c, ss2;
  {
    ss1 = fromJsonObject(parse("{\"a\":0.1,\"b\":0.2,\"c\":0.7}"));
    ss1c = fromJsonObject(parse("{\"b\":0.2,\"c\":0.7,\"a\":0.1}"));
    ss2 = fromJsonObject(parse("{\"a\":0.3,\"b\":0.6,\"c\":0.1}"));
  }

  final FeatureBundle b11 = new FeatureBundle(1L, ss1);
  final FeatureBundle b11c = new FeatureBundle(1L, ss1c);
  final FeatureBundle b21 = new FeatureBundle(2L, ss1);
  final FeatureBundle b12 = new FeatureBundle(1L, ss2);

  final FeatureMap map = new ExactFeatureMap(1L, new String[] {"a", "b", "c"}, false);

  final Datum d11 = new Datum(1L, new Vec(new double[] {0.1, 0.2, 0.7}));

  @Test
  public void testIdentity() {
    assertEquals(b11, b11c);
    assertNotEquals(b11, b21);
    assertNotEquals(b11, b12);
  }

  @Test
  public void testSets() {
    final HashSet<FeatureBundle> set = new HashSet<FeatureBundle>();
    assertEquals(0, set.size());
    set.add(b11);
    assertEquals(1, set.size());
    set.add(b11c);
    assertEquals(1, set.size());
    set.add(b21);
    assertEquals(2, set.size());
  }

  @Test
  public void testJson() {
    final JsonValue json =
        parse("{\"function_sig\":1,\"observations\":{\"b\":0.2,\"c\":0.7,\"a\":0.1}}");

    assertEquals(FeatureBundle.JPARSER.fromJson(json), b11);
    assertEquals(b11, FeatureBundle.JPARSER.fromJson(b11c.toJson()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCannotCreateDatumsWithInconsistentFunctionSig() {
    b21.toDatum(map);
  }

  @Test
  public void testToDatum() {
    assertEquals(d11, b11.toDatum(map));
  }
}
