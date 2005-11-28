## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.3 2005-11-28 06:26:28 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_CLEANFILES = *~
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
