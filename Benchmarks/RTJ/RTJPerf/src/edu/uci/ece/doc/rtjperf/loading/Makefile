#
# $Id: Makefile,v 1.1 2002-07-02 15:53:15 wbeebee Exp $
#

RTJP_CLASSES = ${RTJP_HOME}/classes
RTJP_SRC = ${RTJP_HOME}/src
RTJP_LIB = ${RTJP_HOME}/lib
RTJP_BIN = ${RTJP_HOME}/bin
JNI_INCLUDE = ${JAVA_HOME}/include
JNI_LINUX_INCLUDE = ${JAVA_HOME}/include/linux

GCJ = gcj3
GCC = gcc3
GPP = g++3
GCJH = gcjh

all:
	$(GCJ) -O6 --main=edu.uci.ece.doc.rtjperf.loading.EagerTypeFactoryTest -L${RTJP_LIB} -lHRTime-cni ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/loading/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java -o eagerTest

	$(GCJ) -O6 --main=edu.uci.ece.doc.rtjperf.loading.TypeITest -L${RTJP_LIB} -lHRTime-cni ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/loading/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java -o t1t
