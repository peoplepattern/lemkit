package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.ExactFeatureMap
import org.scalatest._

class CounterIndexerSpec extends FlatSpec {

  "CounterIndexer" should "count stuff" in {
    val ci = new CounterIndexer(0L, Seq("a", "b", "c", "d"))
    assert(ci("a") == 0)
    assert(ci("d") == 3)
    assert(ci.size == 4)
    assert(ci("e") == 4)
    assert(ci.size == 5)
  }

  it should "generate labels" in {
    val ci = new CounterIndexer(0L, Seq("a", "b", "c", "d"))
    assert(ci.labels === Array("a", "b", "c", "d"))
    ci("e")
    assert(ci.labels === Array("a", "b", "c", "d", "e"))
  }

  it should "create an exact feature map" in {
    val ci = new CounterIndexer(0L, Seq("a", "b", "c", "d"))
    assert(ci.featureMap == new ExactFeatureMap(0L, Array("a", "b", "c", "d")))
  }
}
