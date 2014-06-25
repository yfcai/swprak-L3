# Usage
#
# make                 # compile
# make run NaiveTests  # run tests
# make clean           # delete .class files

CLASSPATH = .:*
JC = javac -cp $(CLASSPATH)
JV = java -cp $(CLASSPATH)
SRC = $(wildcard *.java)
OBJ = $(SRC:.java=.class)

.SUFFIXES: .java .class

.java.class:
	$(JC) $*.java

default:	compile

compile:	$(OBJ)

# http://stackoverflow.com/a/6273809
# part 1: get argument list by filtering out the action
run:	compile
	java -cp .:* org.junit.runner.JUnitCore $(filter-out $@,$(MAKECMDGOALS))


clean:
	rm *.class

# http://stackoverflow.com/a/6273809
# part 2: disregard unrecognized targets
%:
	@:
