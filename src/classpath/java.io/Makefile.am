## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

noinst_LTLIBRARIES = libjavaio.la

libjavaio_la_SOURCES = \
	java_io_ObjectInputStream.h java_io_ObjectInputStream.c \
	java_io_VMObjectStreamClass.h java_io_VMObjectStreamClass.c

JAVASRC=

EXTRA_DIST=$(JAVASRC)

#uncomment this if you actually add any java files to JAVASRC
#include $(top_srcdir)/JavaRules.make
