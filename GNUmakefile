# makefile.

#all: design.ps
preview: design.dvi

design.dvi: design.tex harpoon.bib
	latex design
	bibtex design
	latex design
	latex design
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
	$(RM) *.dvi *.log *.aux *.bbl *.blg *.ps *.pdf
wipe: clean
	$(RM) *~ core

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
