## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libuser.la
if ARM32
ENGINE=engine-arm32-linux-1.0.c
ENGINEH=engine-arm32-linux-1.0.h
else
ENGINE=engine-i386-linux-1.0.c
ENGINEH=engine-i386-linux-1.0.h
endif

libuser_la_SOURCES = threads.c $(ENGINE) threads.h $(ENGINEH)
