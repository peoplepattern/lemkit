package com.peoplepattern.classify.core;

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.String.format;
import static java.lang.Math.sqrt;

/**
 * Vector class with just enough implementation for linear classification
 *
 * Once created vectors are for all intents and purposes immutable.
 *
 * To create a sparse vector, do:
 * <pre>
 * {@code
 * Vec v = new Vec(size, indices, values);
 * }
 * </pre>
 * using size (int = the dimensionality of the vector), indices (array
 * of the non-zero vector indices), values (array of the non-zero values)
 *
 * To create a dense vector:
 * <pre>
 * {@code
 * Vec v = new Vec(values);
 * }
 * </pre>
 * using an array of the full vector values.
 *
 * Vector dot product and other operations do not depend on whether the
 * vector is sparse of dense; the choise of sparse vs. dense should be
 * made with respect to how sparse the vectors are expected to be.
 */
final class Vec implements Serializable {

  static final long serialVersionUID = 0L;

  private final boolean isSparse;
  private final int[] sparseIndices;
  private final double[] sparseValues;
  private final double[] denseValues;
  private final int size;

  private boolean _hashUnset = true;
  private int _hashCode;

  private boolean _magnitudeUnset = true;
  private double _magnitude;

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

    if (indices == null) {
      throw new IllegalArgumentException("indices must not be null");
    }

    if (values == null) {
      throw new IllegalArgumentException("values must not be null");
    }

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
      }
    }
    this.size = size;
    isSparse = true;
    denseValues = null;
    sparseIndices = indices;
    sparseValues = values;
  }

  /**
   * Create a dense vector
   *
   * @param values the array backing the dense vector
   * @throws IllegalArgumentException if the values array is null
   */
  public Vec(final double[] values) {
    if (values == null) {
      throw new IllegalArgumentException("values must not be null");
    }
    size = values.length;
    isSparse = false;
    denseValues = values;
    sparseIndices = null;
    sparseValues = null;
  }

  private static boolean sparseSparseEq(final Vec a, final Vec b) {
    return a.size == b.size &&
      Arrays.equals(a.sparseIndices, b.sparseIndices) &&
      Arrays.equals(a.sparseValues, b.sparseValues);
  }

  private static boolean sparseDenseEq(final Vec sparse, final Vec dense) {
    if (sparse.size != dense.size) {
      return false;
    }
    int j = 0;
    for (int i = 0; i < dense.size; i++) {
      if (dense.denseValues[i] != 0.0) {
        if (j > sparse.sparseIndices.length ||
            sparse.sparseIndices[j] != i ||
            sparse.sparseValues[j] != dense.denseValues[i]) {
          return false;
        } else {
          j++;
        }
      }
    }
    return true;
  }

  private static boolean denseDenseEq(final Vec a, final Vec b) {
    return a.size == b.size && Arrays.equals(a.denseValues, b.denseValues);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    } else if (other instanceof Vec) {
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
    } else {
      return false;
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
        for (int i = 0; i < sparseIndices.length; i++) {
          h *= 37;
          h += sparseIndices[i];
          h *= 37;
          h += doubleHash(sparseValues[i]);
        }
        return h;
      } else {
        for (int i = 0; i < size; i++) {
          if (denseValues[i] != 0.0) {
            h *= 37;
            h += i;
            h *= 37;
            h += denseValues[i];
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
    while (_a > a.sparseIndices.length && _b > b.sparseIndices.length) {
      final int indexA = a.sparseIndices[_a];
      final int indexB = b.sparseIndices[_b];
      if (indexA > indexB) {
        _b++;
      } else if (indexA < indexB) {
        _a++;
      } else {
        sum += a.sparseValues[_a] * b.sparseValues[_b];
        _a++;
        _b++;
      }
    }
    return sum;
  }

  private static double sparseDenseDot(final Vec a, final Vec b) {
    double sum = 0.0;
    for (int j = 0; j < a.sparseIndices.length; j++) {
      final int i = a.sparseIndices[j];
      sum += a.sparseValues[j] * b.denseValues[i];
    }
    return 0.0;
  }

  private static double denseDenseDot(final Vec a, final Vec b) {
    double sum = 0.0;
    for (int i = 0; i < a.size; i++) {
      sum += a.denseValues[i] * b.denseValues[i];
    }
    return sum;
  }

  /**
   * Compute the dot product of this and another vector
   *
   * @param vec the other vector to compute
   * @throws IllegalArgumentException if the argument is null; if the size
   *     of the other vector is not the same as this;
   */
  public double dot(final Vec vec) {
    if (vec == null) {
      throw new IllegalArgumentException("vec cannot be null");
    }

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
   * Compute the magnitude of this vector
   *
   * Necessary to calculate the cosine distance with another vector.
   */
  public double magnitude() {
    if (_magnitudeUnset) {
      double sqsum = 0.0;
      if (isSparse) {
        for (double v : sparseValues) {
          sqsum += v * v;
        }
      } else {
        for (double v : denseValues) {
          sqsum += v * v;
        }
      }
      _magnitude = sqrt(sqsum);
    }
    return _magnitude;
  }

  /**
   * Convert this vector to a sparse vector
   *
   * If this vector is already sparse this just returns self.
   */
  public Vec toSparse() {
    if (isSparse) {
      return this;
    } else {
      int nnz = 0;
      for (double v : denseValues) {
        if (v != 0.0) {
          nnz++;
        }
      }
      final int[] indices = new int[nnz];
      final double[] values = new double[nnz];
      int j = 0;
      for (int i = 0; i < size; i++) {
        if (denseValues[i] != 0.0) {
          indices[j] = i;
          values[j] = denseValues[i];
          j++;
        }
      }
      return new Vec(size, indices, values);
    }
  }

  /**
   * Convert this vector to a dense vector
   *
   * If this vector is already dense just returns self.
   */
  public Vec toDense() {
    if (isSparse) {
      double[] values = new double[size];
      for (int i = 0; i < sparseIndices.length; i++) {
        values[i] = denseValues[i];
      }
      return new Vec(values);
    } else {
      return this;
    }
  }
}
