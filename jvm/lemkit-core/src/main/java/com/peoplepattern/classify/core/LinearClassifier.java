package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peoplepattern.classify.core.ClassifierIO.*;
import static com.peoplepattern.classify.core.Util.sorted;
import static java.lang.Math.exp;

/**
 * Minimal and foundational implementation linear classifiers
 *
 * <p>Lightweight object wrapping a vector of model parameters.
 * This class operates on {@link Datum} directly, so no featurization
 * or object tranformation is performed. It should be reasonably fast.
 *
 * <p>Like several other objects in com.peoplepattern.classify, it
 * carried a numeric representation of the feature function associated
 * with this classifier, and checks data being classified whether
 * it has the same signature.
 */
public class LinearClassifier implements Classifier<Datum>, Serializable {

  static final long serialVersionUID = 1L;

  private final Vec[] parameters;

  private final String[] labels;

  private final long functionSig;

  private static class Pair implements Comparable<Pair> {
    final String label;
    final Vec vec;

    Pair(final String label, final Vec vec) {
      this.label = label;
      this.vec = vec;
    }

    public int compareTo(Pair other) {
      return label.compareTo(other.label);
    }
  }

  /**
   * Build a linear classifier from a set of classification labels and a
   * model parameter vector
   *
   * @param functionSig the signature of the feature function associated
   *        with this model
   * @param labels the labels associated with the classification output
   * @param parameters model parameters; this array must be the same length
   *        as the labels
   * @throws IllegalArgumentException if any of the inputs are invalid: if
   *         the labels or model parameters are null; if any of the labels
   *         or parameter vectors are null; or if there is a different number
   *         of labels and parameter vectors.
   */
  public LinearClassifier(final long functionSig, final String[] labels, final Vec[] parameters) {
    if (labels == null)
      throw new IllegalArgumentException("Null labels value");

    for (String label : labels)
      if (label == null)
        throw new IllegalArgumentException("Null label value");

    if (parameters == null)
      throw new IllegalArgumentException("Null model parameters");

    for (Vec paramVec : parameters)
      if (paramVec == null)
        throw new IllegalArgumentException("Null model dimension parameters");

    if (labels.length != parameters.length)
      throw new IllegalArgumentException("Different number of labels and parameters");

    if (labels.length == 0)
      throw new IllegalArgumentException("Trivial model with no parameters");

    this.functionSig = functionSig;

    if (sorted(labels)) {
      this.labels = labels;
      this.parameters = parameters;
    } else {
      final int n = labels.length;
      final Pair[] pairs = new Pair[n];
      for (int i = 0; i < n; i++)
        pairs[i] = new Pair(labels[i], parameters[i]);
      Arrays.sort(pairs);
      this.labels = new String[n];
      this.parameters = new Vec[n];
      for (int i = 0; i < n; i++) {
        this.labels[i] = pairs[i].label;
        this.parameters[i] = pairs[i].vec;
      }
    }
  }

  public String[] getLabels() {
    return labels;
  }

  /**
   * Generate a classification given inputs
   *
   * <p>Given a raw {@link Datum} input, compute a classification
   * for the inputs using the model parameters. Linear classification is
   * pretty simple at the end of the day: each class (label) in the
   * classifier is associated with a weight vector
   */
  public Classification predict(final Datum datum) {

    if (datum == null)
      throw new IllegalArgumentException("Null datum for prediction");

    if (datum.functionSig() != functionSig)
      throw new IllegalArgumentException("Datum produced by inconsistent feature function");

    final Vec datumVec = datum.vector();

    final int n = parameters.length;

    final double[] scores = new double[n];

    for (int i = 0; i < n; i++)
      scores[i] = parameters[i].dot(datumVec);

    return new Classification(labels, scores);
  }

  public long functionSig() {
    return functionSig;
  }

  /**
   * Write model parameters to a binary stream
   *
   * @param out the binary stream to write model parameters to
   * @throws IOException if anything goes wrong with the writing
   */
  public void writeWeightsToStream(final DataOutputStream out) throws IOException {
    out.writeInt(parameters.length);
    for (Vec vec : parameters)
      vec.writeToStream(out);
  }

  /**
   * Convert model parameters to JSON
   *
   * @return a JSON value representing the underlying model parameters
   */
  public JsonValue getWeightsJson() {
    final JsonArray paramsJ = new JsonArray();
    for (Vec vec : parameters)
      paramsJ.add(vec.toJson());
    return paramsJ;
  }

  /**
   * Parse a JSON value into model parameters
   *
   * @param json the JSON input
   * @return an array of model vectors
   * @throws IllegalArgumentException if the JSON is missing or has badly
   *         formed data
   */
  public static Vec[] readWeightsFromJson(final JsonValue json) {
    if (json == null)
      throw new IllegalArgumentException("parameter weights JSON cannot be null");

    if (!json.isArray())
      throw new IllegalArgumentException("parameter weights JSON must be array");

    final JsonArray jsonA = json.asArray();
    final int n = jsonA.size();

    final Vec[] params = new Vec[n];

    for (int i = 0; i < n; i++)
      params[i] = Vec.JPARSER.fromJson(jsonA.get(i));

    return params;
  }
}
