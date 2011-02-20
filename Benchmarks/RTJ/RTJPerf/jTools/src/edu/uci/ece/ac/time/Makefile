#
# $Id: Makefile,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
#
JTOOLS_HOME = ../../../../../..
JTOOLS_CLASSES = ${JTOOLS_HOME}/classes
JTOOLS_SRC = ${JTOOLS_HOME}/src
JTOOLS_LIB = ${JTOOLS_HOME}/lib
JTOOLS_BIN = ${JTOOLS_HOME}/bin
JNI_INCLUDE = ${JAVA_HOME}/include
JNI_LINUX_INCLUDE = ${JAVA_HOME}/include/linux

GCJ = gcj
GCC = gcc
GPP = g++
GCJH = gcjh

all:
	python genSysInfo.py
	javah -jni -d . -classpath ${JTOOLS_CLASSES} edu.uci.ece.ac.time.HighResClock
	${GCJH}  -I${JTOOLS_CLASSES} edu.uci.ece.ac.time.HighResClock -d . -o HighResClock.h
	${GCJH}  -I${JTOOLS_CLASSES} edu.uci.ece.ac.time.HighResTime -d . -o HighResTime.h
	${GPP} -O6 -I${JNI_INCLUDE} -I${JNI_LINUX_INCLUDE}  -shared edu_uci_ece_ac_time_HighResClock.cpp -o ${JTOOLS_LIB}/libHRTime.so

	${GPP} -O6   -shared HighResClock.cc  -o ${JTOOLS_LIB}/libHRTime-cni.so
	${GPP} -c  HighResClock.cc  -o HighResClock.o
	ar rc ${JTOOLS_LIB}/libHRTime-cni.a HighResClock.o
	ranlib ${JTOOLS_LIB}/libHRTime-cni.a

clean:
	rm ${JTOOLS_LIB}/*.so
	rm ${JTOOLS_LIB}/*.a
	rm ./*.h
	rm ./*.o
