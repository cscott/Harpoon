## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libstats.la

# this library will be empty unless we've elected to build statistics support
if STATISTICS
STATSRC=stats.c memstats.c heapstats.c
endif

libstats_la_SOURCES = $(STATSRC)
