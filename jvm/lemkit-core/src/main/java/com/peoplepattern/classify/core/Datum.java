package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Serializable;

import static java.lang.String.format;

/**
 * Input for a {@link LinearClassifier}
 *
 * <p>Essentially carries a vector ({@link Vec}) to be classified; also
 * carries the signature of the feature function used to generate this
 */
public final class Datum implements Serializable, JsonSupport {

  static final long serialVersionUID = 1L;

  private final long sig;
  private final Vec vec;

  /**
   * Construct a datum from a vector
   *
   * @param functionSig the signature of the feature function used to
   *                    generate this datum
   * @param vec the vector content of this datum
   */
  public Datum(final long functionSig, final Vec vec) {
    if (vec == null)
      throw new IllegalArgumentException("cannot have null datum vector");

    this.sig = functionSig;
    this.vec = vec;
  }

  /**
   * Retrieve the content
   *
   * @return the vector content of this datum
   */
  public Vec vector() {
    return vec;
  }

  /**
   * Retrieve the function signature
   *
   * @return the signature of the feature function used to generate this
   */
  public long functionSig() {
    return sig;
  }

  @Override
  public String toString() {
    return format("<function_sig=%d, vector=%s>", sig, vec);
  }

  @Override
  public int hashCode() {
    return 29 * Double.hashCode(sig) + 31 * vec.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;

    if (o == this)
      return true;

    if (o instanceof Datum) {
      final Datum d = (Datum) o;
      return sig == d.sig && vec.equals(d.vec);
    } else
      return false;
  }

  public JsonValue toJson() {
    final JsonObject j = Json.object();
    j.add("function_sig", sig);
    j.add("vector", vec.toJson());
    return j;
  }

  /**
   * JSON parser factory for Datum
   */
  public static final JsonSupport.Parser<Datum> JPARSER = new JsonSupport.Parser<Datum>() {
    public Datum fromJson(JsonValue json) {
      if (json == null)
        throw new IllegalArgumentException("datum JSON must not be null");

      if (!json.isObject())
        throw new IllegalArgumentException("datum JSON must be an object");

      final JsonObject jobj = json.asObject();

      final long functionSig;
      {
        final JsonValue jval = jobj.get("function_sig");
        if (jval == null)
          throw new IllegalArgumentException("function_sig value must be present");

        if (!jval.isNumber())
          throw new IllegalArgumentException("function_sig value must be number");

        functionSig = jval.asLong();
      }

      final Vec vec = Vec.JPARSER.fromJson(jobj.get("vector"));

      return new Datum(functionSig, vec);
    }
  };
}
