## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.5 2006-01-05 20:29:40 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_CLEANFILES = *~
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libjavaio.la
else
noinst_LIBRARIES = libjavaio.a
endif

libjavaio_la_SOURCES = \
	java_io_ObjectInputStream.h java_io_ObjectInputStream.c \
	java_io_VMObjectStreamClass.h java_io_VMObjectStreamClass.c

libjavaio_a_SOURCES = $(libjavaio_la_SOURCES)


JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
