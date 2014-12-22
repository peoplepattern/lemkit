package com.peoplepattern.classify

import data._
import scala.io.Source

/**
 * The options for LIBLINEAR classifier learning.
 *
 *   hashingOptions: Options for feature hashing.
 *
 *   regularization: The value for L2 regularization.
 *
 *   verbose: Useful for debugging. If true, don't delete tmp files when
 *      program exits, don't suppress verbose output from LIBLINEAR, and output
 *      various status messages.
 *
 *   defaultOptions: Default options passed to the underlying LIBLINEAR
 *      classifier.
 *
 *   extraOptions: Additional options to pass to the underlying classifier.
 */
case class LibLinearClassifierOptions(
  hashingOptions: HashingOptions,
  var regularization: Double = 1.0,
  var verbose: Boolean = false,
  var defaultOptions: String = "-s 7",
  var extraOptions: String = "")

/**
 * Utilities for training LIBLINEAR classifiers and reading the resulting
 * parameters.
 */
object LibLinearClassifier extends LinearClassifierTrainer {

  import sys.process._
  import java.io.File
  import math.{ max, min, log, exp, ceil, floor }

  /**
   * Train a LIBLINEAR classifier. Currently fixes most of the options to
   * reasonable values for some of our standard use cases.
   */
  def train[I](
    trainingExamples: TraversableOnce[Example[String, I]],
    featurizer: Featurizer[I, String],
    options: LibLinearClassifierOptions,
    filePrefix: String = "train") = {

    // Create the files used and produced by LIBLINEAR.
    val trainingFile = tmpFile(s"$filePrefix-liblinear-training-")
    val modelFile = tmpFile(s"$filePrefix-model-")

    // Unless we are verbose, clean up all of the above files.
    if (!options.verbose) {
      trainingFile.deleteOnExit
      modelFile.deleteOnExit
    }

    // Index and write the training examples in LIBLINEAR format.
    val (indexer, numExamples) =
      LibLinearIndexer.indexAndWriteExamples(trainingExamples, featurizer,
        trainingFile, options.hashingOptions)

    val lmap = indexer.lmap
    val fmap = indexer.fmap
    val numClasses = lmap.size

    val quietOption = if (options.verbose) "" else "--quiet"

    val filteredFmapOpt: Option[FeatureMap] = None
    // FIXME!! Implement L1 regularization like is done in VowpalClassifier

    // Depending on the outcome of the attempt to reduce the number of features
    // with L1 feature selection, return either the filtered data/feature-map,
    // or the original data and feature map.
    val (fmapFinal: FeatureMap, trainingFileFinal) = filteredFmapOpt match {
      // case Some(filteredFmap) => (filteredFmap, trainingFileFiltered)
      case Some(filteredFmap) => ???
      case None => (fmap, trainingFile)
    }

    // Train the final model with L2 regularization.
    val trainCommand = s"train -c ${options.regularization} ${options.defaultOptions} ${options.extraOptions} $trainingFile $modelFile"

    if (options.verbose)
      println("\nLIBLINEAR training command:\n\n" + trainCommand + "\n")
    val result = (trainCommand !!)

    // Read the parameters from the file.
    val parameters = readParameters(modelFile, numClasses, fmapFinal.size)

    new LinearClassifier(new ClassifierIndexer(lmap, fmapFinal, featurizer),
      parameters)
  }

  /**
   * Read the parameters output by LIBLINEAR.
   */
  def readParameters(paramsFile: File, numClasses: Int, numFeatures: Int) = {
    val parameters = Array.fill(numClasses, numFeatures)(0.0)
    var start = false
    var parameterIndex = 0
    for (line <- Source.fromFile(paramsFile).getLines) {
      if (line == "w") start = true
      else if (start) {
        for ((param, classIndex) <- line.split(" ").zipWithIndex) {
          parameters(classIndex)(parameterIndex) = param.toDouble
        }
        parameterIndex += 1
      }
    }
    parameters
  }
}
