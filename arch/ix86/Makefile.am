## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.6 2006-12-28 17:05:19 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
EXTRA_DIST = atomicity.h

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
noinst_LTLIBRARIES = libheader.la libfooter.la

# uncomment the end of these lines once you've written these assembly files.
libheader_la_SOURCES = header.S
# footer.S must be last.  other files, including an implementation of
# FNI_dispatch_<foo>, may precede footer.S
libfooter_la_SOURCES = footer.S
