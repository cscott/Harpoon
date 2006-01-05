## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
EXTRA_CLEANFILES = *~
INCLUDES = @IFLAGS@
LDFLAGS =
LIBS =

if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libgnuvmstack.la
endif

libgnuvmstack_la_SOURCES = \
	empty.c \
	gnu_vm_stack_StackFrame.h \
	gnu_vm_stack_StackTrace.h


## START [AS] 12/09/05 - plain, static .a libs 
noinst_LIBRARIES = libgnuvmstack.a
libgnuvmstack_a_SOURCES = $(libgnuvmstack_la_SOURCES)
## END [AS] 12/09/05 - plain, static .a libs


JAVASRC = \
StackFrame.java \
StackTrace.java

JARFILE=../cpvm.jar

EXTRA_DIST=$(JAVASRC)

# include .h file dependencies; don't try to build JARFILE.
include $(top_srcdir)/JavaRules.make
