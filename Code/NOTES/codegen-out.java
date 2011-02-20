/* mapping from temps to types. (but see caveat below) */
HashMap t2ty = new HashMap();

/* cgg outputs pair of functions: munchStm() and munchExp() */

void munchStm(Stm stm) {
    // pattern: MOVE(MEM(CONST(c)), e) %pred %( is13bit(c) )%
    if (stm instanceof MOVE && 
	((MOVE) stm).dst instanceof MEM &&
	((MEM) ((MOVE) stm).dst) instanceof CONST) { // shape matches
	// definitions for all variables which can be used in predicate,
	// which do *not* include things we have to munch.
	Number c = ((CONST) ((MEM) ((MOVE) stm).dst).exp).value;
	if (is13bit(c)) { // predicate match.
	    MOVE ROOT = (MOVE) stm;
	    Temp e = munchExp(ROOT.src); // only munch after match
	    // NOW insert rules clause from spec:
	String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
	emit(new InstrMEM(if, ROOT,
			  "\t st"+suffix((Typed)ROOT.dst)+" `s0, ["+c+"]\n",
			  null, new Temp[] { e }));
	    //we're done.
	    return;
	}
    }
    // [...] (more user-specified patterns here)
    // patterns with EXPR and SEQ at root handled by generator automatically
    if (stm instanceof EXPR) {
	munchExp(((EXPR)stm).exp); // throw away Temp result.
	return;
    }
    if (stm instanceof SEQ) {
	munchStm(((SEQ)stm).left);
	munchStm(((SEQ)stm).right);
	return;
    }
    throw new Error("Unhandled Tree pattern: "+stm);
}
Temp munchExp(Exp exp) {
    // BUILT-IN pattern for TEMP(t)=r
    if (exp instanceof TEMP) {
	TEMP ROOT = (TEMP) exp;
	t2ty.put(ROOT.temp, ROOT.type()); // save temp-to-type mapping.
	// (note - actually, type probably needs to be associated with
	//  an instr, which requires returning a Temp+type pair from each
	//  munchExp.  Exercise left to the reader.)
	return ROOT.temp;
    }
    // user-specified patterns, in sorted order:
    // pattern CONST<i,l,p>(0)=r
    if (exp instanceof CONST &&
	(((CONST) exp).type()==Type.INTEGER ||
	 ((CONST) exp).type()==Type.LONG ||
	 ((CONST) exp).type()==Type.POINTER)) { // shape matches.
	Number _c = ((CONST) exp).value; // tool-generated variable
	if (!Type.isFloatingPoint(ROOT.type()) &&
	    _c.longValue()==0) { // tool-generated predicate
	    CONST ROOT = (CONST) exp;
	    Temp r = new Temp(); // use appropriate temp factory here.
	    t2ty.put(r, ROOT.type());
	    // NOW insert rules clause from spec.
	 emit(new Instr(if, ROOT, "\t mov %g0, `d0\n", new Temp[] {r},null));
	    // after rules clause, insert return statement.
	    return r;
	}
    }
    // pattern: BINOP<i,p>(op, CONST(c), e)=r [with predicate]
    if (exp instanceof BINOP &&
	(((BINOP) exp).type()==Type.INTEGER ||
	 ((BINOP) exp).type()==Type.POINTER) &&
	((BINOP) exp).left instanceof CONST) { // shape matches.
	int op = ((BINOP) exp).op;
	Number c = ((CONST) ((BINOP) exp).left).value;
	if (isCommutative(op) && is13bit(c)) { // predicate clause
	    BINOP ROOT = (BINOP) exp;
	    Temp e = munchExp(ROOT.left); // only munch after match
	    Temp r = new Temp(); // use appropriate temp factory.
	    t2ty.put(r, ROOT.type());
	    // NOW insert rules clause from Spec
	    emit(new Instr(if, ROOT, "\t "+bop(op)+" `s0, "+c+", `d0\n",
			   new Temp[] { r }, new Temp[] { e }));
	    // and insert return statement after rules clause
	    return r;
	}
    }
    // [...more patterns here...]
    throw new Error("Unmatched Tree exp: "+exp);
}
