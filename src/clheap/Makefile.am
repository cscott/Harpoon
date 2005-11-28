## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.5 2005-11-28 06:26:28 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libclheap.la

libclheap_la_SOURCES = alloc.h clheap.c clheap.h misc.h stats.h \
	stkalloc.c gblalloc.c thralloc.c
