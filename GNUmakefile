# makefile.
HOST = miris.lcs.mit.edu

all: design.ps bibnote.ps readnote.ps
preview: design-xdvi

# bibtex dependencies
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
html: design.dvi bibnote.dvi
	$(RM) -r html ; mkdir html
	latex2html -local_icons -dir html/design design
	latex2html -local_icons -dir html/biblio bibnote
html-pdf: html design.pdf bibnote.pdf
	$(RM) html/design/design.pdf html/biblio/bibnote.pdf
	ln design.pdf html/design
	ln bibnote.pdf html/biblio 
html-install: html-pdf
	chmod a+r html/*/* ; chmod a+rx html/*
	ssh $(HOST) /bin/rm -rf public_html/Projects/Harpoon/design \
		public_html/Projects/Harpoon/biblio
	cd html; scp -r design biblio \
		$(HOST):public_html/Projects/Harpoon

clean:
	$(RM) *.dvi *.log *.aux *.bbl *.blg
	$(RM) design.ps design.pdf \
	      bibnote.ps bibnote.pdf \
	      readnote.ps readnote.pdf
	$(RM) harpoon_.bib unread_.bib
	$(RM) -r html

wipe: clean
	$(RM) *~ core

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
.INTERMEDIATE: %_.bib
