## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.6 2006-01-05 20:29:40 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libclheap.la
else
noinst_LIBRARIES = libclheap.a
endif 

libclheap_la_SOURCES = alloc.h clheap.c clheap.h misc.h stats.h \
	stkalloc.c gblalloc.c thralloc.c


libclheap_a_SOURCES = $(libclheap_a_SOURCES)
