## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
SUBDIRS = jni java.io java.lang java.util.zip $(GCDIR)

# for precise gc, add gc to SUBDIRS
if PRECISE_GC
GCDIR=gc
endif
#	$(foreach lib,@GC_LIB@,../$(lib))

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
	java.util.zip/libjavautilzip.la
libflexc1_la_LDFLAGS = -version-info 0:0:0

# backwards compatibility: a.out targets ####################
if AOUT_BUILD
LDADD = jni/libjni.la ../Java.a libflexc.la \
	$(foreach lib,@GC_LIB@,../$(lib))
bin_PROGRAMS = startup # dump-fixup test-lookup test-gc static
AOUTLIBRARY = libflexc.la
endif
libflexc_la_SOURCES =
libflexc_la_LIBADD = \
        java.io/libjavaio.la java.lang/libjavalang.la \
        java.util.zip/libjavautilzip.la
libflexc_la_LDFLAGS = -version-info 0:0:0
