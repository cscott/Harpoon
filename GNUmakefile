# $Id: GNUmakefile,v 1.62 1998-11-25 09:27:48 cananian Exp $
JFLAGS=-d . -g
JFLAGSVERB=-verbose -J-Djavac.pipe.output=true
JIKES=jikes
JCC=javac -J-mx64m
JDOC=javadoc
JAR=jar
JDOCFLAGS=-version -author # -package
JDOCIMAGES=/usr/local/jdk/docs/api/images
SSH=ssh
SCP=scp -A
MUNGE=bin/munge
UNMUNGE=bin/unmunge
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/

CVS_TAG:=$(firstword $(shell cvs status GNUmakefile | \
			awk '/Sticky Tag/{print $$3}'))
CVS_BRANCH:=$(firstword $(shell cvs status GNUmakefile | \
			awk '/Sticky Tag/{print $$5}' | sed -e 's/[^0-9.]//g'))
CVS_REVISION:=$(patsubst %,-r %,$(CVS_TAG))

BUILD_IGNORE := $(strip $(shell if [ -f .ignore ]; then cat .ignore; fi))

ALLPKGS := $(shell find . -type d | grep -v CVS | grep -v AIRE | \
		$(patsubst %,egrep -v % |,$(BUILD_IGNORE)) \
		egrep -v "^[.]/(harpoon|silicon|gnu|doc|NOTES|bin|jdb)" | \
		sed -e "s|^[.]/*||")
ALLSOURCE := $(filter-out .%.java, \
		$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.java)))
TARSOURCE := $(filter-out JavaChip%, \
	        $(filter-out Test%,$(ALLSOURCE))) GNUmakefile
JARPKGS := $(subst harpoon/Contrib,gnu, \
		$(foreach pkg, $(filter-out JavaChip%, \
			$(filter-out Test%,$(ALLPKGS))), harpoon/$(pkg)))

all:	java

list:
	@echo $(filter-out GNUmakefile,$(TARSOURCE))

java:	$(ALLSOURCE) Contrib/getopt/MessagesBundle.properties
	if [ ! -d harpoon ]; then \
	  $(MAKE) first; \
	fi
	${JCC} ${JFLAGS} ${JFLAGSVERB} $(ALLSOURCE) | \
		egrep -v '^\[[lc]'
	@$(MAKE) --no-print-directory properties
	touch java

jikes: 	
	@if [ ! -d harpoon ]; then $(MAKE) first; fi
	@echo -n Compiling... ""
	@${JIKES} ${JFLAGS} ${ALLSOURCE}
	@echo done.
	@$(MAKE) --no-print-directory properties

properties:
	@echo -n Updating properties... ""
	@cp Contrib/getopt/MessagesBundle.properties gnu/getopt
	@echo done.

first:
	@echo Please wait...
	-${JCC} ${JFLAGS} $(ALLSOURCE) 2> /dev/null
	-${JCC} ${JFLAGS} $(ALLSOURCE) 2> /dev/null
