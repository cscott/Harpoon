JFLAGS=-g
JFLAGSVERB=-verbose -J-Djavac.pipe.output=true
JCC=javac -d .
JDOC=javadoc
JDOCFLAGS=-version -author # -package
JDOCIMAGES=/usr/local/jdk/docs/api/images
SSH=ssh
SCP=scp -A

ALLPKGS = $(shell find . -type d | grep -v CVS | grep -v "^./harpoon" | grep -v "^./doc" | grep -v "^./NOTES" | sed -e "s|^[.]/*||")
ALLSOURCE = $(filter-out .%.java, \
		$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.java)))

all:	java

java:	$(ALLSOURCE)
#	${JCC} ${JFLAGS} `javamake.sh */*.java`
#	${JCC} ${JFLAGS} ${JFLAGSVERB} `javamake.sh $(ALLSOURCE)` | \
#		egrep -v '^\[[lc]'
	${JCC} ${JFLAGS} ${JFLAGSVERB} $(ALLSOURCE) | \
		egrep -v '^\[[lc]'
	touch java

cvs-add:
	-for dir in $(filter-out Test,$(ALLPKGS)); do \
		(cd $$dir; cvs add *.java 2>/dev/null); \
	done
cvs-commit: cvs-add
	cvs -q diff -u | tee cvs-tmp # view changes we are committing.
	cvs -q commit
	$(RM) cvs-tmp
commit: cvs-commit # convenient abbreviation
update:
	cvs update # it's so easy to forget...

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	$(ALLSOURCE)
	make doc-clean
	mkdir doc
	cd doc; ln -s .. harpoon
	cd doc; ${JDOC} ${JDOCFLAGS} -d . \
		$(foreach dir, $(filter-out Test,$(ALLPKGS)), \
			  harpoon.$(subst /,.,$(dir))) | \
		grep -v "^@see warning:"
	$(RM) doc/harpoon
	munge doc | \
	  sed -e 's/<cananian@/\&lt;cananian@/g' \
	      -e 's/princeton.edu>/princeton.edu\&gt;/g' \
	      -e 's/<dd> "The,/<dd> /g' > doc-tmp
	unmunge doc-tmp; $(RM) doc-tmp
	cd doc; ln -s $(JDOCIMAGES) images
	cd doc; ln -s packages.html index.html
	cd doc; ln -s index.html API_users_guide.html
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	chmod a+rx doc doc/*

doc-install: doc/TIMESTAMP
	$(SSH) miris.lcs.mit.edu /bin/rm -rf public_html/Projects/Harpoon/doc
	$(SCP) -r doc miris.lcs.mit.edu:public_html/Projects/Harpoon

doc-clean:
	-${RM} -r doc

wc:
	@wc -l $(ALLSOURCE)
	@echo Top Five:
	@wc -l $(ALLSOURCE) | sort -n | tail -6 | head -5

clean:
	-${RM} -r harpoon 
	-${RM} java `find . -name "*.class"`

polish: clean
	-${RM} *~ */*~ `find . -name "*.java~"`

wipe:	clean doc-clean

backup:
	$(RM) ../harpoon-backup.tar.gz
	cd ..; tar czf harpoon-backup.tar.gz CVSROOT
	$(SCP) ../harpoon-backup.tar.gz \
		miris.lcs.mit.edu:public_html/Projects/Harpoon
	$(RM) ../harpoon-backup.tar.gz
