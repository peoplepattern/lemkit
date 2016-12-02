package com.peoplepattern.classify

import data._

/**
 * Helper object to deal with indexing and writing examples for processing
 * by LIBLINEAR.
 */
object LibLinearIndexer extends ClassifierIndexerWriter {
  import java.io._

  /**
   * Write out the indexed examples to disk in a format that LibLinear can process.
   */
  def writeExamples(examples: TraversableOnce[Example[Int, Int]], file: File) = {
    var numExamples = 0
    val out = new BufferedWriter(new FileWriter(file))
    try {
      val examplesIterator = examples.toIterator
      while (examplesIterator.nonEmpty) {
        val ex = examplesIterator.next
        numExamples += 1
        val featureStrings =
          ex.features.map(fo => (fo.feature + 1) + ":" + fo.magnitude)

        // Write out the example.
        out.write((ex.label + 1) + " " + featureStrings.mkString(" ") + "\n")
      }
    } finally {
      out.close()
    }
    numExamples
  }
}
