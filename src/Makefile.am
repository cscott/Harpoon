## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
SUBDIRS = jni java.io java.lang java.net java.util.zip \
	$(PGCDIR) $(CLHEAPDIR) $(NATIVEIODIR)

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
	java.net/libjavanet.la \
	java.util.zip/libjavautilzip.la \
	$(PGCLIB) $(CLHEAPLIB) $(NATIVEIOLIB)
libflexc1_la_LDFLAGS = -version-info 0:0:0

# backwards compatibility: a.out targets ####################
if AOUT_BUILD
LDADD = jni/libjni.la ../Java.a libflexc.la \
	jni/jni-inflate.lo jni/jni-jnidata.lo \
	$(foreach lib,@GC_LIB@,../$(lib))
bin_PROGRAMS = startup # dump-fixup test-lookup test-gc static
AOUTLIBRARY = libflexc.la
endif
libflexc_la_SOURCES =
libflexc_la_LIBADD = \
        java.io/libjavaio.la java.lang/libjavalang.la \
	java.net/libjavanet.la \
        java.util.zip/libjavautilzip.la
libflexc_la_LDFLAGS = -version-info 0:0:0
