## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libjni.la

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
