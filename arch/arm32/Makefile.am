## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
EXTRA_DIST = README
# The following is a hack, but it works...
CCASFLAGS=@CFLAGS@ -I../../include -Darm32

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
noinst_LTLIBRARIES = libheader.la libfooter.la

libheader_la_SOURCES = header.S segments.h

if PRECISE_GC
if PRECISE_C_BACKEND 
libfooter_la_SOURCES = lookup.S dispatch.S footer.S
else
libfooter_la_SOURCES = precise_malloc.S lookup.S dispatch.S footer.S
endif
else
libfooter_la_SOURCES = lookup.S dispatch.S footer.S
endif
