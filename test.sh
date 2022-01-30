#!/bin/bash

# This is a test script to run all test cases

function usage() {
    echo "Usage: bash test.sh /path/to/joos #Assignment(a1,a2...)"
    exit 1
}

function print_result() {
    echo "for file: $1, our parser gives a $2"
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
    echo "processing file ${f}"
    ./${EXE} "${PREF}/${ASSIGNMENT}/${f}" 1
    if [ $? -eq 0 ]; then
      print_result $f "SUCCESS"
    else
      print_result $f "FAILED"
    fi
    echo "   "
done


