## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build statistics support
noinst_LTLIBRARIES = libstats.la

STATSRC =\
		stats.c memstats.c heapstats.c
if STATISTICS
libstats_la_SOURCES = $(STATSRC)
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when STATISTICS is set)
EXTRA_DIST = $(STATSRC)
