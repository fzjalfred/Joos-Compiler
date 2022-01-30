SRCS=target/scala-2.13/CS444Compiler-assembly-0.1.0-SNAPSHOT.jar
LIBS=lib/*.jar
FLEXS=src/flex/* src/lexer/*
JAVA_OPT=-Xss10m

# Compiler used
SBT ?= sbt

#src dir
SRC_DIRS ?= ./src


.PHONY: clean
all: lexer main

lexer	:	$(FLEXS)
	java -cp ./lib/jflex-1.6.1.jar jflex.Main -d ./src/lexer ./src/flex/joos.flex;
	java -cp ./lib/java_cup.jar java_cup.Main -destdir ./src/lexer  < ./src/flex/joos.cup;
	javac -d ./build -cp ./lib/java_cup.jar  ./src/lexer/*.java;
	cd build && jar cvf ../lib/lexer.jar ./*.class;
	@echo "#!/bin/sh\n\
java -cp ./lib/lexer.jar:./lib/java_cup.jar Main \$$@" > lexer
	chmod +x lexer

$(SRCS) : $(shell find $(SRC_DIRS) -name *.scala -o -name *.java)
	sbt assembly

main: $(SRCS)
	@echo "#!/bin/sh\n\
java $(JAVA_OPT) -jar $(SRCS) $(LIBS) \$$@" > main
	chmod +x main

clean:
	rm -f main
	rm -f lexer
	rm -rf build/*

