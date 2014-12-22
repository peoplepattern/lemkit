package com.peoplepattern.classify.data

/**
 * A single data instance, consisting of a label and collection of features
 * (and also a string ID, which is mostly for debugging purposes). To create
 * such an object, use the companion object.
 *
 * @tparam L type of label
 * @tparam T type of features; normally a collection of some sort
 */
trait Example[+L, +T] extends Observation[T] with Labeled[L] with Serializable {
  outer =>
  def id: String
  def label: L
  def importance: Option[Double] = None

  /**
   * Converts the features in this example to a different one while still
   * preserving label and id.
   */
  override def map[U](f: T => U): Example[L, U] = new Example[L, U] {
    val label = outer.label
    val id = outer.id
    override val importance = outer.importance
    val features = f(outer.features)
  }

  /**
   * Converts the label in this example to a different one while still
   * preserving features and id.
   */
  def relabel[L2](f: L => L2): Example[L2, T] = new Example[L2, T] {
    val label = f(outer.label)
    val id = outer.id
    override val importance = outer.importance
    val features = outer.features
  }

  /**
   * Converts the features in this example to a different one while still
   * preserving label and id.
   */
  override def flatMap[U](f: T => U) = map(f)

  override def toString = {
    s"Example { id = $id, label = $label, importance = $importance, features = $features }"
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
  def apply[L, T](label: L, features: T, id: String = "",
    importance: Option[Double] = None): Example[L, T] = {
    val l = label
    val f = features
    val i = id
    val im = importance
    new Example[L, T] {
      val id = i
      val label = l
      val features = f
      override val importance = im
    }
  }

  /**
   * Lifts a function to operate over Examples,
   * Rather than the contained object.
   */
  def lift[T, U, L](f: T => U) = (o: Example[L, T]) => o.map(f)

}
