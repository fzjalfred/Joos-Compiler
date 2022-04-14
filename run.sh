#!/bin/bash
./joosc $1 $(find stdlib/2.0/java/ -name *.java )
cd output
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf foo.java.s
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf Object.java.s
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf runtime.s
ld -melf_i386 -o main *.o
./main
echo $?
