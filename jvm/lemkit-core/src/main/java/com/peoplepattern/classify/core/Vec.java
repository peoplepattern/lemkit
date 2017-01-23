package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;

import static java.lang.String.format;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import static com.peoplepattern.classify.core.ClassifierIO.*;
import static com.peoplepattern.classify.core.JsonSupport.*;

/**
 * Vector class with just enough implementation for linear classification
 *
 * <p>Once created vectors are for all intents and purposes immutable.
 *
 * <p>To create a sparse vector, do:
 * <pre>{@code Vec v = new Vec(size, indices, values);}</pre>
 * using size (int = the dimensionality of the vector), indices (array
 * of the non-zero vector indices), values (array of the non-zero values)
 *
 * <p>To create a dense vector:
 * <pre>{@code Vec v = new Vec(values); }</pre>
 * using an array of the full vector values.
 *
 * <p>Vector dot product and other operations do not depend on whether the
 * vector is sparse of dense; the choise of sparse vs. dense should be
 * made with respect to how sparse the vectors are expected to be.
 *
 * <p>Vectors are read-only and immutable, so should be thread-safe.
 * Unless you mess with the arrays passed to the vector constructurs, which
 * are not cloned or copied internally. <b>Don't mess with the arrays
 * passed into the constructor after a vector has been constructed.</b>
 */
public final class Vec implements Serializable, JsonSupport {

  static final long serialVersionUID = 0L;

  private final boolean isSparse;
  private final int[] indices;
  private final double[] values;
  private final int size;

  private boolean _hashUnset = true;
  private int _hashCode;

  /**
   * Create a sparse vector
   *
   * @param size the size or dimenstionality of the vector
   * @param indices the non-zero indices of the vector
   * @param values the values of the non-zero indices of the vector
   * @throws IllegalArgumentException if the size of the indices array is
   *    not the same as the size of the values array; if any of the indices
   *    is greater than or equal to the specified size; if any of the indices
   *    is less than zero; if either the indices array or the values array
   *    is null.
   */
  public Vec(final int size, final int[] indices, final double[] values) {

    if (indices == null)
      throw new IllegalArgumentException("indices must not be null");

    if (values == null)
      throw new IllegalArgumentException("values must not be null");

    if (indices.length != values.length) {
      final String msg = "Sparse vector indices must line up with values";
      throw new IllegalArgumentException(msg);
    }

    for (int index : indices) {
      if (index < 0) {
        final String templ = "Indices must be non-negative: %d";
        final String msg = format(templ, index);
        throw new IllegalArgumentException(msg);
      }

      if (index >= size) {
        final String templ = "Index %d is greater than specified size %d";
        final String msg = format(templ, index, size);
        throw new IllegalArgumentException(msg);
      }
    }

    for (int i = 0; i < indices.length - 1; i++) {
      final int j = i + 1;
      if (indices[i] >= indices[j]) {
        final String templ = "Indices must be strictly ordered: %d %d";
        final String msg = String.format(templ, indices[i], indices[j]);
        throw new IllegalArgumentException(msg);
      }
    }

    this.size = size;
    isSparse = true;
    this.indices = indices;
    this.values = values;
  }

  /**
   * Create a dense vector
   *
   * @param values the array backing the dense vector
   * @throws IllegalArgumentException if the values array is null
   */
  public Vec(final double[] values) {
    if (values == null)
      throw new IllegalArgumentException("values must not be null");

    size = values.length;
    isSparse = false;
    this.values = values;
    indices = null;
  }

  private static boolean sparseSparseEq(final Vec a, final Vec b) {
    return a.size == b.size && Arrays.equals(a.indices, b.indices)
        && Arrays.equals(a.values, b.values);
  }

  private static boolean sparseDenseEq(final Vec sparse, final Vec dense) {
    if (sparse.size != dense.size)
      return false;

    int j = 0;

    for (int i = 0; i < dense.size; i++) {
      if (dense.values[i] != 0.0) {
        if (j >= sparse.indices.length)
          return false;
        else if (sparse.indices[j] != i)
          return false;
        else if (sparse.values[j] != dense.values[i])
          return false;
        else
          j++;
      }
    }

    return j == sparse.indices.length;
  }

  private static boolean denseDenseEq(final Vec a, final Vec b) {
    return a.size == b.size && Arrays.equals(a.values, b.values);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (!(other instanceof Vec))
      return false;

    final Vec vec = (Vec) other;
    if (this.isSparse && vec.isSparse) {
      return sparseSparseEq(this, vec);
    } else if (this.isSparse && !vec.isSparse) {
      return sparseDenseEq(this, vec);
    } else if (!this.isSparse && vec.isSparse) {
      return sparseDenseEq(vec, this);
    } else {
      return denseDenseEq(this, vec);
    }
  }

