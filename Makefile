# Generated automatically from Makefile.in by configure.
# Makefile.in generated automatically by automake 1.4-p5 from Makefile.am

# Copyright (C) 1994, 1995-8, 1999, 2001 Free Software Foundation, Inc.
# This Makefile.in is free software; the Free Software Foundation
# gives unlimited permission to copy and/or distribute it,
# with or without modifications, as long as this notice is preserved.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY, to the extent permitted by law; without
# even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.


SHELL = /bin/sh

srcdir = .
top_srcdir = ..
prefix = /usr/local
exec_prefix = ${prefix}

bindir = ${exec_prefix}/bin
sbindir = ${exec_prefix}/sbin
libexecdir = ${exec_prefix}/libexec
datadir = ${prefix}/share
sysconfdir = ${prefix}/etc
sharedstatedir = ${prefix}/com
localstatedir = ${prefix}/var
libdir = ${exec_prefix}/lib
infodir = ${prefix}/info
mandir = ${prefix}/man
includedir = ${prefix}/include
oldincludedir = /usr/include

DESTDIR =

pkgdatadir = $(datadir)/freeciv
pkglibdir = $(libdir)/freeciv
pkgincludedir = $(includedir)/freeciv

top_builddir = ..

ACLOCAL = aclocal
AUTOCONF = autoconf
AUTOMAKE = automake
AUTOHEADER = autoheader

INSTALL = /usr/bin/install -c
INSTALL_PROGRAM = ${INSTALL} $(AM_INSTALL_PROGRAM_FLAGS)
INSTALL_DATA = ${INSTALL} -m 644
INSTALL_SCRIPT = ${INSTALL_PROGRAM}
transform = s,x,x,

NORMAL_INSTALL = :
PRE_INSTALL = :
POST_INSTALL = :
NORMAL_UNINSTALL = :
PRE_UNINSTALL = :
POST_UNINSTALL = :
host_alias = i686-pc-linux-gnu
host_triplet = i686-pc-linux-gnu
ARFLAGS = 
AWK = gawk
BUILD_INCLUDED_LIBINTL = no
CATALOGS =  da.gmo de.gmo en_GB.gmo es.gmo fi.gmo fr.gmo hu.gmo it.gmo ja.gmo nl.gmo no.gmo pl.gmo pt.gmo pt_BR.gmo ro.gmo ru.gmo sv.gmo
CATOBJEXT = .gmo
CC = gcc
CLIENT_CFLAGS = -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/usr/X11R6/include -I/usr/X11R6/include
CLIENT_LIBS = -L/usr/lib -lgdk_imlib -L/usr/lib -L/usr/X11R6/lib -lgtk -lgdk -rdynamic -lgmodule -lglib -ldl -lXi -lXext -lX11 -lm
CPP = gcc -E
CVS_DEPS = yes
CXX = @CXX@
DATADIRNAME = share
ESD_CFLAGS = 
ESD_CONFIG = /usr/bin/esd-config
ESD_LIBS = -L/usr/lib -lesd -laudiofile -lm
GDK_IMLIB_CFLAGS = -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/usr/X11R6/include -I/usr/X11R6/include
GDK_IMLIB_LIBS = -L/usr/lib -lgdk_imlib -L/usr/lib -L/usr/X11R6/lib -lgtk -lgdk -rdynamic -lgmodule -lglib -ldl -lXi -lXext -lX11 -lm
GENCAT = gencat
GLIBC21 = yes
GLIB_CFLAGS = @GLIB_CFLAGS@
GLIB_CONFIG = @GLIB_CONFIG@
GLIB_GENMARSHAL = @GLIB_GENMARSHAL@
GLIB_LIBS = @GLIB_LIBS@
GLIB_MKENUMS = @GLIB_MKENUMS@
GMOFILES =  da.gmo de.gmo en_GB.gmo es.gmo fi.gmo fr.gmo hu.gmo it.gmo ja.gmo nl.gmo no.gmo pl.gmo pt.gmo pt_BR.gmo ro.gmo ru.gmo sv.gmo
GMSGFMT = /usr/bin/msgfmt
GOBJECT_QUERY = @GOBJECT_QUERY@
GTK_CFLAGS = -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/usr/X11R6/include
GTK_CONFIG = /usr/bin/gtk-config
GTK_LIBS = -L/usr/lib -L/usr/X11R6/lib -lgtk -lgdk -rdynamic -lgmodule -lglib -ldl -lXi -lXext -lX11 -lm
IMLIB_CFLAGS = @IMLIB_CFLAGS@
IMLIB_CONFIG = /usr/bin/imlib-config
IMLIB_LIBS = @IMLIB_LIBS@
INSTOBJEXT = .mo
INTLBISON = bison
INTLDEPS = @INTLDEPS@
INTLLIBS = 
INTLOBJS = 
INTL_LIBTOOL_SUFFIX_PREFIX = 
LIBICONV = 
LN_S = ln -s
MAINT = #
MAKEINFO = makeinfo
MKINSTALLDIRS = ./mkinstalldirs
MSGFMT = /usr/bin/msgfmt
PACKAGE = freeciv
PKG_CONFIG = 
POFILES =  da.po de.po en_GB.po es.po fi.po fr.po hu.po it.po ja.po nl.po no.po pl.po pt.po pt_BR.po ro.po ru.po sv.po
POSUB = po
RANLIB = ranlib
SDL_CFLAGS = 
SDL_CONFIG = no
SDL_LIBS = 
SERVER_LIBS = -lreadline  -lncurses -lm
SOUND_CFLAGS =  
SOUND_LIBS =  -L/usr/lib -lesd -laudiofile -lm
UNAME = uname
UP = @UP@
USE_INCLUDED_LIBINTL = no
USE_NLS = yes
VERSION = 1.13.0
gui_sources = gui-gtk

