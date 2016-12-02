package com.peoplepattern.classify.data

/**
 * A feature map that uses the hashing trick (with a MurmurHash3 hash). This
 * saves memory because no explicit map of Strings to Ints is maintained,
 * and because you can use a model with fewer actual parameters than features,
 * if you can accept collisions. If the number of features used is too small,
 * you'll get a degradation in performance.
 *
 * For more details on the hashing trick, see:
 *   http://hunch.net/~jl/projects/hash_reps/index.html
 */
class HashedFeatureMap protected (val maxNumberOfFeatures: Int)
    extends FeatureMap {
  import scala.util.hashing.MurmurHash3.bytesHash
  import java.nio.charset.Charset
  val stringSeed = 0
  val utf8 = Charset.forName("UTF-8")

  val size = maxNumberOfFeatures

  def hashString(x: String) = bytesHash(x.getBytes(utf8), stringSeed)
  def hashIndex(x: String) =
    ((hashString(x) % maxNumberOfFeatures) + maxNumberOfFeatures) %
      maxNumberOfFeatures
  def indexOfFeature(feature: String): Option[Int] = Some(hashIndex(feature))
}

trait HashedFeatureMapCompanion {
  def actualMaxNumberOfFeatures(maxNumberOfFeatures: Int) = {
    // Obviously could be more efficient, but we pay the price
    // once up front, and it is reasonably fast up to 10,000,000 or so.
    // val biggestPrimeBelow = primes.takeWhile(maxNumberOfFeatures>).last
    // biggestPrimeBelow
    maxNumberOfFeatures
  }

  /**
   * Took the simple code for computing primes from:
   *   http://stackoverflow.com/questions/6802112/why-is-this-scala-prime-generation-so-slow-memory-intensive
   */
  private lazy val primes = 2 #:: sieve(3)

  private def sieve(n: Int): Stream[Int] =
    if (primes.takeWhile(p => p * p <= n).exists(n % _ == 0)) sieve(n + 2)
    else n #:: sieve(n + 2)
}

object HashedFeatureMap extends HashedFeatureMapCompanion {

  /**
   * Construct a HashedFeatureMap given the maximum number of features.
   * Originally we determined the actual mod value by finding the greatest
   * prime below the feature bound; now we use the feature bound directly
   * as the mod value.
   */
  def apply(maxNumberOfFeatures: Int) = {
    new HashedFeatureMap(actualMaxNumberOfFeatures(maxNumberOfFeatures))
  }
}
