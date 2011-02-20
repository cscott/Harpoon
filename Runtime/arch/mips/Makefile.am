## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in Flex_MIPS.S
EXTRA_DIST = Flex_MIPS.pl
#CFLAGS=
LIBS =
# The following is a hack, but it works...
AM_CCAS=$(CC)
AM_CCASFLAGS=@CFLAGS@ -I../../include -Dmips

# changing libheader.la to libheader.lo fixes a lot of problems, but
# automake complains.
noinst_LTLIBRARIES = libheader.la libfooter.la

if PRECISE_C_BACKEND
FOOTER_SUPPORT=
else
FOOTER_SUPPORT= lookup.S dispatch.S Flex_MIPS.S \
	floatem.c long_long_support.c softfloat.c
endif

libheader_la_SOURCES = header.S segments.h
libfooter_la_SOURCES = $(FOOTER_SUPPORT) footer.S

Flex_MIPS.S: Flex_MIPS.pl
	perl $< > $@