noinst_LIBRARIES = libchecker.a

libchecker_a_SOURCES = \
ActionAssign.cc	\
ActionAssign.h	\
Action.cc	\
ActionEQ1.cc	\
ActionEQ1.h	\
ActionGEQ1.cc	\
ActionGEQ1.h	\
Action.h	\
ActionInSet.cc	\
ActionInSet.h	\
ActionNormal.cc	\
ActionNormal.h	\
ActionNotInSet.cc	\
ActionNotInSet.h	\
amodel.cc	\
amodel.h	\
aparser.cc	\
aparser.h	\
bitreader.cc	\
bitreader.h	\
bitwriter.cc	\
bitwriter.h	\
classlist.h	\
cmodel.cc	\
cmodel.h	\
common.c	\
common.h	\
cparser.cc	\
cparser.h	\
DefaultGuidance3.cc	\
DefaultGuidance3.h	\
DefaultGuidance2.cc	\
DefaultGuidance2.h	\
DefaultGuidance.cc	\
DefaultGuidance.h	\
dmodel.cc	\
dmodel.h	\
dparser.cc	\
dparser.h	\
element.cc	\
element.h	\
fieldcheck.cc	\
fieldcheck.h	\
file.cc		\
file.h		\
GenericHashtable.c	\
GenericHashtable.h	\
Guidance.cc	\
Guidance.h	\
Hashtable.cc	\
Hashtable.h	\
list.c		\
list.h		\
model.cc	\
model.h		\
normalizer.cc	\
normalizer.h	\
omodel.cc	\
omodel.h	\
oparser.cc	\
oparser.h	\
processabstract.cc	\
processabstract.h	\
processconcrete.cc	\
processconcrete.h	\
processobject.cc	\
processobject.h	\
Relation.cc	\
Relation.h	\
repair.cc	\
repair.h	\
set.cc	\
set.h	\
test.cc	\
test.h	\
tmodel.cc	\
tmodel.h	\
token.cc	\
token.h		\
typeparser.cc	\
typeparser.h	\
tmap.cc		\
tmap.h		\
redblack.c	\
redblack.h	\
cmemory.h

