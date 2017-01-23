package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.ExactFeatureMap
import com.peoplepattern.classify.core.FeatureMap
import scala.collection.mutable.Buffer
import scala.collection.mutable.{ Map => MMap }

class CounterIndexer(functionSig: Long, items: Seq[String] = Seq.empty, useIntercept: Boolean = true) extends Indexer {

  private val buf = Buffer.empty[String]
  private val map = MMap.empty[String, Int]

  for (item <- items)
    apply(item)

  def apply(f: String): Int = {
    map.get(f) match {
      case Some(n) => n
      case None => {
        val n = buf.size
        map(f) = n
        buf += f
        n
      }
    }
  }

  def featureMap: FeatureMap = {
    new ExactFeatureMap(functionSig, labels, useIntercept)
  }

  def labels: Array[String] = buf.toArray

  def size: Int = buf.size
}
