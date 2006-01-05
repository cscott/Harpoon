## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.4 2006-01-05 20:29:40 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_CLEANFILES = *~
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libjavalangreflect.la
else
noinst_LIBRARIES = libjavalangreflect.a
endif

libjavalangreflect_la_SOURCES = \
	java_lang_reflect_Array.h java_lang_reflect_Array.c

libjavalangreflect_a_SOURCES = $(libjavalangreflect_la_SOURCES)


JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
