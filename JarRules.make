# additional rules for making java archives.
# define $(JAVASRC) and $(JARFILE) before including this.

#CLEANFILES = $(JARFILE) $(EXTRA_CLEANFILES)
if BUILD_JAVA_SOURCE
noinst_DATA=$(JARFILE)
endif

include $(top_srcdir)/JavaRules.make

$(addprefix classes/,$(JAVACLS)): $(JAVASRC)
	-$(RM) -rf classes
	mkdir -p classes
	$(JAVAC) -bootclasspath $(BOOTCP) -d classes -g $^
$(JARFILE): $(addprefix classes/,$(JAVACLS))
	$(JAR) -cf $@ -C classes .
mostlyclean clean: clean-classes
clean-classes:
	-$(RM) -rf classes
