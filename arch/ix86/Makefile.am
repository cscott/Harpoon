## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.5 2006-01-05 20:29:39 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_DIST = atomicity.h

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libheader.la libfooter.la
else
noinst_LIBRARIES = libheader.a libfooter.a
CCASCOMPILE = gcc
endif

# uncomment the end of these lines once you've written these assembly files.
libheader_la_SOURCES = header.S
# footer.S must be last.  other files, including an implementation of
# FNI_dispatch_<foo>, may precede footer.S
libfooter_la_SOURCES = footer.S

libheader_a_SOURCES = header.S
libfooter_a_SOURCES = footer.S
