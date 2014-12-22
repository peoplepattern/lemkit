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

: ${LEMKIT:?"Need to set LEMKIT variable non-empty"}

printf "Argument lang is %s\n" "$lang"
printf "Argument mode is %s\n" "$mode"
printf "LEMKIT environment variable set to $LEMKIT \n"

case "$lang" in
  *scala*)
   printf "Building in Scala \n"
   # code when lang has scala
  ;;
esac

case "$lang" in
	*python*)
	 printf "Building in Python \n"
	 case "$mode" in
	 	*predict*)
		 printf "Building Predict mode \n"
		 cd $LEMKIT/python/predict
		 sudo python setup.py build
		 sudo python setup.py install
		 cd $LEMKIT
		;;
	 esac
	 case "$mode" in
	 	*train*)
		 printf "Building Train mode \n"
		 cd $LEMKIT/python/train
		 sudo python setup.py build
		 sudo python setup.py install
		 cd $LEMKIT
		;;
	 esac
	;;
esac
		