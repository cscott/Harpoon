# used by automake to generate Makefile.in

SUBDIRS = gnu.vm.stack java.lang

noinst_DATA = cpvm.jar

cpvm.jar: $(wildcard */*.java)
	-$(RM) -rf $(basename $@)
	jikes-classpath -d $(basename $@) -g $^
	cd $(basename $@) && jar -cf ../$@ .
	$(RM) -rf $(basename $@)

%.h: %.java
	javah -bootclasspath cpvm.jar `echo $< | sed -e 's:^./::' -e 's:/:.:g' -e 's:.java$::'`
	mv `echo $< | sed -e 's:^./::' -e 's:/:_:g' -e 's:.java$:.h:'` `dirname  $@`
