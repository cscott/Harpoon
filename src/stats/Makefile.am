## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.8 2006-01-05 20:29:41 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build statistics support
if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libstats.la
else
noinst_LIBRARIES = libstats.a
endif

STATSRC = stats.c memstats.c heapstats.c

if STATISTICS
libstats_la_SOURCES = $(STATSRC)
else
libstats_la_SOURCES = empty.c
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when STATISTICS is set)
EXTRA_DIST = $(STATSRC)

libstats_a_SOURCES = $(libstats_la_SOURCES)
