## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
CFLAGS=-Darm32
LIBS =

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
noinst_LTLIBRARIES = libheader.la libfooter.la

libheader_la_SOURCES = header.S
libfooter_la_SOURCES = lookup.S dispatch.S footer.S
