## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

noinst_LTLIBRARIES = libjavalang.la

libjavalang_la_SOURCES = \
	java_lang_Class.h \
	java_lang_Runtime.h \
	java_lang_Thread.h \
	java_lang_Throwable.h \
	java_lang_VMClassLoader.h \
	java_lang_VMObject.c java_lang_VMObject.h \
	java_lang_VMSecurityManager.h \
	java_lang_VMSystem.c java_lang_VMSystem.h

JAVASRC = \
Class.java \
Runtime.java \
Thread.java \
Throwable.java \
VMClassLoader.java \
VMObject.java \
VMSecurityManager.java \
VMSystem.java
JARFILE=../cpvm.jar

EXTRA_DIST=$(JAVASRC)

# include .h file dependencies; don't try to build JARFILE.
include $(top_srcdir)/JavaRules.make
