package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import static com.peoplepattern.classify.core.MurmurHash3.murmurhash3_x86_32;
import static com.peoplepattern.classify.core.ClassifierIO.*;
import static java.lang.String.format;

/**
 * A {@link FeatureMap} using the hash trick for memory efficiency
 *
 * <p>Rather than storing an exact hash map from string feture keys to
 * indices in a vector-space, this class maps keys by hashing them into
 * a finite sequence of integers within a 0...N range. This can result
 * in key-clashes, but with sufficiently large N and sufficiently many
 * features this can have a minimal effect on classifier performance,
 * which is outstripped by the advantage of using very many features.
 *
 * <p>A good random hashing strategy should be used to keys are
 * distributed evenly across the range; this implementation uses
 * MurmurHash3.
 */
public class HashedFeatureMap implements FeatureMap, Serializable, BinarySupport, JsonSupport {

  public static final long serialVersionUID = 0L;

  private final int seed;
  private final long sig;
  private final int size;
  private final boolean addIntercept;

  /**
   * Consturct from a feature function signature, the size of the vector space
   * being targeted, and a seed for the hash function
   *
   * @param functionSig the unique signature of the featue function used to
   *             create the features this map will operate on
   * @param size the length of the vectors to be created; the
   *             {@link #indexOfFeature} method will not generate a number
   *             greater-than-or-equal to this value
   * @param seed a seed for the underlying MurmurHash3
   * @param addIntercept whether to add an intercept value to the feature vector
   */
  public HashedFeatureMap(final long functionSig, final int size, final int seed,
      final boolean addIntercept) {
    this.seed = seed;
    this.sig = functionSig;
    this.size = size;
    this.addIntercept = addIntercept;
  }

  /**
   * Consturct from a feature function signature, the size of the vector space
   * being targeted, and a seed for the hash function
   *
   * @param functionSig the unique signature of the featue function used to
   *             create the features this map will operate on
   * @param size the length of the vectors to be created; the
   *             {@link #indexOfFeature} method will not generate a number
   *             greater-than-or-equal to this value
   * @param seed a seed for the underlying MurmurHash3
   */
  public HashedFeatureMap(final long functionSig, final int size, final int seed) {
    this(functionSig, size, seed, true);
  }


  public boolean addIntercept() {
    return addIntercept;
  }

  /**
   * Consturct from a feature function signature, the size of the vector space
   * being targeted
   *
   * @param functionSig the unique signature of the featue function used to
   *             create the features this map will operate on
   * @param size the length of the vectors to be created; the
   *             {@link #indexOfFeature} method will not generate a number
   *             greater-than-or-equal to this value
   */
  public HashedFeatureMap(final long functionSig, final int size) {
    this(functionSig, size, 0);
  }

  public long functionSig() {
    return sig;
  }

  public int size() {
    return size;
  }

  public int indexOfFeature(final String feature) {
    final int hashVal = murmurhash3_x86_32(feature, 0, feature.length(), seed);
    return (hashVal % size + size) % size;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (other instanceof HashedFeatureMap) {
      final HashedFeatureMap otherMap = (HashedFeatureMap) other;
      return seed == otherMap.seed && sig == otherMap.sig && size == otherMap.size;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 983 + 919 * seed + 439 * Long.hashCode(sig) + 1229 * size;
  }

  @Override
  public String toString() {
    return format("#<function_sig=%d, seed=%d, size=%d>", sig, seed, size);
  }

  public void writeToStream(final DataOutputStream out) throws IOException {
    out.writeShort(FEATURE_TYPE_HASHED);
    out.writeInt(size);
    out.writeInt(seed);
  }

  public JsonValue toJson() {
    final JsonObject jobj = Json.object();
    jobj.add("type", "hashed");
    jobj.add("seed", seed);
    jobj.add("function_sig", sig);
    jobj.add("maxfeats", size);
    return jobj;
  }

  public static JsonSupport.Parser<HashedFeatureMap> jsonParser(final long functionSig) {
    return new JsonSupport.Parser<HashedFeatureMap>() {
      public HashedFeatureMap fromJson(JsonValue json) {
        if (json == null)
          throw new IllegalArgumentException("JSON for features map cannot be null");

        if (!json.isObject())
          throw new IllegalArgumentException("JSON for feature map must be an object");

        final JsonObject jobj = json.asObject();

        final int size;
        {
          final JsonValue sizeJ = jobj.get("maxfeats");
          if (sizeJ == null)
            throw new IllegalArgumentException("hashed feature map must contain 'maxfeats' key");

          if (!sizeJ.isNumber())
            throw new IllegalArgumentException("maxfeats value must be numeric");

          size = sizeJ.asInt();
        }

        final JsonValue seedJ = jobj.get("seed");
        if (seedJ != null) {
          if (!seedJ.isNumber())
            throw new IllegalArgumentException("seed value must be numeric");

          final int seed = seedJ.asInt();

          return new HashedFeatureMap(functionSig, size, seed);
        } else {
          return new HashedFeatureMap(functionSig, size);
        }
      }
    };
  }
}
