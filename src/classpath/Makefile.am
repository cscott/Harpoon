# used by automake to generate Makefile.in

SUBDIRS = gnu java

cpvm.jar:
	jikes-classpath -d cpvm -g `find . -name "*.java"`
	cd cpvm && jar cf ../$@ .
	$(RM) -rf cpvm

%.h: %.java
	javah -bootclasspath cpvm.jar `echo $< | sed -e 's:^./::' -e 's:/:.:g' -e 's:.java$::'`
	mv `echo $< | sed -e 's:^./::' -e 's:/:_:g' -e 's:.java$:.h:'` `dirname  $@`
