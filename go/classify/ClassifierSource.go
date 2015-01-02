package classify

import (
	"bufio"
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"
)

/**
 * Read a file into a series of instances, each of which is an Example,
 * where the data in the instance is directly stored as a sequence of
 * FeatureObservations. The format of the file is lines like this:
 *
 * label feat:val feat:val ...
 *
 * The value can be omitted, and defaults to 1.0.
 */
func ReadDataFile(file string) ([]Example, error) {
	in, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	defer in.Close()
	return ReadDataReader(in)
}

/*
   Read an io.Reader into a series of instances, each of which is an
   Example, where the data in the instance is directly stored as a sequence
   of FeatureObservations. The format of the file is lines like this:

   label [importance] | feat:val feat:val ...

   The vertical bar must be present to separate label from features, and
   must have a space after it. This is to support future expansion.

   The value can be omitted, and defaults to 1.0.
*/
func ReadDataReader(in io.Reader) ([]Example, error) {
	examples := make([]Example, 0)
	bufin := bufio.NewReader(in)
	line, err := bufin.ReadString('\n')
	for err == nil {
		label_feats := strings.FieldsFunc(line, func(r rune) bool { return r == '|' })
		if len(label_feats) != 2 {
			return nil, fmt.Errorf("Should have one vertical bar separating label from features")
		}
		label_importance := strings.Fields(strings.TrimSpace(label_feats[0]))
		if len(label_importance) != 1 && len(label_importance) != 2 {
			return nil, fmt.Errorf("Should have either label alone or label + importance in label portion '%s'",
				label_feats[0])
		}
		label := label_importance[0]
		importance, has_importance := 0.0, false
		if len(label_importance) > 1 {
			val, valerr := strconv.ParseFloat(label_importance[1], 64)
			if valerr != nil {
				return nil, valerr
			}
			importance, has_importance = val, true
		}
		feats := strings.Fields(strings.TrimSpace(label_feats[1]))
		features := make([]FeatureObservation, len(feats))
		for _, field := range feats {
			featval := strings.FieldsFunc(field, func(r rune) bool { return r == ':' })
			if len(featval) != 1 && len(featval) != 2 {
				return nil, fmt.Errorf("Should have at most one colon in field %s", field)
			}
			if len(featval) == 1 {
				features = append(features, FeatureObservation{field, 1.0})
			} else {
				value, valerr := strconv.ParseFloat(featval[1], 64)
				if valerr != nil {
					return nil, valerr
				}
				features = append(features, FeatureObservation{featval[0], value})
			}
		}
		examples = append(examples, Example{features, label, importance, has_importance})
		line, err = bufin.ReadString('\n')
	}
	if err != io.EOF {
		return nil, err
	}
	return examples, nil
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
