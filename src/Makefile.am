## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = -I../include
SUBDIRS = jni
LDADD = ../Java.a Runtime.a

bin_PROGRAMS = print-fixup test-lookup test-reflect

Runtime.a: Runtime.a(stubs.o)
	ranlib $@
