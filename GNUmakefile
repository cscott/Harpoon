JFLAGS=-g
JFLAGSVERB=-verbose -J-Djavac.pipe.output=true
JCC=javac -d .
JDOC=javadoc
JDOCFLAGS=-version -author -package
JDOCIMAGES=/usr/local/jdk1.1.6/docs/api/images

ALLPKGS = $(shell find . -type d | grep -v CVS | grep -v "^./harpoon" | grep -v "^./doc" | sed -e "s|^[.]/*||")
ALLSOURCE = $(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.java))

all:	java

java:	$(ALLSOURCE)
#	${JCC} ${JFLAGS} `javamake.sh */*.java`
	${JCC} ${JFLAGS} ${JFLAGSVERB} `javamake.sh */*.java */*/*.java` | \
		egrep -v '^\[[lc]'
	touch java

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	$(ALLSOURCE)
	make doc-clean
	mkdir doc
	cd doc; ln -s .. harpoon
	cd doc; ${JDOC} ${JDOCFLAGS} -d . \
		$(foreach dir, $(ALLPKGS), harpoon.$(subst /,.,$(dir)))
	$(RM) doc/harpoon
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	cd doc; ln -s $(JDOCIMAGES) images
	cd doc; ln -s packages.html index.html
	cd doc; ln -s index.html API_users_guide.html
	chmod a+rx doc doc/*

doc-install: doc/TIMESTAMP
	ssh miris.lcs.mit.edu /bin/rm -rf public_html/Projects/Harpoon/doc
	scp -r doc miris.lcs.mit.edu:public_html/Projects/Harpoon

doc-clean:
	-${RM} -r doc

clean:
	-${RM} java
	-${RM} -r harpoon

polish: clean
	-${RM} *~ [A-Z][a-z]*/*.java~

wipe:	clean doc-clean

backup:
	$(RM) ../harpoon-backup.tar.gz
	cd ..; tar czvf harpoon-backup.tar.gz CVSROOT
	scp ../harpoon-backup.tar.gz \
		miris.lcs.mit.edu:public_html/Projects/Harpoon
	$(RM) ../harpoon-backup.tar.gz
