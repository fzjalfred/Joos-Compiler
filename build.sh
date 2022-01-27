#!/bin/bash

rm -rf build
mkdir build
#compile
java -cp ./lib/jflex-1.6.1.jar jflex.Main -d ./src/lexer ./src/flex/lcalc.flex;
java -cp ./lib/java-cup-11b.jar java_cup.Main -destdir ./src/lexer < ./src/flex/joos.cup;
javac -d ./build -cp ./lib/java-cup-11b.jar  ./src/lexer/*.java;

#run
cd build
java -cp .:../lib/java-cup-11b-runtime.jar Main ../test/allreqs.in;

#clean
# rm Lexer.java;
# rm parser.java;
# rm sym.java;
# rm *.class;

