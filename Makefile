# put the location of the BBN UAV OEP distribution files ATRManip.idl,
# quo.idl, and rss.idl here.
UAVDIST=src/corba/UAV

JACORB_HOME=contrib/JacORB1_3_30
JAVAC=javac 
JAVA=java
IDLCC=$(JACORB_HOME)/bin/idl -I$(JACORB_HOME)/idl/omg
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
MANIFEST=$(wildcard src/manifest/*.MF)
SCRIPTS=$(wildcard script/*)
CONTRIB=$(shell find contrib -type f | grep -v 'CVS' | grep -v '.cvsignore')
RELEASE=$(SOURCES) README BUILDING COPYING CREDITS Makefile $(IMAGES) $(DSOURCES) 
RELEASE += $(BISOURCES) $(MANIFEST) $(SCRIPTS)
JDIRS=imagerec FrameManip omg ATRManip quo rss HTTPClient demo java_cup org

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

CP:=$(shell if test `echo $(CLASSPATH) | fgrep -c 'contrib/jacorb.jar'` == 0; \
            then if test "$(CLASSPATH)" == ""; \
	         then echo contrib/jacorb.jar; \
	         else echo contrib/jacorb.jar:$(CLASSPATH); \
	         fi; \
	    fi)

JDOCFLAGS += -classpath $(CP)

JCC = $(JAVAC) -classpath $(CP)

all:    clean doc imagerec.jar # imagerec.tgz

clean:
	@echo Cleaning up docs and jars.
	@rm -rf doc $(JDIRS) META-INF
	@rm -f *.jar *.jar.TIMESTAMP
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
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

#test.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
#	@echo Generating $@ file...
#	@rm -rf $(JDIRS)
#	@$(IDLCC) -d . $(ISOURCES)
#	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
#	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
#	@rm -rf $(GJSOURCES)
#	@$(JAR) xf contrib/jacorb.jar
#	@rm -rf META-INF
#	@$(JAR) xf movie/tank.jar
#	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) tank.gz.*
#	@rm -rf $(JDIRS)
#	@rm -rf tank.gz.*
#	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

GUI.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) xf movie/tank.jar
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) tank.gz.*
	@rm -rf $(JDIRS) 
	@rm -rf tank.gz.*
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

receiverStub.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

trackerStub.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

ATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

embeddedATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

groundATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

RTJ.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

ns.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

jars: clean doc
	@echo Generating imagerec.jar file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . $(ISOURCES)
	@$(IDLCC) -d . -I$(UAVDIST) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm imagerec.jar src/manifest/imagerec.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > imagerec.jar.TIMESTAMP
	@echo Generating receiverStub.jar file...
	@$(JAR) cfm receiverStub.jar src/manifest/receiverStub.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > receiverStub.jar.TIMESTAMP
	@echo Generating trackerStub.jar file...
	@$(JAR) cfm trackerStub.jar src/manifest/trackerStub.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > trackerStub.jar.TIMESTAMP
	@echo Generating ATR.jar file...
	@$(JAR) cfm ATR.jar src/manifest/ATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > ATR.jar.TIMESTAMP
	@echo Generating embeddedATR.jar file...
	@$(JAR) cfm embeddedATR.jar src/manifest/embeddedATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > embeddedATR.jar.TIMESTAMP
	@echo Generating groundATR.jar file...
	@$(JAR) cfm groundATR.jar src/manifest/groundATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > groundATR.jar.TIMESTAMP
	@echo Generating ns.jar file...
	@$(JAR) cfm ns.jar src/manifest/ns.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > ns.jar.TIMESTAMP
	@echo Generating RTJ.jar file...
	@$(JAR) cfm RTJ.jar src/manifest/RTJ.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > RTJ.jar.TIMESTAMP
	@echo Generating GUI.jar file...
	@$(JAR) xf movie/tank.jar
	@rm -rf META-INF
	@$(JAR) cfm GUI.jar src/manifest/GUI.jar.MF $(JDIRS) tank.gz.*
	@date '+%-d-%b-%Y at %r %Z.' > GUI.jar.TIMESTAMP
	@rm -rf tank.gz.*
	@rm -rf $(JDIRS)

imagerec.tgz: $(RELEASE) $(CONTRIB)
	@echo Generating $@ file.
	@rm -rf $@.lst
	@for x in $(RELEASE); do echo $$x >> $@.lst; done
	@find contrib -type f | grep -v 'CVS' | grep -v '.cvsignore' >> $@.lst
	@tar -cT $@.lst | gzip -9 > $@
	@rm -rf $@.lst
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

jar-install: jars
	@echo Installing jars.
	tar -c *.jar *.jar.TIMESTAMP | \
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