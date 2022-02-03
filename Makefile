SRCS= $(shell find $(SRC_DIRS) -name *.java)
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


main: $(SRCS) $(FLEXS)
	javac -d ./build -cp ./lib/java_cup.jar  $(SRCS);
	cd build && jar cvf ../lib/lexer.jar ./ast/*.class ./lexer/*.class;
	@echo "#!/bin/sh\n\
    java -cp ./lib/lexer.jar:./lib/java_cup.jar lexer.Main \$$@" > main
	chmod +x main

clean:
	rm -f main
	rm -f lexer
	rm -rf build/*

