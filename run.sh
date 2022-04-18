#!/bin/bash
FILES=($(ls output/*.java.s))
for file in "${FILES[@]}"
do
  rm $file
done
FILES=($(ls output/*.java.o))
for file in "${FILES[@]}"
do
  rm $file
done

./joosc $* $(find stdlib/2.0/java/ -name *.java )
cd output
FILES=($(ls *.s))

cp ../stdlib/5.0/runtime.s .
for file in "${FILES[@]}"
do
  /u/cs444/bin/nasm -O1 -f elf -g -F dwarf $file
  done
ld -melf_i386 -o main *.o
./main
echo $?