mkinstalldirs = $(SHELL) $(top_srcdir)/mkinstalldirs
CONFIG_HEADER = ../config.h
CONFIG_CLEAN_FILES = 
LIBRARIES =  $(noinst_LIBRARIES)


DEFS = -DHAVE_CONFIG_H -I. -I$(srcdir) -I..
CPPFLAGS = 
LDFLAGS = 
LIBS = -lz 
X_CFLAGS = 
X_LIBS = 
X_EXTRA_LIBS = 
X_PRE_LIBS = 
libchecker_a_LIBADD = 
libchecker_a_OBJECTS =  ActionAssign.o Action.o ActionEQ1.o ActionGEQ1.o \
ActionInSet.o ActionNormal.o ActionNotInSet.o amodel.o aparser.o \
bitreader.o bitwriter.o cmodel.o common.o cparser.o DefaultGuidance3.o \
DefaultGuidance2.o DefaultGuidance.o dmodel.o dparser.o element.o \
fieldcheck.o file.o GenericHashtable.o Guidance.o Hashtable.o list.o \
model.o normalizer.o omodel.o oparser.o processabstract.o \
processconcrete.o processobject.o Relation.o repair.o set.o test.o \
tmodel.o token.o typeparser.o tmap.o redblack.o
AR = ar
CXXFLAGS = 
CXXCOMPILE = $(CXX) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CXXFLAGS) $(CXXFLAGS)
CXXLD = $(CXX)
CXXLINK = $(CXXLD) $(AM_CXXFLAGS) $(CXXFLAGS) $(LDFLAGS) -o $@
CFLAGS = -g -O2 -Wall
COMPILE = $(CC) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CFLAGS) $(CFLAGS)
CCLD = $(CC)
LINK = $(CCLD) $(AM_CFLAGS) $(CFLAGS) $(LDFLAGS) -o $@
DIST_COMMON =  Makefile.am Makefile.in


DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) $(TEXINFOS) $(EXTRA_DIST)

TAR = gtar
GZIP_ENV = --best
DEP_FILES =  .deps/Action.P .deps/ActionAssign.P .deps/ActionEQ1.P \
.deps/ActionGEQ1.P .deps/ActionInSet.P .deps/ActionNormal.P \
.deps/ActionNotInSet.P .deps/DefaultGuidance.P .deps/DefaultGuidance2.P \
.deps/DefaultGuidance3.P .deps/GenericHashtable.P .deps/Guidance.P \
.deps/Hashtable.P .deps/Relation.P .deps/amodel.P .deps/aparser.P \
.deps/bitreader.P .deps/bitwriter.P .deps/cmodel.P .deps/common.P \
.deps/cparser.P .deps/dmodel.P .deps/dparser.P .deps/element.P \
.deps/fieldcheck.P .deps/file.P .deps/list.P .deps/model.P \
.deps/normalizer.P .deps/omodel.P .deps/oparser.P \
.deps/processabstract.P .deps/processconcrete.P .deps/processobject.P \
.deps/redblack.P .deps/repair.P .deps/set.P .deps/test.P .deps/tmap.P \
.deps/tmodel.P .deps/token.P .deps/typeparser.P
SOURCES = $(libchecker_a_SOURCES)
OBJECTS = $(libchecker_a_OBJECTS)

all: all-redirect
.SUFFIXES:
.SUFFIXES: .S .c .cc .o .s
$(srcdir)/Makefile.in: # Makefile.am $(top_srcdir)/configure.ac $(ACLOCAL_M4) 
	cd $(top_srcdir) && $(AUTOMAKE) --gnu checker/Makefile

Makefile: $(srcdir)/Makefile.in  $(top_builddir)/config.status $(BUILT_SOURCES)
	cd $(top_builddir) \
	  && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status


mostlyclean-noinstLIBRARIES:

clean-noinstLIBRARIES:
	-test -z "$(noinst_LIBRARIES)" || rm -f $(noinst_LIBRARIES)

