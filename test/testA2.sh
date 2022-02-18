#!/bin/bash

# This is a test script to run all test cases

function usage() {
    echo "Usage: bash test.sh /path/to/joos"
    echo "Need to execute at project root"
    exit 1
}

function print_result() {
    echo "for test $1 our compiler returns: $2"
}

# check number of arguments correct or not
if [ $# -ne 1 ]; then
    usage
fi

EXE=$1
ASSIGNMENT=$2
PREF="test/assignment_testcases/a2"
TESTCASES=($(ls "${PREF}"))
STDLIBSRC=$(find stdlib/2.0/java/ -name *.java )

echo "JOOS is ${EXE}"

for dir in "${TESTCASES[@]}"
do
  testDir=${PREF}/${dir}
  files=$(find $testDir -name *.java)
  ./${EXE} $STDLIBSRC $files  #execute compiler
  returnCode=$? #extract return code
  print_result $dir $returnCode
done