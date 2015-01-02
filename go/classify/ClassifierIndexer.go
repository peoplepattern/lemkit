package classify

type ClassifierIndexer struct {
	lmap map[string]int
	fmap FeatureMap
}

/* Condense combines multiple instances of the same feature given a sequence
   of feature observations (a feature and its magnitude).

   E.g. Condense([]FeatureObservation{{"foo",1.0},{"bar",1.0},{"foo",2.0})

   becomes

   []FeatureObservation{{"bar",1.0},{"foo",3.0}}
*/
func Condense(feats []IntFeatureObservation) []IntFeatureObservation {
	featmap := make(map[int]float64, 0)
	for _, feat := range feats {
		featmap[feat.Feature] += feat.Magnitude
	}
	retval := make([]IntFeatureObservation, 0)
	for index, magnitude := range featmap {
		retval = append(retval, IntFeatureObservation{index, magnitude})
	}
	return retval
}

func (i *ClassifierIndexer) Index(feats []FeatureObservation) []IntFeatureObservation {
	intfeats := make([]IntFeatureObservation, 0)
	addfeat := func(feat FeatureObservation) {
		if index := i.fmap.IndexOfFeature(feat.Feature); index >= 0 {
			intfeats = append(intfeats, IntFeatureObservation{index, feat.Magnitude})
		}
	}
	addfeat(FeatureObservation{"", 1.0})
	for _, feat := range feats {
		addfeat(feat)
	}
	return Condense(intfeats)
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
