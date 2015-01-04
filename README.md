lemkit
======

Utilities for training linear classifiers in Scala and Python. Models are output in a lightweight standardized format. Trained models can then be utilized in Scala, Python, or Go to classify unlabeled data.

### Setting up lemkit

Begin by cloning the repository and exporting the $LEMKIT environment variable.

    git clone https://github.com/peoplepattern/lemkit.git
    cd lemkit
    export LEMKIT=$(pwd)

### Build and install lemkit 

Build and install lemkit in the desired language. The following versions of the three languages are recommended:

Language      | Version                   | Base Dependencies |
--------------|---------------------------|-------------------|
Python        |2.7.x                      |setuptools         |
Scala         |2.10+                      |sbt                |
Go            |1.3.3                      |                   |

If the language and base dependencies have already been installed it is possible to build lemkit. To build lemkit in Python execute the following commands:

    cd $LEMKIT/python
    sudo python setup.py build
    sudo python setup.py install

To build in Scala execute the following commands:

    cd $LEMKIT/scala
    sbt update stage

To build in Go, make sure you have set up `$GOPATH` to point to the directory
of your Go workspace and use `go get` as follows:

   go get github.com/peoplepattern/lemkit/go/lkpredict

   This will automatically re-fetch the Go portion of the Lemkit repository
   as well as any dependencies, build both the `classify` library and
   `lkpredict` executable, and place them in `$GOPATH/bin` and
   `$GOPATH/pkg`.

If Lemkit is used only for prediciting using pre-trained linear models then there are no other dependencies. If Lemkit will be used for training linear models there are potentially a number of other dependencies.

### Setup Training in Python

The python component of lemkit requires Scikit-learn, Scipy, and Numpy for training models. Users are encouraged to utilize their preferred distribution method to install the packages.

Debian/Ubuntu

    sudo apt-get install python-sklearn

Mac OSX

    port install py27-scikit-learn

Or

    pip install -U numpy scipy scikit-learn

### Setup Training in Scala

Scala training has dependencies on LIBLINEAR and/or Vowpal Wabbit. To install,
either download and compile the executables, or use prebuilt packages.
Vowpal Wabbit support requires that the Vowpal executable `vw` be placed on
the PATH; LIBLINEAR support similarly requires that the LIBLINEAR executables
`train` and `predict` are on the PATH.

For Mac OSX, using MacPorts, prebuilt packages are available:

    sudo port install vowpal_wabbit
    sudo port install liblinear

Vowpal Wabbit can be downloaded [here](https://github.com/JohnLangford/vowpal_wabbit/wiki)

LIBLINEAR can be downloaded [here](http://www.csie.ntu.edu.tw/~cjlin/liblinear/)

### Setup Training in Go

 Go currently only supports predicting using pre-trained models, so no other dependencies exist.

### Running lemkit

Information on running lemkit can be found in the [wiki](https://github.com/peoplepattern/lemkit/wiki)

