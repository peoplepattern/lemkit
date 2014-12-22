package com.peoplepattern.classify

/**
 * A classifier. Predicts class labels for an item.
 * @tparam L type of labels
 * @tparam I type of item (data instance)
 */
trait Classifier[L, I] extends (I => L) {

  /**
   * Classifies the object `x`, returns a sequence of scores, one per label,
   * where the largest score corresponds to the predicted label; scores
   * are not necessary in the range [0,1]
   */
  def evalRaw(x: I): Seq[Double]

  /**
   * The sequence of labels of the classifier, can be
   * zipped correctly with the output of `evalRaw`
   */
  def labels: Seq[L]

  /**
   * Classifies the object `x`, returns a map from labels
   * to scores such that the scores are ranked such that
   * the largest score is the predicted label and all the
   * scores are &gt;= 0 and &lt;= 1
   */
  def scores(x: I): Seq[(L, Double)]
}
