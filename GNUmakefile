# $Id: GNUmakefile,v 1.94 2003-07-21 21:25:57 cananian Exp $
# CAUTION: this makefile doesn't work with GNU make 3.77
#          it works w/ make 3.79.1, maybe some others.

empty:=
space:= $(empty) $(empty)

JAVA:=java
JFLAGS=-d . -g
JFLAGSVERB=#-verbose -J-Djavac.pipe.output=true
JIKES:=jikes $(JIKES_OPT)
JCC:=javac -J-mx64m -source 1.4
# find a gj compiler.  tries $JIKES first, then $JCC, then $JAVAC, then
# $(JSR14DISTR)/scripts/javac, then some other paths.  Set JSR14_v1
# in your environment to skip the check.
export JAVAC JIKES JCC
JSR14_v1?=$(shell chmod u+x bin/find-gj ; bin/find-gj) #-warnunchecked
# hard-coded path to JSR-14 v2 compiler; set JSR14_v2 in your environment
# to override.
JSR14_v2?=bin/jsr14-2.0 -source 1.5
JDOC:=sinjdoc
JAR=jar
JDOCFLAGS:=-J-mx128m -version -author -breakiterator \
  -overview overview.html -doctitle "FLEX API documentation"
JDOCGROUPS:=\
  -group "Basic Class/Method/Field handling" "harpoon.ClassFile*" \
  -group "Intermediate Representations" "harpoon.IR*:harpoon.Temp*" \
  -group "Interpreters for IRs" "harpoon.Interpret*" \
  -group "Analyses and Transformations" "harpoon.Analysis*:harpoon.Runtime*" \
  -group "Backends (including code selection and runtime support)" "harpoon.Backend*" \
  -group "Tools and Utilities" "harpoon.Tools*:harpoon.Util*" \
  -group "Contributed packages" "gnu.*"
JDOCIMAGES=/usr/local/jdk/docs/api/images
SSH=ssh
#SCP=scp -q # not needed anymore, and deprecated.
MUNGE=bin/munge
UNMUNGE=bin/unmunge
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@flex-compiler.csail.mit.edu
INSTALLDIR=public_html/Harpoon/
JDKDOCLINK = http://java.sun.com/j2se/1.4/docs/api
#JDKDOCLINK = http://java.sun.com/products/jdk/1.2/docs/api
#JDKDOCLINK = http://palmpilot.lcs.mit.edu/~pnkfelix/jdk-javadoc/java.sun.com/products/jdk/1.2/docs/api
CVSWEBLINK = http://flex-cvs.csail.mit.edu/cgi-bin/viewcvs.cgi

# make this file work with make version less than 3.77
ifndef CURDIR
	CURDIR := $(shell pwd)
endif
# Add "-link" to jdk's javadoc if we are using a javadoc that supports it
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-link " > /dev/null ; \
	then echo -link $(JDKDOCLINK) ; fi)
# Add "-group" options to JDOCFLAGS if we're using a javadoc that supports it
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-group " > /dev/null ; \
	then echo '$(JDOCGROUPS)' ; fi)

SUPPORT := Support/Lex.jar Support/CUP.jar Support/jasmin.jar \
	   $(wildcard SupportNP/ref.jar) $(wildcard SupportNP/collections.jar)
SUPPORTP := $(filter-out SupportNP/%,$(SUPPORT))
# filter out collections.jar if we don't need it.
ifeq (0, ${MAKELEVEL})
SUPPORTC:= $(filter-out \
   $(shell chmod u+x bin/test-collections;\
           if bin/test-collections; then echo SupportNP/collections.jar; fi),\
   $(SUPPORT))
CLASSPATH:=$(subst $(space),:,$(addprefix $(CURDIR)/,$(SUPPORTC))):$(CLASSPATH)
CLASSPATH:=.:$(CLASSPATH)
endif
# cygwin compatibility
ifdef CYGWIN
CLASSPATH:=$(shell cygpath -w -p $(CLASSPATH))
JAVA:=$(JAVA) -cp "$(CLASSPATH)"
JIKES:=$(JIKES) -classpath "$(CLASSPATH);c:/jdk1.3.0_01/jre/lib/rt.jar"
else
	export CLASSPATH
