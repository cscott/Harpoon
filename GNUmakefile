# makefile.
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
export TEXINPUTS=/home/cananian/src/tex4ht//:

ALLDOCS=design bibnote readnote quads proposal thesis exec pldi99 pldi02

all: $(ALLDOCS:=.ps)
preview: thesis-xdvi

# comdef dependencies
exec.dvi thesis.dvi proposal.dvi: comdef.sty
pldi99.dvi pldi99-outline.dvi: comdef.sty
pldi02.dvi: comdef.sty

# bibtex dependencies
exec.dvi quads.dvi design.dvi thesis.dvi: harpoon.bib
pldi99.dvi pldi99-outline.dvi: harpoon.bib
pldi02.dvi: harpoon.bib
bibnote.dvi: harpoon_.bib
readnote.dvi: unread_.bib

# lots of dependencies for the pldi99 paper
pldi99.dvi: pldi99-intro.tex pldi99-abstract.tex pldi99-tech.tex
pldi99.dvi: pldi99-example.tex pldi99-results.tex
pldi99.dvi: Figures/THussi.tex Figures/THv0.tex Figures/THscccomp.eps

# more dependencies for the pldi02 paper.
#pldi02.dvi: pldi02-abstract.tex


# thesis figure dependencies
export THESIS_FIGURES=\
	Figures/THundir.fig \
	Figures/THcqdata.tex Figures/THcqalg.tex Figures/THcqex.fig \
	Figures/THsesedata.tex Figures/THsesealg.tex \
	Figures/THpst.fig Figures/evil.fig \
	Figures/THdeaddata.tex Figures/THdeadalg.tex Figures/THlattice.fig \
	Figures/THlat1.fig Figures/THlat2.fig \
	Figures/THlat3.fig Figures/THlat4.fig Figures/THlat5.fig \
	Figures/THsccalg1.tex Figures/THsccalg2.tex Figures/THsccssi.tex \
	Figures/THscctyped.tex Figures/THsptc.tex \
	Figures/THssiren1.tex Figures/THssiren2.tex Figures/THssirend.tex \
	Figures/THmorephi.fig Figures/THssi2ssa.tex \
	Figures/THlat6.fig
thesis.dvi: $(patsubst %.fig,%.tex,$(THESIS_FIGURES)) \
	Figures/THex1base.tex \
	Figures/THex1ssa.tex Figures/THex1ssaPr.tex \
	Figures/THex1ssi.tex Figures/THex1ssiPr.tex \
	Figures/THcqex2.tex Figures/phisig.tex \
	Figures/THussi.tex Figures/THv0.tex Figures/THscccomp.eps

# thesis figure rules
Figures/%: always
	@$(MAKE) --no-print-directory -C Figures $(notdir $@)
always:

# Tex rules. [have to add explicit dependencies on appropriate bibtex files.]
%.dvi %.aux: %.tex
	latex $*
	if egrep -q '^[^%]*\\bibliography' $< ; then bibtex $*; fi
	if egrep -q 'Rerun to get cross-r' $*.log; then latex $*; fi
	if egrep -q 'Rerun to get cross-r' $*.log; then latex $*; fi
	if egrep -q 'undefined references' $*.log; then grep undefined $*.log; fi

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

# progress graphs.
%.stats: %.tex
	scripts/make.stats $< > $@
%.gif: %.stats
	scripts/make.graph $*

allthesis.stats: thesis.stats Figures/THex1.fig.stats \
		$(patsubst %,%.stats,$(THESIS_FIGURES))
	(head -1 thesis.stats ; (cat $^ | sed -e '/^[^0-9]/d' \
	  -e 's.\([0-9][0-9]/[0-9][0-9]\)/\([0-9][0-9][0-9][0-9]\).\2/\1.g' \
	| sort -r | sed \
	  -e 's.\([0-9][0-9][0-9][0-9]\)/\([0-9][0-9]/[0-9][0-9]\).\2/\1.g' \
	) ) > $@

# latex2html

html/% : %.dvi %.aux %.ps %.pdf
	if [ ! -d html ]; then mkdir html; fi
	-$(RM) -r $@
	latex2html -local_icons -dir $@ $*
	ln $*.ps $@
	ln $*.pdf $@
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
	$(RM) *.aux *.bbl *.blg *.dvi *.glo *.gls *.idx *.ilg *.ind *.lis \
	      *.loa *.lof *.log *.lot *.toc
	$(RM) $(foreach doc,$(ALLDOCS),$(doc).ps $(doc).pdf $(doc).gif $(doc).stats) allthesis.stats allthesis.gif
	$(RM) harpoon_.bib unread_.bib
	$(RM) -r html
	$(MAKE) -C Figures clean

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
.PHONY: always

# Try to convince make to delete these sometimes.
.INTERMEDIATE: $(ALLDOCS:=.log)
.INTERMEDIATE: %_.bib
.SECONDARY: $(ALLDOCS:=.dvi) $(ALLDOCS:=.aux)
