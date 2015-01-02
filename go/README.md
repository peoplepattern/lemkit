commons-go
==========
Go language library code for basic text processing (e.g. tokenization)
and predicting using linear-classifier models.

# Getting Started

If you don't have a Go workspace hierarchy already, create a directory to hold
it (e.g. `$HOME/go`), point `GOPATH` to it, and add the `bin` directory to
your `PATH`:

```
mkdir $HOME/go
export GOPATH=$HOME/go
export PATH=$HOME/go/bin:$PATH
```

Then use `go get` to fetch the `commons-go` package and its dependencies:

```
go get github.com/peoplepattern/commons-go/...
```

This will download and build the package and its dependencies (including
`github.com/spaolacci/murmur3`). You should find a directory
`$GOPATH/bin` containing apps called `commons-predict` and `commons-tokenize`.

# Command-Line Apps

There are three command-line apps: `commons-acctoken`, `commons-tokenize`, and `commons-predict`.

## commons-acctoken

`commons-acctoken` displays the current accuracy the the tokenizer along many test cases. To run it do something like the following

```
$GOPATH/bin/commons-acctoken $GOPATH/src/github.com/peoplepattern/commons-go/commons-acctoken/tokens_testgroups.txt
...
Twitterspecific test_mention PASS
Basic test_whitespace PASS
SymbolicGrammar test_whatever PASS
SymbolicGrammar test_with FAIL
sandwich w / bread != sandwich w/ bread
SymbolicGrammar test_andor PASS
Acc: 49/55
```


## commons-tokenize

`commons-tokenize` takes a single argument, a file to tokenize. It outputs
tokenized text to stdout, with words separated by a space, one output line
per input line. For example, if a file `tweets.txt` contains the following:

```
Smart! Uber and Cheezburger deliver cat playtime: http://t.co/FFMbIkWo0O @thenextweb CC @allysquires @dflyonthefly it's our cat!
"65% of Time Spent on Social Networks Happens on Mobile" good read @mashable http://t.co/odT6b6KQse
#BigData market to top $18B in 2013, $50B by 2017. IBM recognized as Big Data share leader. Significant tailwinds for #IBMWatson to leverage
10 Steps to 9-figure exits... not sure about "Raise large $$". Rather use "Look after your team &amp; share the wealth" http://t.co/WVeUXl3x
"Why Donors stop". Good reminder on the importance of personalized engagement with donors. Data from 20,000 lapsed don…http://t.co/oCHNcjqL
```

And the following command is run:

```
commons-tokenize tweets.txt
```

Then the output is as follows:

```
Smart ! Uber and Cheezburger deliver cat playtime : http://t.co @thenextweb CC @allysquires @dflyonthefly it's our cat !
" 65% of Time Spent on Social Networks Happens on Mobile " good read @mashable http://t.co
#BigData market to top $18B in 2013 , $50B by 2017 . IBM recognized as Big Data share leader . Significant tailwinds for #IBMWatson to leverage
10 Steps to 9 - figure exits ... not sure about " Raise large $$ " . Rather use " Look after your team &amp; share the wealth " http://t.co
" Why Donors stop " . Good reminder on the importance of personalized engagement with donors . Data from 20,000 lapsed don ... http://t.co
```

Note that Twitter @-references and hashtags are not split, nor URL's,
ellipses, numbers with commas in them, monetary figures, emoticons, times, etc.

## commons-predict

`commons-predict` is used to do prediction on linear classifier models
trained using `lib-classify`. It takes arguments as follows:

