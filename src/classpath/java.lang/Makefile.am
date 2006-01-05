## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.13 2006-01-05 20:29:40 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = *~
EXTRA_CLEANFILES = *~
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libjavalang.la
endif

libjavalang_la_SOURCES = \
	java_lang_Class.c java_lang_Class.h \
	java_lang_Console.c java_lang_Console.h \
	java_lang_Thread.c java_lang_Thread.h \
	java_lang_Throwable.h \
	java_lang_VMClassLoader.c java_lang_VMClassLoader.h \
	java_lang_VMObject.c java_lang_VMObject.h \
	java_lang_VMRuntime.c java_lang_VMRuntime.h \
	java_lang_VMSecurityManager.c java_lang_VMSecurityManager.h \
	java_lang_VMSystem.c java_lang_VMSystem.h


## START [AS] 12/09/05 - plain, static .a libs 
noinst_LIBRARIES = libjavalang.a
libjavalang_a_SOURCES = $(libjavalang_la_SOURCES)
## END [AS] 12/09/05 - plain, static .a libs 


JAVASRC = \
Class.java \
Console.java \
Runtime.java \
Thread.java \
Throwable.java \
VMClassLoader.java \
VMObject.java \
VMRuntime.java \
VMSecurityManager.java \
VMSystem.java
JARFILE=../cpvm.jar

EXTRA_DIST=$(JAVASRC)

# include .h file dependencies; don't try to build JARFILE.
include $(top_srcdir)/JavaRules.make
