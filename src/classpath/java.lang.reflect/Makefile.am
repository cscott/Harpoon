## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

noinst_LTLIBRARIES = libjavalangreflect.la

libjavalangreflect_la_SOURCES = \
	java_lang_reflect_Array.h java_lang_reflect_Array.c

JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
