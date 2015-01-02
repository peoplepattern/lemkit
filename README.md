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
python        |2.7.x                      |setuptools         |
scala         |2.10+                      |                   |
go            |?                          |                   |  

If the language and base dependencies have already been installed it is possible to build lemkit. To build lemkit in python and scala one would execute build.sh as such:

    $LEMKIT/build.sh -l python,scala

Argument      | options                   |
--------------|---------------------------|
-l (languages)|scala,python,go (1 or more)|


If Lemkit is used only for prediciting using pre-trained linear models then there are no other major dependencies. If Lemkit will be used for training linear models there are potentially a number of other dependencies.

### Setup Training in Python

The python component of lemkit requires Scikit-learn, Scipy, and Numpy for training models. Users are encouraged to utilize their preferred distribution method to install the packages.

Debain/Ubuntu

    sudo apt-get install python-sklearn

Mac OSX

	port install py27-scikit-learn

Or

	pip install -U numpy scipy scikit-learn

### Setup Training in Scala

Scala training has dependencies on LIBLINEAR and/or Vowpal Wabbit.

### Setup Training in Go

 Go currently only supports predicting using pre-trained models, so no other dependencies exist.

### Running lemkit

Information on running lemkit can be found in the [wiki](https://github.com/peoplepattern/lemkit/wiki)

