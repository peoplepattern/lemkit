package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import static com.peoplepattern.classify.core.BinarySupport.BinaryReader;
import static com.peoplepattern.classify.core.ClassifierIO.*;

/**
 * An exact feature map -- each string feature is mapped to a unique index
 *
 * <p>The indices for the feature map are ordered from 0 to {@link #size} - 1
 */
public final class ExactFeatureMap implements FeatureMap, Serializable, JsonSupport, BinarySupport {
  public static final long serialVersionUID = 1L;

  private final long sig;
  private final Object2IntMap<String> hash;
  private final String[] features;
  private final boolean addIntercept;
  private final int size;

  public ExactFeatureMap(final long functionSig, final String[] features, final boolean addIntercept) {
    if (features == null)
      throw new IllegalArgumentException("Features values cannot be null");

    for (int i = 0; i < features.length; i++)
      if (features[i] == null)
        throw new IllegalArgumentException("Features cannot be null");

    sig = functionSig;
    this.features = features;
    hash = new Object2IntOpenHashMap<String>(features.length);
    for (int i = 0; i < features.length; i++) {
      hash.put(features[i], i);
    }

    if (addIntercept && !hash.containsKey("")) {
      hash.put("", features.length);
      size = features.length + 1;
    } else {
      size = features.length;
    }

    this.addIntercept = addIntercept;
  }

  public ExactFeatureMap(final long functionSig, final String[] features) {
    this(functionSig, features, true);
  }

  public long functionSig() {
    return sig;
  }

  public boolean addIntercept() {
    return addIntercept;
  }

  /**
   * Retrieve the index of a feature
   *
   * <p>The maximum value of this (+1) is given by {@link #size}
   *
   * @param feature the string representation of a feature
   * @return the integer index of the feature in a vector space; returns
   *         -1 if the feature isn't known to be in the feature set
   */
  public int indexOfFeature(final String feature) {
    if (hash.containsKey(feature)) {
      return hash.getInt(feature);
    } else {
      return -1;
    }
  }

  public int size() {
    return size;
  }

  public void writeToStream(final DataOutputStream out) throws IOException {
    out.writeShort(FEATURE_TYPE_EXACT);
    out.writeInt(size());
    for (String feature : features)
      writeString(out, feature);
  }

  public JsonValue toJson() {
    final JsonObject json = Json.object();
    json.add("type", "exact");
    json.add("function_sig", sig);
    json.add("features", Json.array(features));
    return json;
  }

  @Override
  public int hashCode() {
    return 263 * Arrays.hashCode(features) + 457 * Long.hashCode(sig);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;

    if (o == this)
      return true;

    if (o instanceof ExactFeatureMap) {
      final ExactFeatureMap e = (ExactFeatureMap) o;
      return sig == e.sig && Arrays.equals(features, e.features);
    } else
      return false;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("@<function_sig=");
    sb.append(sig);
    sb.append(", ");
    if (features.length > 5) {
      for (int i = 0; i < 5; i++) {
        sb.append(features[i]);
        sb.append(" -> ");
        sb.append(i);
        sb.append(", ");
      }
      sb.append("...>");
    } else {
      for (int i = 0; i < features.length; i++) {
        sb.append(features[i]);
        sb.append(" -> ");
        sb.append(i);
        if (i != features.length - 1) {
          sb.append(", ");
        }
      }
      sb.append(">");
    }
    return sb.toString();
  }

  public static final JsonSupport.Parser<ExactFeatureMap> jsonParser(final long functionSig) {
    return new JsonSupport.Parser<ExactFeatureMap>() {
      public ExactFeatureMap fromJson(final JsonValue json) {
        if (json == null)
          throw new IllegalArgumentException("JSON for features map cannot be null");

        if (!json.isObject())
          throw new IllegalArgumentException("JSON for feature map must be an object");

        final JsonObject jobj = json.asObject();

        final JsonValue featsJ = jobj.get("features");
        if (featsJ == null)
          throw new IllegalArgumentException("exact feature map must contain 'features' key");

        if (!featsJ.isArray())
          throw new IllegalArgumentException("features must be a JSON array");

        final JsonArray featsA = featsJ.asArray();
        final int n = featsA.size();
        final String[] features = new String[n];
        for (int i = 0; i < n; i++) {
          final JsonValue featJ = featsA.get(i);
          if (featJ == null)
            throw new IllegalArgumentException("feature value must not be null");

          if (!featJ.isString())
            throw new IllegalArgumentException("feature value must be string");

          features[i] = featJ.asString();
        }

        return new ExactFeatureMap(functionSig, features);
      }
    };
  }
}
