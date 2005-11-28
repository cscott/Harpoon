## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.7 2005-11-28 06:26:28 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build statistics support
noinst_LTLIBRARIES = libstats.la

STATSRC =\
		stats.c memstats.c heapstats.c
if STATISTICS
libstats_la_SOURCES = $(STATSRC)
else
libstats_la_SOURCES = empty.c
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when STATISTICS is set)
EXTRA_DIST = $(STATSRC)