Argument                     | Meaning
---------------------------- | -------------------------------------------
`--model-format | -f`        | Model format (json or binary, default binary)
`--predict | -p`             | File containing data instances to predict
`--model | -m`               | Trained model file
`--show-accuracy | -a`       | Output accuracy at end
`--show-correct | -c`        | Output column indicating correct or wrong""")

The arguments `--predict` (or `-p`) and `--model` (or `-m`) are required.

A basic invocation of `commons-predict` might be:

```
commons-predict -m vw.iris.exact.model.bin -p iris.data.test.txt
```

The file passed to `--predict` looks as follows:

```
Iris-setosa | sepal-length:4.7 sepal-width:3.2 petal-length:1.6 petal-width:0.2
Iris-versicolor | sepal-length:5.0 sepal-width:2.3 petal-length:3.3 petal-width:1.0
...
Iris-versicolor | sepal-length:6.0 sepal-width:2.7 petal-length:5.1 petal-width:1.6
...
```

That is, each line has one data instance, with the correct label followed by
a space-separated vertical bar and then the features, consisting of feature
name and value, separated by a colon. The colon and value can be omitted,
with the value defaulting to 1.0.

An optional importance weight can be specified after the label, e.g.

```
Iris-setosa 1.5 | sepal-length:4.7 sepal-width:3.2 petal-length:1.6 petal-width:0.2
Iris-versicolor 0.1 | sepal-length:5.0 sepal-width:2.3 petal-length:3.3 petal-width:1.0
...
Iris-versicolor 4 | sepal-length:6.0 sepal-width:2.7 petal-length:5.1 petal-width:1.6
...
```

This weights the data instance accordingly when training, and for integral
weights is similar to duplicating the data instance that many times.

The file passed to `--model` should be a binary-format or JSON-format model
file as created using `lib-classify`.

The predictions are sent to stdout, normally formatted as follows:

```
1 Iris-setosa Iris-setosa
2 Iris-versicolor Iris-versicolor
...
26 Iris-versicolor Iris-virginica
...
```

Each line consists of a line number, then the correct label, then the predicted
label.

If `--show-correct` (or `-c`) is used, a second column is added indicating
whether the prediction was correct or wrong.  If `--show-accuracy` (or `-a`)
is used, a line at the end is output showing overall accuracy. For example,
executing the following:

```
bin/commons-predict --model vw.iris.exact.model.bin \
  --predict iris.data.test.txt --show-accuracy --show-correct
```

Might produce output as follows:

```
1 CORRECT Iris-setosa Iris-setosa
2 CORRECT Iris-versicolor Iris-versicolor
...
26 WRONG Iris-versicolor Iris-virginica
...
30 CORRECT Iris-setosa Iris-setosa
Accuracy: 93.33%
```

# Library Functions

## Tokenization

Example code is as follows:

```
package main

import (
    "fmt"
    "github.com/peoplepattern/commons-go/tokenize"
)

func main() {
    tokens := tokenize.Tokenize(`10 Steps to 9-figure exits... not sure about "Raise large $$". Rather use "Look after your team &amp; share the wealth" http://t.co/WVeUXl3x`)
    fmt.Println(tokens)
}
```

Output is as follows:

```
[10 Steps to 9-figure exits ... not sure about " Raise large $$ " . Rather use " Look after your team &amp; share the wealth " http://t.co/WVeUXl3x]
```

## Classifier Prediction

This library assumes you have already trained a model using `lib-classify`.

`github.com/peoplepattern/commons-go/commons-predict/PredictApp` implements
`commons-predict` and shows how to do prediction.

Example code is as follows:

```
package main

import (
    "fmt"
    "github.com/peoplepattern/commons-go/classify"
)

func main() {
    classifier, err := classify.ReadBinaryModel("model.binary")
    if err != nil {
        fmt.Println(err)
        os.Exit(1)
    }

    predictData, err := classify.ReadDataFile("data.file")
    if err != nil {
        fmt.Println(err)
        os.Exit(1)
    }

    for i := 0; i < len(predictData); i++ {
        prediction, _ := classifier.Predict(predictData[i].Features)
        correct := predictData[i].Label
        fmt.Printf("%d %s %s\n", i+1, correct, prediction)
    }
}
```

Note that the second return value to `classifier.Predict` is a probability.
If you want the full set of scores for each possible label, use
`classifier.Scores` or `classifier.ScoresMap`. (These are methods of the
`LinearClassifier` struct.)

If you want to read a JSON model instead of a binary model, use
`classify.ReadJSONModel`.

