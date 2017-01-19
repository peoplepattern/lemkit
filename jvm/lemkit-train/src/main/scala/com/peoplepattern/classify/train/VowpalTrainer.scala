package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.FeatureBundle
import com.peoplepattern.classify.core.LinearClassifier
import com.peoplepattern.classify.core.PortableLinearClassifier
import com.peoplepattern.classify.core.Vec
import java.io.BufferedWriter
import java.io.FileWriter
import scala.collection.JavaConverters._
import scala.io.Source

object VowpalTrainer {

  /**
   * The options for Vowpal classifier learning.
   *
   *   hashingOptions: Options for feature hashing.
   *
   *   regularization: The value for L2 regularization.
   *
   *   verbose: Useful for debugging. If true, don't delete tmp files when
   *      program exits, don't suppress verbose output from Vowpal, and output
   *      various status messages.
   *
   *   featureSelectionMultiple: Perform L1 feature selection. The Int value
   *      provided indicates the multiple of the number of training instances
   *      to use as a cutoff. For example, if there are 10,000 training
   *      instances and the multiple is 3, then we will try to find about
   *      30,000 features. The final model is then computed with L2
   *      regularization with those reduced features.
   *
   *   defaultOptions: Default options passed to the underlying Vowpal
   *      Wabbit classifier.
   *
   *   extraOptions: Additional options to pass to the underlying classifier.
   */
  case class Options(
    hashingOptions: HashingOptions,
    regularization: Double = 1.0,
    verbose: Boolean = false,
    featureSelectionMultiple: Option[Int] = None,
    defaultOptions: String = "--bfgs --passes 100 --loss_function logistic --holdout_off",
    extraOptions: String = "")
}

/**
 * Utility for training Vowpal classifiers and reading the resulting
 * parameters.
 */
