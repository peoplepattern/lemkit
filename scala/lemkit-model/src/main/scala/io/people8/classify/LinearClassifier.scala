package io.people8.classify

import scala.io.Source
import scala.collection.mutable

import java.io._

import net.liftweb.{ json => liftjson }
import net.liftweb.json.JsonDSL._

import data._
import util.NumNormalizer._

/**
 * A Scala-usable version of a linear classifier.
 *
 * FIXME: Generalize label type
 *
 * @tparam I type of data instance
 * @param parameters Array of weight arrays, one per label; should have
 *   same number of weights per array
 * @param lmap Map from label names to corresponding identifying integers
 * @param fmap Map from feature names to corresponding identifying integers
 * @param featurizer Object to generate features for data instance
 */
@SerialVersionUID(1)
class LinearClassifier[I](
    private val indexer: ClassifierIndexer[I],
    private val parameters: Array[Array[Double]]) extends FeaturizingClassifier[I](indexer) {
  def rawEvalFeatures(feats: Seq[FeatureObservation[Int]]): Seq[Double] = {
    val numClasses = indexer.lmap.size

    // Start with zero array
    val scoresLogs = Array.fill(numClasses)(0.0)

    // Update the scoresLogs based on the features.
    for (fobs <- feats; classIndex <- 0 until numClasses)
      scoresLogs(classIndex) += parameters(classIndex)(fobs.feature) * fobs.magnitude

    scoresLogs.toSeq
  }

  def scores(text: I): Seq[(String, Double)] = {
    labelIndex zip sumNormalize(exp(evalRaw(text)))
  }
}

class InvalidFormatException(msg: String) extends Exception(msg)

/**
 * General utilities for dealing with linear classifiers.
 */
object LinearClassifier {
  val magicNumber = 0x6A48B9DD
  val majorVersion = 1
  val minorVersion = 0
  val labelID = 100
  val featureTypeID = 110
  val featureTypeExact = 1
  val featureTypeHashed = 2
  val featureTypeBloomFilterHashed = 3
  val featureFeaturesID = 111
  val featureMaxFeatsID = 112
  val featureBloomFilterID = 113
  val weightsTypeID = 120
  val weightsTypeDense = 1
  val weightsTypeSparse = 2
  val weightsValuesID = 121

  def apply[I](indexer: ClassifierIndexer[I], params: Array[Array[Double]]) =
    new LinearClassifier(indexer, params)

  def readJSONModel[I](file: String, featurizer: Featurizer[I, String]) =
    readJSONModelSource(Source.fromFile(file), featurizer)

  def readJSONModelSource[I](source: Source,
    featurizer: Featurizer[I, String]) = {
    val filecontents = source.getLines.toSeq.mkString("\n")
    val json = liftjson.parse(filecontents)
    val jsonfeats = json \ "features"
    val fmap = (jsonfeats \ "type").values.toString match {
      case "exact" =>
        new ExactFeatureMap((jsonfeats \ "features").values.
          asInstanceOf[Map[String, BigInt]].map {
            case (key, value) => (key, value.toInt)
          })
      case "hashed" =>
        HashedFeatureMap((jsonfeats \ "maxfeats").values.toString.toInt)
    }
    val lmap = (json \ "labels").values.asInstanceOf[Map[String, BigInt]].map {
      case (key, value) => (key, value.toInt)
    }

    val jsonweights = json \ "weights"
    val weights = (jsonweights \ "type").values.toString match {
      case "dense" =>
        (jsonweights \ "values").values.asInstanceOf[Seq[Seq[Double]]]
      case "sparse" =>
        ???
    }
    val params = weights.map(_.toArray).toArray
    apply(new ClassifierIndexer(lmap, fmap, featurizer), params)
  }

  import java.nio.charset.Charset
  val utf8 = Charset.forName("UTF-8")

  def writeString(out: DataOutput, str: String) {
    val bytes = str.getBytes(utf8)
    out.writeInt(bytes.length)
    out.write(bytes, 0, bytes.length)
  }

  def writeStringSeq(out: DataOutput, labels: Seq[String]) {
    out.writeInt(labels.length)
    labels.foreach(label => writeString(out, label))
  }

  def writeTable(out: DataOutput, table: Map[String, Int]) {
    out.writeInt(table.size)
    for ((label, value) <- table) {
      writeString(out, label)
      out.writeInt(value)
    }
  }

  def readString(in: DataInput) = {
    val len = in.readInt
    val bytes = new Array[Byte](len)
    in.readFully(bytes)
    new String(bytes, utf8)
  }

