/**
 * Java framework for linear classification
 *
 * <p>
 * This library supports the core data structures for execution of linear classifier (machine
 * learning) models. It does <i>not</i> provide any support for training of such models, that is
 * delegated to other libraries. However, linear models trained by other frameworks can be easily
 * converted into the data structures defined here. Moreover, this library supports cross-language
 * support for linear classifiers, and provides the reference implementation of both JSON and binary
 * encoding of linear models for use in other applications. For example, a model can be trained
 * using scikit-learn in Python, saved into the cross language binary format there, the read into
 * this library for use in JVM-oriented compute environments, such as Hadoop.
 *
 * <p>
 * See <a target=1 href=https://github.com/peoplepattern/lemkit/tree/master/python>lemkit/python</a>
 * for details on Python support.
 *
 * <p>
 * The core class here is a {@link com.peoplepattern.classify.core.Classifier}, which is basically a
 * function (a {@link com.peoplepattern.classify.core.Predictor}) to a
 * {@link com.peoplepattern.classify.core.Classification}. Also, classifiers provide a set of string
 * labels -- the classes being predicted.
 *
 * <p>
 * Several flavors of classifier are defined:
 * <ul>
 * <li>A {@link com.peoplepattern.classify.core.GeneralClassifier} classifies general objects. It
 * does so with the aid of a {@link com.peoplepattern.classify.core.FeatureFunction}, which maps
 * objects into {@link com.peoplepattern.classify.core.FeatureBundle}s. These may be thought of a
 * maps from string-valued feature keys to double values (1.0/0.0 for binary features); and a
 * {@link com.peoplepattern.classify.core.FeatureMap} which aids in converting feature bundles into
 * {@link com.peoplepattern.classify.core.Datum}, which minimally wrap the feature vector used by
 * the underlying classifier.
 * <li>A {@link com.peoplepattern.classify.core.PortableLinearClassifier} is not a general as a
 * GeneralClassifier, but basically classifies {@link com.peoplepattern.classify.core.FeatureBundle}
 * directly without the aid of a feature function. All feature bundles are assumed to be generated
 * by <i>some</i> feature function, possibly a trivial one, so all classifiers carry around the
 * signature of feature function used for classification. This class is "portable" in that it can be
 * translated (along with its feature map) into cross-language JSON and binary formats, compatible
 * with the other lemkit projects.
 * <li>Finally, a {@link com.peoplepattern.classify.core.LinearClassifier} is a small implementation
 * of a linear classifier, and operates directly on {@link com.peoplepattern.classify.core.Datum}.
 * </ul>
 *
 * <p>
 * In addition, this library provides a very small pure-Java implemetation of vectors (
 * {@link com.peoplepattern.classify.core.Vec}), with "just enough" implementation to support linear
 * classification. Support for dense and sparse vectors is provided.
 */
package com.peoplepattern.classify.core;

