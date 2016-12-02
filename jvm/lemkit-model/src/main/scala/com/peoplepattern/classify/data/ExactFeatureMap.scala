package com.peoplepattern.classify.data

/**
  * A feature map that stores all feature strings and their indices in
  * an in-memory `Map`.
  *
  * @param fmap Map storing feature strings and corresponding indices.
  */
class ExactFeatureMap(val fmap: Map[String, Int]) extends FeatureMap {
  def indexOfFeature(feature: String) = fmap.get(feature)
  lazy val size = fmap.size
}

object ExactFeatureMap {

  /**
    * Create an exact feature map from a seq of feature strings, where the
    * index for each feature is its 0-based index in the seq.
    */
  def apply(indexedFeatures: Seq[String]) =
    new ExactFeatureMap(indexedFeatures.zipWithIndex.toMap)
}
