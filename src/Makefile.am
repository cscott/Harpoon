## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.52 2006-01-05 20:29:40 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_CLEANFILES = *~
INCLUDES = @IFLAGS@
SUBDIRS = reflect-thunk jni java.lang java.lang.reflect \
	$(NATIVEDIR) \
	$(PGCDIR) $(CLHEAPDIR) $(NATIVEIODIR) stats cam servo $(USERDIR) \
	dynsync transact $(REALTIMEDIR) $(RTJPERFDIR) $(ROLEINFERDIR) \
	$(MZFDIR)
# make sure even the conditionally-compiled subdirectories are included
# in the distribution.
DIST_SUBDIRS = reflect-thunk jni java.lang java.lang.reflect \
	sunjdk classpath \
        gc clheap nativeio stats cam servo user \
        dynsync transact realtime rtjperf roles mzf
EXTRA_DIST = README

BUILD_A_LIB = ../scripts/build-a-lib

# cruft to define the conditional directories. --------------------------
# please keep these in the same order that they appear in the SUBDIRS list


## [AS] 12/09/05: all X_A variables correspond to plain, static .a libs
## The rules for them are almost identical to the rules for .la libs

# for compiling against Sun'd JDK, add sunjdk to SUBDIRS
if SUNJDK
NATIVEDIR=sunjdk
NATIVELIB=\
	sunjdk/java.lang/libjavalang.la \
	sunjdk/java.io/libjavaio.la \
	sunjdk/java.net/libjavanet.la \
	sunjdk/java.util/libjavautil.la \
	sunjdk/java.util.zip/libjavautilzip.la
NATIVELIB_A=\
	sunjdk/java.lang/libjavalang.a \
	sunjdk/java.io/libjavaio.a \
	sunjdk/java.net/libjavanet.a \
	sunjdk/java.util/libjavautil.a \
	sunjdk/java.util.zip/libjavautilzip.a
else
NATIVEDIR=classpath
NATIVELIB=\
	classpath/java.io/libjavaio.la \
	classpath/java.lang/libjavalang.la \
	classpath/java.lang.reflect/libjavalangreflect.la \
	classpath/gnu.vm.stack/libgnuvmstack.la
NATIVELIB_A=\
	classpath/java.io/libjavaio.a \
	classpath/java.lang/libjavalang.a \
	classpath/java.lang.reflect/libjavalangreflect.a \
	classpath/gnu.vm.stack/libgnuvmstack.a
endif

# for precise gc, add gc to SUBDIRS
if PRECISE_GC
PGCDIR=gc # rename to precisegc?
PGCLIB=gc/libgc.la
PGCLIB_A=gc/libgc.a
endif
# for clustered heap support, add clheap to SUBDIRS
if CLUSTERED_HEAPS
CLHEAPDIR=clheap
CLHEAPLIB=clheap/libclheap.la
CLHEAPLIB_A=clheap/libclheap.a
endif
# for event-driven support, build nativeio libraries.
if EVENT_DRIVEN
NATIVEIODIR=nativeio # rename to eventdriven?
NATIVEIOLIB=nativeio/libnativeio.la
NATIVEIOLIB_A=nativeio/libnativeio.a
endif
# for statistics support, build stats library.
# (always built, sometimes library is empty)

# for camera support, build camera libraries.
# (always built, sometimes library is empty)

# for servo support, build servo libraries.
# (always built, sometimes library is empty)

# for user threads add user to SUBDIRS
if USER_THREADS
USERDIR=user
USERLIB=user/libuser.la
USERLIB_A=user/libuser.a
endif

# for dynamic synchronization removal support, build libdynsync libraries.
# (always built, sometimes library is empty)

# for transaction support, build transaction libraries.
# (always built, sometimes library is empty)

# for mostly-zero fields support, build mzf libraries.
if MZF_SUPPORT
MZFDIR=mzf
MZFLIB=mzf/libmzf.la
MZFLIB_A=mzf/libmzf.a
endif
# for role support, build role libraries
if ROLE_INFER
ROLEINFERDIR=roles
ROLEINFERLIB_A=roles/libroleinfer.a
endif
# for realtime java support, add realtime to SUBDIRS
if REALTIME_JAVA
REALTIMEDIR=realtime
REALTIMELIB_A=realtime/librealtime.a
endif
# for realtime timer support (RTJPerf adapted for FLEX)
if RTJ_PERF
RTJPERFDIR=rtjperf
RTJPERFLIB_A=rtjperf/librtjperf.a
endif

# see run% rule in top-level makefile to see how to generate a binary
# executable from the Java.a file and these two libraries.
if PRODUCE_LA_LIBS
lib_LTLIBRARIES = libflexc0.la libflexc1.la
else
noinst_LIBRARIES = libflexc0.a libflexc1.a
endif

libflexc0_la_SOURCES = startup.c
libflexc0_la_LIBADD = \
	jni/libjni.la \
	../arch/@ARCHDIR@/libheader.la
libflexc0_la_LDFLAGS = -version-info 0:0:0

libflexc1_la_SOURCES = config_checker.c
libflexc1_la_LIBADD = \
	../arch/@ARCHDIR@/libfooter.la \
	java.lang/libjavalang-indep.la \
	java.lang.reflect/libjavalangreflect-indep.la \
	$(NATIVELIB) \
	$(PGCLIB) $(CLHEAPLIB) $(NATIVEIOLIB) stats/libstats.la cam/libcam.la servo/libservo.la $(USERLIB) \
	dynsync/libdynsync.la transact/libtransact.la $(REALTIMELIB) $(RTJPERFLIB) $(ROLEINFERLIB) $(MZFLIB)

libflexc1_la_LDFLAGS = -version-info 0:0:0 # -lbfd -liberty
# this library depends on -lbfd and -liberty, but these ought to be in $LIBS



## START [AS] 12/09/05 - plain, static .a libs 
libflexc0.a : startup.o jni/libjni.a ../arch/@ARCHDIR@/libheader.a
	$(BUILD_A_LIB) $@ $^

libflexc1.a : config_checker.o \
		../arch/@ARCHDIR@/libfooter.a \
		java.lang/libjavalang-indep.a \
		java.lang.reflect/libjavalangreflect-indep.a \
		$(NATIVELIB_A) \
		$(PGCLIB_A) $(CLHEAPLIB_A) $(NATIVEIOLIB_A) stats/libstats.a cam/libcam.a servo/libservo.a $(USERLIB_A) \
		dynsync/libdynsync.a transact/libtransact.a $(REALTIMELIB_A) $(RTJPERFLIB_A) $(ROLEINFERLIB_A) $(MZFLIB_A)
	$(BUILD_A_LIB) $@ $^


libflexc1_a_SOURCES = config_checker.c
libflexc1_a_LIBADD = \
	../arch/@ARCHDIR@/libfooter.a \
	java.lang/libjavalang-indep.a \
	java.lang.reflect/libjavalangreflect-indep.a \
	$(NATIVELIB_A) \
	$(PGCLIB_A) $(CLHEAPLIB_A) $(NATIVEIOLIB_A) stats/libstats.a cam/libcam.a servo/libservo.a $(USERLIB_A) \
	dynsync/libdynsync.a transact/libtransact.a $(REALTIMELIB_A) $(RTJPERFLIB_A) $(ROLEINFERLIB_A) $(MZFLIB_A)

libflexc1_a_LDFLAGS = -version-info 0:0:0 # -lbfd -liberty
## END [AS] 12/09/05 - plain, static .a libs
