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

THex1ssi.tex: $(EX1FIG) Makefile
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed $(SEDCMD) > $@
THex1ssiPr.tex: $(EX1FIG) Makefile
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[79]//' $(SEDCMD) > $@
THex1ssa.tex: $(EX1FIG) Makefile
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[37]//' $(SEDCMD) -e 's/X_[12]/X_0/g' > $@
THex1ssaPr.tex: $(EX1FIG) Makefile
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[379]//' $(SEDCMD) -e 's/X_[12]/X_0/g' > $@
THex1base.tex: $(EX1FIG) Makefile
	fig2dev -L latex $(EX1SCALE) $(EX1FIG) \
	| sed -e 's/STMT[3789]//' $(SEDCMD) -e 's/\([PXYZ]\)_[01234]/\1/g'> $@

clean:
	$(RM) THex1*.tex *.fig.bak *~
