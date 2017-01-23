package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.FeatureBundle
import com.peoplepattern.classify.core.PortableLinearClassifier
import com.peoplepattern.classify.core.Scored
import org.scalatest._
import scala.collection.JavaConverters._
import scala.io.Source

object TestUtil {

  val functionSig = 0L

  def readDatasetResource(res: String) = {
    val resource = Source.fromURL(getClass.getResource(res))
    readDataSource(resource).toSeq
  }

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
  def readDataSource(source: Source): Iterator[Example[FeatureBundle]] = {
    for (line <- source.getLines) yield {
      assert(line.count(_ == '|') == 1)
      val Array(labelPart, featsPart) = line.split("\\|").map(_.trim)

      val labelParts = labelPart.split("\\s+")

      val (label, importance): (String, Double) = if (labelParts.size == 1)
        (labelParts(0), 1.0)
      else if (labelParts.size == 2)
        (labelParts(0), labelParts(1).toDouble)
      else
        throw new RuntimeException(s"Invalid label $labelPart")

      val feats = for (feat <- featsPart.split("\\s+")) yield {
        val featParts = feat.split(":")
        val featKey = featParts(0)
        val score = if (featParts.size == 1)
          1.0
        else if (featParts.size == 2)
          featParts(1).toDouble
        else
          throw new RuntimeException(s"Invalid feature $feat")
        new Scored(featKey, score)
      }

      val bundle = new FeatureBundle(functionSig, feats.toList.asJava)
      Example(label, bundle, importance)
    }
  }

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

  def testIrisResource(classifier: PortableLinearClassifier,
    resource: String,
    correctPredicted: Seq[Seq[String]]) {
    val testResource = readDatasetResource(resource)
    val predictions = testResource.map(i => classifier.predict(i.item).best)
    for (((prediction, inst), Seq(shouldCorrect, shouldPredicted)) <- (predictions zip testResource) zip correctPredicted) {
      assert(shouldCorrect == inst.label)
      assert(shouldPredicted == prediction)
    }
  }

  def testIris(classifier: PortableLinearClassifier) =
    testIrisResource(classifier,
      "/datasets/iris/iris.test.txt",
      irisCorrectPredictedVowpal)

  def testIrisUtf8(classifier: PortableLinearClassifier) =
    testIrisResource(classifier,
      "/datasets/iris/iris.utf.test.txt",
      irisUTFCorrectPredictedVowpal)

  val IrisTraining = "/datasets/iris/iris.train.txt"
  val IrisTrainingUtf8 = "/datasets/iris/iris.utf.train.txt"
}

