package com.peoplepattern.classify.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.peoplepattern.classify.core.JsonSupport.*;
import static com.peoplepattern.classify.core.Scored.maxByScore;
import static com.peoplepattern.classify.core.Util.logistic;
import static com.peoplepattern.classify.core.Util.sorted;
import static java.lang.String.format;
import static java.util.Arrays.binarySearch;

/**
 * The output of a classifier {@link Predictor}
 *
 * A scored prediction for a classification problem, carries the possible
 * outputs (labels) with the scores provided by the {@link Classifier} to
 * those outputs. This class manages finding the highest scoring label.
 */
public final class Classification implements Serializable, JsonSupport {

  final static long serialVersionUID = 1L;

  private final String best;
  private final String[] labels;
  private final double[] scores;
  private transient double[] _probabilities = null;

  public Classification(final SortedSet<Scored<String>> scores) {
    final int n = scores.size();
    labels = new String[n];
    this.scores = new double[n];
    int i = 0;
    for (Scored<String> s : scores) {
      labels[i] = s.item();
      this.scores[i] = s.score();
      i++;
    }
    this.best = maxByScore(scores).item();
  }

  /**
   * Build a classification from a set of labels and associated scores
   *
   * @param labels the classification labels (classes)
   * @param scores the associated predicted scores of the labels
   * @throws IllegalArgumentException if labels or scores are null,
   *   or if the length of the labels array != the length of the the
   *   scores array, of if the length of either is 0
   */
  public Classification(final String[] labels, final double[] scores) {
    if (labels == null)
      throw new IllegalArgumentException("labels cannot be null");

    if (scores == null)
      throw new IllegalArgumentException("scores cannot be null");

    final int n = labels.length;

    if (n != scores.length) {
      final String msg = "must have same number of labels as scores";
      throw new IllegalArgumentException(msg);
    }

    if (n == 0) {
      final String msg = "trivial classification with no scores";
      throw new IllegalArgumentException(msg);
    }

    if (sorted(labels)) {
      this.labels = labels;
      this.scores = scores;
      this.best = argmax(labels, scores);
    } else {

      this.labels = new String[n];
      this.scores = new double[n];

      {
        final SortedSet<Scored<String>> set = new TreeSet<Scored<String>>();

        for (int i = 0; i < n; i++)
          set.add(new Scored<String>(labels[i], scores[i]));

        int i = 0;
        for (Scored<String> s : set) {
          this.labels[i] = s.item();
          this.scores[i] = s.score();
          i++;
        }

        this.best = argmax(this.labels, this.scores);
      }
    }
  }

  /**
   * The best scoring label or class in a classification
   *
   * @return the string label with the highest score in this ciassification
   */
  public String best() {
    return best;
  }

  /**
   * The scored labels (classes) in thie classification.
   *
   * <p>Output is guaranteed to be sorted alphabetically and corresponds
   * one-for-one with the scores in {@link #scores}
   *
   * @return the set of labels in this classification, sorted
   */
  public String[] labels() {
    return labels;
  }

  /**
   * Logistic regression class prediction probabilities
   *
   * @return the logistic regression probabilities of class labels; these
   *     values will sum to 1.0; the individual probabilities correspond
   *     to the class labels in {@link #labels}
   */
  public double[] probabilities() {
    final int n = scores.length;
    final double[] probs = Arrays.copyOf(scores, n);

    for (int j = 0; j < n; j++)
      probs[j] = logistic(probs[j]);

    final double sum;
    {
      double s = 0;
      for (int j = 0; j < n; j++)
        s += probs[j];
      sum = s;
    }

    for (int j = 0; j < n; j++)
      probs[j] /= sum;

    return probs;
  }

  public double probabilityOf(final String label) {
    if (_probabilities == null)
      _probabilities = probabilities();

    final int i = binarySearch(labels, label);
    if (i < 0)
      throw new IllegalArgumentException(format("Unknown label: %s", label));

    else
      return _probabilities[i];
  }

  public double probabilityOfBest() {
    return probabilityOf(best);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (other.getClass() != Classification.class)
      return false;

    final Classification c = (Classification) other;

    return Arrays.equals(labels, c.labels) && Arrays.equals(scores, c.scores);
  }

  @Override
  public int hashCode() {
    return 23 * Arrays.hashCode(labels) + 31 * Arrays.hashCode(scores);
  }

  @Override
  public String toString() {
    return format("(pred=%s)", best);
  }

  public JsonValue toJson() {
    final JsonObject j = Json.object();
    j.add("best", best);

    final JsonObject scoresJ = Json.object();
    for (int i = 0; i < labels.length; i++) {
      scoresJ.add(labels[i], scores[i]);
    }

    j.add("scores", scoresJ);
    return j;
  }

  /** JSON parser to create {@link Classification}s from JSON input */
  public static final JsonSupport.Parser<Classification> JPARSER =
      new JsonSupport.Parser<Classification>() {
        public Classification fromJson(JsonValue json) {
          if (json == null)
            throw new IllegalArgumentException("Classification JSON must not be null");

          if (!json.isObject())
            throw new IllegalArgumentException("JSON not formatted as classification");

          final JsonValue scoresJ = json.asObject().get("scores");
          if (scoresJ == null)
            throw new IllegalArgumentException("JSON object did not have key \"scores\"");

          if (!scoresJ.isObject())
            throw new IllegalArgumentException("\"scores\" value not a JSON object");

          final JsonObject scoresO = scoresJ.asObject();

          int i = 0;
          final int n = scoresO.size();

          final String[] labels = new String[n];
          final double[] scores = new double[n];

          for (Member m : scoresO) {
            final JsonValue v = m.getValue();
            if (!v.isNumber())
              throw new IllegalArgumentException("Scores must be numbers");

            labels[i] = m.getName();
            scores[i] = v.asDouble();
            i++;
          }

          return new Classification(labels, scores);
        }
      };

  private static String argmax(final String[] labels, final double[] scores) {
    int index = -1;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < labels.length; i++) {
      if (scores[i] > max) {
        max = scores[i];
        index = i;
      }
    }
    return labels[index];
  }
}
