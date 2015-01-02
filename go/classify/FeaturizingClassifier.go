package classify

type FeaturizingClassifier struct {
	indexer *ClassifierIndexer
}

func (c *FeaturizingClassifier) Labels() []string {
	keys := make([]string, len(c.indexer.lmap))
	for k, v := range c.indexer.lmap {
		keys[v] = k
	}
	return keys
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
