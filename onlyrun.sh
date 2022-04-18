#!/bin/bash

cd output
FILES=($(ls *.s))
cp ../stdlib/5.0/runtime.s .
for file in "${FILES[@]}"
do
  echo /u/cs444/bin/nasm -O1 -f elf -g -F dwarf $file
  /u/cs444/bin/nasm -O1 -f elf -g -F dwarf $file
  done
ld -melf_i386 -o main *.o
./main
echo $?
