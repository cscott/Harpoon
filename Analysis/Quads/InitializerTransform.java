// InitializerTransform.java, created Tue Oct 17 14:35:29 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
/**
 * <code>InitializerTransform</code> transforms class initializers so
 * that they are idempotent and so that they perform all needed
 * initializer ordering checks before accessing non-local data.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InitializerTransform.java,v 1.1.2.1 2000-10-19 19:38:31 cananian Exp $
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
	HEADER qH = (HEADER) hc.getRootElement();
	FOOTER qF = (FOOTER) qH.next(0);
	METHOD qM = (METHOD) qH.next(1);
	QuadFactory qf = qH.getFactory();
	// add checks.
	hc = addChecks(hc);
	// make idempotent.
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
	return hc;
    }
    private String encode(String s) {
	return s;
    }
}
