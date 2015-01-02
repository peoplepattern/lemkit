package classify

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io"
	"math"
	"os"
)

type LinearClassifier struct {
	FeaturizingClassifier
	Parameters [][]float64
}

func (c *LinearClassifier) IntRawScores(feats []IntFeatureObservation) []float64 {
	numClasses := len(c.indexer.lmap)
	scores := make([]float64, numClasses)
	for _, fobs := range feats {
		for class := 0; class < numClasses; class++ {
			scores[class] += c.Parameters[class][fobs.Feature] * fobs.Magnitude
		}
	}
	return scores
}

func (c *LinearClassifier) RawScores(feats []FeatureObservation) []float64 {
	return c.IntRawScores(c.indexer.Index(feats))
}

func (c *LinearClassifier) Scores(feats []FeatureObservation) []float64 {
	scores := c.RawScores(feats)
	for i := range scores {
		scores[i] = math.Exp(scores[i])
	}
	sum := 0.0
	for i := range scores {
		sum += scores[i]
	}
	if sum != 0.0 {
		for i := range scores {
			scores[i] /= sum
		}
	}
	return scores
}

func (c *LinearClassifier) ScoresMap(feats []FeatureObservation) map[string]float64 {
	labels := c.Labels()
	scores := c.Scores(feats)
	if len(labels) != len(scores) {
		panic(fmt.Sprintf("wrong-length sequences: len(labels)=%s, len(scores)=%s",
			len(labels), len(scores)))
	}
	retval := make(map[string]float64, 0)
	for i := range labels {
		retval[labels[i]] = scores[i]
	}
	return retval
}

func (c *LinearClassifier) Predict(feats []FeatureObservation) (label string, score float64) {
	labels := c.Labels()
	scores := c.Scores(feats)
	if len(labels) != len(scores) {
		panic(fmt.Sprintf("wrong-length sequences: len(labels)=%s, len(scores)=%s",
			len(labels), len(scores)))
	}
	label = labels[0]
	score = scores[0]
	for i := 1; i < len(labels); i++ {
		if scores[i] > score {
			score = scores[i]
			label = labels[i]
		}
	}
	return
}

/**
 * General utilities for dealing with linear classifiers.
 */
const (
	magicNumber       = 0x6A48B9DD
	majorVersion      = 1
	minorVersion      = 0
	labelID           = 100
	featureTypeID     = 110
	featureTypeExact  = 1
	featureTypeHashed = 2
	featureFeaturesID = 111
	featureMaxFeatsID = 112
	weightsTypeID     = 120
	weightsTypeDense  = 1
	weightsTypeSparse = 2
	weightsValuesID   = 121
)

type JSONClassifier struct {
	Labels   map[string]int
	Features struct {
		Type     string
		Features map[string]int
		MaxFeats int
	}
	Weights struct {
		Type   string
		Values [][]float64
	}
}

func ReadJSONModel(file string) (*LinearClassifier, error) {
	in, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	return ReadJSONModelReader(in)
}

func ReadJSONModelReader(in io.Reader) (*LinearClassifier, error) {
	dec := json.NewDecoder(in)
	var v JSONClassifier
	if err := dec.Decode(&v); err != nil {
		return nil, err
	}

	if v.Labels == nil {
		return nil, fmt.Errorf("Missing labels in JSON")
	}

	var fmap FeatureMap
	if v.Features.Type == "exact" {
		if v.Features.Features == nil {
			return nil, fmt.Errorf("Missing features -> features in JSON")
		}
		fmap = NewExactFeatureMap(v.Features.Features)
	} else if v.Features.Type == "hashed" {
		if v.Features.MaxFeats <= 0 {
			return nil, fmt.Errorf("Missing or invalid maxfeats in JSON: %s",
				v.Features.MaxFeats)
		}
		fmap = NewHashedFeatureMap(v.Features.MaxFeats)
	} else {
		return nil, fmt.Errorf("Missing or invalid features -> type in JSON: %s",
			v.Features.Type)
	}

	if v.Weights.Type != "dense" {
		return nil, fmt.Errorf("Missing or invalid weights -> type in JSON: %s",
			v.Weights.Type)
	}

	return &LinearClassifier{FeaturizingClassifier{&ClassifierIndexer{v.Labels, fmap}}, v.Weights.Values}, nil
}