  def readStringSeq(in: DataInput) = {
    val length = in.readInt
    (for (i <- 0 until length) yield readString(in)).toSeq
  }

  def readTable(in: DataInput) = {
    val size = in.readInt
    val map = mutable.Map[String, Int]()
    for (i <- 0 until size) {
      val label = readString(in)
      val value = in.readInt
      map(label) = value
    }
    map.toMap
  }

  def checkInt(in: DataInput, value: Int) {
    val checked = in.readInt
    if (checked != value)
      throw new InvalidFormatException(
        "Invalid binary format; expected int %s but saw %s" format (
          value, checked))
  }

  def checkShort(in: DataInput, value: Int) {
    val checked = in.readShort
    if (checked != value)
      throw new InvalidFormatException(
        "Invalid binary format; expected short %s but saw %s" format (
          value, checked))
  }

  def readBinaryModel[I](file: String, featurizer: Featurizer[I, String]) = {
    val in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
    try {
      readBinaryModelStream(in, featurizer)
    } finally {
      in.close()
    }
  }

  def readBinaryModelStream[I](in: DataInputStream, featurizer: Featurizer[I, String]) = {
    checkInt(in, magicNumber)
    checkShort(in, majorVersion)
    checkShort(in, minorVersion)
    checkShort(in, labelID)
    val lmap = readTable(in)
    checkShort(in, featureTypeID)
    val featureType = in.readShort
    val fmap = if (featureType == featureTypeExact) {
      checkShort(in, featureFeaturesID)
      ExactFeatureMap(readStringSeq(in))
    } else if (featureType == featureTypeHashed) {
      checkShort(in, featureMaxFeatsID)
      HashedFeatureMap(in.readInt)
    } else {
      throw new InvalidFormatException(
        "Invalid binary format; unknown feature map type %s" format
          featureType)
    }
    checkShort(in, weightsTypeID)
    // FIXME: Implement sparse weights
    checkShort(in, weightsTypeDense)
    checkShort(in, weightsValuesID)
    val numClasses = in.readInt
    val parameters = new Array[Array[Double]](numClasses)
    for (i <- 0 until numClasses) {
      val len = in.readInt
      val oneparams = new Array[Double](len)
      for (j <- 0 until len) {
        oneparams(j) = in.readDouble
      }
      parameters(i) = oneparams
    }
    apply(new ClassifierIndexer(lmap, fmap, featurizer), parameters)
  }

  def writeJSONModel[I](classifier: LinearClassifier[I], file: String) = {
    val features = classifier.indexer.fmap match {
      case f: ExactFeatureMap =>
        ("type" -> "exact") ~ ("features" -> f.fmap)
      case f: HashedFeatureMap =>
        ("type" -> "hashed") ~ ("maxfeats" -> f.maxNumberOfFeatures)
    }
    val seqparams = classifier.parameters.map(_.toIndexedSeq).toIndexedSeq
    val json =
      ("labels" -> classifier.indexer.lmap) ~ ("features" -> features) ~
        ("weights" ->
          ("type" -> "dense") ~ ("values" -> seqparams)
        )
    val rendered = liftjson.pretty(liftjson.render(json))
    val out = new BufferedWriter(new FileWriter(new File(file)))
    try {
      out.write(rendered + "\n")
    } finally {
      out.close()
    }
  }

  def writeBinaryModel[I](classifier: LinearClassifier[I], file: String) {
    val out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      writeBinaryModel(classifier, out)
    } finally {
      out.close()
    }
  }

  def writeBinaryModel[I](classifier: LinearClassifier[I], out: DataOutputStream) {
    out.writeInt(magicNumber)
    out.writeShort(majorVersion)
    out.writeShort(minorVersion)
    out.writeShort(labelID)
    writeTable(out, classifier.indexer.lmap)
    out.writeShort(featureTypeID)
    classifier.indexer.fmap match {
      case f: ExactFeatureMap => {
        out.writeShort(featureTypeExact)
        out.writeShort(featureFeaturesID)
        writeStringSeq(out, f.fmap.toSeq.sortBy(_._2).unzip._1)
      }
      case f: HashedFeatureMap => {
        out.writeShort(featureTypeHashed)
        out.writeShort(featureMaxFeatsID)
        out.writeInt(f.maxNumberOfFeatures)
      }
    }
    out.writeShort(weightsTypeID)
    out.writeShort(weightsTypeDense)
    out.writeShort(weightsValuesID)
    out.writeInt(classifier.parameters.length)
    for (oneparams <- classifier.parameters) {
      out.writeInt(oneparams.length)
      for (param <- oneparams) {
        out.writeDouble(param)
      }
    }
  }

}
