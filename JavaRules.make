# additional rules for making java archives.
# define $(JAVASRC) and $(JARFILE) before including this.

# determine the package a java source file belongs to.
javapkg=$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1/p')
# full dotted name of class given source file name.
javacls=$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1./p')$(notdir $(1:.java=))
# javah file name from source file name
javahdr=$(subst .,_,$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1./p'))$(notdir $(1:.java=.h))

# make list of .class files from JAVASRC
JAVACLS:=$(foreach f,$(JAVASRC),$(subst .,/,$(call javapkg,$(f)))/$(notdir $(f:.java=.class)))
# make list of .h files from JAVASRC
JAVAHDR:=$(foreach f,$(JAVASRC),$(dir $f)$(call javahdr,$(f)))


if BUILD_JAVA_SOURCE
# rebuild .h files if they go out of date
# note that we have to manually touch the files, as javah doesn't
# alter the timestamp if its output is identical to the existing file.
$(JAVAHDR): $(JAVASRC) $(JARFILE)
	$(JAVAH) -bootclasspath $(JARFILE) \
		$(foreach f,$(JAVASRC),$(call javacls,$(f)))
	for f in $(JAVAHDR); do \
	  if [ `dirname $$f` != "." ] ; then \
	    mv `basename $$f` `dirname $$f` ; \
	  fi ; \
	  touch $$f ; \
	done
endif
