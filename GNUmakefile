# makefile.
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/

ALLDOCS=design bibnote readnote quads pldi99

all: $(ALLDOCS:=.ps)
preview: pldi99-xdvi

# bibtex dependencies
quads.dvi: harpoon.bib
design.dvi: harpoon.bib
bibnote.dvi: harpoon_.bib
readnote.dvi: unread_.bib
# lots dependencies for the pldi paper
pldi99.dvi: harpoon.bib pldi99-intro.tex pldi99-abstract.tex pldi99-body.tex

# Tex rules. [have to add explicit dependencies on appropriate bibtex files.]
%.dvi %.aux: %.tex
	latex $(basename $<)
	if grep -q bibliography $< ; then bibtex $(basename $<); fi
	latex $(basename $<)
	latex $(basename $<)
# Make annotation-visible versions of bibtex files.
%_.bib : %.bib
	sed -e "s/^  note =/  Xnote =/" -e "s/^  annote =/  note =/" \
		$< > $@
# dvi-to-postscript-to-acrobat chain.
%.ps : %.dvi
	dvips -e 0 -o $@ $<
%.pdf : %.ps
	ps2pdf $< $@
%-xdvi : %.dvi
	@if ps | grep -v grep | grep -q "xdvi $*.dvi" ; then \
		echo "Xdvi already running." ; \
	else \
		(xdvi $< &) ; \
	fi

# latex2html

html/% : %.dvi %.aux %.ps %.pdf
	if [ ! -d html ]; then mkdir html; fi
	-$(RM) -r $@
	latex2html -local_icons -dir $@ $(basename $<)
	ln $(basename $<).ps $@
	ln $(basename $<).pdf $@
	date '+%-d-%b-%Y at %r %Z.' > $@/TIMESTAMP

html: $(foreach doc,$(ALLDOCS),html/$(doc))

html-install: html
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

# performance:
.PHONY: clean wipe backup only-me update install
.PHONY: html html-pdf html-install
.PHONY: preview all

# Try to convince make to delete these sometimes.
.INTERMEDIATE: $(ALLDOCS:=.log)
.INTERMEDIATE: %_.bib
.SECONDARY: $(ALLDOCS:=.dvi) $(ALLDOCS:=.aux)
