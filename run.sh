#!/bin/bash
rm output/*.o
./joosc $1 $2 $(find stdlib/2.0/java/ -name *.java )
cd output
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf classTestA.java.s
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf classTest.java.s
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf Object.java.s
/u/cs444/bin/nasm -O1 -f elf -g -F dwarf runtime.s
ld -melf_i386 -o main *.o
./main
echo $?

