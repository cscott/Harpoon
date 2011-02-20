# used by automake to generate Makefile.in
# $Id: Makefile.am,v 1.9 2006-12-28 17:05:21 cananian Exp $

# build .jar file before recursing into subdirs, as the rules to
# build the .h files from the .java source requires that the .jar
# already be built.
SUBDIRS = . gnu.vm.stack java.io java.lang java.lang.reflect 

JAVASRC=$(wildcard */*.java)
JARFILE=cpvm.jar

#include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