// abstract class ClassifierBase(method: String) extends FlatSpec {
//
//   val functionSig = 0L
//
//   /**
//    * Create a temporary file, with reasonable defaults for suffix and base dir.
//    */
//   def tmpFile(prefix: String,
//     suffix: String = ".txt",
//     baseDir: String = "/tmp") =
//     File.createTempFile(prefix, suffix, new File(baseDir))
//
//   /**
//    * Read a Scala source into a series of instances, each of which is an
//    * Example, where the data in the instance is directly stored as a sequence
//    * of FeatureObservations. The format of the file is lines like this:
//    *
//    * label [importance] | feat:val feat:val ...
//    *
//    * The vertical bar must be present to separate label from features, and
//    * must have a space after it. This is to support future expansion.
//    *
//    * The value can be omitted, and defaults to 1.0.
//    */
//   def readDataSource(source: Source): Iterator[Example[FeatureBundle]] = {
//     for (line <- source.getLines) yield {
//       assert(line.count(_ == '|') == 1)
//       val Array(labelPart, featsPart) = line.split("\\|").map(_.trim)
//
//       val labelParts = labelPart.split("\\s+")
//
//       val (label, importance): (String, Double) = if (labelParts.size == 1)
//         (labelParts(0), 1.0)
//       else if (labelParts.size == 2)
//         (labelParts(0), labelParts(1).toDouble)
//       else
//         throw new RuntimeException(s"Invalid label $labelPart")
//
//       val feats = for (feat <- featsPart.split("\\s+")) yield {
//         val featParts = feat.split(":")
//         val featKey = featParts(0)
//         val score = if (featParts.size == 1)
//           1.0
//         else if (featParts.size == 2)
//           featParts(1).toDouble
//         else
//           throw new RuntimeException(s"Invalid feature $feat")
//         new Scored(featKey, score)
//       }
//
//       val bundle = new FeatureBundle(functionSig, feats.toList.asJava)
//       Example(label, bundle, importance)
//     }
//   }
//
//   def readDatasetResource(res: String) = {
//     val resource = Source.fromURL(getClass.getResource(res))
//     readDataSource(resource).toSeq
//   }
//
//   def readJSONModelResource(res: String): PortableLinearClassifier = {
//     val resource = Source.fromURL(getClass.getResource(res))
//     PortableLinearClassifier.JPARSER.readJson(resource.bufferedReader)
//   }
//
//   def readBinaryModelResource(res: String) = {
//     val resource = getClass.getResource(res).openStream()
//     val in = new DataInputStream(new BufferedInputStream(resource))
//     try {
//       PortableLinearClassifier.BREADER.readFromStream(in)
//     } finally {
//       in.close()
//     }
//   }
//
//   def createMockClassifier(isUTF: Boolean) = {
//     val features =
//       if (isUTF)
//         Array("", "東京no", "ྊ", "ඎ", "กรุงเทพมหานคร")
//       else
//         Array("", "sepal-length", "sepal-width", "petal-length", "petal-width")
//     val labels =
//       if (isUTF)
//         Array("Übeض-setosa", "ßen-東京", "шя-الرياض‎-virginica")
//       else
//         Array("Iris-setosa", "Iris-versicolor", "Iris-virginica")
//
//     val fmap = new ExactFeatureMap(functionSig, features)
//     val weights = Array(
//       new Vec(Array(0.42899, 0.125613, 1.712036, -2.214484, -0.754824)),
//       new Vec(Array(0.567636, 0.426266, -1.413687, 0.603592, -1.531267)),
//       new Vec(Array(-1.22069, -1.038105, -1.901427, 1.961815, 2.268535)))
//
//     val lc = new LinearClassifier(functionSig, labels, weights)
//     new PortableLinearClassifier(lc, fmap)
//   }
//
//   val mockClassifier = createMockClassifier(isUTF = false)
//   val mockUTFClassifier = createMockClassifier(isUTF = true)
//
//   val irisCorrectPredictedVowpal = Seq(
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-versicolor", "Iris-versicolor"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-versicolor", "Iris-virginica"),
//     Seq("Iris-versicolor", "Iris-virginica"),
//     Seq("Iris-virginica", "Iris-virginica"),
//     Seq("Iris-setosa", "Iris-setosa"),
//     Seq("Iris-setosa", "Iris-setosa")
//   )
//
//   val irisUTFCorrectPredictedVowpal = Seq(
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("ßen-東京", "ßen-東京"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("ßen-東京", "шя-الرياض‎-virginica"),
//     Seq("ßen-東京", "шя-الرياض‎-virginica"),
//     Seq("шя-الرياض‎-virginica", "шя-الرياض‎-virginica"),
//     Seq("Übeض-setosa", "Übeض-setosa"),
//     Seq("Übeض-setosa", "Übeض-setosa")
//   )
//
//   def testIrisResource(classifier: PortableLinearClassifier,
//     resource: String,
//     correctPredicted: Seq[Seq[String]]) {
//     val testResource = readDatasetResource(resource)
//     val predictions = testResource.map(i => classifier.predict(i.item).best)
//     for (((prediction, inst), Seq(shouldCorrect, shouldPredicted)) <- (predictions zip testResource) zip correctPredicted) {
//       assert(shouldCorrect == inst.label)
//       assert(shouldPredicted == prediction)
//     }
//   }
//
//   def testIris(classifier: PortableLinearClassifier) =
//     testIrisResource(classifier,
//       "/datasets/iris/iris.test.txt",
//       irisCorrectPredictedVowpal)
//
//   def testUTFIris(classifier: PortableLinearClassifier) =
//     testIrisResource(classifier,
//       "/datasets/iris/iris.utf.test.txt",
//       irisUTFCorrectPredictedVowpal)
//
//   class Runner(hashtrick: Option[Int]) {
//     def trainIrisResource(method: String, resource: String) = {
//       val trainResource = readDatasetResource(resource)
//       val hashOptions = new HashingOptions(hashtrick = hashtrick)
//       if (method == "vowpal") {
//         val options = VowpalTrainer.Options(hashOptions)
//         new VowpalTrainer(options).train(trainResource, functionSig)
//       } else {
//         val options = LibLinearTrainer.Options(hashOptions)
//         new LibLinearTrainer(options).train(trainResource, functionSig)
//       }
//     }
//
//     def trainIris(method: String) =
//       if (method == "mock")
//         mockClassifier
//       else
//         trainIrisResource(method, "/datasets/iris/iris.train.txt")
//     def trainUTFIris(method: String) =
//       if (method == "mock")
//         mockUTFClassifier
//       else
//         trainIrisResource(method, "/datasets/iris/iris.utf.train.txt")
//
//     def run() {
//       val name =
//         if (hashtrick != None) "the hashing classifier"
//         else "the exact classifier"
//
//       name should s"work correctly on Iris with method $method" in {
//         testIris(trainIris(method))
//       }
//
//       it should s"work correctly on Iris UTF-8 with method $method" in {
//         testIris(trainIris(method))
//       }
//
//       it should s"write and read back a JSON model with method $method" in {
//         val classifier = trainIris(method)
//         val modelFile = tmpFile(s"iris-$method-json-model")
//         modelFile.deleteOnExit
//         classifier.writeJson(modelFile)
//         val cfier2 = PortableLinearClassifier.JPARSER.readJson(modelFile)
//         testIris(cfier2)
//       }
//
//       it should s"write and read back a binary model with method $method" in {
//         val classifier = trainIris(method)
//         val modelFile = tmpFile(s"iris-$method-binary-model")
//         modelFile.deleteOnExit
//         classifier.writeToBinaryFile(modelFile)
//         val cfier2 = PortableLinearClassifier.BREADER.readFromBinaryFile(modelFile)
//         testIris(cfier2)
//       }
//
//       it should s"write and read back a UTF-8 JSON model with method $method" in {
//         val classifier = trainUTFIris(method)
//         val modelFile = tmpFile(s"iris-$method-json-model")
//         modelFile.deleteOnExit
//         classifier.writeJson(modelFile)
//         val cfier2 = PortableLinearClassifier.JPARSER.readJson(modelFile)
//         testUTFIris(cfier2)
//       }
//
//       it should s"write and read back a UTF-8 binary model with method $method" in {
//         val classifier = trainUTFIris(method)
//         val modelFile = tmpFile(s"iris-$method-binary-model")
//         modelFile.deleteOnExit
//         classifier.writeToBinaryFile(modelFile)
//         val cfier2 = PortableLinearClassifier.BREADER.readFromBinaryFile(modelFile)
//         testUTFIris(cfier2)
//       }
//     }
//   }
//
//   def run(hashtrick: Option[Int]) {
//     new Runner(hashtrick).run()
//   }
//
//   run(None)
//   run(Some(100))
// }
//
// // Run this with 'sbt vowpal:test' or 'sbt all:test'
// class RunVowpalSpec extends ClassifierBase("vowpal") {}
//
// // Run this with 'sbt liblinear:test' or 'sbt all:test'
// class RunLibLinearSpec extends ClassifierBase("liblinear") {}
//
// // Run this with 'sbt test' or 'sbt all:test'
// class RunMockSpec extends ClassifierBase("mock") {
//   it should "fail on unsupported binary model version an existing binary model" in {
//     for (
//       resource <- Seq(
//         "/datasets/iris/iris.vw.model.bin",
//         "/datasets/iris/iris.vw.hashed.model.bin",
//         "/datasets/iris/iris.vw.model.utf.json",
//         "/datasets/iris/iris.vw.model.utf.bin")
//     ) {
//
//       try {
//         readBinaryModelResource(resource)
//       } catch {
//         case _: IOException => info(s"Correctly threw IO error reading $resource")
//       }
//     }
//   }
// }
