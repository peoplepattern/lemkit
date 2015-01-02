package com.peoplepattern.classify

import scala.util.control.Breaks._
import data._

object ClassifyApp extends App {
  var regularization: Option[Double] = None
  var verbose = false
  var hashtrick: Option[Int] = None
  var featureSelectionMultiple: Option[Int] = None
  var defaultOptions: Option[String] = None
  var extraOptions: Option[String] = None
  var inputFormat = "sparse"
  var modelFormat = "binary"
  var method = "vowpal"
  var trainFile: Option[String] = None
  var predictFile: Option[String] = None
  var writeModel: Option[String] = None
  var readModel: Option[String] = None

  //var etNodeSize: Option[Int] = None
  //var etMTry: Option[Int] = None
  //var etNTrees: Option[Int] = None
  //var etSparse = false
  //var bloomFilter = false
  //var bfExpectedNumEls: Option[Int] = None
  //var bfFalsePosProb: Option[Double] = None

  var i = 0
  val len = args.size

  def getarg() = {
    i += 1
    require(i < len, s"Argument for ${args(i - 1)} required")
    args(i)
  }
  def getChoice(choices: Seq[String]) = {
    val value = getarg()
    require(choices.contains(value),
      "Argument for %s must be %s" format (args(i - 1),
        choices.map(c => s"'$c'").mkString(" or ")))
    value
  }

  // Parse arguments
  breakable {
    while (i < len) {
      args(i) match {
        case "--l1" => {
          featureSelectionMultiple = Some(getarg().toInt)
        }
        case "--l2" => {
          regularization = Some(getarg().toDouble)
        }
        case "--verbose" | "-v" => {
          verbose = true
        }
        case "--hashtrick" => {
          hashtrick = Some(getarg().toInt)
        }
        case "--default-options" => {
          defaultOptions = Some(getarg())
        }
        case "--extra-options" => {
          extraOptions = Some(getarg())
        }
        // Not implemented yet! May never be. Input is always sparse.
        case "--input-format" | "--if" => {
          inputFormat = getChoice(Seq("sparse", "dense"))
        }
        case "--model-format" | "--mf" | "-f" => {
          modelFormat = getChoice(Seq("json", "binary"))
        }
        case "--method" | "-m" => {
          method = getChoice(Seq("vowpal", "liblinear", "extratrees"))
        }
        case "--train" | "-t" => {
          trainFile = Some(getarg())
        }
        case "--predict" | "-p" => {
          predictFile = Some(getarg())
        }
        case "--write-model" | "-w" => {
          writeModel = Some(getarg())
        }
        case "--read-model" | "-r" => {
          readModel = Some(getarg())
        }
        case _ => {
          // If we took positional arguments, we would eliminate the
          // error here and process them down below
          require(false, "Unrecognized arguments: %s" format
            args.slice(i, len).mkString(" "))
          break
        }
      }
      i += 1
    }
  }

  val hashOptions = HashingOptions(hashtrick = hashtrick)

  // Train classifier or read in model
  val classifier = (trainFile, readModel) match {
    case (Some(file), _) => {
      val trainData = ClassifierSource.readDataFile(file)
      method match {
        case "vowpal" => {
          val options = VowpalClassifierOptions(hashOptions)
          regularization.map { options.regularization = _ }
          options.verbose = verbose
          options.featureSelectionMultiple = featureSelectionMultiple
          defaultOptions.map { options.defaultOptions = _ }
          extraOptions.map { options.extraOptions = _ }
          VowpalClassifier.train(trainData, options)
        }
        case "liblinear" => {
          val options = LibLinearClassifierOptions(hashOptions)
          regularization.map { options.regularization = _ }
          options.verbose = verbose
          defaultOptions.map { options.defaultOptions = _ }
          extraOptions.map { options.extraOptions = _ }
          LibLinearClassifier.train(trainData, options)
        }
      }
    }
    case (_, Some(file)) => {
      if (modelFormat == "json")
        LinearClassifier.readJSONModel(file)
      else
        LinearClassifier.readBinaryModel(file)
    }

    case (None, None) => {
      require(false, "Either --train or --read-model should be given")
      ???
    }
  }

  // Maybe write out model
  writeModel match {
    case Some(file) => {
      classifier match {
        case cfier: LinearClassifier => {
          if (modelFormat == "json")
            LinearClassifier.writeJSONModel(cfier, file)
          else
            LinearClassifier.writeBinaryModel(cfier, file)
        }
        case _ =>
          require(false, "Currently unable to serialize extraTrees model")
      }
    }
    case None => ()
  }

  // Maybe do predictions
  predictFile match {
    case Some(file) => {
      val predictData = ClassifierSource.readDataFile(file).toSeq
      val predictions = predictData.map(i => classifier(i.features))
      val numinsts = predictData.size
      var numcorrect = 0
      for (
        ((prediction, inst), index) <- (predictions zip predictData).zipWithIndex
      ) {
        val correct = inst.label
        val isCorrect = correct == prediction
        println("%s: %s, correct=%s, predicted=%s" format (
          index + 1, if (isCorrect) "CORRECT" else "WRONG",
          correct, prediction))
        if (isCorrect)
          numcorrect += 1
      }
      println("Accuracy: %.2f%%" format (numcorrect.toDouble * 100 / numinsts))
    }
    case None => ()
  }
}
