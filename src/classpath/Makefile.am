# used by automake to generate Makefile.in

# build .jar file before recursing into subdirs, as the rules to
# build the .h files from the .java source requires that the .jar
# already be built.
SUBDIRS = . gnu.vm.stack java.lang

JAVASRC=$(wildcard */*.java)
JARFILE=cpvm.jar

#include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