class VowpalTrainer(
    options: VowpalTrainer.Options,
    filePrefix: String = "train") extends LinearClassifierTrainer {

  import sys.process._
  import java.io.File
  import math.{ max, min, log, exp, ceil, floor }
  import LinearClassifierTrainer.tmpFile

  val quietOption = if (options.verbose) "" else "--quiet"

  val useHashtrick = options.hashingOptions.hashtrick != None

  val doFeatureSelection = options.featureSelectionMultiple != None

  /**
   * Train a Vowpal classifier. Currently fixes most of the options to
   * reasonable values for some of our standard use cases.
   */
  def train(trainingExamples: TraversableOnce[Example[FeatureBundle]], functionSig: Long): PortableLinearClassifier = {

    // Create the files used and produced by Vowpal.
    val trainingFile = tmpFile(s"$filePrefix-vw-training-final-")
    val trainingFileFiltered = tmpFile(s"$filePrefix-vw-training-filtered-")
    val cacheFile = tmpFile(s"$filePrefix-vw-cache-")
    val paramsFile = tmpFile(s"$filePrefix-vw-params-")
    val paramsL1File = tmpFile(s"$filePrefix-vw-paramsL1-")

    // Unless we are verbose, clean up all of the above files.
    if (!options.verbose) {
      trainingFile.deleteOnExit
      trainingFileFiltered.deleteOnExit
      cacheFile.deleteOnExit
      paramsFile.deleteOnExit
      paramsL1File.deleteOnExit
    }

    val featureIndexer = options.hashingOptions.hashtrick match {
      case Some(n) => new HashingIndexer(functionSig, n)
      case None => new CounterIndexer(functionSig)
    }

    val labelIndexer = new CounterIndexer(0L)

    // Write example out to file
    var numExamples = 0

    val trainOut = new BufferedWriter(new FileWriter(trainingFile))
    try {
      for (ex <- trainingExamples) {
        val labelInt = labelIndexer(ex.label) + 1
        val labelImp = ex.importance
        val feats = for (obs <- ex.item.observations.asScala) yield {
          s"${featureIndexer(obs.item)}:${obs.score}"
        }
        trainOut.write(s"$labelInt $labelImp | ${feats.mkString(" ")}\n")
        numExamples += 1
      }
    } finally {
      trainOut.close()
    }

    val numClasses = labelIndexer.size

    // Depending on whether we want to reduce the number of features
    // with L1 feature selection, return either the filtered data/feature-map,
    // or the original data and feature map.
    val (featureMap, trainingFileFinal) = if (useHashtrick || !doFeatureSelection) {
      (featureIndexer.featureMap, trainingFile)
    } else {
      val filteredFeatureIndexer =
        makeFilteredFeatureIndexerL1(
          featureIndexer,
          numClasses,
          numExamples,
          trainingFile,
          trainingFileFiltered,
          paramsL1File,
          cacheFile,
          functionSig)
      (filteredFeatureIndexer.featureMap, trainingFileFiltered)
    }

    val numBits = getNumBitsNeeded(featureMap.size, numClasses)

    // Train the final model with L2 regularization.
    val trainCommand =
      s"vw -d $trainingFileFinal --oaa $numClasses --l2 ${options.regularization} --noconstant --readable_model $paramsFile --cache_file $cacheFile -k $quietOption -b $numBits ${options.defaultOptions} ${options.extraOptions}"

    if (options.verbose)
      println("\nVW training command:\n\n" + trainCommand + "\n")
    val vwResult = (trainCommand !!)

    // Read the parameters from the file.
    val parameters = readParameters(paramsFile, numClasses, featureMap.size)

    val lc = new LinearClassifier(functionSig, labelIndexer.labels, parameters)

    new PortableLinearClassifier(lc, featureMap)
  }

  // Compute and store 2^x for every index x from 0 to 30, inclusive.
  lazy val maxNumClassesBits = 30
  lazy val bitshift = (0 to maxNumClassesBits).map(x => math.pow(2, x).toInt)

  private def makeFilteredFeatureIndexerL1(
    featureIndexer: Indexer,
    numClasses: Int,
    numExamples: Int,
    trainingFile: File,
    trainingFileFiltered: File,
    paramsL1File: File,
    cacheFile: File,
    functionSig: Long) = {
    val numFeatures = featureIndexer.size

    if (options.verbose)
      println(
        s"\nPerforming L1 feature selection on $numExamples training instances with $numFeatures features.")

    // Seek the L1 value that gets us within a delta of the desired number of parameters.
    // This is done by taking two extreme values, l1min and l1max, and searching between
    // them based on the number of parameters returned for midpoints between them. After
    // each L1 attempt, l1min or l1max are updated and provide a new l1reg midpoint to
    // try.
    val numBits = getNumBitsNeeded(numFeatures, numClasses)

    val parameterThreshold = numExamples * options.featureSelectionMultiple.get

    var numParameters = 0
    //var (l1max,l1min) = (0.01,0.000000000001)
    //var l1reg = exp((log(l1min)+log(l1max))/2.0)

    // Set upperbound to be half the number of examples more than the parameter
    // threshold, and the lowerbound to be a quarter of that (which means we allow
    // a bit of slippage below what the caller asked for, but are biased to more
    // features rather than fewer (which tends to produce better models).
    val upperbound = parameterThreshold + floor(numExamples / 2.0)
    val lowerbound = parameterThreshold - floor(numExamples / 4.0)

    // Work with L1 parameter exploration in log space. Note that l1Upper is strictly
    // less than l1Lower, which may seem counter-intuitive, but they are called upper
    // and lower because small values select more features.
    var l1Upper = -30.0
    var l1Lower = -2.0
    var l1Curr = -16.0 // Start the search in the middle (in log space).

    // Track the upper and lowerbounds on number of parameters currently.
    var npUpper = numFeatures
    var npLower = 0

    var numIters = 1

    //while (numIters < 10 && (numParameters < lowerbound || numParameters > upperbound)) {}
    while (numIters < 10 && npUpper > upperbound && npLower < lowerbound) {

      //  l1Curr is in log space, so exponentiate to pass to VW.
      val l1reg = exp(l1Curr)

      // Train the model given the current L1 regularization parameter.
      val trainL1Command =
        s"vw -d $trainingFile --oaa $numClasses --passes 50 --loss_function logistic --l1 $l1reg --noconstant --readable_model $paramsL1File --cache_file $cacheFile -k $quietOption -b $numBits"

      if (options.verbose)
        println("\nVW training L1 command:\n\n" + trainL1Command + "\n")

      val vwL1Result = (trainL1Command !!)

      // Find out how many parameters had non-zero values.
      val paramsFileLength = Source.fromFile(paramsL1File).getLines.length
      numParameters = (paramsFileLength - 12) / numClasses
      val npCurr = numParameters

      if (options.verbose)
        println(s"Num parameters active for L1=$l1reg: " + numParameters)

      // Upperbound or lowerbound depending on whether we are over or under the threshold.
      // Note: the new number of parameters might be less close to what we seek because
      // the graph of l1 to number of parameters is not monotonic. We accept this for now.
      if (npCurr > parameterThreshold) {
        npUpper = npCurr
        l1Upper = l1Curr
      } else {
        npLower = npCurr
        l1Lower = l1Curr
      }

      // Pick the midpoint.
      l1Curr = (l1Lower + l1Upper) / 2.0

      if (options.verbose) {
        println("\n***********************************************")
        println(
          s"L1 feature selection (Iter $numIters): Lower $npLower\tUpper $npUpper\tL1-curr $l1Curr\tL1-lower $l1Lower\t L1-upper $l1Upper\n")
        val l1CurrString = "%1.20f" format exp(l1Curr)
        println(s"OUTPUT\t$l1CurrString\t$numParameters")
      }

      numIters += 1
    }

    // Pick l1Final as the upper or lowerbound that is the closest to the desired
    // number of parameters (but go with the l1Upper if npLower is still below the
    // lowerbound).
    val l1Final =
      if (npLower < lowerbound ||
        (npUpper - parameterThreshold < parameterThreshold - npLower)) {
        l1Upper
      } else {
        l1Lower
      }

    val l1FinalExp = exp(l1Final)

    // Train the model given the current L1 regularization parameter.
    val trainL1Command =
      s"vw -d $trainingFile --oaa $numClasses --passes 50 --loss_function logistic --l1 $l1FinalExp --noconstant --readable_model $paramsL1File --cache_file $cacheFile -k $quietOption -b $numBits"

    if (options.verbose)
      println("\nVW training L1 command:\n\n" + trainL1Command + "\n")

    val vwL1Result = (trainL1Command !!)

    val activeFeatureIndices =
      getActiveFeatures(paramsL1File, numClasses).toSeq

    // val rawFmap = fmap.asInstanceOf[ExactFeatureMap].fmap
    val currFeats = featureIndexer.asInstanceOf[CounterIndexer].labels

    val headerFeatures = List("") // intercept

    val activeFeatureNames =
      activeFeatureIndices.map(currFeats).toSet

    val activeFeatures =
      headerFeatures ++ (activeFeatureNames -- headerFeatures.toSet).toList

    if (options.verbose)
      println(
        s"\n--> L1 feature selection reduced ${featureIndexer.size} features to $numParameters\n")

    val fmask = (for ((feature, index) <- activeFeatures.zipWithIndex)
      yield (featureIndexer(feature), index)).toMap

    // VowpalIndexer.maskFeatures(fmask, trainingFile, trainingFileFiltered)

    val maskedWriter = new BufferedWriter(new FileWriter(trainingFileFiltered))
    try {
      for {
        line <- Source.fromFile(trainingFile).getLines
        Array(labelString, featuresString) = line.split(" \\| ")
      } {
        val features = for {
          fo <- featuresString.split(" ")
          Array(f, o) = fo.split(":")
          maskedIndex <- fmask.get(f.toInt)
        } yield (maskedIndex.toInt + ":" + o)
        maskedWriter.write(labelString + " | " + features.mkString(" ") + "\n")
      }
    } finally {
      maskedWriter.close()
    }

    new CounterIndexer(functionSig, activeFeatures)
  }

  /**
   * How many bits to shift the Vowpal feature index by to get the original index.
   */
  private def getBitShift(numClasses: Int) = {
    assert(numClasses > 1)
    val index = bitshift.indexWhere(numClasses <=)
    assert(index >= 0)
    index
  }

  /**
   * Given a Vowpal index and the number of bits to shift (which is computed
   * from getBitShift based on the number of classes being predicted), return
   * the index of the parameter in the original feature map and the index
   * of the class.
   */
  private def getOriginalIndices(vwIndex: Int, numBits: Int) = {
    val parameterIndex = vwIndex >> numBits
    val classIndex = vwIndex - (parameterIndex << numBits)
    (parameterIndex, classIndex)
  }

  private def getNumBitsNeeded(numFeatures: Int, numClasses: Int) = {
    val numBits = ceil(log(numFeatures) / log(2)).toInt + getBitShift(
      numClasses)
    assert(numBits < 33)
    numBits
  }

  /**
   * Read the parameters output by VW for the option --readable_model.
   * Note: this is possibly brittle. It was tested for several models
   * trained with L2 logistic regression, but it is conceivable that
   * some aspects of the format will change (especially the header),
   * so look here for obvious errors.
   */
  private def readParameters(paramsFile: File,
    numClasses: Int,
    numFeatures: Int): Array[Vec] = {
    val bitsToShift = getBitShift(numClasses)
    val parameters = Array.fill(numClasses, numFeatures)(0.0)
    for {
      line <- Source.fromFile(paramsFile).getLines.drop(12)
      Array(indexString, weightString) = line.split(":")
      (parameterIndex, classIndex) = getOriginalIndices(indexString.toInt,
        bitsToShift)
    } {
      parameters(classIndex)(parameterIndex) = weightString.toDouble
    }

    for (paramArray <- parameters) yield new Vec(paramArray)
  }

  /**
   * Read the parameters output by VW for the option --readable_model. Reads
   * in for sparse representation so we don't have a huge 2-D Array with lots
   * of zero entries.
   */
  private def getActiveFeatures(paramsFile: File, numClasses: Int) = {
    val bitsToShift = getBitShift(numClasses)
    for {
      line <- Source.fromFile(paramsFile).getLines.drop(12)
      Array(indexString, weightString) = line.split(":")
      if weightString.toDouble != 0.0
      index = indexString.toInt
      (parameterIndex, _) = getOriginalIndices(indexString.toInt, bitsToShift)
    } yield parameterIndex
  }
}
