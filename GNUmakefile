# makefile.
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/

ALLDOCS=design bibnote readnote quads pldi

all: $(ALLDOCS:=.ps)
preview: pldi-xdvi

# bibtex dependencies
quads.dvi: harpoon.bib
design.dvi: harpoon.bib
bibnote.dvi: harpoon_.bib
readnote.dvi: unread_.bib

# Tex rules. [have to add explicit dependencies on appropriate bibtex files.]
%.dvi : %.tex
	latex $(basename $<)
	bibtex $(basename $<)
	latex $(basename $<)
	latex $(basename $<)
# Make annotation-visible versions of bibtex files.
%_.bib : %.bib
	sed -e "s/^  note =/  Xnote =/" -e "s/^  annote =/  note =/" \
		$< > $@
# dvi-to-postscript-to-acrobat chain.
%.ps : %.dvi
	dvips -o $@ $<
%.pdf : %.ps
	ps2pdf $< $@
%-xdvi : %.dvi
	@if ps | grep -v grep | grep -q "xdvi $*.dvi" ; then \
		echo "Xdvi already running." ; \
	else \
		(xdvi $*.dvi &) ; \
	fi

# latex2html
html: $(ALLDOCS:=.dvi)
	$(RM) -r html ; mkdir html
	for doc in $(ALLDOCS); do \
		latex2html -local_icons -dir html/$$doc $$doc; \
		date '+%-d-%b-%Y at %r %Z.' > html/$$doc/TIMESTAMP; \
	done

html-pdf: html $(ALLDOCS:=.pdf)
	-for doc in $(ALLDOCS); do $(RM) html/$$doc/$$doc.pdf; done
	for doc in $(ALLDOCS); do ln $$doc.pdf html/$$doc; done
html-install: html-pdf
	chmod a+r html/*/* ; chmod a+rx html/*
	ssh $(INSTALLMACHINE) /bin/rm -rf \
		$(foreach doc,$(ALLDOCS),$(INSTALLDIR)/$(doc))
	cd html; scp -r $(ALLDOCS) $(INSTALLMACHINE):$(INSTALLDIR)

install: html-install

update:
	cvs update -Pd

clean:
	$(RM) *.dvi *.log *.aux *.bbl *.blg
	$(RM) $(foreach doc,$(ALLDOCS),$(doc).ps $(doc).pdf)
	$(RM) harpoon_.bib unread_.bib
	$(RM) -r html

wipe: clean
	$(RM) *~ core

backup: only-me # DOESN'T WORK ON NON-LOCAL MACHINES
	if [ ! `hostname` = "lesser-magoo.lcs.mit.edu" ]; then exit 1; fi
	$(RM) ../harpoon-backup.tar.gz
	cd ..; tar czvf harpoon-backup.tar.gz CVSROOT
	scp ../harpoon-backup.tar.gz \
		miris.lcs.mit.edu:public_html/Projects/Harpoon
	$(RM) ../harpoon-backup.tar.gz

# some rules only makes sense if you're me.
only-me:
	if [ ! `whoami` = "cananian" ]; then exit 1; fi

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
.INTERMEDIATE: %_.bib
