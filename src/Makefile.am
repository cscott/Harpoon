## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in Runtime.a
INCLUDES = @IFLAGS@
SUBDIRS = jni java.io java.lang java.util.zip
LDADD = jni/libjni.la ../Java.a libflexc.la

bin_PROGRAMS = startup dump-fixup test-lookup

lib_LTLIBRARIES = libflexc.la
libflexc_la_SOURCES = # Hello3.c
libflexc_la_LIBADD = \
	java.io/libjavaio.la java.lang/libjavalang.la \
	java.util.zip/libjavautilzip.la
libflexc_la_LDFLAGS = -version-info 0:0:0