endif

CVS_TAG=$(firstword $(shell cvs status GNUmakefile | grep -v "(none)" | \
			awk '/Sticky Tag/{print $$3}'))
CVS_BRANCH=$(firstword $(shell cvs status GNUmakefile | grep -v "(none)" |\
			awk '/Sticky Tag/{print $$5}' | sed -e 's/[^0-9.]//g'))
CVS_REVISION=$(patsubst %,-r %,$(CVS_TAG))

# skip these definitions if we invoke make recursively; use parent's defs.
ifeq (0, ${MAKELEVEL})
BUILD_IGNORE := $(strip $(shell if [ -f .ignore ]; then cat .ignore; fi))

ALLPKGS := $(shell find . -type d | grep -v CVS | grep -v AIRE | \
		$(patsubst %,egrep -v "%" |,$(BUILD_IGNORE)) \
		egrep -v "^[.]/(harpoon|silicon|gnu|(src)?doc|NOTES|bin|jdb)"|\
		egrep -v "^[.]/(as|java_cup)" | \
		egrep -v "/doc-files" | \
		sed -e "s|^[.]/*||")

SCRIPTS := bin/test-collections bin/annotate.perl $(MUNGE) $(UNMUNGE) \
	   bin/source-markup.perl bin/cvsblame.pl

