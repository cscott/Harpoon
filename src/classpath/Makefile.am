# used by automake to generate Makefile.in
# $Id: Makefile.am,v 1.8 2005-11-28 06:26:27 salcianu Exp $

# build .jar file before recursing into subdirs, as the rules to
# build the .h files from the .java source requires that the .jar
# already be built.
SUBDIRS = . gnu.vm.stack java.io java.lang java.lang.reflect 

EXTRA_CLEANFILES = *~

JAVASRC=$(wildcard */*.java)
JARFILE=cpvm.jar

#include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
