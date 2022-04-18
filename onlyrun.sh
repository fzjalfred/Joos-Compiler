#!/bin/bash

cd output
FILES=($(ls *.s))

for file in "${FILES[@]}"
do
  /u/cs444/bin/nasm -O1 -f elf -g -F dwarf $file
  done
ld -melf_i386 -o main *.o
./main
echo $?
