package com.peoplepattern.classify
package data

/**
 * A single data instance, consisting of a label and collection of features
 * (also an optional importance weight and a string ID, which is mostly for
 * debugging purposes). To create such an object, use the companion object.
 *
 * @tparam T type of features
 * @tparam L type of label
 */
trait Example[T, +L] extends Serializable { outer =>
  def features: FeatureSet[T]
  def label: L
  def id: String
  def importance: Option[Double] = None

  /**
   * Converts the features in this example to a different one while still
   * preserving label and id.
   */
  def map[U](f: FeatureSet[T] => FeatureSet[U]): Example[U, L] =
    new Example[U, L] {
      val features = f(outer.features)
      val label = outer.label
      override val importance = outer.importance
      val id = outer.id
    }

  /**
   * Converts the label in this example to a different one while still
   * preserving features and id.
   */
  def relabel[L2](f: L => L2): Example[T, L2] = new Example[T, L2] {
    val features = outer.features
    val label = f(outer.label)
    override val importance = outer.importance
    val id = outer.id
  }

  /**
   * Converts the features in this example to a different one while still
   * preserving label and id.
   */
  def flatMap[U](f: FeatureSet[T] => FeatureSet[U]) = map(f)

  override def toString = {
    "Example { id = %s, label = %s, importance = %s, features = %s }" format (id, label, importance, features)
  }
}

/**
 * Companion object for creating objects of type `Example`.
 * Also contains `lift`, which converts a function that applies to
 * a feature collection into a function that applies to `Example` objects.
 */
object Example {

  /**
   * Create a new Example.
   */
  def apply[T, L](features: FeatureSet[T],
    label: L,
    id: String = "",
    importance: Option[Double] = None): Example[T, L] = {
    val f = features
    val l = label
    val i = id
    val im = importance
    new Example[T, L] {
      val features = f
      val label = l
      override val importance = im
      val id = i
    }
  }

  /**
   * Lifts a function to operate over Examples,
   * rather than the contained object.
   */
  def lift[T, U, L](f: FeatureSet[T] => FeatureSet[U]) =
    (o: Example[T, L]) => o.map(f)
}
