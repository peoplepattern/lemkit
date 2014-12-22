package io.people8.classify

import scala.util.control.Breaks._

import data._

object PredictApp extends App {
  var modelFormat = "binary"
  var predictFile: Option[String] = None
  var readModel: Option[String] = None
  var showCorrect = false
  var showAccuracy = false
  type FeatObs = FeatureObservation[String]
  type Examp = Example[String, Seq[FeatObs]]

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

  breakable {
    while (i < len) {
      args(i) match {
        case "--model-format" | "--mf" | "-f" => {
          modelFormat = getChoice(Seq("json", "binary"))
        }
        case "--predict" | "-p" => {
          predictFile = Some(getarg())
        }
        case "--model" | "-m" => {
          readModel = Some(getarg())
        }
        case "--show-accuracy" | "-a" => {
          showAccuracy = true
        }
        case "--show-correct" | "-c" => {
          showCorrect = true
        }
        case "--help" | "-h" => {
          println("""Usage:

    --model-format | --mf | -f  Model format (json or binary, default binary)
    --predict | -p              File containing data instances to predict
    --model | -m                Trained model file
    --show-accuracy | -a        Output accuracy at end
    --show-correct | -c         Output column indicating correct or wrong""")
        }
        case _ => {
          // If we took positional arguments, we would eliminate the
          // error here and process them down below
          throw new IllegalArgumentException("Unrecognized arguments: %s" format
            args.slice(i, len).mkString(" "))
        }
      }
      i += 1
    }
  }

  object IdentityFeaturizer extends Featurizer[Seq[FeatObs], String] {
    def apply(input: Seq[FeatObs]) = input
  }

  val classifier =
    readModel match {
      case Some(file) => {
        if (modelFormat == "json")
          LinearClassifier.readJSONModel(file, IdentityFeaturizer)
        else
          LinearClassifier.readBinaryModel(file, IdentityFeaturizer)
      }
      case None =>
        throw new IllegalArgumentException("Must specify --model (or -m)")
    }

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
        println("%s%s %s %s" format (
          index + 1,
          if (!showCorrect) "" else if (isCorrect) " CORRECT" else " WRONG",
          correct, prediction))
        if (isCorrect)
          numcorrect += 1
      }
      if (showAccuracy)
        println("Accuracy: %.2f%%" format (numcorrect.toDouble * 100 / numinsts))
    }
    case None =>
      throw new IllegalArgumentException("Must specify --test (or -t)")
  }
}
