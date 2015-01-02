package classify

type FeatureObservation struct {
	Feature   string
	Magnitude float64
}

type IntFeatureObservation struct {
	Feature   int
	Magnitude float64
}

type Example struct {
	Features      []FeatureObservation
	Label         string
	Importance    float64
	HasImportance bool
}

type IntExample struct {
	Features      []IntFeatureObservation
	Label         int
	Importance    float64
	HasImportance bool
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
