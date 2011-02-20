## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.5 2006-12-28 17:05:22 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build dynamic
# synchronization removal support.
noinst_LTLIBRARIES = libdynsync.la

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
