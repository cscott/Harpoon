## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libgc.la

if SEMI_PRECISE_GC
libgc_la_SOURCES = \
	sp_gc.c
else
if PRECISE_GC
libgc_la_SOURCES = \
	precise_gc.c copying.c gc-data.c
endif
endif

