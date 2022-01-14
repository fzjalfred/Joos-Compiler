## boot script
`./boot.sh` will

pull the image and
automatically mount the the current directory to the `/root` and run the container in background.

When exiting, the container will not be stopped or deleted.
Use `./boot.sh clean` instead.


Usage of JFlex to generate Scala lexer:
    1. open sbt : sbt
    2. in sbt: >  jflexGenerate

