package io.people8.classify.util

object NumNormalizer {

  def exp(seq: Seq[Double]) = seq.map(math.exp)

  def sumNormalize(seq: Seq[Double], sumTo: Double = 1.0) = {
    val denom = seq.sum
    for (n <- seq) yield sumTo * n / denom
  }
}
