// InitializerTransform.java, created Tue Oct 17 14:35:29 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Util.*;

import java.lang.reflect.Modifier;
/**
 * <code>InitializerTransform</code> transforms class initializers so
 * that they are idempotent and so that they perform all needed
 * initializer ordering checks before accessing non-local data.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InitializerTransform.java,v 1.1.2.3 2000-10-19 21:34:15 cananian Exp $
 */
public class InitializerTransform
    extends harpoon.Analysis.Transformation.MethodSplitter {
    /** Token for the initializer-ordering-check version of a method. */
    public static final Token CHECKED = new Token("initcheck");

    /** Creates a <code>InitializerTransform</code>. */
    public InitializerTransform(HCodeFactory parent) {
	// we only allow quad with try as input.
	super(QuadWithTry.codeFactory(parent));
    }
    /** Checks the token types handled by this 
     *  <code>MethodSplitter</code> subclass. */
    protected boolean isValidToken(Token which) {
	return which==CHECKED || super.isValidToken(which);
    }
    /** Mutate a given <code>HCode</code> to produce the version
     *  specified by <code>which</code>. */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	Code hc = (QuadWithTry) input.hcode();
	if (which==CHECKED)
	    return addChecks(hc);
	else if (which==ORIGINAL && hc.getMethod() instanceof HInitializer)
	    return mutateInitializer(hc);
	return hc;
    }
    /** Add idempotency to initializer and add checks. */
    private Code mutateInitializer(Code hc) {
	HMethod hm = hc.getMethod();
	Util.assert(hm.getReturnType()==HClass.Void);
	// add checks.
	hc = addChecks(hc);
	// make idempotent.
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = (FOOTER) qH.next(0);
	METHOD qM = (METHOD) qH.next(1);
	QuadFactory qf = qH.getFactory();
	HClass declcls = hc.getMethod().getDeclaringClass();
	HField ifield = declcls.getMutator().addDeclaredField
	    ("$$has$been$initialized$$", HClass.Boolean);
	ifield.getMutator().setSynthetic(true);
	ifield.getMutator().setModifiers(Modifier.STATIC | Modifier.PUBLIC);
	Temp tst = new Temp(qf.tempFactory(), "uniq");
	Quad q0 = new GET(qf, qM, tst, ifield, null);
	Quad q1= new CJMP(qf, qM, tst, new Temp[0]);
	Quad q2 = new RETURN(qf, qM, null);
	Edge splitedge = qM.nextEdge(0);
	Quad.addEdges(new Quad[] { qM, q0, q1 });
	Quad.addEdge(q1, 1, q2, 0);
	Quad.addEdge(q1, 0, (Quad)splitedge.to(), splitedge.which_pred());
	qF = qF.attach(q2, 0);
	// done.
	return hc;
    }
    /** Add initialization checks to every static use of a class. */
    private Code addChecks(Code hc) {
	final HEADER qH = (HEADER) hc.getRootElement();
	// static references are found in GET/SET/ANEW/NEW/CALL
	QuadVisitor qv = new QuadVisitor() {
	    /** classes already initialized in this method. */
	    Environment seenSet = new HashEnvironment();
	    /* constructor */ { traverse((METHOD)qH.next(1)); }
	    // recursive traversal.
	    private void traverse(Quad q) {
		q.accept(this);
		Environment.Mark m = seenSet.getMark();
		for (int i=0; i<q.nextLength(); i++) {
		    traverse(q.next(i));
		    if (i+1<q.nextLength())
			seenSet.undoToMark(m);
		}
	    }
	    public void visit(Quad q) { /* default, do nothing. */ }
	    public void visit(PHI q) {
		// XXX: merging at phis (instead of throwing away
		// seenset) would lead to less unnecessary
		// initializations.  cost may be prohibitive?
		seenSet.clear();
	    }
	    public void visit(ANEW q) {
		addCheckBefore(q, q.hclass(), seenSet);
	    }
	    public void visit(CALL q) {
		if (q.isStatic())
		    addCheckBefore(q, q.method().getDeclaringClass(), seenSet);
		// XXX: non-virtual methods need to use special checking vrsns.
	    }
	    public void visit(GET q) {
		if (q.isStatic())
		    addCheckBefore(q, q.field().getDeclaringClass(), seenSet);
	    }
	    public void visit(NEW q) {
		addCheckBefore(q, q.hclass(), seenSet);
	    }
	    public void visit(SET q) {
		if (q.isStatic())
		    addCheckBefore(q, q.field().getDeclaringClass(), seenSet);
	    }
	};
	return hc;
    }
    private static void addCheckBefore(Quad q, HClass class2check,
				       Environment seenSet) {
	QuadFactory qf = q.getFactory();
	if (qf.getMethod().getDeclaringClass().equals(class2check))
	    return; // we've already initialized (or are initializing) this.
	if (seenSet.containsKey(class2check))
	    return; // already checked on this execution path.
	else seenSet.put(class2check, class2check); // don't double check.
	HMethod clinit = class2check.getClassInitializer();
	if (clinit==null) return; // no class initializer for this class.
	Util.assert(q.prevLength()==1); // otherwise don't know where to link
	Quad q0 = new CALL(qf, q, clinit, new Temp[0], null, null, false,
			   false, new Temp[0]);
	// insert the new call on an edge
	Edge splitedge = q.prevEdge(0);
	Quad.addEdge((Quad)splitedge.from(), splitedge.which_succ(), q0, 0);
	Quad.addEdge(q0, 0, (Quad)splitedge.to(), splitedge.which_pred());
	// cover this call with the handlers of q
	q0.addHandlers(q.handlers());
	// done.
	return;
    }
}
