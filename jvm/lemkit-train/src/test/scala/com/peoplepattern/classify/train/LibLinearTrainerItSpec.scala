package com.peoplepattern.classify.train

import org.scalatest._

class LibLinearTrainerItSpec extends FlatSpec {

  import TestUtil._
  import LibLinearTrainer.Options

  "LibLinearTrainer" should "learn the Iris model - exact feature map" in {
    val trainResource = readDatasetResource(IrisTraining)
    val trainer = new LibLinearTrainer()
    val classifier = trainer.train(trainResource, 0L)
    testIris(classifier)
  }

  it should "learn the Iris model - hased feature map" in {
    val trainResource = readDatasetResource(IrisTraining)
    val trainer = new LibLinearTrainer(Options(HashingOptions(Some(1000))))
    val classifier = trainer.train(trainResource, 0L)
    testIris(classifier)
  }

  it should "learn the Iris model (UTF8 special chars) - exact feature map" in {
    val trainResource = readDatasetResource(IrisTrainingUtf8)
    val trainer = new LibLinearTrainer(Options(verbose = true))
    val classifier = trainer.train(trainResource, 0L)
    testIrisUtf8(classifier)
  }

  it should "learn the Iris model (UTF8 special chars) - hashed feature map" in {
    val trainResource = readDatasetResource(IrisTrainingUtf8)
    val trainer = new LibLinearTrainer(Options(hashing = HashingOptions(Some(10)), verbose = true))
    val classifier = trainer.train(trainResource, 0L)
    testIrisUtf8(classifier)
  }
}
