package io.people8.classify.data

/**
 * A trait for classes that can index features represented as Strings.
 * Non-general at the moment.
 */
trait FeatureMap extends Serializable {
  /** Convert a feature string to an index. */
  def indexOfFeature(feature: String): Option[Int]
  /** Number of indexed feature strings stored. */
  def size: Int
}
