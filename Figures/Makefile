# definitions for example 1
EX1FIG=THex1.fig
EX1SCALE=-m 0.7
SEDCMD=	-e 's/STMT1/$$P_0 \\leftarrow (X_0 \\neq 2)$$/' \
	-e 's/STMT2/if $$P_0$$ jump/' \
	-e 's/STMT3/$$\\left\\langle X_1, X_2 \\right\\rangle \\leftarrow \\sigma \\left (X_0 \\right )$$/' \
	-e 's/STMT4/$$Y_1 \\leftarrow 4+X_1$$/' \
	-e 's/STMT5/$$Y_2 \\leftarrow 6+X_2$$/' \
	-e 's/STMT6/$$Z_1 \\leftarrow 5$$/' \
	-e 's/STMT7/$$X_3 \\leftarrow \\phi \\left (X_1, X_2 \\right )$$/' \
	-e 's/STMT8/$$Y_3 \\leftarrow \\phi \\left (Y_1, Y_2 \\right )$$/' \
	-e 's/STMT9/$$Z_2 \\leftarrow \\phi \\left (Z_0, Z_1 \\right )$$/' \
	-e 's/STMTA/$$Y_4 \\leftarrow Y_3+1$$/' \
	-e 's,STMTB,/* no further uses of $$X$$ or $$Z$$ */,'

# rules for example 1
THex1ssi.tex: $(EX1FIG)
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed $(SEDCMD) > $@
THex1ssiPr.tex: $(EX1FIG)
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[79]//' $(SEDCMD) > $@
THex1ssa.tex: $(EX1FIG)
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[37]//' $(SEDCMD) -e 's/X_[12]/X_0/g' > $@
THex1ssaPr.tex: $(EX1FIG)
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[379]//' $(SEDCMD) -e 's/X_[12]/X_0/g' > $@
THex1base.tex: $(EX1FIG)
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[3789]//' $(SEDCMD) -e 's/\([PXYZ]\)_[01234]/\1/g'> $@

# default fig->tex rule -- insert a snippet from head.tex
%.tex: %.fig head.tex
	fig2dev -L latex -m 1.0 $< \
	| sed -e '/begin{picture}/,/end{picture}/s/SetFigFont/MySetFigFont/g' \
	      -e '/setlength{.unitlength}/r head.tex' > $@
	echo '\renewcommand{\figscale}{1.00}%' >> $@
	if grep -q '^\\gdef\\SetFigFont#1#2#3{%$$' $@; then \
	  sed -e '/MySetFigFont/,/MyFixFigFont/s/\\SetFigFont/\\MyFixFigFont/' < $@ > $@.tmp; \
	  mv -f $@.tmp $@; \
	fi

# sed out dashed arc-boxes and left-justified special text from the
# cycle-equivalency figure.
THcqex2.tex: THcqex.fig
	awk "/^[[:blank:]]/{if (foo==1) next;} /^2 4 1/||/^4 0 0 100 0 0 12 0.0000 2/{foo=1;next} {print; foo=0;}" < $< | \
	fig2dev -L latex -m 0.5 > $@

# tricky THlat6c-processing.
THlat6c2.tex: THlat6c.tex
	sed -e 's/\\color\[rgb\]{0,0,0}/\\white/g' \
	    -e 's/\\color\[rgb\]{1,0,0}/\\white\\onlySlide*{2}{\\yellow}/g' \
	    -e 's/\\color\[rgb\]{1,1,0}/\\white\\onlySlide*{2}{\\yellow}\\onlySlide*{3}{\\yellow}/g' \
	    -e 's/\\color\[rgb\]{0,1,0}/\\white\\onlySlide*{3}{\\yellow}/g' \
	    -e 's/\\color\[rgb\]{0,1,1}/\\white\\onlySlide*{3}{\\yellow}\\onlySlide*{4}{\\yellow}/g' \
	    -e 's/\\color\[rgb\]{0,0,1}/\\white\\onlySlide*{4}{\\yellow}/g' \
	< $< > $@

# default fig->eps rule
%.eps: %.fig
	fig2dev -L eps -m 1.0 $< > $@

#default gnu-plot to latex rule
%.tex: %.gnuplot
	gnuplot $< | uniq | sed -e '/setlength{.unitlength}/r head.tex' > $@
	echo '\renewcommand{\figscale}{1.00}%' >> $@

# extra dependencies for data files of gnuplots
phisig.tex: phisig.data-sorted
THussi.tex: Data/ussi.data
THv0.tex: Data/v0.data
ptrsize.tex: ptrsize.data

# default .jpg-to-eps rule
%.eps: %.jpg
	jpegtopnm $< | pnmtops -dpi 72 -equalpixels -noturn -nocenter > $@

# fix bounding boxes of kontour eps files (hence the a4 papersize)
%-bbox.eps : %.eps
	( head -1 $< ; \
	gs -sDEVICE=bbox -dNOPAUSE -dBATCH -sPAPERSIZE=a4 -q $< 2>&1 ; \
	tail +2 $< | \
	  grep -v "^%%BoundingBox:" | \
	  grep -v "^%%HiResBoundingBox:" ) > $@

# graphing helpers
%.stats: %
	../scripts/make.stats $< > $@

clean:
	$(RM) THex1*.tex *.fig.bak THcqex2.tex phisig.tex \
		$(patsubst %.fig,%.tex,\
		  $(patsubst Figures/%,%,$(filter %.fig,$(THESIS_FIGURES)))) \
		*.stats *~ texput.log
	$(RM) THussi.tex THv0.tex phisig.tex ptrsize.tex