distclean-noinstLIBRARIES:

maintainer-clean-noinstLIBRARIES:

.s.o:
	$(COMPILE) -c $<

.S.o:
	$(COMPILE) -c $<

mostlyclean-compile:
	-rm -f *.o core *.core

clean-compile:

distclean-compile:
	-rm -f *.tab.c

maintainer-clean-compile:

libchecker.a: $(libchecker_a_OBJECTS) $(libchecker_a_DEPENDENCIES)
	-rm -f libchecker.a
	$(AR) cru libchecker.a $(libchecker_a_OBJECTS) $(libchecker_a_LIBADD)
	$(RANLIB) libchecker.a
.cc.o:
	$(CXXCOMPILE) -c $<

tags: TAGS

ID: $(HEADERS) $(SOURCES) $(LISP)
	list='$(SOURCES) $(HEADERS)'; \
	unique=`for i in $$list; do echo $$i; done | \
	  awk '    { files[$$0] = 1; } \
	       END { for (i in files) print i; }'`; \
	here=`pwd` && cd $(srcdir) \
	  && mkid -f$$here/ID $$unique $(LISP)

TAGS:  $(HEADERS) $(SOURCES)  $(TAGS_DEPENDENCIES) $(LISP)
	tags=; \
	here=`pwd`; \
	list='$(SOURCES) $(HEADERS)'; \
	unique=`for i in $$list; do echo $$i; done | \
	  awk '    { files[$$0] = 1; } \
	       END { for (i in files) print i; }'`; \
	test -z "$(ETAGS_ARGS)$$unique$(LISP)$$tags" \
	  || (cd $(srcdir) && etags $(ETAGS_ARGS) $$tags  $$unique $(LISP) -o $$here/TAGS)

mostlyclean-tags:

clean-tags:

distclean-tags:
	-rm -f TAGS ID

maintainer-clean-tags:

distdir = $(top_builddir)/$(PACKAGE)-$(VERSION)/$(subdir)

subdir = checker

distdir: $(DISTFILES)
	here=`cd $(top_builddir) && pwd`; \
	top_distdir=`cd $(top_distdir) && pwd`; \
	distdir=`cd $(distdir) && pwd`; \
	cd $(top_srcdir) \
	  && $(AUTOMAKE) --include-deps --build-dir=$$here --srcdir-name=$(top_srcdir) --output-dir=$$top_distdir --gnu checker/Makefile
	@for file in $(DISTFILES); do \
	  d=$(srcdir); \
	  if test -d $$d/$$file; then \
	    cp -pr $$d/$$file $(distdir)/$$file; \
	  else \
	    test -f $(distdir)/$$file \
	    || ln $$d/$$file $(distdir)/$$file 2> /dev/null \
	    || cp -p $$d/$$file $(distdir)/$$file || :; \
	  fi; \
	done

DEPS_MAGIC := $(shell mkdir .deps > /dev/null 2>&1 || :)

-include $(DEP_FILES)

mostlyclean-depend:

clean-depend:

distclean-depend:
	-rm -rf .deps

maintainer-clean-depend:

%.o: %.c
	@echo '$(COMPILE) -c $<'; \
	$(COMPILE) -Wp,-MD,.deps/$(*F).pp -c $<
	@-cp .deps/$(*F).pp .deps/$(*F).P; \
	tr ' ' '\012' < .deps/$(*F).pp \
	  | sed -e 's/^\\$$//' -e '/^$$/ d' -e '/:$$/ d' -e 's/$$/ :/' \
	    >> .deps/$(*F).P; \
	rm .deps/$(*F).pp

%.lo: %.c
	@echo '$(LTCOMPILE) -c $<'; \
	$(LTCOMPILE) -Wp,-MD,.deps/$(*F).pp -c $<
	@-sed -e 's/^\([^:]*\)\.o[ 	]*:/\1.lo \1.o :/' \
	  < .deps/$(*F).pp > .deps/$(*F).P; \
	tr ' ' '\012' < .deps/$(*F).pp \
	  | sed -e 's/^\\$$//' -e '/^$$/ d' -e '/:$$/ d' -e 's/$$/ :/' \
	    >> .deps/$(*F).P; \
	rm -f .deps/$(*F).pp

