## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libjavalangreflect-indep.la

libjavalangreflect_indep_la_SOURCES = \
	java_lang_reflect_Array.c java_lang_reflect_Array.h \
	java_lang_reflect_Constructor.c java_lang_reflect_Constructor.h \
	java_lang_reflect_Field.c java_lang_reflect_Field.h \
	java_lang_reflect_Member.h \
	java_lang_reflect_Method.c java_lang_reflect_Method.h \
	java_lang_reflect_Modifier.h \
	reflect-util.c reflect-util.h
# XXX: Array is missing java sources.

JAVASRC = \
AccessibleObject.java \
Constructor.java \
Field.java \
Member.java \
Method.java \
Modifier.java
JARFILE=reflect-thunk.jar
EXTRA_DIST=$(JAVASRC)

# include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
