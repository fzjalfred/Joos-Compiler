SRCS=target/scala-2.13/CS444Compiler-assembly-0.1.0-SNAPSHOT.jar
JAVA_OPT=-Xss10m

# Compiler used
SBT ?= sbt

#src dir
SRC_DIRS ?= ./src


.PHONY: clean
all: main

$(SRCS) : $(shell find $(SRC_DIRS) -name *.scala)
	sbt assembly

main: $(SRCS)
	@echo "#!/bin/sh\n\
java $(JAVA_OPT) -jar $(SRCS) \$$@" > main
	chmod +x main

clean:
	rm -f main
	sbt clean

