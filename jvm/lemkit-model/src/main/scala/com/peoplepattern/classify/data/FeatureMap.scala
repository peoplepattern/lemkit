package com.peoplepattern.classify.data

/**
  * A trait for classes that can index features represented as Strings.
  */
trait FeatureMap extends Serializable {

  /** Convert a feature string to an index. */
  def indexOfFeature(feature: String): Option[Int]

  /** Number of indexed feature strings stored. */
  def size: Int
}
