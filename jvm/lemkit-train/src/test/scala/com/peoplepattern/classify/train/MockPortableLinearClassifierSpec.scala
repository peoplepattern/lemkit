package com.peoplepattern.classify.train;

import com.peoplepattern.classify.core.ExactFeatureMap
import com.peoplepattern.classify.core.FeatureBundle
import com.peoplepattern.classify.core.LinearClassifier
import com.peoplepattern.classify.core.PortableLinearClassifier
import com.peoplepattern.classify.core.Scored
import com.peoplepattern.classify.core.Vec
import org.scalatest._
import scala.collection.JavaConverters._

class MockPortableLinearClassifierSpec extends FlatSpec {

  import TestUtil.{ functionSig, testIris, testIrisUtf8 }

  val featuresUtf8 = Array("", "東京no", "ྊ", "ඎ", "กรุงเทพมหานคร")
  val featuresAscii = Array("", "sepal-length", "sepal-width", "petal-length", "petal-width")

  val labelsUtf8 = Array("Übeض-setosa", "ßen-東京", "шя-الرياض‎-virginica")
  val labelsAscii = Array("Iris-setosa", "Iris-versicolor", "Iris-virginica")

  def createMockClassifier(features: Array[String], labels: Array[String]) = {
    val fmap = new ExactFeatureMap(functionSig, features)
    val weights = Array(
      new Vec(Array(0.42899, 0.125613, 1.712036, -2.214484, -0.754824)),
      new Vec(Array(0.567636, 0.426266, -1.413687, 0.603592, -1.531267)),
      new Vec(Array(-1.22069, -1.038105, -1.901427, 1.961815, 2.268535)))

    val lc = new LinearClassifier(functionSig, labels, weights)
    new PortableLinearClassifier(lc, fmap)
  }

  val mockClassifier = createMockClassifier(featuresAscii, labelsAscii)
  val mockClassifierUtf8 = createMockClassifier(featuresUtf8, labelsUtf8)

  "The mocked PortableLinearClassifier" should "produce expected predictions" in {
    val fb = new FeatureBundle(functionSig, Seq(new Scored("sepal-length", 1.0)).asJava)
    assert(mockClassifier.predict(fb).best == "Iris-versicolor")
  }

  it should "pass the Iris test" in {
    testIris(mockClassifier)
  }

  "The mocked UTF-8 PortableLinearClassifier" should "produce expected predictions" in {
    val fb = new FeatureBundle(functionSig, Seq(new Scored("東京no", 1.0)).asJava)
    assert(mockClassifierUtf8.predict(fb).best == "ßen-東京")
  }

  it should "pass the Iris test" in {
    testIrisUtf8(mockClassifierUtf8)
  }
}