MACHINE_SRC := Tools/PatMat/Lexer.jlex Tools/PatMat/Parser.cup \
               Tools/Annotation/Java12.cup \
	       $(wildcard Runtime/AltArray/*JavaType.jt)
MACHINE_GEN := Tools/PatMat/Lexer.java Tools/PatMat/Parser.java \
               Tools/Annotation/Java12.java \
               Tools/PatMat/Sym.java Tools/Annotation/Sym.java \
	       $(foreach file, $(patsubst %JavaType.jt,%,\
				$(wildcard Runtime/AltArray/*JavaType.jt)),\
		$(file)Boolean.java $(file)Byte.java $(file)Short.java\
		$(file)Int.java $(file)Long.java $(file)Float.java\
		$(file)Double.java $(file)Char.java $(file)Object.java)

CGSPECS:=$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.spec))
CGJAVA :=$(patsubst %.spec,%.java,$(CGSPECS))
MACHINE_SRC+=$(CGSPECS)
MACHINE_GEN+=$(filter-out Tools/PatMat/% Backend/Jouette/%,$(CGJAVA))
# apply .ignore to MACHINE_GEN
MACHINE_GEN := $(filter-out .%.java $(patsubst %,\%%,$(BUILD_IGNORE)),\
		$(MACHINE_GEN))

ALLSOURCE :=  $(MACHINE_GEN) $(filter-out $(MACHINE_GEN), \
		$(filter-out .%.java \#% $(patsubst %,\%%,$(BUILD_IGNORE)),\
		$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.java))))
JARPKGS := $(subst harpoon/Contrib,gnu, \
		$(foreach pkg, $(filter-out JavaChip%, \
			$(filter-out Test%,$(ALLPKGS))), harpoon/$(pkg)))
PROPERTIES:=Contrib/getopt/MessagesBundle.properties \
	    Support/nativecode-makefile.template \
	    Support/precisec-makefile.template \
	    Support/precisec-no-sect-makefile.template \
	    Support/mipsda-makefile.template \
	    $(wildcard Backend/Runtime1/*.properties) \
            $(wildcard Analysis/Realtime/*.properties)
PKGDESC:=$(wildcard overview.html) $(wildcard README) \
	 $(foreach dir, $(ALLPKGS),\
	    $(wildcard $(dir)/package.html) $(wildcard $(dir)/README))

endif
# list all the definitions in the above block for export to children.
export BUILD_IGNORE ALLPKGS MACHINE_SRC MACHINE_GEN CGSPECS CGJAVA
export SCRIPTS ALLSOURCE JARPKGS PROPERTIES PKGDESC
# recompute these, to keep the size of the environment down.
TARSOURCE := $(filter-out $(MACHINE_GEN), $(filter-out JavaChip%, \
	        $(filter-out Test%,$(ALLSOURCE)))) GNUmakefile $(MACHINE_SRC)
NONEMPTYPKGS := $(shell ls  $(filter-out GNUmakefile,$(TARSOURCE))  | \
		sed -e 's|/*[A-Za-z0-9_]*\.[A-Za-z0-9_]*$$||' | sort -u)
PKGSWITHJAVASRC := $(shell ls  $(filter-out GNUmakefile,$(TARSOURCE))  | \
		   sed -ne 's|/*[A-Za-z0-9_]*\.java$$||p' | sort -u)	

all:	java

list:
	@echo $(filter-out GNUmakefile,$(TARSOURCE))
list-source:
	@for f in $(ALLSOURCE) ; do echo $$f ; done
list-source-gj0:
	@for f in $(filter-out $(shell grep -v "^#" gj-files) $(shell grep -v "^#" gj-files-2), $(ALLSOURCE)) ; do \
	echo $$f ; done
list-source-gj1:
	@for f in $(filter $(shell grep -v "^#" gj-files), $(ALLSOURCE)) ; \
	do echo $$f ; done
list-source-gj2:
	@for f in $(filter $(shell grep -v "^#" gj-files-2), $(ALLSOURCE)) ; \
	do echo $$f ; done

list-packages:
	@echo $(filter-out Test,$(ALLPKGS))
list-nonempty-packages:
	@echo $(filter-out Test,$(NONEMPTYPKGS))
list-packages-with-java-src:
	@echo $(filter-out Test,$(PKGSWITHJAVASRC))

# hack to let people build stuff w/o gj compiler until it stabilizes.
Support/gjlib.jar: $(shell grep -v ^\# gj-files ) gj-files
	$(RM) -rf gjlib
	mkdir gjlib
	@${JSR14_v1} -d gjlib -g $(shell grep -v "^#" gj-files)
	${JAR} cf $@ -C gjlib .
	$(RM) -rf gjlib
# *another* hack to let people build stuff w/o gj compiler until it stabilizes.
Support/gjlib2.jar: $(shell grep -v ^\# gj-files-2 ) gj-files-2
	$(RM) -rf gjlib2
	mkdir gjlib2
	@${JSR14_v2} -d gjlib2 -g $(shell grep -v "^#" gj-files-2)
	${JAR} cf $@ -C gjlib2 .
	$(RM) -rf gjlib2

# collect the names of all source files modified since last compile
out-of-date:	$(ALLSOURCE) FORCE
	@for f in FORCE $? ; do \
	  if [ "$$f" != "FORCE" ]; then \
	    echo $$f >> $@ ; \
	  fi ; \
	done
	@if [ -f .ignore -a .ignore -nt out-of-date ]; then \
	  echo ".ignore has changed; clearing old 'out-of-date' entries." ; \
	  $(RM) -f $@ ; \
	  for f in $(ALLSOURCE); do echo $$f >> $@ ; done ; \
	fi
	@if [ "$(REBUILD)" != "no" -a ! -s $@ ]; then \
	  echo "No files are outdated.  Forcing rebuild of everything." ; \
	  for f in $(ALLSOURCE); do echo $$f >> $@ ; done ; \
	fi
#	only sort the file if it is not already sorted.  this prevents
#	unnecessarily changing the timestamp.
	@if sort -u -c $@ 2> /dev/null ; then : ; else sort -u -o $@ $@ ; fi
FORCE: # force target eval, even if no source files are outdated
# this allows us to implement the "rebuild everything if nothing's old" rule

java:	PASS = 1
java:	$(PROPERTIES) out-of-date gj-files gj-files-2
	@rm -f "##out-of-date##"
	@touch "##out-of-date##"
# yuckity-yuk: v2.0 of JSR-14 compiler is incompatible with previous in
# various ways, especially regarding the silent coercion of Comparable to
# Comparable<K> in various situations.  Also v2.0 fixes some bugs in
# type inference which are inavoidably triggered by some new code.  So
# we can't compile the whole thing using one version or another. =(
# NOTE THAT variance annotations are incompatible with files compiled
# with the v1.x compiler... so don't use them yet.
	@if [ ! -d harpoon ] ; then \
	  touch harpoon.first ; \
	fi
	@if ${JSR14_v2} -test && [ ! -f harpoon.first ] ; then \
	  if [ -n "$(firstword $(filter $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)))" ] ; then \
	    echo Building with $(firstword ${JSR14_v2}). ;\
	    ${JSR14_v2} ${JFLAGS} $(filter $(shell grep -v "^#" gj-files-2), $(shell cat out-of-date)) ; \
	  fi \
	else \
	  echo "**** Using pre-built JSR14 v2 classes (in Support/gjlib2.jar) ****" ;\
	  echo "See http://www.flex-compiler.csail.mit.edu/Harpoon/jsr14.txt";\
	  echo " for information on how to install/use the GJ compiler." ; \
	  ${JAR} xf Support/gjlib2.jar harpoon ; \
	fi
# some folk might not have the GJ compiler; use the pre-built gjlib for them.
# also use gjlib when building the first time from scratch, to work around
# two-compiler dependency problems.
# [also don't build w/ GJ if there are no out-of-date GJ files, although
#  the GJ compiler doesn't really seem to mind.]
	@if [ -x $(firstword ${JSR14_v1}) -a ! -f harpoon.first ]; then \
	  if [ -n "$(firstword $(filter $(shell grep -v '^#' gj-files), $(shell cat out-of-date)))" ] ; then \
	    echo Building with $(firstword ${JSR14_v1}). ;\
	    ${JSR14_v1} ${JFLAGS} $(filter $(shell grep -v "^#" gj-files), $(shell cat out-of-date)) ; \
	  fi \
	else \
	  echo "**** Using pre-built GJ classes (in Support/gjlib.jar) ****" ;\
	  echo "See http://www.flex-compiler.csail.mit.edu/Harpoon/jsr14.txt";\
	  echo " for information on how to install/use the GJ compiler." ; \
	  ${JAR} xf Support/gjlib.jar harpoon ; \
	fi
	@if [ ! -d harpoon ]; then \
	  $(MAKE) first; \
	fi
	@if [ -n "$(firstword $(filter-out $(shell grep -v '^#' gj-files) $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)))" ] ; then \
	  echo Compiling... ; \
	  ${JCC} ${JFLAGS} $(filter-out $(shell grep -v "^#" gj-files) $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)) ; \
	fi
	@if [ -f stubbed-out ]; then \
	  $(RM) `sort -u stubbed-out`; \
	  $(MAKE) --no-print-directory PASS=2 `sort -u stubbed-out` || exit 1; \
	  echo Rebuilding `sort -u stubbed-out`; \
	  ${JCC} ${JFLAGS} `sort -u stubbed-out` || exit 1; \
	  $(RM) stubbed-out; \
	fi 
	@$(MAKE) --no-print-directory properties
	@mv -f "##out-of-date##" out-of-date
	@$(RM) harpoon.first

jikes:	PASS = 1
jikes: 	$(PROPERTIES) out-of-date gj-files gj-files-2
	@echo JIKES DOESNT YET SUPPORT JAVA 1.5
	@echo YOU MUST USE "'make java'"
	@exit 1
	@rm -f "##out-of-date##"
	@touch "##out-of-date##"
# yuckity-yuk: v2.0 of JSR-14 compiler is incompatible with previous in
# various ways, especially regarding the silent coercion of Comparable to
# Comparable<K> in various situations.  Also v2.0 fixes some bugs in
# type inference which are inavoidably triggered by some new code.  So
# we can't compile the whole thing using one version or another. =(
# NOTE THAT variance annotations are incompatible with files compiled
# with the v1.x compiler... so don't use them yet.
	@if [ ! -d harpoon ] ; then \
	  touch harpoon.first ; \
	fi
	@if ${JSR14_v2} -test && [ ! -f harpoon.first ] ; then \
	  if [ -n "$(firstword $(filter $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)))" ] ; then \
	    echo Building with $(firstword ${JSR14_v2}). ;\
	    ${JSR14_v2} ${JFLAGS} $(filter $(shell grep -v "^#" gj-files-2), $(shell cat out-of-date)) ; \
	  fi \
	else \
	  echo "**** Using pre-built JSR14 v2 classes (in Support/gjlib2.jar) ****" ;\
	  echo "See http://www.flex-compiler.csail.mit.edu/Harpoon/jsr14.txt";\
	  echo " for information on how to install/use the GJ compiler." ; \
	  ${JAR} xf Support/gjlib2.jar harpoon ; \
	fi
	@if [ -x $(firstword ${JSR14_v1}) -a ! -f harpoon.first ]; then \
	  if [ -n "$(firstword $(filter $(shell grep -v '^#' gj-files), $(shell cat out-of-date)))" ] ; then \
	    echo Building with $(firstword ${JSR14_v1}). ;\
	    ${JSR14_v1} ${JFLAGS} $(filter $(shell grep -v "^#" gj-files), $(shell cat out-of-date)) ; \
	  fi \
	else \
	  echo "**** Using pre-built GJ classes (in Support/gjlib.jar) ****" ;\
	  echo "See http://www.flex-compiler.csail.mit.edu/Harpoon/jsr14.txt";\
	  echo " for information on how to install/use the GJ compiler." ; \
	  ${JAR} xf Support/gjlib.jar harpoon ; \
	fi
	@if [ ! -d harpoon ]; then \
	  $(MAKE) first; \
	fi
	@if [ -n "$(firstword $(filter-out $(shell grep -v '^#' gj-files) $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)))" ] ; then \
	  echo -n Compiling... "" && \
	  ${JIKES} ${JFLAGS} $(filter-out $(shell grep -v "^#" gj-files) $(shell grep -v '^#' gj-files-2), $(shell cat out-of-date)) && \
	  echo done. ; \
	fi
	@if [ -f stubbed-out ]; then \
	  $(RM) `sort -u stubbed-out`; \
	  $(MAKE) --no-print-directory PASS=2 `sort -u stubbed-out` || exit 1; \
	  echo Rebuilding `sort -u stubbed-out`; \
	  ${JIKES} ${JFLAGS} `sort -u stubbed-out` || exit 1; \
	  $(RM) stubbed-out; \
	fi 
	@$(MAKE) --no-print-directory properties
	@mv -f "##out-of-date##" out-of-date
	@$(RM) harpoon.first

properties:
	@echo -n Updating properties... ""
	@-mkdir -p gnu/getopt harpoon/Support harpoon/Backend/Runtime1
	@cp Contrib/getopt/MessagesBundle.properties gnu/getopt
	@cp Support/nativecode-makefile.template harpoon/Support
	@cp Support/precisec-makefile.template harpoon/Support
	@cp Support/precisec-no-sect-makefile.template harpoon/Support
	@cp Backend/Runtime1/*.properties harpoon/Backend/Runtime1
	@cp Analysis/Realtime/*.properties harpoon/Analysis/Realtime
	@echo done.

first:
	mkdir -p harpoon silicon gnu
	for pkg in $(sort \
	  $(filter-out Contrib%,$(filter-out JavaChip%,$(ALLPKGS)))); do \
		mkdir -p harpoon/$$pkg; \
	done

# the package-by-package hack below is made necessary by brain-dead shells
# that don't accept arbitrarily-large argument lists.
Harpoon.jar Harpoon.jar.TIMESTAMP: REBUILD=no
Harpoon.jar Harpoon.jar.TIMESTAMP: java COPYING VERSIONS
	@echo -n "Building JAR file..."
	@${JAR} c0f Harpoon.jar COPYING VERSIONS
	@for pkg in $(JARPKGS); do \
	  for suffix in class properties; do\
	    if echo $$pkg/*.$$suffix | grep -vq '/[*][.]' ; then \
	      ${JAR} u0f Harpoon.jar $$pkg/*.$$suffix ;\
	      echo -n . ;\
	    fi;\
	  done;\
	done
	@echo " done."
	date '+%-d-%b-%Y at %r %Z.' > Harpoon.jar.TIMESTAMP

JARFILES=Harpoon.jar Harpoon.jar.TIMESTAMP $(SUPPORTP)
jar:	$(filter-out %.TIMESTAMP, $(JARFILES))
jar-install: jar
	$(RM) -rf jar-install
	mkdir jar-install
	cp $(JARFILES) jar-install
	chmod a+r jar-install/*
	cd jar-install ; tar c . | \
		$(SSH) $(INSTALLMACHINE) tar -C $(INSTALLDIR) -xv
	$(RM) -rf jar-install

VERSIONS: $(TARSOURCE) # collect all the RCS version ID tags.
	@echo -n Compiling VERSIONS... ""
	@grep -Fh ' $$I''d: ' \
		$(filter-out %.jar, $(TARSOURCE)) > VERSIONS
	@echo done.

ChangeLog: needs-cvs $(TARSOURCE) # not strictly accurate anymore.
	-$(RM) ChangeLog
# used to include TARSOURCE on rcs2log cmdline
	rcs2log | sed -e 's:/[^,]*/CVSROOT/Code/::g' > ChangeLog

# check out properties files appropriate for classpath 0.05 (not CVS head)
cvs-update-classpath-0.05:
	cvs update -r classpath_0_05 Backend/Runtime1/class-root.properties Backend/Runtime1/method-root.properties Backend/Runtime1/init-safe.properties Backend/Runtime1/transact-safe.properties

cvs-add: needs-cvs
	-for dir in $(filter-out Test,$(ALLPKGS)); do \
		(cd $$dir; cvs add *.java 2>/dev/null); \
	done
cvs-commit: needs-cvs cvs-add
	cvs -q diff -u | tee cvs-tmp # view changes we are committing.
	cvs -q commit $(patsubst %,-r %,$(CVS_BRANCH))
	$(RM) cvs-tmp
commit: cvs-commit # convenient abbreviation
update: needs-cvs
	cvs -q update -Pd $(CVS_REVISION)
	@-if [ -x $(FORTUNE) ]; then echo ""; $(FORTUNE); fi

# CodeGeneratorGenerator
%.java : %.spec $(filter Tools/PatMat/%,$(ALLSOURCE)) Support/NullCodeGen.template
	@if [ "PASS $(PASS)" = "PASS 1" ]; then \
	  echo PASS $(PASS): stubbing out $@; \
	  sed -e 's/PACKAGE/$(subst /,.,$(shell dirname $@))/g' \
	      -e 's/NullCodeGen/$(basename $(notdir $@))/g' \
	      < Support/NullCodeGen.template > $@; \
	  touch stubbed-out; echo $@ >> stubbed-out; \
	else \
	  echo PASS $(PASS): generating $@ from $<; \
	  $(JAVA) harpoon.Tools.PatMat.Main $< $(notdir $*) > $@; \
	fi

# JLex
%.java : %.jlex
	$(JAVA) JLex.Main $< && mv $<.java $@
# CUP
%.java : %.cup
	cd `dirname $@` && \
	$(JAVA) java_cup.Main -parser `basename $@ .java` -symbols Sym \
	< `basename $<`

# don't know how to automagically generate this dependency.
Tools/PatMat/Sym.java : Tools/PatMat/Parser.java

# Generic Types
%Boolean.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Boolean/g' -e 's/javaType/boolean/g' < $< > $@
%Byte.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Byte/g' -e 's/javaType/byte/g' < $< > $@
%Short.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Short/g' -e 's/javaType/short/g' < $< > $@
%Int.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Int/g' -e 's/javaType/int/g' < $< > $@
%Long.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Long/g' -e 's/javaType/long/g' < $< > $@
%Float.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Float/g' -e 's/javaType/float/g' < $< > $@
%Double.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Double/g' -e 's/javaType/double/g' < $< > $@
%Char.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Char/g' -e 's/javaType/char/g' < $< > $@
%Object.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Object/g' -e 's/javaType/Object/g' < $< > $@

# print graphs
%.ps : %.vcg
	@if [ -f $@ ]; then rm $@; fi # xvcg won't overwrite an output file.
	xvcg -psoutput $@ -paper 8x11 -color $(VCG_OPT) $<
	@echo "" # xvcg has a nasty habit of forgetting the last newline.

harpoon.tgz harpoon.tgz.TIMESTAMP: $(TARSOURCE) COPYING ChangeLog $(SUPPORTP) $(PROPERTIES) $(PKGDESC) Support/gjlib.jar Support/gjlib2.jar Support/NullCodeGen.template $(wildcard Support/*-root-set) $(SCRIPTS) gj-files gj-files-2 mark-executable
	tar czf harpoon.tgz COPYING $(TARSOURCE) ChangeLog $(SUPPORTP) $(PROPERTIES) $(PKGDESC) Support/NullCodeGen.template $(wildcard Support/*-root-set) $(SCRIPTS)
	date '+%-d-%b-%Y at %r %Z.' > harpoon.tgz.TIMESTAMP

tar:	harpoon.tgz harpoon.tgz.TIMESTAMP
tar-install: tar
	chmod a+r harpoon.tgz harpoon.tgz.TIMESTAMP
	tar c harpoon.tgz harpoon.tgz.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) tar -C $(INSTALLDIR) -xv

srcdoc/harpoon/%.html: %.java
	mkdir -p $(dir $@); bin/source-markup.perl \
		`if [ -f $*.spec ]; then echo -j ; fi` \
		-u $(CVSWEBLINK) $< > $@
srcdoc/gnu/%.html: Contrib/%.java
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc/silicon/%.html: %.java
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc/Code/%.html: %
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc: $(patsubst srcdoc/harpoon/Contrib/%,srcdoc/gnu/%,\
	$(patsubst srcdoc/harpoon/JavaChip/%,srcdoc/silicon/JavaChip/%,\
	$(addprefix srcdoc/harpoon/,\
	$(patsubst %.java,%.html,$(filter-out Test/%,$(ALLSOURCE)))))) \
	$(addprefix srcdoc/Code/,$(addsuffix .html,\
	$(MACHINE_SRC) $(SCRIPTS) GNUmakefile))
srcdoc/java srcdoc/sun srcdoc/sunw: Support/stdlibdoc.tgz
	mkdir -p srcdoc
	$(RM) -r $@
	if [ -r $< ]; then tar -C srcdoc -xzf $<; fi
	-(echo "order deny,allow"; echo "deny from all"; \
	 echo "allow from .mit.edu" ) > srcdoc/java/.htaccess && \
	 cp -f srcdoc/java/.htaccess srcdoc/sun/.htaccess && \
	 cp -f srcdoc/java/.htaccess srcdoc/sunw/.htaccess
	touch $@
srcdoc-clean:
	-${RM} -r srcdoc
srcdoc-install: srcdoc srcdoc/java
	chmod -R a+rX srcdoc
	tar c srcdoc | $(SSH) $(INSTALLMACHINE) \
		"/bin/rm -rf $(INSTALLDIR)/srcdoc ; tar -C $(INSTALLDIR) -xv"

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	$(ALLSOURCE) mark-executable
# check for .*.java and #*.java files that will give javadoc headaches
	@badfiles="$(strip $(foreach dir,\
			$(filter-out Test JavaChip,$(PKGSWITHJAVASRC)),\
			$(wildcard $(dir)/[.\#]*.java)))"; \
	if [ ! "$$badfiles" = "" ]; then \
	  echo "Please remove $$badfiles before running 'make doc'";\
	  exit 1;\
	fi
# okay, safe to make doc.
	$(RM) -rf doc doc-link
	mkdir doc doc-link
	cd doc-link; ln -s .. harpoon ; ln -s .. silicon ; ln -s ../Contrib gnu
	-${JDOC} ${JDOCFLAGS} -d doc -sourcepath doc-link \
		$(subst harpoon.Contrib,gnu, \
		$(foreach dir, $(filter-out Test, \
			  $(filter-out JavaChip,$(PKGSWITHJAVASRC))), \
			  harpoon.$(subst /,.,$(dir))) silicon.JavaChip) | \
		grep -v "^@see warning:"
	$(RM) -r doc-link
	$(MUNGE) doc | \
	  sed -e 's/<\([a-z]\+\)@\([-A-Za-z.]\+\).\(edu\|EDU\)>/\&lt;\1@\2.edu\&gt;/g' \
	      -e 's/<dd> "The,/<dd> /g' -e 's/<body>/<body bgcolor=white>/' | \
		bin/annotate.perl -link $(JDKDOCLINK) -u $(CVSWEBLINK) | \
		$(UNMUNGE)
	cd doc; if [ -e $(JDOCIMAGES) ]; then ln -s $(JDOCIMAGES) images; fi
	cd doc; if [ ! -f index.html ]; then ln -s packages.html index.html; fi
	cd doc; if [ ! -f API_users_guide.html ]; then ln -s index.html API_users_guide.html; fi
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	chmod -R a+rX doc

doc-install: doc/TIMESTAMP mark-executable
	# only include ChangeLog if we've got CVS access
	if [ -d CVS ]; then \
          $(MAKE) ChangeLog; \
	  cp ChangeLog doc/ChangeLog.txt; \
	  chmod a+r doc/ChangeLog.txt; \
	fi
	tar -c doc | $(SSH) $(INSTALLMACHINE) \
		"/bin/rm -rf $(INSTALLDIR)/doc ; tar -C $(INSTALLDIR) -x"

doc-clean:
	-${RM} -r doc

mark-executable:
	@chmod a+x bin/*
find-no-copyright:
	@find . -name "*.java" \( ! -exec grep -q "GNU GPL" "{}" ";" \) -print
find-import-star:
	@grep -l "import.*\*;" $(filter-out GNUmakefile,$(TARSOURCE)) || true

wc:
	@echo Top Five:
	@wc -l $(filter-out Contrib/%,$(TARSOURCE)) \
		| sort -n | tail -6 | head -5
	@echo Total lines of source:
	@cat $(filter-out Contrib/%,$(TARSOURCE)) | wc -l

clean:
	-${RM} -r harpoon silicon gnu Harpoon.jar* harpoon.tgz* \
		VERSIONS ChangeLog $(MACHINE_GEN) \
		out-of-date "##out-of-date##" .find-gj-cache
	-${RM} java `find . -name "*.class"`

polish: clean
	-${RM} *~ */*~ `find . -name "*.java~"` core

wipe:	clean doc-clean

NONLOCAL=$(shell if [ ! `hostname` = "flex-cvs.csail.mit.edu" ]; then echo $(SSH) cananian@flex-cvs.csail.mit.edu ; fi)
backup: only-me # SLOW ON NON-LOCAL MACHINES
	$(NONLOCAL) tar -C /data -c cvs | \
	   $(SSH) catfish.lcs.mit.edu \
	      "gzip -9 -c > public_html/Projects/Harpoon/flex-backup.tar.gz"

# some rules only make sense if you're me.
only-me:
	if [ ! `whoami` = "cananian" ]; then exit 1; fi

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	   exit 1; \
	fi

install: jar-install tar-install # doc-install

# [AS + Ovy] - recompile only one class:
# Usage: harpoon/[somepath]/[someclass.class]
# Warning: don't use it if you change some API. 
harpoon/%.class: %.java
	${JCC} ${JFLAGS} $*.java
