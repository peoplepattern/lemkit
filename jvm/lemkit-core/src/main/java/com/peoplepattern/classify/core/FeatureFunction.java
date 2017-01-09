package com.peoplepattern.classify.core;

/**
 * Builds a {@link FeatureBundle} from an arbitary business object
 *
 * <p>Arbitrary objects may be e.g. a block of text, a Web page, an
 * image, a tweet, etc. This object creates a feature bundle from its
 * inputs, which are basically a map from string feature keys to a
 * real-valued feature weight.
 *
 * <p>A real simple example would be like a bag-of-words featurizer:
 * given a block of text, the feature bundle consists of the terms
 * of the text, with weights equal to the count (or idf-df or whatever)
 * of the term in the text.
 *
 * <p>This must provide a signature, provided by {@link #functionSig},
 * which identifies this feature function uniquely.
 */
public interface FeatureFunction<I> {

  /**
   * Convert into a feature bundle
   *
   * @param obj object to conver
   * @return a feature map (string key, double valued)
   */
  public FeatureBundle toBundle(I obj);

  /**
   * Unique signature for this function
   *
   * <p>The identity of the feature function is very important to
   * maintaining sanity in machine learning-based applications.
   * If a model (say a linear classifier) M is trained in a pipeline
   * using feature function F1, but then at run time a different
   * function F2 is used, this can completely mess up your application.
   * If F1 and F2 are very similar -- producing, say, feature vectors
   * of the same length -- this can be very difficult to debug.
   *
   * <p>This value should be calcualated similar to the way
   * {@link java.lang.Object#hashCode} should be -- incorporate all
   * the content of the feature function into the computation of its
   * signature.
   *
   * @return a unique signature identifying this feature function
   */
  public long functionSig();
}
