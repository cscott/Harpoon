## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in configure aclocal.m4 \
		       include/config.h.in include/stamp-h.in include/asm \
		       scripts/config.guess scripts/config.sub \
		       scripts/install-sh scripts/ltconfig scripts/ltmain.sh \
		       scripts/missing scripts/mkinstalldirs
CONFIG_STATUS_DEPENDENCIES = scripts/gen-link-script
CLEANFILES = configure.done
EXTRA_DIST = AUTHORS COPYING INSTALL NEWS README REQUIRED \
	include/README include/config.h.in include/flexthread.h \
	include/fni-stats.h include/fni-threadstate.h include/fni-wrap.h \
	include/fni-ptroff.h \
	include/fni-objsize.h include/jni-gcthreads.h include/memstats.h \
	include/jni-func.h include/jni-gc.h include/jni-link.h \
	include/jni-private.h include/jni-types.h include/jni.h \
	include/precisec.c include/precisec.h include/sym.h \
	scripts/config.guess scripts/config.sub scripts/gen-link-script \
	scripts/install-sh scripts/ltmain.sh \
	scripts/missing scripts/mkinstalldirs scripts/flexrun-config.in
SUBDIRS = Contrib arch src

CVS_TAG=$(firstword $(shell cvs status Makefile.am | grep -v "(none)" | \
		@AWK@ '/Sticky Tag/{print $$3}'))
CVS_REVISION=$(patsubst %,-r %,$(CVS_TAG))

list:
	@find . -path './Contrib' -prune -o "(" -name "*.[ch]" -o -name "*.java" ")" -print

update: needs-cvs
	cvs -q update -Pd $(CVS_REVISION)
	@-if [ -x @FORTUNE@ ]; then echo ""; @FORTUNE@; fi

ChangeLog: needs-cvs
	-$(RM) $@
	rcs2log | sed -e 's:/[^,]*/CVSROOT/Runtime/::g' > $@
dist: ChangeLog

flexrun-config: scripts/flexrun-config
	cp $< $@
	chmod 755 $@
all: flexrun-config

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	  exit 1; \
	fi

.PHONY: list update needs-cvs

run%: %-Java.a src/libflexc0.la src/libflexc1.la all
	@rm -f $@
	$(LIBTOOL) --mode=link $(CC) $(STATIC_FLAG) $(AM_CFLAGS) $(CFLAGS) $(LDFLAGS) -o $@ -Wl,-Tinclude/asm/link-script "-Wl,-(" src/libflexc0.la $< src/libflexc1.la "-Wl,-)" @GC_LIB@ @LIBS@

## automake version 1.4 is required.
AUTOMAKE_OPTIONS = 1.4
