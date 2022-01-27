SRCS=target/scala-2.13/CS444Compiler-assembly-0.1.0-SNAPSHOT.jar
LIBS=lib/*.jar
FLEXS=src/flex/*
JAVA_OPT=-Xss10m

# Compiler used
SBT ?= sbt

#src dir
SRC_DIRS ?= ./src


.PHONY: clean
all: flex main

flex	:	$(FLEXS)
	java -cp ./lib/jflex-1.6.1.jar jflex.Main -d src/lexer src/flex/lcalc.flex

$(SRCS) : $(shell find $(SRC_DIRS) -name *.scala -o -name *.java)
	sbt assembly

main: $(SRCS)
	@echo "#!/bin/sh\n\
java $(JAVA_OPT) -jar $(SRCS) $(LIBS) \$$@" > main
	chmod +x main

clean:
	rm -f main
	sbt clean

