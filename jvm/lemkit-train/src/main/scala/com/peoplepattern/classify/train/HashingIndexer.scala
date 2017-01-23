package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.HashedFeatureMap

class HashingIndexer(functionSig: Long, val size: Int, seed: Int = 0, useIntercept: Boolean = true) extends Indexer {

  val featureMap = new HashedFeatureMap(functionSig, size, seed, useIntercept)

  def apply(f: String): Int = featureMap.indexOfFeature(f)
}
