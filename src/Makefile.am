## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
#INCLUDES = -I../include
SUBDIRS = jni java.io java.lang
LDADD = ../Java.a Runtime.a

bin_PROGRAMS = print-fixup test-lookup test-reflect test-hello
test_hello_LDADD = jni/libjni.a $(LDADD)

Runtime.a: Runtime.a(stubs.o)
	ranlib $@
