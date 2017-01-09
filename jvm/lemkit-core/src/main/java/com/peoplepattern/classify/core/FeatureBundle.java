package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;

import static com.peoplepattern.classify.core.Scored.uniqueItems;
import static java.lang.String.format;

/**
 * A set of weighted features for classification
 *
 * <p>A feature bundle is basically implemented as a map from string feature
 * codes to double-valued weights. For boolean features, the weights are
 * usually simply 0.0 and 1.0.
 */
public final class FeatureBundle implements Serializable, JsonSupport {

  static final long serialVersionUID = 1L;

  private final long sig;
  private final SortedSet<Scored<String>> observations;

  private static final SortedSet<Scored<String>> mkset(Collection<Scored<String>> obs) {
    if (obs == null)
      throw new IllegalArgumentException("Null observations");

    return new TreeSet<Scored<String>>(obs);
  }

  public FeatureBundle(final long functionSig, final SortedSet<Scored<String>> observations) {
    if (observations == null)
      throw new IllegalArgumentException("Null observations");

    if (!uniqueItems(observations))
      throw new IllegalArgumentException("Features must be unique");

    for (Scored<String> s : observations)
      if (s == null)
        throw new IllegalArgumentException("Null feature observation");

    this.sig = functionSig;
    this.observations = observations;
  }

  public FeatureBundle(final long sig, final Collection<Scored<String>> obs) {
    this(sig, mkset(obs));
  }

  public Datum toDatum(final FeatureMap map, final boolean sparse) {

    if (map == null) {
      throw new IllegalArgumentException("Null feature map");
    }

    if (sig != map.functionSig()) {
      final String tmpl = "map has inconsistend sig: %d expected: %d";
      final String msg = format(tmpl, map.functionSig(), sig);
      throw new IllegalArgumentException(msg);
    }

    if (sparse) {

      final Int2DoubleRBTreeMap sort = new Int2DoubleRBTreeMap();
      for (Scored<String> o : observations)
        sort.addTo(map.indexOfFeature(o.item()), o.score());

      final int n = sort.size();
      final int[] indices = new int[n];
      final double[] values = new double[n];

      int j = 0;
      for (Entry e : sort.int2DoubleEntrySet()) {
        indices[j] = e.getIntKey();
        values[j] = e.getDoubleValue();
        j++;
      }

      return new Datum(sig, new Vec(map.size(), indices, values));

    } else {

      final double[] params = new double[map.size()];
      for (Scored<String> o : observations)
        params[map.indexOfFeature(o.item())] += o.score();

      return new Datum(sig, new Vec(params));
    }
  }

  public Datum toDatum(final FeatureMap map) {
    return toDatum(map, true);
  }

  public JsonValue toJson() {
    final JsonObject bundle = Json.object();
    bundle.add("function_sig", sig);
    bundle.add("observations", Scored.asJsonObject(observations));
    return bundle;
  }

  public static JsonSupport.Parser<FeatureBundle> JPARSER =
      new JsonSupport.Parser<FeatureBundle>() {
        public FeatureBundle fromJson(JsonValue json) {
          if (json == null)
            throw new IllegalArgumentException("Feature bundle JSON cannot be null");

          if (!json.isObject())
            throw new IllegalArgumentException("Feature bundle JSON must be an object");

          final JsonObject jobj = json.asObject();

          final long functionSig;
          {
            final JsonValue sigJ = jobj.get("function_sig");
            if (sigJ == null)
              throw new IllegalArgumentException("Feature bundle must have non-null function_sig");

            if (!sigJ.isNumber())
              throw new IllegalArgumentException("function_sig must be a number");

            functionSig = sigJ.asLong();
          }

          final Collection<Scored<String>> obs = Scored.fromJsonObject(jobj.get("observations"));

          return new FeatureBundle(functionSig, obs);
        }
      };

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;

    if (this == o)
      return true;

    if (o instanceof FeatureBundle) {
      final FeatureBundle fb = (FeatureBundle) o;
      return this.sig == fb.sig && this.observations.equals(fb.observations);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int code = 709 * Long.hashCode(sig);
    for (Scored<String> s : observations)
      if (s.score() != 0.0) {
        code *= 827;
        code += s.item().hashCode();
        code *= 181;
        code += Double.hashCode(s.score());
      }

    return code;
  }

  @Override
  public String toString() {
    return format("function_sig=%d, features=%s", sig, observations);
  }
}