%.o: %.cc
	@echo '$(CXXCOMPILE) -c $<'; \
	$(CXXCOMPILE) -Wp,-MD,.deps/$(*F).pp -c $<
	@-cp .deps/$(*F).pp .deps/$(*F).P; \
	tr ' ' '\012' < .deps/$(*F).pp \
	  | sed -e 's/^\\$$//' -e '/^$$/ d' -e '/:$$/ d' -e 's/$$/ :/' \
	    >> .deps/$(*F).P; \
	rm .deps/$(*F).pp

%.lo: %.cc
	@echo '$(LTCXXCOMPILE) -c $<'; \
	$(LTCXXCOMPILE) -Wp,-MD,.deps/$(*F).pp -c $<
	@-sed -e 's/^\([^:]*\)\.o[ 	]*:/\1.lo \1.o :/' \
	  < .deps/$(*F).pp > .deps/$(*F).P; \
	tr ' ' '\012' < .deps/$(*F).pp \
	  | sed -e 's/^\\$$//' -e '/^$$/ d' -e '/:$$/ d' -e 's/$$/ :/' \
	    >> .deps/$(*F).P; \
	rm -f .deps/$(*F).pp
info-am:
info: info-am
dvi-am:
dvi: dvi-am
check-am: all-am
check: check-am
installcheck-am:
installcheck: installcheck-am
install-exec-am:
install-exec: install-exec-am

install-data-am:
install-data: install-data-am

install-am: all-am
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
install: install-am
uninstall-am:
uninstall: uninstall-am
all-am: Makefile $(LIBRARIES)
all-redirect: all-am
install-strip:
	$(MAKE) $(AM_MAKEFLAGS) AM_INSTALL_PROGRAM_FLAGS=-s install
installdirs:


mostlyclean-generic:

clean-generic:

distclean-generic:
	-rm -f Makefile $(CONFIG_CLEAN_FILES)
	-rm -f config.cache config.log stamp-h stamp-h[0-9]*

maintainer-clean-generic:
mostlyclean-am:  mostlyclean-noinstLIBRARIES mostlyclean-compile \
		mostlyclean-tags mostlyclean-depend mostlyclean-generic

mostlyclean: mostlyclean-am

clean-am:  clean-noinstLIBRARIES clean-compile clean-tags clean-depend \
		clean-generic mostlyclean-am

clean: clean-am

distclean-am:  distclean-noinstLIBRARIES distclean-compile \
		distclean-tags distclean-depend distclean-generic \
		clean-am

distclean: distclean-am

maintainer-clean-am:  maintainer-clean-noinstLIBRARIES \
		maintainer-clean-compile maintainer-clean-tags \
		maintainer-clean-depend maintainer-clean-generic \
		distclean-am
	@echo "This command is intended for maintainers to use;"
	@echo "it deletes files that may require special tools to rebuild."

maintainer-clean: maintainer-clean-am

.PHONY: mostlyclean-noinstLIBRARIES distclean-noinstLIBRARIES \
clean-noinstLIBRARIES maintainer-clean-noinstLIBRARIES \
mostlyclean-compile distclean-compile clean-compile \
maintainer-clean-compile tags mostlyclean-tags distclean-tags \
clean-tags maintainer-clean-tags distdir mostlyclean-depend \
distclean-depend clean-depend maintainer-clean-depend info-am info \
dvi-am dvi check check-am installcheck-am installcheck install-exec-am \
install-exec install-data-am install-data install-am install \
uninstall-am uninstall all-redirect all-am all installdirs \
mostlyclean-generic distclean-generic clean-generic \
maintainer-clean-generic clean mostlyclean distclean maintainer-clean


# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
