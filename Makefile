#
# Determine whether we're running Cygwin or ordinary linux
CYGWIN=$(shell grep -c 'CYGWIN' /proc/version)

# put the location of the BBN UAV OEP distribution files ATRManip.idl,
# quo.idl, and rss.idl here.
UAVDIST=src/corba/UAV

JACORB_HOME=contrib/JacORB1_3_30

JAVA_HOME=$(shell if test $(CYGWIN) -eq 0; then echo `cd /usr/java/j2s*; pwd`; \
		  else echo /cygdrive/c/j2*; \
		  fi)
JAVAC=$(JAVA_HOME)/bin/javac
JAVAI=$(JAVA_HOME)/bin/java
JAR=$(JAVA_HOME)/bin/jar
JDOC=javadoc
SSH=ssh
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
ISOURCES=$(wildcard src/graph/*.idl src/corba/*.idl)
BISOURCES=$(wildcard src/corba/UAV/*.idl)
JSOURCES=$(wildcard src/*.java src/graph/*.java src/util/*.java src/corba/*.java src/ipaq/*.java)
GJSOURCES1=imagerec/graph/*.java imagerec/corba/*.java FrameManip/*.java
GJSOURCES=$(GJSOURCES1) omg/org/CosPropertyService/*.java ATRManip/*.java quo/*.java rss/*.java
ICHANNEL_SOURCES=*.idl
JCHANNEL_SOURCES=RtecBase/*.java RtecDefaultEventData/*.java RtecEventChannelAdmin/*.java RtecEventChannelAdmin/EventChannelPackage/*.java RtecEventComm/*.java RtecScheduler/*.java TimeBase/*.java
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
EVENTDIRS=com TimeBase RtecBase RtecDefaultEventData RtecEventChannelAdmin RtecEventComm RtecScheduler
JDIRS=imagerec FrameManip omg ATRManip quo rss HTTPClient demo java_cup org ipaq
ZDIRS=edu

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

JACORB_JAR=$(shell echo $(CLASSPATH) | grep -c 'contrib/jacorb.jar')

CP:=$(shell if test $(CYGWIN) -eq 0; \
	     then if test $(JACORB_JAR) -eq 0; \
                  then if test $(CLASSPATH)z == z; \
	               then echo `pwd`/contrib/jacorb.jar:$(JACORB_HOME)/lib/idl.jar:`pwd`/contrib/lm_eventChannel.jar:`pwd`/contrib/zen.jar; \
	               else echo `pwd`/contrib/jacorb.jar:$(JACORB_HOME)/lib/idl.jar:`pwd`/contrib/lm_eventChannel.jar:`pwd`/contrib/zen.jar:$(CLASSPATH); \
	               fi; \
	          fi; \
	     else echo \`cygpath -da contrib/jacorb.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`\\\\\\\;\`cygpath -da $(JACORB_HOME)/lib/idl.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`\\\\\\\;\`cygpath -da contrib/lm_eventChannel.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`\\\\\\\;\`cygpath -da contrib/zen.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\` ; \
	     fi)

CP_ZEN:=$(shell if test $(CYGWIN) -eq 0; \
	     then if test $(JACORB_JAR) -eq 0; \
                  then if test $(CLASSPATH)z == z; \
	               then echo `pwd`/contrib/zen.jar:`pwd`/contrib/lm_eventChannel.jar:`pwd`/contrib/jacorb.jar:$(JAVA_HOME)/jre/lib/rt.jar:$(JAVA_HOME)/lib/tools.jar; \
	               else echo `pwd`/contrib/zen.jar:`pwd`/contrib/lm_eventChannel.jar:`pwd`/contrib/jacorb.jar:$(JAVA_HOME)/jre/lib/rt.jar:$(JAVA_HOME)/lib/tools.jar$(CLASSPATH); \
	               fi; \
	          fi; \
	     else echo \`cygpath -da contrib/zen.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`\\\\\\\;\`cygpath -da contrib/lm_eventChannel.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`\\\\\\\;\`cygpath -da contrib/jacorb.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`;\`cygpath -da $(JAVA_HOME)/jre/lib/rt.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\`;\`cygpath -da $(JAVA_HOME)/lib/tools.jar \| awk \'\{print gensub\(\/\\\\\\\/,\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\"G\"\)\}\'\` ; \
	     fi)

JAVA = $(JAVAI) -classpath $(CP)

IDL_FLAGS=-I$(JACORB_HOME)/idl/omg

IDL_FLAGS_ZEN=-Dorg.omg.CORBA.ORBClass=edu.uci.ece.zen.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=edu.uci.ece.zen.orb.ORBSingleton

ZEN_IDLS=$(ISOURCES) $(BISOURCES)

ZEN_CHANNEL_IDLS=$(ICHANNEL_SOURCES)

IDLCC=$(JAVAI) -classpath $(CP) org.jacorb.idl.parser $(IDL_FLAGS)

IDLCC_ZEN=$(JAVAI) -Xbootclasspath:$(CP_ZEN) $(IDL_FLAGS_ZEN) edu.uci.ece.zen.xidl.Idl

JDOCFLAGS += -classpath $(CP)

JCC = $(JAVAC) -classpath $(CP)

all:    clean doc imagerec.jar # imagerec.tgz

clean:
	@echo Cleaning up docs and jars.
	@rm -rf doc $(JDIRS) $(EVENTDIRS) $(ZDIRS) META-INF
	@rm -f *.jar *.jar.TIMESTAMP imagerec.tgz imagerec.tgz.TIMESTAMP *.idl
#	@rm -f ChangeLog

doc:	$(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating documentation...
	@rm -rf doc $(JDIRS)
	@mkdir -p doc
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JDOC) $(JDOCFLAGS) -d doc/ $(JSOURCES) $(GJSOURCES) > doc/STATUS
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	@chmod -R a+rX doc

imagerec.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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

GUI.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES) movie/tank.jar
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

receiverStub-ZEN.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/jacorb.jar
#	@$(IDLCC_ZEN) -o . $(ZEN_IDLS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/zen.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) $(ZDIRS)
	@rm -rf $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP


trackerStub.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

trackerStub-ZEN.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/jacorb.jar
#	@$(IDLCC_ZEN) -o . $(ZEN_IDLS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/zen.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) $(ZDIRS)
	@rm -rf $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

ATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/lm_eventChannel.jar
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(ICHANNEL_SOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES) $(JCHANNEL_SOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) $(EVENTDIRS)
	@rm -rf $(JDIRS) $(EVENTDIRS) *.idl
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

ATR-ZEN.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/jacorb.jar
	@$(JAR) xf contrib/lm_eventChannel.jar
#	@$(IDLCC_ZEN) -o . $(ZEN_IDLS) $(ZEN_CHANNEL_IDLS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(ICHANNEL_SOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES) $(JCHANNEL_SOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/zen.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) $(EVENTDIRS) $(ZDIRS)
	@rm -rf $(JDIRS) $(EVENTDIRS) *.idl $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

embeddedATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

groundManual.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

embeddedManual.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoGroundATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoEmbeddedATR.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoReceiverStub.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoTrackerStub.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoIPAQ.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

carDemoIPAQ2.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

ns-ZEN.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/jacorb.jar
#	@$(IDLCC_ZEN) -d . $(ZEN_IDLS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/zen.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) $(ZDIRS)
	@rm -rf $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

buffer.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES)
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS)
	@rm -rf $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

Watermark.jar: $(ISOURCES) $(JSOURCES) $(RTJSOURCES) movie/tank.jar
	@echo Generating $@ file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/jacorb.jar
	@rm -rf META-INF
	@$(JAR) xf movie/tank.jar
	@$(JAR) cfm $@ src/manifest/$@.MF $(JDIRS) tank.gz.*
	@rm -rf $(JDIRS) 
	@rm -rf tank.gz.*
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

movie/tank.jar:
	@echo Downloading the tank movie...
	@wget -nv http://www.flex-compiler.lcs.mit.edu/Harpoon/ImageRec/tank.jar -O movie/tank.jar

jars: clean doc movie/tank.jar
	@echo Generating imagerec.jar file...
	@rm -rf $(JDIRS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
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

	@echo Generating embeddedATR.jar file...
	@$(JAR) cfm embeddedATR.jar src/manifest/embeddedATR.jar.MF $(JDIRS)

	@echo Generating embeddedManual.jar file...
	@$(JAR) cfm embeddedManual.jar src/manifest/embeddedManual.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > embeddedATR.jar.TIMESTAMP

	@echo Generating groundATR.jar file...
	@$(JAR) cfm groundATR.jar src/manifest/groundATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > groundATR.jar.TIMESTAMP

	@echo Generating groundManual.jar file...
	@$(JAR) cfm groundManual.jar src/manifest/groundManual.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > groundManual.jar.TIMESTAMP

	@echo Generating carDemoGroundATR.jar file...
	@$(JAR) cfm carDemoGroundATR.jar src/manifest/carDemoGroundATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoGroundATR.jar.TIMESTAMP

	@echo Generating carDemoEmbeddedATR.jar file...
	@$(JAR) cfm carDemoEmbeddedATR.jar src/manifest/carDemoEmbeddedATR.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoEmbeddedATR.jar.TIMESTAMP

	@echo Generating carDemoReceiverStub.jar file...
	@$(JAR) cfm carDemoReceiverStub.jar src/manifest/carDemoReceiverStub.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoReceiverStub.jar.TIMESTAMP

	@echo Generating carDemoTrackerStub.jar file...
	@$(JAR) cfm carDemoTrackerStub.jar src/manifest/carDemoTrackerStub.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoTrackerStub.jar.TIMESTAMP

	@echo Generating carDemoIPAQ.jar file...
	@$(JAR) cfm carDemoIPAQ.jar src/manifest/carDemoIPAQ.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoIPAQ.jar.TIMESTAMP

	@echo Generating carDemoIPAQ2.jar file...
	@$(JAR) cfm carDemoIPAQ2.jar src/manifest/carDemoIPAQ2.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > carDemoIPAQ2.jar.TIMESTAMP

	@echo Generating ns.jar file...
	@$(JAR) cfm ns.jar src/manifest/ns.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > ns.jar.TIMESTAMP

	@echo Generating RTJ.jar file...
	@$(JAR) cfm RTJ.jar src/manifest/RTJ.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > RTJ.jar.TIMESTAMP

	@echo Generating buffer.jar file...
	@$(JAR) cfm buffer.jar src/manifest/buffer.jar.MF $(JDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > buffer.jar.TIMESTAMP

	@echo Generating ATR.jar file...
	@$(JAR) xf contrib/lm_eventChannel.jar
	@$(IDLCC) -d . $(ICHANNEL_SOURCES)
	@$(JCC) -d . -g $(JCHANNEL_SOURCES)
	@$(JAR) cfm ATR.jar src/manifest/ATR.jar.MF $(JDIRS) $(EVENTDIRS)
	@rm -rf $(EVENTDIRS) *.idl
	@date '+%-d-%b-%Y at %r %Z.' > ATR.jar.TIMESTAMP

	@echo Generating Watermark.jar file...
	@$(JAR) cfm Watermark.jar src/manifest/Watermark.jar.MF $(JDIRS) 
	@date '+%-d-%b-%Y at %r %Z.' > Watermark.jar.TIMESTAMP

	@echo Generating GUI.jar file...
	@$(JAR) xf movie/tank.jar
	@rm -rf META-INF
	@$(JAR) cfm GUI.jar src/manifest/GUI.jar.MF $(JDIRS) tank.gz.*
	@date '+%-d-%b-%Y at %r %Z.' > GUI.jar.TIMESTAMP
	@rm -rf tank.gz.*
	@rm -rf *idl
	@rm -rf $(JDIRS)

	@echo Generating receiverStub-ZEN.jar file...
	@rm -rf $(JDIRS)
	@$(JAR) xf contrib/jacorb.jar
#	@$(IDLCC_ZEN) -o . $(ZEN_IDLS)
	@$(IDLCC) -d . -I$(UAVDIST) $(ISOURCES) $(BISOURCES)
	@$(JCC) -d . -g $(JSOURCES) $(GJSOURCES)
	@rm -rf $(GJSOURCES)
	@$(JAR) xf contrib/zen.jar
	@rm -rf META-INF
	@$(JAR) cfm receiverStub-ZEN.jar src/manifest/receiverStub-ZEN.jar.MF $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > receiverStub-ZEN.jar.TIMESTAMP

	@echo Generating trackerStub-ZEN.jar file...
	@$(JAR) cfm trackerStub-ZEN.jar src/manifest/trackerStub-ZEN.jar.MF $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > trackerStub-ZEN.jar.TIMESTAMP

	@echo Generating ns-ZEN.jar file...
	@$(JAR) cfm ns-ZEN.jar src/manifest/ns-ZEN.jar.MF $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > ns-ZEN.jar.TIMESTAMP

	@echo Generating ATR-ZEN.jar file...
	@$(JAR) xf contrib/lm_eventChannel.jar
#	@$(IDLCC_ZEN) -o . $(ZEN_CHANNEL_IDLS)
	@$(IDLCC) -d . $(ICHANNEL_SOURCES)
	@$(JCC) -d . -g $(JCHANNEL_SOURCES)
	@$(JAR) cfm ATR-ZEN.jar src/manifest/ATR-ZEN.jar.MF $(JDIRS) $(ZDIRS)
	@rm -rf $(EVENTDIRS) *.idl $(JDIRS) $(ZDIRS)
	@date '+%-d-%b-%Y at %r %Z.' > ATR-ZEN.jar.TIMESTAMP


cardemo: carDemoReceiverStub.jar carDemoTrackerStub.jar carDemoEmbeddedATR.jar carDemoGroundATR.jar carDemoIPAQ.jar carDemoIPAQ2.jar

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

run-ipaq: carDemoIPAQ.jar
	@echo Running the program...
	@$(JAVA) -jar carDemoIPAQ.jar
