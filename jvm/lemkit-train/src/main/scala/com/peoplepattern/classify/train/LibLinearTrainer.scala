package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.FeatureBundle
import com.peoplepattern.classify.core.LinearClassifier
import com.peoplepattern.classify.core.PortableLinearClassifier
import com.peoplepattern.classify.core.Vec
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import scala.collection.JavaConverters._
import scala.io.Source

object LibLinearTrainer {
  /**
   * The options for LIBLINEAR classifier learning.
   *
   *   @param hashing Options for feature hashing.
   *   @param regularization The value for L2 regularization.
   *   @param verbose Useful for debugging. If true, don't delete tmp files when
   *      program exits, don't suppress verbose output from LIBLINEAR, and output
   *      various status messages.
   *   @param defaultOptions Default options passed to the underlying LIBLINEAR
   *      classifier.
   *   @param extraOptions Additional options to pass to the underlying classifier.
   */
  case class Options(
    hashing: HashingOptions = HashingOptions(),
    regularization: Double = 1.0,
    verbose: Boolean = false,
    defaultOptions: String = "-s 7",
    extraOptions: String = "")

  /**
   * Read the parameters output by LIBLINEAR.
   */
  private def readParameters(paramsFile: File, numClasses: Int, numFeatures: Int): Array[Vec] = {
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
    for (paramArray <- parameters) yield new Vec(paramArray)
  }
}

/**
 * Utilities for training LIBLINEAR classifiers and reading the resulting
 * parameters.
 */
class LibLinearTrainer(
    options: LibLinearTrainer.Options = LibLinearTrainer.Options(),
    filePrefix: String = "train") extends LinearClassifierTrainer {

  import sys.process._
  import LinearClassifierTrainer.tmpFile
  import LibLinearTrainer.readParameters

  /**
   * Train a LIBLINEAR classifier. Currently fixes most of the options to
   * reasonable values for some of our standard use cases.
   */
  def train(trainingExamples: TraversableOnce[Example[FeatureBundle]], functionSig: Long) = {

    // Create the files used and produced by LIBLINEAR.
    val trainingFile = tmpFile(s"$filePrefix-liblinear-training-")
    val modelFile = tmpFile(s"$filePrefix-model-")

    // Unless we are verbose, clean up all of the above files.
    if (!options.verbose) {
      trainingFile.deleteOnExit
      modelFile.deleteOnExit
    }

    val featureIndexer = options.hashing.hashtrick match {
      case Some(n) => new HashingIndexer(functionSig, n, useIntercept = false)
      case None => new CounterIndexer(functionSig, useIntercept = false)
    }

    val labelIndexer = new CounterIndexer(0L)

    var numExamples = 0
    val trainOut = new BufferedWriter(new FileWriter(trainingFile))
    try {
      for (ex <- trainingExamples) {
        numExamples += 1
        val labelInt = labelIndexer(ex.label) + 1

        val featsInts = for (obs <- ex.item.observations.asScala) yield {
          (featureIndexer(obs.item), obs.score)
        }

        val feats = for ((feat, score) <- featsInts.toVector.sorted) yield {
          s"${1 + feat}:$score"
        }

        trainOut.write(s"+$labelInt ${feats.mkString(" ")}\n")
      }
    } finally {
      trainOut.close()
    }

    //     val lmap = indexer.lmap
    //     val fmap = indexer.fmap

    val numClasses = labelIndexer.size

    val quietOption = if (options.verbose) "" else "--quiet"

    // FIXME!! Implement L1 regularization like is done in VowpalTrainer

    // Train the final model with L2 regularization.
    val trainCommand =
      s"train -c ${options.regularization} ${options.defaultOptions} ${options.extraOptions} $trainingFile $modelFile"

    if (options.verbose)
      println("\nLIBLINEAR training command:\n\n" + trainCommand + "\n")

    val result = (trainCommand !!)

    // Read the parameters from the file.
    val parameters = readParameters(modelFile, numClasses, featureIndexer.size)

    val lc = new LinearClassifier(functionSig, labelIndexer.labels, parameters)
    new PortableLinearClassifier(lc, featureIndexer.featureMap)
  }
}
