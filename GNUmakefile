# makefile.

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
clean:
	$(RM) *.dvi *.log *.aux *.bbl *.blg
	$(RM) design.ps design.pdf \
	      bibnote.ps bibnote.pdf \
	      readnote.ps readnote.pdf
	$(RM) harpoon_.bib unread_.bib

wipe: clean
	$(RM) *~ core

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
.INTERMEDIATE: %_.bib
