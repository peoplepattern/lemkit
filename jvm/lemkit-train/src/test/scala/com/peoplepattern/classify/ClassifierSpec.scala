package com.peoplepattern.classify

import java.io._

import scala.io.Source

import org.scalatest._

import data._

abstract class ClassifierBase(method: String) extends FlatSpec {

  /**
   * Create a temporary file, with reasonable defaults for suffix and base dir.
   */
  def tmpFile(prefix: String,
    suffix: String = ".txt",
    baseDir: String = "/tmp") =
    File.createTempFile(prefix, suffix, new File(baseDir))

  def readDatasetResource(res: String) = {
    val resource = Source.fromURL(getClass.getResource(res))
    ClassifierSource.readDataSource(resource).toSeq
  }

  def readJSONModelResource(res: String) = {
    val resource = Source.fromURL(getClass.getResource(res))
    LinearClassifier.readJSONModelSource(resource)
  }

  def readBinaryModelResource(res: String) = {
    val resource = getClass.getResource(res).openStream()
    val in = new DataInputStream(new BufferedInputStream(resource))
    try {
      LinearClassifier.readBinaryModelStream(in)
    } finally {
      in.close()
    }
  }

  def createMockClassifier(isUTF: Boolean) = {
    val featureMap =
      if (isUTF)
        Map(
          "" -> 0,
          "東京no" -> 1,
          "ྊ" -> 2,
          "ඎ" -> 3,
          "กรุงเทพมหานคร" -> 4
        )
      else
        Map(
          "" -> 0,
          "sepal-length" -> 1,
          "sepal-width" -> 2,
          "petal-length" -> 3,
          "petal-width" -> 4
        )
    val labelMap =
      if (isUTF)
        Map(
          "Übeض-setosa" -> 0,
          "ßen-東京" -> 1,
          "шя-الرياض‎-virginica" -> 2
        )
      else
        Map(
          "Iris-setosa" -> 0,
          "Iris-versicolor" -> 1,
          "Iris-virginica" -> 2
        )
    val fmap = new ExactFeatureMap(featureMap)
    val indexer = new ClassifierIndexer(labelMap, fmap)
    val weights = Array(
      Array(0.42899, 0.125613, 1.712036, -2.214484, -0.754824),
      Array(0.567636, 0.426266, -1.413687, 0.603592, -1.531267),
      Array(-1.22069, -1.038105, -1.901427, 1.961815, 2.268535))
    LinearClassifier(indexer, weights)
  }
  val mockClassifier = createMockClassifier(isUTF = false)
  val mockUTFClassifier = createMockClassifier(isUTF = true)

