package com.peoplepattern.classify.train

import com.peoplepattern.classify.core.FeatureMap

trait Indexer extends (String => Int) {

  def featureMap: FeatureMap

  def size: Int
}
