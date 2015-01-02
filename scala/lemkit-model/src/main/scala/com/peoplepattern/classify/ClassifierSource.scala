package com.peoplepattern.classify

import scala.io.Source

import data._

object ClassifierSource {
  /**
   * Read a file into a series of instances, each of which is an Example,
   * where the data in the instance is directly stored as a sequence of
   * FeatureObservations. The format of the file is lines like this:
   *
   * label feat:val feat:val ...
   *
   * The value can be omitted, and defaults to 1.0.
   */
  def readDataFile(file: String): Iterator[Example[String, String]] =
    readDataSource(Source.fromFile(file))

  /**
   * Read a Scala source into a series of instances, each of which is an
   * Example, where the data in the instance is directly stored as a sequence
   * of FeatureObservations. The format of the file is lines like this:
   *
   * label [importance] | feat:val feat:val ...
   *
   * The vertical bar must be present to separate label from features, and
   * must have a space after it. This is to support future expansion.
   *
   * The value can be omitted, and defaults to 1.0.
   */
  def readDataSource(source: Source): Iterator[Example[String, String]] = {
    for (line <- source.getLines) yield {
      val label_feats = line.split("""\|""")
      require(label_feats.size == 2,
        "Should have one vertical bar separating label from features")
      val label_importance = label_feats(0).trim.split("""\s+""")
      require(label_importance.size == 1 || label_importance.size == 2,
        "Should have either label alone or label + importance in label portion " + label_feats(0))
      val label = label_importance(0)
      val importance =
        if (label_importance.size == 1) None
        else Some(label_importance(1).toDouble)
      val feats = label_feats(1).trim.split("""\s+""")
      val features = for (field <- feats) yield {
        val featval = field.split(":")
        require(featval.size == 1 || featval.size == 2,
          "Should have at most one colon in field " + field)
        if (featval.size == 1) (field, 1.0)
        else (featval(0), featval(1).toDouble)
      }
      Example(features.toSeq.map {
        case (feature, value) =>
          FeatureObservation(feature, value)
      }, label, importance = importance)
    }
  }
}