  private static int doubleHash(final double d) {
    return Double.valueOf(d).hashCode();
  }

  @Override
  public int hashCode() {
    if (_hashUnset) {
      int h = 0;
      if (isSparse) {
        for (int i = 0; i < indices.length; i++) {
          h *= 37;
          h += indices[i];
          h *= 37;
          h += doubleHash(values[i]);
        }
        return h;
      } else {
        for (int i = 0; i < size; i++) {
          if (values[i] != 0.0) {
            h *= 37;
            h += i;
            h *= 37;
            h += doubleHash(values[i]);
          }
        }
      }
      _hashCode = h;
      _hashUnset = false;
    }

    return _hashCode;
  }

  private static double sparseSparseDot(final Vec a, final Vec b) {
    double sum = 0.0;
    int _a = 0;
    int _b = 0;

    while (_a < a.indices.length && _b < b.indices.length) {
      final int indexA = a.indices[_a];
      final int indexB = b.indices[_b];
      if (indexA > indexB) {
        _b++;
      } else if (indexA < indexB) {
        _a++;
      } else {
        sum += a.values[_a] * b.values[_b];
        _a++;
        _b++;
      }
    }

    return sum;
  }

  private static double sparseDenseDot(final Vec sparse, final Vec dense) {
    double sum = 0.0;

    for (int j = 0; j < sparse.indices.length; j++)
      sum += sparse.values[j] * dense.values[sparse.indices[j]];

    return sum;
  }

  private static double denseDenseDot(final Vec a, final Vec b) {
    double sum = 0.0;

    for (int i = 0; i < a.size; i++) {
      sum += a.values[i] * b.values[i];
    }

    return sum;
  }

  /**
   * Compute the dot product of this and another vector
   *
   * @param vec the other vector to compute
   * @return the dot product &Sigma;<sub><small><i>i</i></small></sub><i>v</i><sub><small><i>i</i></small></sub><i>u</i><sub><small><i>i</i></small></sub>
   *         of this and the other vector
   * @throws IllegalArgumentException if the argument is null; if the size
   *     of the other vector is not the same as this, i.e. they are of
   *     different dimensions
   */
  public double dot(final Vec vec) {
    if (vec == null)
      throw new IllegalArgumentException("vec cannot be null");

    if (size != vec.size) {
      String templ = "Vectors from different dimensionalities: %d != %d";
      String msg = format(templ, size, vec.size);
      throw new IllegalArgumentException(msg);
    }

    if (isSparse && vec.isSparse) {
      return sparseSparseDot(this, vec);
    } else if (isSparse && !vec.isSparse) {
      return sparseDenseDot(this, vec);
    } else if (!isSparse && vec.isSparse) {
      return sparseDenseDot(vec, this);
    } else {
      return denseDenseDot(this, vec);
    }
  }

  /**
   * Convert this vector to a sparse vector
   *
   * <p>If this vector is already sparse this just returns self.
   *
   * @return a sparse vector equal to this vector
   */
  public Vec toSparse() {
    if (isSparse)
      return this;

    int nnz = 0;
    for (double v : values)
      if (v != 0.0)
        nnz++;

    final int[] indices = new int[nnz];
    final double[] values = new double[nnz];

    int j = 0;
    for (int i = 0; i < size; i++) {
      if (values[i] != 0.0) {
        indices[j] = i;
        values[j] = values[i];
        j++;
      }
    }

    return new Vec(size, indices, values);
  }

  /**
   * Convert this vector to a dense vector
   *
   * <p>If this vector is already dense just returns self.
   *
   * @return a dense vector equal to this vector
   */
  public Vec toDense() {
    if (!isSparse)
      return this;

    double[] values = new double[size];
    for (int i = 0; i < indices.length; i++)
      values[indices[i]] = values[i];

    return new Vec(values);
  }

  @Override
  public String toString() {
    final String fmt = "%.3f";
    final StringBuffer sb = new StringBuffer();
    sb.append("Vec(");
    final int displayLen = min(size, 10);
    if (isSparse) {
      int j = 0;
      for (int i = 0; i < displayLen; i++) {
        if (j < indices.length && i == indices[j]) {
          sb.append(format(fmt, values[j]));
          j++;
        } else {
          sb.append(format(fmt, 0.0));
        }

        if (i < displayLen - 1) {
          sb.append(',');
        }
      }
    } else {
      for (int i = 0; i < displayLen; i++) {
        sb.append(format(fmt, values[i]));

        if (i < displayLen - 1) {
          sb.append(",");
        }
      }
    }

    if (displayLen < size) {
      sb.append(",...");
    }

    sb.append(")");

    return sb.toString();
  }

