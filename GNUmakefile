# makefile.

all: design.ps bibnote.ps
preview: design-xdvi

design.dvi: design.tex harpoon.bib
	latex design
	bibtex design
	latex design
	latex design
bibnote.dvi: bibnote.tex harpoon.bib
	sed -e "s/^  note =/  Xnote =/" -e "s/^  annote =/  note =/" \
		harpoon.bib > bibnote.bib
	latex bibnote
	bibtex bibnote
	latex bibnote
	latex bibnote
	$(RM) bibnote.bib
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
	$(RM) design.ps design.pdf bibnote.ps bibnote.pdf bibnote.bib
wipe: clean
	$(RM) *~ core

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
