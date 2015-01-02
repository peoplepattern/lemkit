package com.peoplepattern

import classify.data.FeatureObservation

package object classify {
  type FeatureSet[F] = Seq[FeatureObservation[F]]
}
