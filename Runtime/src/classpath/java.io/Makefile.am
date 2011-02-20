## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.6 2006-12-28 17:05:21 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libjavaio.la

libjavaio_la_SOURCES = \
	java_io_ObjectInputStream.h java_io_ObjectInputStream.c \
	java_io_VMObjectStreamClass.h java_io_VMObjectStreamClass.c

JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
