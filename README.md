lemkit
======

Utilities for training linear classifiers in Scala and Python. Models are output in a lightweight standardized format. Trained models can then be utilized in Scala, Python, or Go to classify unlabeled data.

### Setting up lemkit

Begin by cloning the repository and exporting the $LEMKIT environment variable.

    git clone https://github.com/peoplepattern/lemkit.git
    cd lemkit
    export LEMKIT=$(pwd)

### Build and install lemkit 

Build and install lemkit in the desired language and mode. To build lemkit in Python and Scala for training and predicting execute this command:

    $LEMKIT/build.sh -l python,scala -m train,predict

Argument      | options                   |
--------------|---------------------------|
-l (languages)|scala,python,go (1 or more)|
-m (modes)    |predict,train (1 or more)  |

Building lemkit with train mode specified creates a number of dependencies. For Python this creates a dependency on Numpy, Scipy, and Scikit-learn. For Scala this creates dependencies on LIBLINEAR and/or Vowpal Wabbit. 

Building lemkit only in predict mode has no dependencies.