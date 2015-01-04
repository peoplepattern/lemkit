#!/bin/sh

: ${LEMKIT:?"Need to set LEMKIT variable non-empty"}

if $LEMKIT/scala/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "SCALA JSON \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Scala is not built properly for predicting, skipping \n"

if $LEMKIT/pyhon/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "PYTHON JSON \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Python is not built properly for predicting, skipping \n"

if $GOPATH/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "GO JSON \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Go is not built properly for predicting, skipping \n"

if $LEMKIT/scala/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "SCALA BINARY \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Scala is not built properly for predicting, skipping \n"

if $LEMKIT/pyhon/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "PYTHON BINARY \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Python is not built properly for predicting, skipping \n"

if $GOPATH/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
	printf "GO BINARY \n"
	cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
else
	printf "Go is not built properly for predicting, skipping \n"