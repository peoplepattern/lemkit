package io.people8.classify

import data._
import util.GrowableIndex
import FeatureObservation.condense

/**
 * Featurizes raw input examples at test time and indexes them.
 */
class ClassifierIndexer[I](
    val lmap: Map[String, Int],
    val fmap: FeatureMap,
    val featurizer: Featurizer[I, String]) {
  def apply(text: I): Seq[FeatureObservation[Int]] = {
    // Get the features. Multiple strings may map to the same integers due
    // to the feature trick so we need to condense the feature counts.
    // Add intercept as an extra feature with empty-string name.
    condense((FeatureObservation("", 1.0) +: featurizer(text)).
      flatMap(_.mapOption(fmap.indexOfFeature)))
  }
}

object ClassifierIndexer {
  protected def indexExamples[I, T](
    examples: TraversableOnce[Example[String, I]],
    lmap: GrowableIndex[String],
    featurizer: Featurizer[I, String],
    toIndex: Seq[FeatureObservation[String]] => Seq[FeatureObservation[Int]]) = {
    for (ex <- examples) yield ex.relabel(lmap)
      .map(featurizer)
      // add intercept
      .map(FeatureObservation("", 1.0) +: _)
      .map(toIndex)
      .map(condense)
  }

  def index[I, T](
    examples: TraversableOnce[Example[String, I]],
    featurizer: Featurizer[I, String],
    process: TraversableOnce[Example[Int, Seq[FeatureObservation[Int]]]] => (T, Int),
    hashingOptions: HashingOptions) = {
    if (hashingOptions.hashtrick == None)
      indexExact(examples, featurizer, process)
    else
      indexHashed(examples, featurizer, process, hashingOptions.hashtrick.get)
  }

  /**
   * As examples come through, featurize them, index them, and process them
   * (e.g. writing to a file or converting to a permanent format). The
   * `process` function processes the examples and returns a "cooked"
   * object containing the examples as well as the number of examples.
   * Return the "cooked" object from `process`, the indexer based on
   * the label and feature maps that can index new examples for
   * classification, and the number of examples. This function uses an
   * exact feature map; use `indexHashed` for a hashed one.
   */
  def indexExact[I, T](
    examples: TraversableOnce[Example[String, I]],
    featurizer: Featurizer[I, String],
    process: TraversableOnce[Example[Int, Seq[FeatureObservation[Int]]]] => (T, Int)) = {

    val lmap = new GrowableIndex[String]()
    val fmap = new GrowableIndex[String]()

    val indexedExamples = indexExamples(examples, lmap, featurizer,
      x => x.map(feature => feature.map(fmap)))

    val (cooked, numExamples) = process(indexedExamples)

    (cooked, new ClassifierIndexer(lmap.toMap, new ExactFeatureMap(fmap.toMap),
      featurizer), numExamples)
  }

  /**
   * Same as `index` but uses a hashed feature map.
   */
  def indexHashed[I, T](
    examples: TraversableOnce[Example[String, I]],
    featurizer: Featurizer[I, String],
    process: TraversableOnce[Example[Int, Seq[FeatureObservation[Int]]]] => (T, Int),
    maxNumFeatures: Int) = {

    val lmap = new GrowableIndex[String]()
    val fmap = HashedFeatureMap(maxNumFeatures)

    val indexedExamples = indexExamples(examples, lmap, featurizer,
      x => x.flatMap(feature => feature.mapOption(fmap.indexOfFeature)))

    val (cooked, numExamples) = process(indexedExamples)

    (cooked, new ClassifierIndexer(lmap.toMap, fmap, featurizer), numExamples)
  }
}
