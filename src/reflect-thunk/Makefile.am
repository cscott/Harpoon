## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.1 2005-11-28 06:23:51 salcianu Exp $
MAINTAINERCLEANFILES = Makefile.in
EXTRA_CLEANFILES = *~ $(JAVAHDR)
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

