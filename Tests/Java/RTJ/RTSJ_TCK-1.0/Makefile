#
#  Define TESTSRCDIR to refer to the directory in which the test
#  java files are installed.  This is the directory which includes
#  this makefile.
#
TESTSRCDIR=/home/username/RTSJ_TCK-1.0

#
#  Define TESTJTIMEDIR to refer to the directory in which the
#  Reference Implementation of RTSJ has been installed.  There
#  should be subdirectories in this directory called "bin", "lib",
#  and "doc", and a file called "LICENSE".
#
TESTJTIMEDIR=/home/username/refimp-1.0

#
#  By default, "make" builds all tests unless "make FILE=filename.java"
#  is given.
#
ifndef FILE
FILE=*.java
endif

#
#  By default, "make run" runs all tests unless "make run TEST=filename"
#  (without the .java extension) is given on the command line.
#
ifndef TEST
TEST=AllTests
endif



IMMORTAL_SIZE=6000000
export IMMORTAL_SIZE

JCC=javac -classpath .:$(TESTJTIMEDIR)/lib/foundation.jar


all:
	$(JCC) $(FILE)

run:
	( cd $(TESTJTIMEDIR)/bin ; \
	  ./tjvm -version ; \
	  ./tjvm -Xms10M -Djava.class.path=$(TESTSRCDIR) $(TEST) )

run1:
	( cd $(TESTJTIMEDIR)/bin ; \
	  ./tjvm -version ; \
	  ./tjvm -Xms10M -Djava.class.path=$(TESTSRCDIR) OneTest $(TEST) )

version:
	( cd $(TESTJTIMEDIR)/bin ; \
	  ./tjvm -version )

clean:
	rm -rf *.class
