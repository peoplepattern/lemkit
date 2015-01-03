#!/bin/sh

: ${LEMKIT:?"Need to set LEMKIT variable non-empty"}

if $LEMKIT/scala/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-expected-predictions.txt ; then
	cmp $LEMKIT/data/conform/iris-vw-expected-predictions.txt /tmp/iris-expected-predictions.txt
else
	printf "Scala is not built properly for predicting, skipping"

