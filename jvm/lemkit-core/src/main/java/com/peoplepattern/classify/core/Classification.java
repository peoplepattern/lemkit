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
import static java.lang.String.format;
import static java.lang.Math.exp;
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
  private final SortedSet<Scored<String>> scoredLabels;
  private transient String[] _labels = null;
  private transient double[] _probabilities = null;

  private static SortedSet<Scored<String>> mkset(final String[] labels, final double scores[]) {

    if (labels == null)
      throw new IllegalArgumentException("labels cannot be null");

    if (scores == null)
      throw new IllegalArgumentException("scores cannot be null");

    if (labels.length != scores.length) {
      final String msg = "must have same number of labels as scores";
      throw new IllegalArgumentException(msg);
    }

    if (labels.length == 0) {
      final String msg = "trivial classification with no scores";
      throw new IllegalArgumentException(msg);
    }

    final SortedSet<Scored<String>> set = new TreeSet<Scored<String>>();

    for (int i = 0; i < labels.length; i++)
      set.add(new Scored<String>(labels[i], scores[i]));

    return set;
  }

  public Classification(final SortedSet<Scored<String>> scores) {
    this.scoredLabels = scores;
    final Scored<String> bestScored = maxByScore(scores);
    this.best = bestScored.item();
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
    this(mkset(labels, scores));
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
    final String[] labels = new String[scoredLabels.size()];
    int i = 0;
    for (Scored<String> s : scoredLabels)
      labels[i++] = s.item();
    return labels;
  }

  public static double logistic(final double x) {
    return 1.0 / (1.0 + exp(-x));
  }

  /**
   * Logistic regression class prediction probabilities
   */
  public double[] probabilities() {
    final double[] scores = new double[scoredLabels.size()];
    int i = 0;
    for (Scored<String> s : scoredLabels)
      scores[i++] = s.score();

    // scores() generates new array, so don't worry we're changing it
    final int n = scores.length;
    for (int j = 0; j < n; j++)
      scores[j] = logistic(scores[j]);

    final double sum;
    {
      double s = 0;
      for (int j = 0; j < n; j++)
        s += scores[j];
      sum = s;
    }

    for (int j = 0; j < n; j++)
      scores[j] /= sum;

    return scores;
  }

  public double probabilityOf(final String label) {
    if (_probabilities == null)
      _probabilities = probabilities();

    if (_labels == null)
      _labels = labels();

    final int i = binarySearch(_labels, label);
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

    return scoredLabels.equals(c.scoredLabels);
  }

  @Override
  public int hashCode() {
    return 23 * scoredLabels.hashCode();
  }

  @Override
  public String toString() {
    return format("(pred=%s)", best);
  }

  public JsonValue toJson() {
    final JsonObject j = Json.object();
    j.add("best", best);
    j.add("scores", Scored.asJsonObject(scoredLabels));
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
          final List<Scored<String>> scores = new ArrayList<Scored<String>>();
          for (Member m : scoresO) {
            final JsonValue v = m.getValue();
            if (!v.isNumber())
              throw new IllegalArgumentException("Scores must be numbers");

            scores.add(new Scored<String>(m.getName(), v.asDouble()));
          }
          return new Classification(new ProbablySortedSet<Scored<String>>(scores));
        }
      };
}
