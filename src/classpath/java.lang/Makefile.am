## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.15 2007-03-23 17:48:17 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libjavalang.la

libjavalang_la_SOURCES = \
	java_lang_Class.c java_lang_Class.h \
	java_lang_Console.c java_lang_Console.h \
	java_lang_Throwable.h \
	java_lang_VMClassLoader.c java_lang_VMClassLoader.h \
	java_lang_VMMain.c java_lang_VMMain.h \
	java_lang_VMObject.c java_lang_VMObject.h \
	java_lang_VMRuntime.c java_lang_VMRuntime.h \
	java_lang_VMSecurityManager.c java_lang_VMSecurityManager.h \
	java_lang_VMSystem.c java_lang_VMSystem.h \
	java_lang_VMThread.c java_lang_VMThread.h

JAVASRC = \
Class.java \
Console.java \
Runtime.java \
Thread.java \
Throwable.java \
VMClassLoader.java \
VMMain.java \
VMObject.java \
VMRuntime.java \
VMSecurityManager.java \
VMSystem.java \
VMThread.java
JARFILE=../cpvm.jar

EXTRA_DIST=$(JAVASRC)

# include .h file dependencies; don't try to build JARFILE.
include $(top_srcdir)/JavaRules.make
