#!/bin/bash

# This is a test script to run all test cases

function usage() {
    echo "Usage: test.sh ./run.sh /path/to/joos"
    echo "Need to execute at project root"
    exit 1
}

function print_result() {
    echo -e "for test $1 our compiler returns: \c"
}

# check number of arguments correct or not
if [ $# -ne 1 ]; then
    usage
fi

EXE=$1
PREF="test/assignment_testcases/a5"
DIRTESTCASES=($(ls -l "${PREF}"| grep ^d | awk '{print $9}'))
TESTCASES=($(ls -l "${PREF}"| grep ^- | awk '{print $9}'))
#STDLIBSRC=$(find stdlib/2.0/java/ -name *.java )

echo "JOOS is ${EXE}"

for dir in "${TESTCASES[@]}"
do
  testDir=${PREF}/${dir}
  #files=$(find $z -name *.java)
  returnCode=$? #extract return code
  print_result $testDir $returnCode
  ./${EXE} $testDir  #execute compiler
done

for dir in "${DIRTESTCASES[@]}"
do
  MainDir=${PREF}/$dir/Main.java
  testDir=$(find "${PREF}/$dir" -name *.java |grep -v "$MainDir")
#   echo $MainDir
#   echo $testDir
  #files=$(find $z -name *.java)
  returnCode=$? #extract return code
  print_result $testDir
  ./${EXE} $MainDir $testDir  #execute compiler
done