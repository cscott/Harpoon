## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in Runtime.a
#INCLUDES = -I../include
SUBDIRS = jni java.io java.lang
LDADD = libflexc.la ../Java.a Runtime.a

#bin_PROGRAMS = print-fixup test-lookup test-reflect test-hello
bin_PROGRAMS = test-hello
test_hello_LDADD = jni/libjni.la ../Java.a libflexc.la

lib_LTLIBRARIES = libflexc.la
libflexc_la_SOURCES = Hello3.c
libflexc_la_LIBADD = \
	java.io/libjavaio.la java.lang/libjavalang.la
libflexc_la_LDFLAGS = -version-info 0:0:0

Runtime.a: Runtime.a(stubs.o)
	ranlib $@
