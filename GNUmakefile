JFLAGS=-g
JFLAGSVERB=-verbose -J-Djavac.pipe.output=true
JCC=javac
JDOC=javadoc
JDOCFLAGS=-version -author -package
JDOCIMAGES=/usr/doc/jdk-docs-1.1.4-1/api/images

ALLSOURCE = $(wildcard [A-Z][a-z]*/*.java PDP8/*.java)

all:	java

java:	$(ALLSOURCE)
#	${JCC} ${JFLAGS} `javamake.sh */*.java`
	${JCC} ${JFLAGS} ${JFLAGSVERB} `javamake.sh */*.java` | \
		egrep -v '^\[[lc]'
	touch java

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	[A-Z][a-z]*/*.java PDP8/*.java
	make doc-clean
	mkdir doc
	${JDOC} ${JDOCFLAGS} -d doc [A-Z][a-z]* PDP8
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	cd doc; ln -s $(JDOCIMAGES) images
	cd doc; ln -s packages.html index.html
	cd doc; ln -s index.html API_users_guide.html
	chmod a+rx doc doc/*

doc-install: doc/TIMESTAMP
	redssh amsterdam.lcs.mit.edu /bin/rm -rf public_html/Projects/Harpoon/doc
	redscp -r doc amsterdam.lcs.mit.edu:public_html/Projects/Harpoon

doc-clean:
	-${RM} -r doc

clean:
	-${RM} java [A-Z]*/*.class

polish: clean
	-${RM} *~ [A-Z][a-z]*/*.java~

wipe:	clean doc-clean
