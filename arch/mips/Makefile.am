## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in Flex_MIPS.S
EXTRA_DIST = Flex_MIPS.pl
CFLAGS=
LIBS =

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
noinst_LTLIBRARIES = libheader.la libfooter.la

libheader_la_SOURCES = header.S segments.h
libfooter_la_SOURCES = lookup.S dispatch.S footer.S Flex_MIPS.S \
floatem.c long_long_support.c

Flex_MIPS.S: Flex_MIPS.pl
	perl $< > $@