func writeInt(out io.Writer, value int) error {
	v := int32(value)
	return binary.Write(out, binary.BigEndian, &v)
}

func writeShort(out io.Writer, value int16) error {
	return binary.Write(out, binary.BigEndian, &value)
}

func writeString(out io.Writer, str string) error {
	err := writeInt(out, len(str))
	if err != nil {
		return err
	}
	_, err = io.WriteString(out, str)
	return err
}

func writeStringSeq(out io.Writer, seq []string) error {
	l := len(seq)
	err := writeInt(out, l)
	if err != nil {
		return err
	}
	for i := 0; i < l; i++ {
		err = writeString(out, seq[i])
		if err != nil {
			return err
		}
	}
	return nil
}

func writeTable(out io.Writer, table map[string]int) error {
	l := len(table)
	err := writeInt(out, l)
	if err != nil {
		return err
	}
	for key := range table {
		value := table[key]
		err = writeString(out, key)
		if err != nil {
			return err
		}
		err = writeInt(out, value)
		if err != nil {
			return err
		}
	}
	return nil
}

func readInt(in io.Reader) (int, error) {
	var value int32
	err := binary.Read(in, binary.BigEndian, &value)
	return int(value), err
}

func readShort(in io.Reader) (int16, error) {
	var value int16
	err := binary.Read(in, binary.BigEndian, &value)
	return value, err
}

func readFloat64(in io.Reader) (float64, error) {
	var value float64
	err := binary.Read(in, binary.BigEndian, &value)
	return value, err
}

func readString(in io.Reader) (string, error) {
	len, err := readInt(in)
	if err != nil {
		return "", err
	}
	bytes := make([]byte, len)
	if _, err := io.ReadFull(in, bytes); err != nil {
		return "", err
	}
	return string(bytes), nil
}

func readStringSeq(in io.Reader) ([]string, error) {
	len, err := readInt(in)
	if err != nil {
		return nil, err
	}
	ret := make([]string, len)
	for i := 0; i < len; i++ {
		str, err := readString(in)
		if err != nil {
			return nil, err
		}
		ret[i] = str
	}
	return ret, nil
}

func readTable(in io.Reader) (map[string]int, error) {
	len, err := readInt(in)
	if err != nil {
		return nil, err
	}
	ret := make(map[string]int, len)
	for i := 0; i < len; i++ {
		key, err := readString(in)
		if err != nil {
			return nil, err
		}
		value, err := readInt(in)
		if err != nil {
			return nil, err
		}
		ret[key] = value
	}
	return ret, nil
}

func checkInt(in io.Reader, value int) error {
	checked, err := readInt(in)
	if err != nil {
		return err
	}
	if checked != value {
		return fmt.Errorf("Invalid binary format; expected int %s but saw %s",
			value, checked)
	}
	return nil
}

func checkShort(in io.Reader, value int16) error {
	checked, err := readShort(in)
	if err != nil {
		return err
	}
	if checked != value {
		return fmt.Errorf("Invalid binary format; expected short %s but saw %s",
			value, checked)
	}
	return nil
}

func ReadBinaryModel(file string) (*LinearClassifier, error) {
	in, err := os.Open(file)
	if err != nil {
		return nil, err
	}
	return ReadBinaryModelReader(in)
}

