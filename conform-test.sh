#!/bin/sh

export LEMKIT=`dirname $0`

printf "LEMKIT environment variable set to $LEMKIT \n"

while getopts ":l:" opt; do
  case $opt in
    l) lang="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

case "$lang" in
  *scala*)
	if $LEMKIT/jvm/lemkit-model/target/universal/stage/bin/lemkit-model -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
		printf "SCALA JSON \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Scala is not built properly for predicting, skipping \n"
	fi

	if $LEMKIT/jvm/lemkit-model/target/universal/stage/bin/lemkit-model -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f binary > /tmp/iris-predictions.txt ; then
		printf "SCALA BINARY \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Scala is not built properly for predicting, skipping \n"
	fi
  ;;
esac

case "$lang" in
  *python*)
	if $LEMKIT/python/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
		printf "PYTHON JSON \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Python is not built properly for predicting, skipping \n"
	fi

	if $LEMKIT/python/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f binary > /tmp/iris-predictions.txt ; then
		printf "PYTHON BINARY \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Python is not built properly for predicting, skipping \n"
	fi
  ;;
esac

case "$lang" in
  *go*)
	if $GOPATH/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.json -p $LEMKIT/data/iris/iris.test.txt -f json > /tmp/iris-predictions.txt ; then
		printf "GO JSON \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Go is not built properly for predicting, skipping \n"
	fi

	if $GOPATH/bin/lkpredict -m $LEMKIT/data/model_files/iris.vw.model.bin -p $LEMKIT/data/iris/iris.test.txt -f binary > /tmp/iris-predictions.txt ; then
		printf "GO BINARY \n"
		cmp $LEMKIT/data/conform/iris-expected-predictions.txt /tmp/iris-predictions.txt
	else
		printf "Go is not built properly for predicting, skipping \n"
	fi
  ;;
esac
