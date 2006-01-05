## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.20 2006-01-05 20:29:41 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
INCLUDES = @IFLAGS@
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libjni.la
else
noinst_LIBRARIES = libjni.a
endif

# a.out build needs to put jni-jnidata.c *after* libflexc
if AOUT_BUILD
all: jni-inflate.lo jni-jnidata.lo
JNIDATA=
else
JNIDATA=jni-inflate.c jni-jnidata.c
endif
libjni_la_SOURCES = \
	jni-invoke.c jni-vtable.c jni-stubs.c jni-lookup.c jni-ref.c \
	jni-exceptions.c jni-init.c jni-object.c jni-field.c jni-string.c \
	jni-array.c jni-alloc.c jni-monitor.c hashimpl.h $(JNIDATA)

libjni_a_SOURCES = $(libjni_la_SOURCES)
