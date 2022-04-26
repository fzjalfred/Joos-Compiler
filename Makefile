SRC_DIRS=./src
SRCS= $(shell find $(SRC_DIRS) -name *.java)
IRS = $(shell find $(SRC_DIRS)/tir -name *.java)
LIBS=lib/*.jar
FLEXS=src/flex/* src/lexer/*
JAVA_OPT=-Xss10m


.PHONY: clean
all: lexer main

lexer	:	$(FLEXS)
	java -cp ./lib/jflex-1.6.1.jar jflex.Main -d ./src/lexer ./src/flex/joos.flex;
	java -cp ./lib/java_cup.jar java_cup.Main -destdir ./src/lexer  < ./src/flex/joos.cup;


main: $(SRCS) $(FLEXS)
	javac -d ./build -cp ./lib/java_cup.jar  $(SRCS);
	cd build && jar cvf ../lib/lexer.jar ./ast/*.class ./lexer/*.class ./type/*.class ./exception/*.class ./utils/*.class ./hierarchy/*.class ./visitors/*.class ./dataflowAnalysis/*.class ./backend/*.class ./backend/asm/*.class ./tir/src/joosc/ir/ast/*.class ./tir/src/joosc/ir/interpret/*.class ./tir/src/joosc/ir/visit/*.class ./tir/src/joosc/util/*.class;
	@echo "#!/bin/sh\n\
    java -cp ./lib/lexer.jar:./lib/java_cup.jar lexer.Main \$$@" > joosc
	chmod +x joosc

IRInterpter: $(IRS)
	javac -d ./build $(IRS);
	cd build && jar cvf ../lib/sim.jar ./tir/src/joosc/ir/ast/*.class ./tir/src/joosc/ir/interpret/*.class ./tir/src/joosc/ir/visit/*.class ./tir/src/joosc/util/*.class;
	@echo "#!/bin/sh\n\
    java -cp ./lib/sim.jar tir.src.joosc.ir.interpret.Main \$$@" > sim
	chmod +x sim

clean:
	rm -f joosc
	rm -rf build/*
	rm -f src/lexer/Lexer.java
	rm -f src/lexer/Lexer.java~
	rm -f src/lexer/parser.java
	rm -f src/lexer/sym.java
	rm -f a3.zip

submitzipA3:
	rm -rf build/*
	rm -f a3.zip
	git log > a3.log
	zip -r a3.zip build/ lib/ src/ Makefile test/self_testcases/ a3.log
	rm a3.log

submitzipA4:
	rm -rf build/*
	rm -f a4.zip
	git log > a4.log
	zip -r a4.zip build/ lib/ src/ Makefile test/self_testcases/ a4.log
	rm a4.log

submitzipA5:
	rm -rf build/*
	rm -f a5.zip
	git log > a5.log
	zip -r a5.zip build/ lib/ src/ Makefile test/self_testcases/a5/ a5.log
	rm a5.log