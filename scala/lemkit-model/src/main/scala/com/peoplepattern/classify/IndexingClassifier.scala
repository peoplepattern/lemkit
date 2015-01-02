package com.peoplepattern.classify

import data._

/**
 * A Scala-usable version of an indexing classifier.
 *
 * @param indexer Object to convert data instance to indexed features
 */
abstract class IndexingClassifier(
    indexer: ClassifierIndexer) extends Classifier {
  // ordered set of label strings, corresponding to label indices
  val labels = indexer.lmap.toSeq.sortBy(_._2).unzip._1

  def rawEvalFeatures(feats: FeatureSet[Int]): Seq[Double]

  def evalRaw(feats: FeatureSet[String]): Seq[Double] =
    rawEvalFeatures(indexer(feats))
}
