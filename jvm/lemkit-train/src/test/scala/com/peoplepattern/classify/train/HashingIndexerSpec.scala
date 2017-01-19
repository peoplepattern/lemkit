package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.HashedFeatureMap
import org.scalatest._

class HashingIndexerSpec extends FlatSpec {
  "HashingIndexer" should "create a featureMap" in {
    val indexer = new HashingIndexer(1L, 10000, 42)
    assert(indexer.featureMap == new HashedFeatureMap(1L, 10000, 42))
  }
}
