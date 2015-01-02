package classify

import "github.com/spaolacci/murmur3"

type FeatureMap interface {
	IndexOfFeature(feature string) int
}

type HashedFeatureMap struct {
	MaxNumFeatures int
}

func NewHashedFeatureMap(maxNumFeatures int) *HashedFeatureMap {
	return &HashedFeatureMap{maxNumFeatures}
}

func (m *HashedFeatureMap) IndexOfFeature(feature string) int {
	hashval := murmur3.Sum32([]byte(feature))
	return int(((int32(hashval) % int32(m.MaxNumFeatures)) + int32(m.MaxNumFeatures)) % int32(m.MaxNumFeatures))
}

type ExactFeatureMap struct {
	FMap map[string]int
}

func NewExactFeatureMap(fmap map[string]int) *ExactFeatureMap {
	return &ExactFeatureMap{fmap}
}

func (m *ExactFeatureMap) IndexOfFeature(feature string) int {
	val, ok := m.FMap[feature]
	if !ok {
		return -1
	}
	return val
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