Harpoon.jar Harpoon.jar.TIMESTAMP: java COPYING VERSIONS
	${JAR} c0f Harpoon.jar COPYING VERSIONS gnu/getopt/*.properties \
		$(foreach pkg,$(JARPKGS),$(pkg)/*.class)
	date '+%-d-%b-%Y at %r %Z.' > Harpoon.jar.TIMESTAMP

jar:	Harpoon.jar Harpoon.jar.TIMESTAMP
jar-install: jar
	chmod a+r Harpoon.jar Harpoon.jar.TIMESTAMP
	$(SCP) Harpoon.jar Harpoon.jar.TIMESTAMP \
		$(INSTALLMACHINE):$(INSTALLDIR)

VERSIONS: $(TARSOURCE) # collect all the RCS version ID tags.
	@echo -n Compiling VERSIONS... ""
	@grep -Fh ' $$I''d: ' $(TARSOURCE) > VERSIONS
	@echo done.

ChangeLog: $(TARSOURCE)
	rcs2log $(TARSOURCE) > ChangeLog

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
	cvs update -Pd $(CVS_REVISION)
	@echo ""
	@-$(FORTUNE)

# print graphs
%.ps : %.vcg
	@if [ -f $@ ]; then rm $@; fi # xvcg won't overwrite an output file.
	xvcg -psoutput $@ -paper 8x11 -color $<
	@echo "" # xvcg has a nasty habit of forgetting the last newline.

harpoon.tgz harpoon.tgz.TIMESTAMP: $(TARSOURCE) COPYING ChangeLog
	tar czf harpoon.tgz COPYING $(TARSOURCE) ChangeLog
	date '+%-d-%b-%Y at %r %Z.' > harpoon.tgz.TIMESTAMP

tar:	harpoon.tgz harpoon.tgz.TIMESTAMP
tar-install: tar
	chmod a+r harpoon.tgz harpoon.tgz.TIMESTAMP
	$(SCP) harpoon.tgz harpoon.tgz.TIMESTAMP \
		$(INSTALLMACHINE):$(INSTALLDIR)

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	$(ALLSOURCE) ChangeLog mark-executable
	make doc-clean
	mkdir doc
	cd doc; ln -s .. harpoon ; ln -s .. silicon ; ln -s ../Contrib gnu
	cd doc; ${JDOC} ${JDOCFLAGS} -d . \
		$(subst harpoon.Contrib,gnu, \
		$(foreach dir, $(filter-out Test, \
			  $(filter-out JavaChip,$(ALLPKGS))), \
			  harpoon.$(subst /,.,$(dir))) silicon.JavaChip) | \
		grep -v "^@see warning:"
	$(RM) doc/harpoon doc/silicon
	$(MUNGE) doc | \
	  sed -e 's/<\([a-z]\+\)@\([a-z.]\+\).edu>/\&lt;\1@\2.edu\&gt;/g' \
	      -e 's/<dd> "The,/<dd> /g' | \
		$(UNMUNGE)
	cd doc; ln -s $(JDOCIMAGES) images
	cd doc; ln -s packages.html index.html
	cd doc; ln -s index.html API_users_guide.html
	cp ChangeLog doc/ChangeLog.txt
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	chmod a+rx doc ; chmod a+r doc/*

doc-install: doc/TIMESTAMP
	$(SSH) $(INSTALLMACHINE) \
		/bin/rm -rf $(INSTALLDIR)/doc
	$(SCP) -r doc $(INSTALLMACHINE):$(INSTALLDIR)

doc-clean:
	-${RM} -r doc

mark-executable:
	@chmod a+x bin/*
find-no-copyright:
	@find . -name "*.java" \( ! -exec grep -q "GNU GPL" "{}" ";" \) -print

wc:
	@wc -l $(ALLSOURCE)
	@echo Top Five:
	@wc -l $(ALLSOURCE) | sort -n | tail -6 | head -5

clean:
	-${RM} -r harpoon silicon gnu Harpoon.jar* harpoon.tgz* VERSIONS
	-${RM} java `find . -name "*.class"`

polish: clean
	-${RM} *~ */*~ `find . -name "*.java~"` core

wipe:	clean doc-clean

backup: only-me # DOESN'T WORK ON NON-LOCAL MACHINES
	if [ ! `hostname` = "lesser-magoo.lcs.mit.edu" ]; then exit 1; fi
	$(RM) ../harpoon-backup.tar.gz
	(cd ..; tar czf harpoon-backup.tar.gz CVSROOT)
	$(SCP) ../harpoon-backup.tar.gz \
		miris.lcs.mit.edu:public_html/Projects/Harpoon
	$(RM) ../harpoon-backup.tar.gz

# some rules only make sense if you're me.
only-me:
	if [ ! `whoami` = "cananian" ]; then exit 1; fi

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	   exit 1; \
	fi

install: jar-install tar-install doc-install
