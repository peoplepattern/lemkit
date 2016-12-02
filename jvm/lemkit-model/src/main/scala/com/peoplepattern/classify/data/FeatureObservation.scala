package com.peoplepattern.classify
package data

/**
 * A feature with its observed magnitude in some context. The default is
 * 1.0, which encodes the usual binary presence/absence distinction for
 * features.
 *
 * @tparam F type of feature
 */
case class FeatureObservation[F](feature: F, magnitude: Double = 1.0) {

  /**
   * Return a new `FeatureObservation` where the features are converted into
   * different features while the magnitude is preserved.
   */
  def map[F2](f: F => F2) = FeatureObservation(f(feature), magnitude)

  /**
   * Maybe return a new `FeatureObservation` where the features are converted
   * into different features while the magnitude is preserved. The return
   * value is an `Option` over a `FeatureObservation`.
   */
  def mapOption[F2](f: F => Option[F2]) = f(feature) match {
    case Some(result) => Some(FeatureObservation(result, magnitude))
    case None => None
  }

  /**
   * Return a new `FeatureObservation` created by adding the magnitude to
   * the magnitude of some other `FeatureObservation`, which must have the
   * same feature object.
   */
  def +(other: FeatureObservation[F]) = {
    assert(feature == other.feature)
    FeatureObservation(feature, magnitude + other.magnitude)
  }

  /**
   * Extract the feature and magnitude into a tuple.
   */
  lazy val tuple = (feature, magnitude)

}

object FeatureObservation {

  /**
   * Given a sequence of feature observations (a feature and its magnitude),
   * combine multiple instances of the same feature, and then sort the result.
   *
   * E.g. `Seq[("foo",1.0),("bar",1.0),("foo",2.0)]`
   *  becomes
   *      `Seq[("bar",1.0),("foo",3.0)]`
   */
  def condense(features: FeatureSet[Int]) =
    features
      .groupBy(_.feature)
      .values
      .map(_.reduce(_ + _))
      .toSeq
      .sortBy(_.feature)

}
