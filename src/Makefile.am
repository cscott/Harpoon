## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
SUBDIRS = jni java.io java.lang java.lang.reflect java.net \
	java.util java.util.zip \
	$(PGCDIR) $(CLHEAPDIR) $(NATIVEIODIR) $(STATSDIR) $(CAMDIR) $(USERDIR) \
	$(TRANSACTDIR) $(REALTIMEDIR) $(ROLEINFERDIR)
# make sure even the conditionally-compiled subdirectories are included
# in the distribution.
DIST_SUBDIRS = jni java.io java.lang java.lang.reflect java.net \
	java.util java.util.zip \
        gc clheap nativeio stats cam user transact realtime roles
EXTRA_DIST = README

# cruft to define the conditional directories. --------------------------
# please keep these in the same order that they appear in the SUBDIRS list

# for precise gc, add gc to SUBDIRS
if PRECISE_GC
PGCDIR=gc # rename to precisegc?
PGCLIB=gc/libgc.la
endif
# for clustered heap support, add clheap to SUBDIRS
if CLUSTERED_HEAPS
CLHEAPDIR=clheap
CLHEAPLIB=clheap/libclheap.la
endif
# for event-driven support, build nativeio libraries.
if EVENT_DRIVEN
NATIVEIODIR=nativeio # rename to eventdriven?
NATIVEIOLIB=nativeio/libnativeio.la
endif
# for statistics support, build stats library.
if STATISTICS
STATSDIR=stats
STATSLIB=stats/libstats.la
endif
# for camera support, build camera libraries.
if CAMERA
CAMDIR=cam
CAMLIB=cam/libcam.la
endif
# for user threads add user to SUBDIRS
if USER_THREADS
USERDIR=user
USERLIB=user/libuser.la
endif
# for transaction support, build transaction libraries.
if TRANSACTIONS
TRANSACTDIR=transact
TRANSACTLIB=transact/libtransact.la
endif
# for role support, build role libraries
if ROLE_INFER
ROLEINFERDIR=roles
ROLEINFERLIB=roles/libroleinfer.la
endif
# for realtime java support, add realtime to SUBDIRS
if REALTIME_JAVA
REALTIMEDIR=realtime
REALTIMELIB=realtime/librealtime.la
endif

# see run% rule in top-level makefile to see how to generate a binary
# executable from the Java.a file and these two libraries.
lib_LTLIBRARIES = libflexc0.la libflexc1.la $(AOUTLIBRARY)

libflexc0_la_SOURCES = startup.c
libflexc0_la_LIBADD = \
	jni/libjni.la \
	../arch/@ARCHDIR@/libheader.la
libflexc0_la_LDFLAGS = -version-info 0:0:0

libflexc1_la_SOURCES =
libflexc1_la_LIBADD = \
	../arch/@ARCHDIR@/libfooter.la \
	java.io/libjavaio.la java.lang/libjavalang.la \
	java.lang.reflect/libjavalangreflect.la \
	java.net/libjavanet.la \
	java.util/libjavautil.la \
	java.util.zip/libjavautilzip.la \
	$(PGCLIB) $(CLHEAPLIB) $(NATIVEIOLIB) $(STATSLIB) $(CAMLIB) $(USERLIB) \
	$(TRANSACTLIB) $(REALTIMELIB) $(ROLEINFERLIB)
libflexc1_la_LDFLAGS = -version-info 0:0:0 # -lbfd -liberty
# this library depends on -lbfd and -liberty, but these ought to be in $LIBS

# backwards compatibility: a.out targets ####################
if AOUT_BUILD
LDADD = jni/libjni.la ../Java.a libflexc.la \
	jni/jni-inflate.lo jni/jni-jnidata.lo \
	$(foreach lib,@GC_LIB@,../$(lib)) $(PGCLIB) $(STATSLIB) $(USERLIB) \
	$(CLHEAPLIB) $(NATIVEIOLIB) $(CAMLIB) $(TRANSACTLIB) $(REALTIMELIB) \
	$(ROLEINFERLIB)
bin_PROGRAMS = startup # dump-fixup test-lookup test-gc static
AOUTLIBRARY = libflexc.la
endif
libflexc_la_SOURCES =
libflexc_la_LIBADD = \
        java.io/libjavaio.la java.lang/libjavalang.la \
	java.lang.reflect/libjavalangreflect.la \
	java.net/libjavanet.la \
	java.util/libjavautil.la \
        java.util.zip/libjavautilzip.la
libflexc_la_LDFLAGS = -version-info 0:0:0 # -lbfd -liberty
# this library depends on -lbfd and -liberty, but these ought to be in $LIBS


