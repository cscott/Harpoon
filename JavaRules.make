# additional rules for making java archives.
# define $(JAVASRC) before including this.

CLEANFILES = $(JARFILE)
if BUILD_JAVA_SOURCE
noinst_DATA=$(JARFILE)
endif

# determine the package a java source file belongs to.
javapkg=$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1/p')
# full dotted name of class given source file name.
javacls=$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1./p')$(notdir $(1:.java=))
# javah file name from source file name
javahdr=$(subst .,_,$(shell cat $(1) | sed -ne 's/^package *\([A-Za-z][A-Za-z0-9.]*\) *; *$$/\1./p'))$(notdir $(1:.java=.h))

# make list of .class files from JAVASRC
JAVACLS:=$(foreach f,$(JAVASRC),$(subst .,/,$(call javapkg,$(f)))/$(notdir $(f:.java=.class)))

$(addprefix classes/,$(JAVACLS)): $(JAVASRC)
	-$(RM) -rf classes
	$(JAVAC) -d classes -g $^
$(JARFILE): $(addprefix classes/,$(JAVACLS))
	$(JAR) -cf $@ -C classes .
mostlyclean clean: clean-classes
clean-classes:
	-$(RM) -rf classes

# rules to make .h files from JAVASRC
JAVAHDR:=$(foreach f,$(JAVASRC),$(dir $f)$(call javahdr,$(f)))
$(JAVAHDR): $(JAVASRC) $(JARFILE)
	javah -bootclasspath $(JARFILE) $(foreach f,$(JAVASRC),$(call javacls,$(f)))
	for f in $(JAVAHDR); do \
	  if [ `dirname $$f` != "." ] ; then \
	    mv `basename $$f` `dirname $$f` ; \
	  fi ; \
	done
