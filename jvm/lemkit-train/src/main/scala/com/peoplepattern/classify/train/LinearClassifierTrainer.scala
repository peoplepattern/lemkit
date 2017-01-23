package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.FeatureBundle
import com.peoplepattern.classify.core.FeatureFunction
import com.peoplepattern.classify.core.PortableLinearClassifier

trait LinearClassifierTrainer {
  def train[A](trainingExamples: TraversableOnce[Example[A]], func: FeatureFunction[A]): PortableLinearClassifier = {
    train(trainingExamples.map { ex => ex.copy(item = func.toBundle(ex.item)) }, func.functionSig)
  }

  def train(trainingExamples: TraversableOnce[Example[FeatureBundle]], functionSig: Long): PortableLinearClassifier
}

object LinearClassifierTrainer {

  /**
   * Create a temporary file, with reasonable defaults for suffix and base dir.
   */
  def tmpFile(prefix: String,
    suffix: String = ".txt",
    baseDir: String = "/tmp") =
    java.io.File.createTempFile(prefix, suffix, new java.io.File(baseDir))
}
