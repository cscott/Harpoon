## Process this file with automake to produce Makefile.in
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
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when DYNAMIC_SYNC_REMOVAL is set)
EXTRA_DIST = $(DYNSYNCSRC)
