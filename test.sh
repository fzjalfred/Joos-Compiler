#!/bin/bash

# This is a test script to run all test cases

function usage() {
    echo "Usage: bash test.sh /path/to/joos #Assignment(a1,a2...)"
    exit 1
}

function print_result() {
    echo "$1: $2"
}

# check number of arguments correct or not
if [ $# -ne 2 ]; then
    usage
fi

EXE=$1
ASSIGNMENT=$2
PREF="test/assignment_testcases"
TESTCASES=($(ls "${PREF}/${ASSIGNMENT}"))


echo "JOOS is ${EXE}"
echo "Assignment number ${ASSIGNMENT}"

for f in "${TESTCASES[@]}"
do
    if [[ $f == J1* ]]; then
      ./${EXE} "${PREF}/${ASSIGNMENT}/${f}"
      if [[ $? -ne 0 ]]; then
        print_result $f "FAILED! Should return 0."
      fi
    elif [[ $f == Je* ]]; then
      ./${EXE} "${PREF}/${ASSIGNMENT}/${f}"
      if [[ $? -ne 42 ]]; then
        print_result $f "FAILED! Should return 42. "
      fi
    fi
done