  val irisCorrectPredictedVowpal = Seq(
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-versicolor", "Iris-versicolor"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-versicolor", "Iris-virginica"),
    Seq("Iris-versicolor", "Iris-virginica"),
    Seq("Iris-virginica", "Iris-virginica"),
    Seq("Iris-setosa", "Iris-setosa"),
    Seq("Iris-setosa", "Iris-setosa")
  )

  val irisUTFCorrectPredictedVowpal = Seq(
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("ßen-東京", "ßen-東京"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("ßen-東京", "шя-الرياض‎-virginica"),
    Seq("ßen-東京", "шя-الرياض‎-virginica"),
    Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
    Seq("Übeض-setosa", "Übeض-setosa"),
    Seq("Übeض-setosa", "Übeض-setosa")
  )

  def testIrisResource(classifier: LinearClassifier,
    resource: String,
    correctPredicted: Seq[Seq[String]]) {
    val testResource = readDatasetResource(resource)
    val predictions = testResource.map(i => classifier(i.features))
    for (((prediction, inst), Seq(shouldCorrect, shouldPredicted)) <- (predictions zip testResource) zip correctPredicted) {
      assert(shouldCorrect == inst.label)
      assert(shouldPredicted == prediction)
    }
  }

  def testIris(classifier: LinearClassifier) =
    testIrisResource(classifier,
      "/datasets/iris/iris.test.txt",
      irisCorrectPredictedVowpal)

  def testUTFIris(classifier: LinearClassifier) =
    testIrisResource(classifier,
      "/datasets/iris/iris.utf.test.txt",
      irisUTFCorrectPredictedVowpal)

  class Runner(hashtrick: Option[Int]) {
    def trainIrisResource(method: String, resource: String) = {
      val trainResource = readDatasetResource(resource)
      val hashOptions = new HashingOptions(hashtrick = hashtrick)
      if (method == "vowpal") {
        val options = VowpalTrainer.Options(hashOptions)
        new VowpalTrainer(options).train(trainResource)
      } else {
        val options = LibLinearTrainer.Options(hashOptions)
        new LibLinearTrainer(options).train(trainResource)
      }
    }

    def trainIris(method: String) =
      if (method == "mock")
        mockClassifier
      else
        trainIrisResource(method, "/datasets/iris/iris.train.txt")
    def trainUTFIris(method: String) =
      if (method == "mock")
        mockUTFClassifier
      else
        trainIrisResource(method, "/datasets/iris/iris.utf.train.txt")

    def run() {
      val name =
        if (hashtrick != None) "the hashing classifier"
        else "the exact classifier"

      name should s"work correctly on Iris with method $method" in {
        testIris(trainIris(method))
      }

      it should s"work correctly on Iris UTF-8 with method $method" in {
        testIris(trainIris(method))
      }

      it should s"write and read back a JSON model with method $method" in {
        val classifier = trainIris(method)
        val modelFile = tmpFile(s"iris-$method-json-model")
        modelFile.deleteOnExit
        LinearClassifier.writeJSONModel(classifier, modelFile.toString)
        val cfier2 = LinearClassifier.readJSONModel(modelFile.toString)
        testIris(cfier2)
      }

      it should s"write and read back a binary model with method $method" in {
        val classifier = trainIris(method)
        val modelFile = tmpFile(s"iris-$method-binary-model")
        modelFile.deleteOnExit
        LinearClassifier.writeBinaryModel(classifier, modelFile.toString)
        val cfier2 = LinearClassifier.readBinaryModel(modelFile.toString)
        testIris(cfier2)
      }

      it should s"write and read back a UTF-8 JSON model with method $method" in {
        val classifier = trainUTFIris(method)
        val modelFile = tmpFile(s"iris-$method-json-model")
        modelFile.deleteOnExit
        LinearClassifier.writeJSONModel(classifier, modelFile.toString)
        val cfier2 = LinearClassifier.readJSONModel(modelFile.toString)
        testUTFIris(cfier2)
      }

      it should s"write and read back a UTF-8 binary model with method $method" in {
        val classifier = trainUTFIris(method)
        val modelFile = tmpFile(s"iris-$method-binary-model")
        modelFile.deleteOnExit
        LinearClassifier.writeBinaryModel(classifier, modelFile.toString)
        val cfier2 = LinearClassifier.readBinaryModel(modelFile.toString)
        testUTFIris(cfier2)
      }
    }
  }

  def run(hashtrick: Option[Int]) {
    new Runner(hashtrick).run()
  }

  run(None)
  run(Some(100))
}

// Run this with 'sbt vowpal:test' or 'sbt all:test'
class RunVowpalSpec extends ClassifierBase("vowpal") {}

// Run this with 'sbt liblinear:test' or 'sbt all:test'
class RunLibLinearSpec extends ClassifierBase("liblinear") {}

// Run this with 'sbt test' or 'sbt all:test'
class RunMockSpec extends ClassifierBase("mock") {
  it should "read an existing JSON model" in {
    val cfier = readJSONModelResource("/datasets/iris/iris.vw.model.json")
    testIris(cfier)
  }

  it should "read an existing binary model" in {
    val cfier = readBinaryModelResource("/datasets/iris/iris.vw.model.bin")
    testIris(cfier)
  }

  it should "read an existing hashed binary model" in {
    val cfier =
      readBinaryModelResource("/datasets/iris/iris.vw.hashed.model.bin")
    testIris(cfier)
  }

  it should "read an existing UTF-8 JSON model" in {
    val cfier = readJSONModelResource("/datasets/iris/iris.vw.model.utf.json")
    testUTFIris(cfier)
  }

  it should "read an existing UTF-8 binary model" in {
    val cfier = readBinaryModelResource("/datasets/iris/iris.vw.model.utf.bin")
    testUTFIris(cfier)
  }

  it should "read an existing UTF-8 hashed binary model" in {
    val cfier =
      readBinaryModelResource("/datasets/iris/iris.vw.hashed.model.utf.bin")
    testUTFIris(cfier)
  }
}
