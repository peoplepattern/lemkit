package io.people8.classify

import data._

/**
 * A Scala-usable version of a featurizing classifier.
 *
 * FIXME: Generalize label type
 *
 * @tparam I type of data instance
 * @param indexer Object to convert data instance to indexed features
 */
abstract class FeaturizingClassifier[I](
    indexer: ClassifierIndexer[I]) extends Classifier[String, I] {
  val labels = indexer.lmap.keys.toSeq // unordered set of label strings
  // ordered set of label strings, corresponding to label indices
  val labelIndex = indexer.lmap.toSeq.sortBy(_._2).unzip._1

  /**
   * Make predictions on this input text. So, clearly, this isn't a general
   * implementation, but it's a start and it does what we need.
   */
  def apply(text: I): String = {
    // Return the label of the best scoring label index.
    (labelIndex zip evalRaw(text)).maxBy(_._2)._1
  }

  def rawEvalFeatures(feats: Seq[FeatureObservation[Int]]): Seq[Double]

  def evalRaw(text: I): Seq[Double] = rawEvalFeatures(indexer(text))
}
