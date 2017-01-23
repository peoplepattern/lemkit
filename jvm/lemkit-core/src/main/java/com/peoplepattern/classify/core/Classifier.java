package com.peoplepattern.classify.core;

/**
 * {@link Predictor} that outputs a {@link Classification}
 *
 * <p>A classifier also carried a long-valued "function signature"
 * enabling it to be associated with a specific feature function,
 * even if the feature function isn't linked directly with this
 * classifier.
 *
 * <p>Also, access to the possible classification lables (classes)
 * is provided
 */
public interface Classifier<I> extends Predictor<I, Classification> {

  /**
   * The set of labels being used for classification
   *
   * @return an array of classifier labels, should alphabetically sorted
   */
  public String[] getLabels();

  /**
   * The feature function signature associated with this classifier
   *
   * @return a unique code
   */
  public long functionSig();
}
