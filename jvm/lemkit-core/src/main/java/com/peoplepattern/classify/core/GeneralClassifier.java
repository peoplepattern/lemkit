package com.peoplepattern.classify.core;

/**
 * A general classifier to solve general problems
 *
 * <p>Seeing a classifier as a "black-box" function from arbitary objects
 * to {@link Classification}s is useful for engineering around classifiers.
 * This generic class provides just that, with a {@link LinearClassifier}
 * providing the core underlying model. In support of the underlying model
 * this maintaings a {@link FeatureFunction} and a {@link FeatureMap} to
 * translate arbitrary arguments into vectors for classification.
 */
public class GeneralClassifier<I> implements Classifier<I> {

  private final FeatureFunction<I> featureFunc;
  private final PortableLinearClassifier model;

  /**
   * Construct from a {@link PortableLinearClassifier} and a {@link FeatureFunction}
   *
   * <p>The PortableLinearClassifier already maintains its own {@link FeatureMap}
   *
   * @param func the feature function to translate business objects into feature bundles
   * @param classifier the classifier to run on feature bundles
   */
  public GeneralClassifier(final FeatureFunction<I> func, final PortableLinearClassifier classifier) {
    featureFunc = func;
    model = classifier;
  }

  /**
   * Construct with a feature function, a feature map and an underlying linear
   * classifier model
   *
   * <p>The feature function and feature map work together to translate a
   * given business object into a vector {@link Vec} for classification.
   * The linear classifier executes the classification.
   *
   * @param func a feature function to translate a business object into
   *             a feature bundle
   * @param map a feature map to help translate a feature bundle to a vector
   * @param model the underlying linear classifier
   */
  public GeneralClassifier(final FeatureFunction<I> func, final FeatureMap map,
      final LinearClassifier model) {
    this(func, new PortableLinearClassifier(model, map));
  }

  public Classification predict(I obj) {
    return model.predict(featureFunc.toBundle(obj));
  }

  public String[] getLabels() {
    return model.getLabels();
  }

  public long functionSig() {
    return model.functionSig();
  }
}
