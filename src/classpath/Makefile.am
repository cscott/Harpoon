# used by automake to generate Makefile.in

SUBDIRS = gnu.vm.stack java.lang

JAVASRC=$(wildcard */*.java)
JARFILE=cpvm.jar

#include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JavaRules.make

%.h: %.java
	javah -bootclasspath cpvm.jar `echo $< | sed -e 's:^./::' -e 's:/:.:g' -e 's:.java$::'`
	mv `echo $< | sed -e 's:^./::' -e 's:/:_:g' -e 's:.java$:.h:'` `dirname  $@`
