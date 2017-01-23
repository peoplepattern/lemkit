package com.peoplepattern.classify.train

import org.scalatest._

class VowpalTrainerItSpec extends FlatSpec {

  import TestUtil._
  import VowpalTrainer.Options

  "VowpalTrainer" should "learn the Iris model - exact feature map" in {
    val trainResource = readDatasetResource(IrisTraining)
    val trainer = new VowpalTrainer()
    val classifier = trainer.train(trainResource, 0L)
    testIris(classifier)
  }

  it should "learn the Iris model - hased feature map" in {
    val trainResource = readDatasetResource(IrisTraining)
    val trainer = new VowpalTrainer(Options(HashingOptions(Some(1000))))
    val classifier = trainer.train(trainResource, 0L)
    testIris(classifier)
  }

  it should "learn the Iris model (UTF8 special chars) - exact feature map" in {
    val trainResource = readDatasetResource(IrisTrainingUtf8)
    val trainer = new VowpalTrainer()
    val classifier = trainer.train(trainResource, 0L)
    testIrisUtf8(classifier)
  }

  it should "learn the Iris model (UTF8 special chars) - hashed feature map" in {
    val trainResource = readDatasetResource(IrisTrainingUtf8)
    val trainer = new VowpalTrainer(Options(hashing = HashingOptions(Some(1000))))
    val classifier = trainer.train(trainResource, 0L)
    testIrisUtf8(classifier)
  }
}
