#!/bin/sh

while getopts ":l:m:" opt; do
  case $opt in
    l) lang="$OPTARG"
    ;;
    m) mode="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

: ${LEMKIT:?"Need to set LEMKIT non-empty"}

printf "Argument lang is %s\n" "$lang"
printf "Argument mode is %s\n" "$mode"
printf "LEMKIT environment variable set to $LEMKIT \n"

#if "$lang" == "python"
#then
#	if "$mode" == "predict"
#	then
#		printf "Building Python Lemkit in predict only mode"
#		sudo python python/predict/setup.py build
#		sudo python $LEMKIT/python/predict/setup.py install
#	elif "$mode" == "train"
#	then
#		printf "Building Python Lemkit in train only mode"
#		sudo python python/train/setup.py build
#		sudo python $LEMKIT/python/train/setup.py install
#elif "$lang" == "scala"
#then
#	if "$mode" == "predict"
#	then
#		printf "Building Scala Lemkit in predict only mode"

case "$lang" in
  *scala*)
   printf "Building in Scala \n"
   # code when lang has scala
  ;;
esac

case "$lang" in
	*python*)
	 printf "Building in Python \n"
	 cd $LEMKIT/python/predict
	 sudo python setup.py build
	 sudo python setup.py install
	 cd $LEMKIT
	 # code when lang has scala
	 ;;
esac
		