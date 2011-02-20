## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.2 2006-12-28 17:05:22 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = $(JAVAHDR)
INCLUDES = @IFLAGS@
LIBS =

JAVASRC = \
AccessibleObject.java \
Constructor.java \
Field.java \
Member.java \
Method.java \
Modifier.java
JARFILE=reflect-thunk.jar
EXTRA_DIST=$(JAVASRC)

# include rules to make $(JARFILE) and $(JAVAHDR) from $(JAVASRC)
include $(top_srcdir)/JarRules.make

BUILT_SOURCES = $(JAVAHDR)

