## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in configure aclocal.m4 \
		       include/config.h.in include/stamp-h.in
EXTRA_DIST =
SUBDIRS = src

list:
	@find . -path './Contrib' -prune -o -name "*.[ch]" -print

## automake version 1.4 is required.
AUTOMAKE_OPTIONS = 1.4
