# put the location of the BBN UAV OEP distribution files ATRManip.idl,
# quo.idl, and rss.idl here.
UAVDIST=src/corba/UAV

JCC=javac 
JAVA=java
IDLCC=idl -I$(JACORB_HOME)/idl/omg
JAR=jar
JDOC=javadoc
SSH=ssh
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
ISOURCES=$(wildcard src/graph/*.idl src/corba/*.idl)
BISOURCES=$(wildcard src/corba/UAV/*.idl)
JSOURCES=$(wildcard src/*.java src/graph/*.java src/util/*.java src/corba/*.java)
GJSOURCES1=imagerec/graph/*.java imagerec/corba/*.java FrameManip/*.java
GJSOURCES=$(GJSOURCES1) omg/org/CosPropertyService/*.java ATRManip/*.java quo/*.java rss/*.java
RTJSOURCES=$(wildcard src/rtj/*.java)
STUBSOURCES=$(wildcard src/rtj/stubs/*.java)
SOURCES=$(JSOURCES) $(ISOURCES) $(RTJSOURCES) $(STUBSOURCES)
IMAGES=$(wildcard dbase/plane/*gz* dbase/plane/*.jar dbase/tank/*.jar) $(wildcard movie/*gz* movie/*.jar)
DSOURCES=$(wildcard paper/README paper/p* src/*.html src/graph/*.html)
DSOURCES += $(wildcard src/util/*.html src/corba/*.html src/rtj/*.html)
DSOURCES += $(wildcard src/rtj/stubs/*.html)
MANIFEST=src/MANIFEST.MF
RELEASE=$(SOURCES) README BUILDING COPYING Makefile $(IMAGES) $(DSOURCES) $(BISOURCES) $(MANIFEST)
JDIRS=imagerec FrameManip omg ATRManip quo rss

# figure out what the current CVS branch is, by looking at the Makefile
CVS_TAG=$(firstword $(shell cvs status Makefile | grep -v "(none)" | \
		awk '/Sticky Tag/{print $$3}'))
CVS_REVISION=$(patsubst %,-r %,$(CVS_TAG))

# construct the flags for JavaDoc
JDOCFLAGS:=-J-mx128m -version -author -breakiterator \
	   -doctitle "MIT ATR Program Documentation" \
	   -quiet -private -linksource 
JDOCGROUPS:=\
  -group "MIT Image Recognition Program" "imagerec*" \
  -group "BBN UAV Interface" "CosPropertyService*:FrameManip*:omg*:ATRManip*:quo*:rss*"
JDKDOCLINK=http://java.sun.com/j2se/1.4/docs/api
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-link " > /dev/null ; \
	then echo -link $(JDKDOCLINK) ; fi) 
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-group " > /dev/null ; \
	then echo '$(JDOCGROUPS)' ; fi)

all:    clean doc imagerec.jar # imagerec.tgz

clean:
	@echo Cleaning up docs and imagerec.jar.
	@rm -rf doc $(JDIRS)
	@rm -f imagerec.jar imagerec.jar.TIMESTAMP
	@rm -f imagerec.tgz imagerec.tgz.TIMESTAMP
#	@rm -f ChangeLog

doc:	$(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating documentation...
	@rm -rf doc $(JDIRS)
	@mkdir -p doc
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JDOC) $(JDOCFLAGS) -d doc/ $(JSOURCES) $(GJSOURCES) > doc/STATUS
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	@chmod -R a+rX doc

imagerec.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating imagerec.jar file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -f $(GJSOURCES)
	@$(JAR) -cfm $@ $(MANIFEST) $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

imagerec.tgz: $(RELEASE)
	@echo Generating $@ file.
	@tar -c $(RELEASE) | gzip -9 > $@
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

jar-install: imagerec.jar
	@echo Installing imagerec.jar.
	tar -c imagerec.jar imagerec.jar.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

tar-install: imagerec.tgz
	@echo Installing imagerec.tgz.
	tar -c imagerec.tgz imagerec.tgz.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

doc-install: doc
	@echo Installing documentation.
	tar -c doc | \
		$(SSH) $(INSTALLMACHINE) \
	"mkdir -p $(INSTALLDIR)/ImageRec && /bin/rm -rf $(INSTALLDIR)/ImageRec/doc && tar -C $(INSTALLDIR)/ImageRec -x"

install: jar-install tar-install doc-install

ChangeLog: needs-cvs $(JSOURCES) # dependency is not strictly accurate.
#	@echo Generating ChangeLog...
#	@-$(RM) $@
#	@rcs2log | sed -e 's:/[^,]*/CVSROOT/ImageRec/::g' > $@

update: needs-cvs
	cvs -q update -Pd $(CVS_REVISION)
	@-if [ -x $(FORTUNE) ]; then echo ""; $(FORTUNE); fi

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	   exit 1; \
	fi

run: doc imagerec.jar
	@echo Running the program...
	@$(JAVA) -jar imagerec.jar

run-nodoc: imagerec.jar
	@echo Running the program...
	@$(JAVA) -jar imagerec.jar
