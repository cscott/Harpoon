## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libgnuvmstack.la

libgnuvmstack_la_SOURCES = \
	empty.c \
	gnu_vm_stack_StackFrame.h \
	gnu_vm_stack_StackTrace.h

JAVASRC = \
StackFrame.java \
StackTrace.java

JARFILE=../cpvm.jar

EXTRA_DIST=$(JAVASRC)

# include .h file dependencies; don't try to build JARFILE.
include $(top_srcdir)/JavaRules.make
