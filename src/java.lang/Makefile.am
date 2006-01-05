## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.21 2006-01-05 20:29:41 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libjavalang-indep.la
else
noinst_LIBRARIES = libjavalang-indep.a
endif

libjavalang_indep_la_SOURCES = \
	class.h object.h properties.h runtime.h system.h \
	thread.c thread.h \
	threadlist.c threadlist.h

libjavalang_indep_a_SOURCES = $(libjavalang_indep_la_SOURCES)
