package com.peoplepattern.classify

import data._
import util.GrowableIndex
import FeatureObservation.condense

/**
 * Indexes raw input examples at test time.
 */
class ClassifierIndexer(
    val lmap: Map[String, Int],
    val fmap: FeatureMap) {
  def apply(features: FeatureSet[String]): FeatureSet[Int] = {
    // Get the features. Multiple strings may map to the same integers due
    // to the feature trick so we need to condense the feature counts.
    // Add intercept as an extra feature with empty-string name.
    condense((FeatureObservation("", 1.0) +: features).
      flatMap(_.mapOption(fmap.indexOfFeature)))
  }
}

object ClassifierIndexer {
  protected def indexExamples(
    examples: TraversableOnce[Example[String, String]],
    lmap: GrowableIndex[String],
    toIndex: FeatureSet[String] => FeatureSet[Int]) = {
    for (ex <- examples) yield ex.relabel(lmap)
      // add intercept
      .map(FeatureObservation("", 1.0) +: _)
      .map(toIndex)
      .map(condense)
  }

  def index[T](
    examples: TraversableOnce[Example[String, String]],
    process: TraversableOnce[Example[Int, Int]] => (T, Int),
    hashingOptions: HashingOptions) = {
    if (hashingOptions.hashtrick == None)
      indexExact(examples, process)
    else
      indexHashed(examples, process, hashingOptions.hashtrick.get)
  }

  /**
   * As examples come through, index them, and process them
   * (e.g. writing to a file or converting to a permanent format). The
   * `process` function processes the examples and returns a "cooked"
   * object containing the examples as well as the number of examples.
   * Return the "cooked" object from `process`, the indexer based on
   * the label and feature maps that can index new examples for
   * classification, and the number of examples. This function uses an
   * exact feature map; use `indexHashed` for a hashed one.
   */
  def indexExact[T](
    examples: TraversableOnce[Example[String, String]],
    process: TraversableOnce[Example[Int, Int]] => (T, Int)) = {

    val lmap = new GrowableIndex[String]()
    val fmap = new GrowableIndex[String]()

    val indexedExamples = indexExamples(examples, lmap,
      x => x.map(feature => feature.map(fmap)))

    val (cooked, numExamples) = process(indexedExamples)

    (cooked, new ClassifierIndexer(lmap.toMap, new ExactFeatureMap(fmap.toMap)),
      numExamples)
  }

  /**
   * Same as `index` but uses a hashed feature map.
   */
  def indexHashed[T](
    examples: TraversableOnce[Example[String, String]],
    process: TraversableOnce[Example[Int, Int]] => (T, Int),
    maxNumFeatures: Int) = {

    val lmap = new GrowableIndex[String]()
    val fmap = HashedFeatureMap(maxNumFeatures)

    val indexedExamples = indexExamples(examples, lmap,
      x => x.flatMap(feature => feature.mapOption(fmap.indexOfFeature)))

    val (cooked, numExamples) = process(indexedExamples)

    (cooked, new ClassifierIndexer(lmap.toMap, fmap), numExamples)
  }
}
