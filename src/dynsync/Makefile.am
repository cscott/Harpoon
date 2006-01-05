## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.4 2006-01-05 20:29:41 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build dynamic
# synchronization removal support.

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libdynsync.la
else
noinst_LIBRARIES = libdynsync.a
endif


DYNSYNCSRC = \
	harpoon_Runtime_DynamicSyncImpl.h \
	harpoon_Runtime_DynamicSyncImpl.c

if DYNAMIC_SYNC_REMOVAL
libdynsync_la_SOURCES = $(DYNSYNCSRC)
else
libdynsync_la_SOURCES = empty.c
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when DYNAMIC_SYNC_REMOVAL is set)
EXTRA_DIST = $(DYNSYNCSRC)


libdynsync_a_SOURCES = $(libdynsync_la_SOURCES)
