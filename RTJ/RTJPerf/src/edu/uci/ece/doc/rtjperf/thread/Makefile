#
# $Id: Makefile,v 1.1 2002-07-02 15:54:15 wbeebee Exp $
#

RTJP_CLASSES = ${RTJP_HOME}/classes
RTJP_SRC = ${RTJP_HOME}/src
RTJP_LIB = ${RTJP_HOME}/lib
RTJP_BIN = ${RTJP_HOME}/bin

GCJ_INCLUDE=$(GCC_HOME)/include
GCJ_SRC_INCLUDE=$(GCC_SRC_HOME)/libjava/include
BOEHM_SRC_INCLUDE=$(GCC_HOME)/boehm-gc
INCLUDE_DIRS = -I$(GCJ_INCLUDE)  -I$(GCJ_SRC_INCLUDE) -I$(BOEHM_SRC_INCLUDE) -I$(RTJAVA_HOME)/src/librtj/include -I$(RTJAVA_HOME)/src/librtj/src 

# -I$(RTJAVA_HOME)/classes
all:
	gcj -O6 -static  -I$(RTJAVA_HOME)/src -I$(RTJP_HOME)/src $(RTJAVA_HOME)/src/javax/realtime/*.java $(RTJAVA_HOME)/src/javax/realtime/util/*.java --main=edu.uci.ece.doc.rtjperf.thread.YieldTest -o $(RTJP_BIN)/yieldTest  ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/thread/YieldTest.java   ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/concurrent/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/HighResClock.cc $(INCLUDE_DIRS) $(RTJAVA_HOME)/src/librtj/src/*.cc  -lgcj -lgcjgc

	gcj -O6 -static  -I$(RTJAVA_HOME)/src -I$(RTJP_HOME)/src $(RTJAVA_HOME)/src/javax/realtime/*.java $(RTJAVA_HOME)/src/javax/realtime/util/*.java --main=edu.uci.ece.doc.rtjperf.thread.CreationLatencyTest -o $(RTJP_BIN)/threadLatencyTest  ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/thread/CreationLatencyTest.java   ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/concurrent/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/HighResClock.cc $(INCLUDE_DIRS) $(RTJAVA_HOME)/src/librtj/src/*.cc  -lgcj -lgcjgc

	gcj -O6 -static  -I$(RTJAVA_HOME)/src -I$(RTJP_HOME)/src $(RTJAVA_HOME)/src/javax/realtime/*.java $(RTJAVA_HOME)/src/javax/realtime/util/*.java --main=edu.uci.ece.doc.rtjperf.thread.SynchYieldTest -o $(RTJP_BIN)/synchYieldTest  ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/thread/SynchYieldTest.java   ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/concurrent/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/HighResClock.cc $(INCLUDE_DIRS) $(RTJAVA_HOME)/src/librtj/src/*.cc  -lgcj -lgcjgc

	gcj -O6 -static  -I$(RTJAVA_HOME)/src -I$(RTJP_HOME)/src $(RTJAVA_HOME)/src/javax/realtime/*.java $(RTJAVA_HOME)/src/javax/realtime/util/*.java --main=edu.uci.ece.doc.rtjperf.thread.RTCreationLatencyTest -o $(RTJP_BIN)/rtThreadLatencyTest  ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/thread/RTCreationLatencyTest.java   ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/*.java  ${RTJP_SRC}/edu/uci/ece/doc/util/concurrent/*.java ${RTJP_SRC}/edu/uci/ece/doc/rtjperf/sys/HighResClock.cc $(INCLUDE_DIRS) $(RTJAVA_HOME)/src/librtj/src/*.cc  -lgcj -lgcjgc
