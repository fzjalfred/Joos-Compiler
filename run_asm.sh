#!/bin/bash
FILES=($(ls output/*.s))
for file in "${FILES[@]}"
do
  rm $file
done
FILES=($(ls output/*.o))
for file in "${FILES[@]}"
do
  rm $file
done

./joosc $* $(find stdlib/5.0/java/ -name *.java )
cd output
cp ../stdlib/5.0/runtime.s .
FILES=($(ls *.s))


for file in "${FILES[@]}"
do
  /u/cs444/bin/nasm -O1 -f elf -g -F dwarf $file
  done
ld -melf_i386 -o main *.o
./main
echo $?