  public void writeToStream(final DataOutputStream out) throws IOException {
    if (isSparse) {
      out.writeShort(WEIGHTS_TYPE_SPARSE);
      out.writeInt(size);
      out.writeInt(indices.length);
      for (int ind : indices)
        out.writeInt(ind);
      for (double val : values)
        out.writeDouble(val);
    } else {
      out.writeShort(WEIGHTS_TYPE_DENSE);
      out.writeInt(size);
      for (double val : values)
        out.writeDouble(val);
    }
  }

  public static Vec readVec(final DataInputStream in) throws IOException {
    final short code = in.readShort();
    switch (code) {
      case WEIGHTS_TYPE_SPARSE: {
        final int size = in.readInt();
        final int num = in.readInt();

        final int[] indices = new int[num];
        for (int i = 0; i < num; i++)
          indices[i] = in.readInt();

        final double[] values = new double[num];
        for (int i = 0; i < num; i++)
          values[i] = in.readDouble();

        return new Vec(size, indices, values);
      }

      case WEIGHTS_TYPE_DENSE: {
        final int size = in.readInt();

        final double[] values = new double[size];
        for (int i = 0; i < size; i++)
          values[i] = in.readDouble();

        return new Vec(values);
      }

      default:
        throw new IOException(format("Unexpected code: %d", code));
    }
  }

  public JsonValue toJson() {

    final JsonArray valuesJ = Json.array().asArray();
    for (double v : values)
      valuesJ.add(Json.value(v));

    if (isSparse) {
      final JsonObject json = Json.object();
      json.add("size", size);
      json.add("indices", Json.array(indices));
      json.add("values", valuesJ);
      return json;
    } else {
      return valuesJ;
    }
  }

  public static JsonSupport.Parser<Vec> JPARSER = new JsonSupport.Parser<Vec>() {
    public Vec fromJson(final JsonValue json) {

      if (json == null)
        throw new IllegalArgumentException("Vec JSON must not be null");

      if (json.isArray()) {

        final JsonArray arr = json.asArray();
        final int n = arr.size();
        final double[] values = new double[n];
        for (int i = 0; i < n; i++) {
          final JsonValue v = arr.get(i);
          if (!v.isNumber()) {
            throw new IllegalArgumentException("JSON array must be numeric");
          }
          values[i] = v.asDouble();
        }
        return new Vec(values);

      } else if (json.isObject()) {

        final JsonObject obj = json.asObject();

        final JsonValue sizeJ = obj.get("size");
        if (sizeJ == null)
          throw new IllegalArgumentException("JSON object must have key \"size\"");

        if (!sizeJ.isNumber())
          throw new IllegalArgumentException("\"size\" value must be numeric");

        final int size = sizeJ.asInt();

        final JsonValue indicesJ = obj.get("indices");
        if (indicesJ == null)
          throw new IllegalArgumentException("JSON object must have key \"indices\"");

        if (!indicesJ.isArray())
          throw new IllegalArgumentException("\"indices\" value must be JSON array");

        final JsonArray indicesA = indicesJ.asArray();

        final int n = indicesA.size();

        final int[] indices = new int[n];

        for (int i = 0; i < n; i++) {
          final JsonValue v = indicesA.get(i);
          if (!v.isNumber())
            throw new IllegalArgumentException("\"indices\" array must be numeric");

          indices[i] = v.asInt();
        }

        final JsonValue valuesJ = obj.get("values");

        if (valuesJ == null)
          throw new IllegalArgumentException("JSON object must have key \"values\"");

        if (!valuesJ.isArray())
          throw new IllegalArgumentException("\"values\" value must be JSON array");

        final JsonArray valuesA = valuesJ.asArray();

        if (n != valuesA.size())
          throw new IllegalArgumentException("Must have same number of \"indices\" as \"values\"");

        final double[] values = new double[n];

        for (int i = 0; i < n; i++) {
          final JsonValue v = valuesA.get(i);
          if (!v.isNumber())
            throw new IllegalArgumentException("\"values\" array must be numeric");

          values[i] = v.asDouble();
        }

        return new Vec(size, indices, values);

      } else {
        throw new IllegalArgumentException("JSON must be an object or array");
      }
    }
  };
}
