lemkit
======

Utilities for training linear classifiers in Scala and Python. Models are output in a lightweight standardized format. Trained models can then be utilized in Scala, Python, or Go to classify unlabeled data.

### Setting up Lemkit

Begin by cloning the repository and exporting the $LEMKIT environment variable.

    git clone https://github.com/peoplepattern/lemkit.git
    cd lemkit
    export LEMKIT=$(pwd)

### Build and install lemkit 

Build and install lemkit in the desired language and mode. To build Lemkit in Python and Scala for training and predicting execute this command:

   $LEMKIT/build.sh -l python,scala -m train,predict
