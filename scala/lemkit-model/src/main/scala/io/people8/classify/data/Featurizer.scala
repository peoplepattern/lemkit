package io.people8.classify.data

/**
 * A function that converts objects of some input class into a sequence
 * of FeatureObservations for an output class O.
 *
 * For text classification, I and O will typically be String. E.g. we
 * convert an entire document into the counts of all the words that
 * occur in it (see BowFeaturizer).
 */
trait Featurizer[I, O] extends (I => Seq[FeatureObservation[O]]) with Serializable
