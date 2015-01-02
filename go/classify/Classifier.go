package classify

type Classifier interface {
	EvalRaw(x []FeatureObservation) []float64
	Labels() []string
	Scores(x []FeatureObservation) map[string]float64
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
