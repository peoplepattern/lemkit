package com.peoplepattern.classify

import data._
import scala.io.Source

/**
 * Helper object to deal with indexing and writing examples for processing
 * by Vowpal.
 */
object VowpalIndexer extends ClassifierIndexerWriter {
  import java.io._

  /**
   * Write out the indexed examples to disk in a format that Vowpal can process.
   */
  def writeExamples(
    examples: TraversableOnce[Example[Int, Seq[FeatureObservation[Int]]]],
    file: File) = {
    var numExamples = 0
    val out = new BufferedWriter(new FileWriter(file))
    try {
      val examplesIterator = examples.toIterator
      while (examplesIterator.nonEmpty) {
        val ex = examplesIterator.next
        numExamples += 1
        // Might be good to use Joey's Vowpal format parsers so that we can
        // be more compact, e.g. not writing out magnitude when it is 1.0.
        val featureStrings =
          ex.features.map(fo => fo.feature + ":" + fo.magnitude)

        // Write out the example.
        val label = (ex.label + 1).toString
        val label_importance =
          label + ex.importance.map(" " + _.toString).getOrElse("")
        out.write(label_importance + " | " + featureStrings.mkString(" ") +
          "\n")
      }
    } finally {
      out.close()
    }
    numExamples
  }

  /**
   * Given a feature mask (e.g. one learned by using L1 feature selection),
   * transform a file with the original indexation into the new one.
   */
  def maskFeatures(fmask: Map[Int, Int], originalFile: File, maskedFile: File) {
    val maskedWriter = new BufferedWriter(new FileWriter(maskedFile))
    try {
      for {
        line <- Source.fromFile(originalFile).getLines
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
  }
}