func ReadBinaryModelReader(in io.Reader) (lc *LinearClassifier, err error) {
	err = checkInt(in, magicNumber)
	if err != nil {
		return
	}
	err = checkShort(in, majorVersion)
	if err != nil {
		return
	}
	err = checkShort(in, minorVersion)
	if err != nil {
		return
	}
	err = checkShort(in, labelID)
	if err != nil {
		return
	}
	var lmap map[string]int
	lmap, err = readTable(in)
	if err != nil {
		return
	}
	err = checkShort(in, featureTypeID)
	if err != nil {
		return
	}
	var featureType int16
	featureType, err = readShort(in)
	if err != nil {
		return
	}
	var fmap FeatureMap
	if featureType == featureTypeExact {
		err = checkShort(in, featureFeaturesID)
		if err != nil {
			return
		}
		var seq []string
		seq, err = readStringSeq(in)
		if err != nil {
			return
		}
		exactmap := make(map[string]int, len(seq))
		for i := 0; i < len(seq); i++ {
			exactmap[seq[i]] = i
		}
		fmap = NewExactFeatureMap(exactmap)
	} else if featureType == featureTypeHashed {
		err = checkShort(in, featureMaxFeatsID)
		if err != nil {
			return
		}
		var maxNumFeatures int
		maxNumFeatures, err = readInt(in)
		if err != nil {
			return
		}
		fmap = NewHashedFeatureMap(maxNumFeatures)
	} else {
		err = fmt.Errorf("Invalid binary format; unknown feature map type %s",
			featureType)
		return
	}
	err = checkShort(in, weightsTypeID)
	if err != nil {
		return
	}
	// FIXME: Implement sparse weights
	err = checkShort(in, weightsTypeDense)
	if err != nil {
		return
	}
	err = checkShort(in, weightsValuesID)
	if err != nil {
		return
	}
	var numClasses int
	numClasses, err = readInt(in)
	if err != nil {
		return
	}
	parameters := make([][]float64, numClasses)
	for i := 0; i < numClasses; i++ {
		var numWeights int
		numWeights, err = readInt(in)
		if err != nil {
			return
		}
		oneparams := make([]float64, numWeights)
		for j := 0; j < numWeights; j++ {
			var weight float64
			weight, err = readFloat64(in)
			if err != nil {
				return
			}
			oneparams[j] = weight
		}
		parameters[i] = oneparams
	}

	lc = &LinearClassifier{FeaturizingClassifier{&ClassifierIndexer{lmap, fmap}}, parameters}
	return
}

//  def writeJSONModel[I](classifier: LinearClassifier[I], file: String) = {
//    val features = classifier.indexer.fmap match {
//      case f: ExactFeatureMap =>
//        ("type" -> "exact") ~ ("features" -> f.fmap)
//      case f: HashedFeatureMap =>
//        ("type" -> "hashed") ~ ("maxfeats" -> f.maxNumberOfFeatures)
//    }
//    val seqparams = classifier.parameters.map(_.toIndexedSeq).toIndexedSeq
//    val json =
//      ("labels" -> classifier.indexer.lmap) ~ ("features" -> features) ~
//        ("weights" ->
//          ("type" -> "dense") ~ ("values" -> seqparams)
//        )
//    val rendered = liftjson.pretty(liftjson.render(json))
//    val out = new BufferedWriter(new FileWriter(new File(file)))
//    try {
//      out.write(rendered + "\n")
//    } finally {
//      out.close()
//    }
//  }
//
//  def writeBinaryModel[I](classifier: LinearClassifier[I], file: String) {
//    val out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
//    try {
//      writeBinaryModel(classifier, out)
//    } finally {
//      out.close()
//    }
//  }
//
//  def writeBinaryModel[I](classifier: LinearClassifier[I], out: DataOutputStream) {
//    out.writeInt(magicNumber)
//    out.writeShort(majorVersion)
//    out.writeShort(minorVersion)
//    out.writeShort(labelID)
//    writeTable(out, classifier.indexer.lmap)
//    out.writeShort(featureTypeID)
//    classifier.indexer.fmap match {
//      case f: ExactFeatureMap => {
//        out.writeShort(featureTypeExact)
//        out.writeShort(featureFeaturesID)
//        writeStringSeq(out, f.fmap.toSeq.sortBy(_._2).unzip._1)
//      }
//      case f: HashedFeatureMap => {
//        out.writeShort(featureTypeHashed)
//        out.writeShort(featureMaxFeatsID)
//        out.writeInt(f.maxNumberOfFeatures)
//      }
//    }
//    out.writeShort(weightsTypeID)
//    out.writeShort(weightsTypeDense)
//    out.writeShort(weightsValuesID)
//    out.writeInt(classifier.parameters.length)
//    for (oneparams <- classifier.parameters) {
//      out.writeInt(oneparams.length)
//      for (param <- oneparams) {
//        out.writeDouble(param)
//      }
//    }
//  }

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
