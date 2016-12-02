package com.peoplepattern.classify

import data.FeatureObservation

/**
 * A classifier. Predicts class labels for an item.
 */
trait Classifier {

  /**
   * The sequence of labels of the classifier, can be
   * zipped correctly with the output of `evalRaw`
   */
  def labels: Seq[String]

  /**
   * Classify a data instance defined by a set of features, returning the
   * predicted label.
   */
  def apply(feats: FeatureSet[String]): String =
    scores(feats).maxBy(_._2)._1

  /**
   * Classify a data instance defined by a set of features, returning a
   * a sequence of scores, one per label, where the largest score corresponds
   * to the predicted label; scores are not necessarily in the range [0,1]
   */
  def evalRaw(feats: FeatureSet[String]): Seq[Double]

  /**
   * Classify a data instance defined by a set of features, returning a
   * sequence map from labels to scores such that the scores are ranked
   * such that the largest score is the predicted label and all the
   * scores are &gt;= 0 and &lt;= 1
   */
  def scores(feats: FeatureSet[String]): Seq[(String, Double)]
}
