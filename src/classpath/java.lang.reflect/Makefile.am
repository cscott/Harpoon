## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.5 2006-12-28 17:05:21 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libjavalangreflect.la

libjavalangreflect_la_SOURCES = \
	java_lang_reflect_Array.h java_lang_reflect_Array.c

JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
