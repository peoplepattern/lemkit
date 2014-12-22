package com.peoplepattern.classify

/**
 * The options for hashing for a classifier.
 *
 *   hashtrick: Whether to use feature hashing, with specified maximum number
 *      of features (i.e. number of features to hash into).
 */
case class HashingOptions(var hashtrick: Option[Int] = None)
