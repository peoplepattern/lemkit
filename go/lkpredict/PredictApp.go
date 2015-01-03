package main

import (
	"flag"
	"fmt"
	"github.com/peoplepattern/commons-go/classify"
	"os"
)

func init() {
	flag.CommandLine = flag.NewFlagSet(os.Args[0], flag.ExitOnError)
}

var modelFormat = flag.String("model-format", "binary", "Model format (json or binary, default binary)")
var modelFile = flag.String("model", "", "Trained model file")
var predictFile = flag.String("predict", "", "File containing data instances to predict")
var showAccuracy = flag.Bool("show-accuracy", false, "Output accuracy at end")
var showCorrect = flag.Bool("show-correct", false, "Output column indicating correct or wrong")

func init() {
	flag.StringVar(modelFormat, "f", "binary", "Model format (json or binary, default binary)")
	flag.StringVar(modelFile, "m", "", "Trained model file")
	flag.StringVar(predictFile, "p", "", "File containing data instances to predict")
	flag.BoolVar(showAccuracy, "a", false, "Output accuracy at end")
	flag.BoolVar(showCorrect, "c", false, "Output column indicating correct or wrong")
}

func main() {
	flag.Parse()
	if *modelFormat != "json" && *modelFormat != "binary" {
		fmt.Printf("Invalid value for -model-format: %s\n", *modelFormat)
		os.Exit(1)
	}
	if *modelFile == "" {
		fmt.Println("Must specify -model (or -m)")
		os.Exit(1)
	}
	if *predictFile == "" {
		fmt.Println("Must specify -predict (or -p)")
		os.Exit(1)
	}

	var classifier *classify.LinearClassifier
	var err error
	if *modelFormat == "json" {
		classifier, err = classify.ReadJSONModel(*modelFile)
	} else {
		classifier, err = classify.ReadBinaryModel(*modelFile)
	}
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	predictData, err := classify.ReadDataFile(*predictFile)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	numinsts := len(predictData)
	predictions := make([]string, numinsts)
	for i := 0; i < numinsts; i++ {
		predictions[i], _ = classifier.Predict(predictData[i].Features)
	}
	numcorrect := 0
	for i := 0; i < numinsts; i++ {
		correct := predictData[i].Label
		prediction := predictions[i]
		isCorrect := correct == prediction
		correctString := ""
		if *showCorrect {
			if isCorrect {
				correctString = " CORRECT"
			} else {
				correctString = " WRONG"
			}
		}
		fmt.Printf("%d%s %s %s\n", i+1, correctString, correct, prediction)
		if isCorrect {
			numcorrect++
		}
	}
	if *showAccuracy {
		fmt.Printf("Accuracy: %.2f%%\n", float64(numcorrect)*100.0/float64(numinsts))
	}
}

// For Vim, so we get 4-space tabs
// vim: set ts=4 sw=4 noet:
