# additional rules for making java archives.
# define $(JAVASRC) and $(JARFILE) before including this.

CLEANFILES = $(JARFILE)
if BUILD_JAVA_SOURCE
noinst_DATA=$(JARFILE)
endif

include $(top_srcdir)/JavaRules.make

$(addprefix classes/,$(JAVACLS)): $(JAVASRC)
	-$(RM) -rf classes
	mkdir -p classes
	$(JAVAC) -d classes -g $^
$(JARFILE): $(addprefix classes/,$(JAVACLS))
	$(JAR) -cf $@ -C classes .
mostlyclean clean: clean-classes
clean-classes:
	-$(RM) -rf classes
