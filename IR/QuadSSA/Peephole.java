// Peephole.java, created Fri Sep 25 11:56:48 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.Temp.Temp;
/**
 * <code>Peephole</code> does some peephole optimization on generated
 * Quads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Peephole.java,v 1.2 1998-09-25 18:45:06 cananian Exp $
 */

class Peephole extends QuadVisitor {
    public static void optimize(Code c) {
	Peephole pvisitor = new Peephole(c);

	Quad[] ql = (Quad[]) c.getElements();
	for (int i=0; i<ql.length; i++)
	    ql[i].visit(pvisitor);
    }
    
    /** Hide the constructor. */
    private Peephole(Code c) {
	this.c = c;
    }
    Code c;

    // Implement visitor classes.
    public void visit(Quad q) { /* default, do nothing. */ }

    public void visit(PHI q) {
	/* Looking for:            becomes:
	 * t1=CONST t1=CONST ...    t1=CONST           t1=CONST        ....
	 *       \   |      /          \                   |            /
	 *          PHI             t2=ICMPxx(t1,xx) t2=ICMPxx(t1,xx) ...
	 *   t2=ICMPxx(t1,xx)         CJMP t2           CJMP t2       ...
	 *     CJMP t2                  |___\___________/__|__________/ /
	 *      /    \                  |    \_____________|___________/
	 *     x      y               PHI                 PHI
	 *                             x                   y
	 */
	// phi arity greater than 1
	int arity = q.prev().length;
	if (arity < 2) return;
	// first, all PHI preds have to be CONSTs defining the same t.
	for (int i=0; i < arity; i++)
	    if (! (q.prev(i) instanceof CONST) ) return;
	Temp t1 = ((CONST)q.prev(0)).def()[0];
	for (int i=1; i < arity; i++)
	    if ( ((CONST)q.prev(i)).def()[0] != t1 ) return;

	// after the PHI, a compare and a jump
	if (! (q.next(0) instanceof OPER) ) return;
	OPER op0 = (OPER) q.next(0);
	if (op0.opcode.indexOf("cmp")==-1) return; // not a compare.
	if (t1 != op0.operands[0] &&
	    t1 != op0.operands[1]) return; // not sourced from the PHI.
	if (! (op0.next(0) instanceof CJMP) ) return; // not followed by a cjmp
	CJMP cjmp0 = (CJMP) op0.next(0);
	if (cjmp0.test != op0.dst) return; // not sourced from the oper.
	Temp t2 = cjmp0.test;

	// okay.  The gauntlet has been run.  Let's optimize this baby.
	//   arrays of phis, cjmps, cmps...
	OPER op[]   = new OPER[arity];
	CJMP cjmp[] = new CJMP[arity];
	PHI  phi[]  = new PHI [2]; // for true and false.
	for (int i=0; i<arity; i++) {
	    op[i] = new OPER(op0.getSourceElement(), op0.opcode,
			     op0.dst, (Temp[]) op0.operands.clone());
	    cjmp[i] = new CJMP(cjmp0.getSourceElement(), 
			       cjmp0.test, new Temp[0]);
	}
	phi[0] = new PHI(cjmp0.next(0).getSourceElement(), new Temp[0], arity);
	phi[1] = new PHI(cjmp0.next(1).getSourceElement(), new Temp[0], arity);

	// make all the links.
	Quad.addEdge(phi[0], 0, cjmp0.next(0), cjmp0.nextEdge(0).which_pred());
	Quad.addEdge(phi[1], 0, cjmp0.next(1), cjmp0.nextEdge(1).which_pred());
	for (int i=0; i<arity; i++) {
	    Quad.addEdge(q.prev(i), q.prevEdge(i).which_succ(), op[i], 0);
	    Quad.addEdge(op[i], 0, cjmp[i], 0);
	    Quad.addEdge(cjmp[i], 0, phi[0], i);
	    Quad.addEdge(cjmp[i], 1, phi[1], i);
	}
	// done!
    }
    public void visit(INSTANCEOF q) {
	// all sorts of conditions on this before we target it.
	if (! (q.next(0) instanceof OPER) ) return;
	OPER oper = (OPER) q.next(0);
	if (! oper.opcode.startsWith("icmp") ) return;
	if (q.dst != oper.operands[0] &&
	    q.dst != oper.operands[1] ) return;
	if (! (oper.next(0) instanceof CJMP) ) return;
	CJMP cjmp = (CJMP) oper.next(0);
	if (cjmp.test != oper.dst) return;
	// okay.  The command sequence matches our peephole.
	// rewrite such that the INSTANCEOF directly feeds a CJMP.
	/* Here's a picture:
         *   t1 = INSTANCEOF ...         t1 = INSTANCEOF ...
         *   t2 = ICMPEQ t1, 0              CJMP t1
         *   CJMP t2                to     /       \
         *   /      \            t2=ICMPEQ t1,0    t2=ICMPEQ t1,0
         *  x        y               CJMP t2          CJMP t2
         *                           /       \_______/_______\
         *                          /_______________/         \
         *                        PHI                         PHI
         *                         x                           y
	 *
	 * The idea is that both of the ICMPEQs will be eliminated during
	 * constant propagation, leaving only the INSTANCEOF feeding the
	 * branch. (The PHIs should go away once the ICMPs go away.)
	 */
	Quad qq0 = new CJMP(q.getSourceElement(), q.dst, new Temp[0]);
	Quad.addEdge(q, 0, qq0, 0);
	// duplicate following ICMP/CJMP; should be optimized out later on.
	Quad qq1 = new OPER(oper.getSourceElement(), oper.opcode,
			    oper.dst, (Temp[]) oper.operands.clone() );
	Quad qq2 = new CJMP(cjmp.getSourceElement(), cjmp.test, cjmp.src);
	// link tops up.
	Quad.addEdge(qq0, 0, oper, 0);
	Quad.addEdge(qq0, 1, qq1,  0);
	Quad.addEdge(qq1, 0, qq2,  0);
	// now add new PHI nodes where the two CJMPs link up.
	PHI phi0 = new PHI(cjmp.next(0).getSourceElement(), new Temp[0], 2);
	PHI phi1 = new PHI(cjmp.next(1).getSourceElement(), new Temp[0], 2);
	// link phis to their successors.
	Quad.addEdge(phi0, 0, cjmp.next(0), cjmp.nextEdge(0).which_pred());
	Quad.addEdge(phi1, 0, cjmp.next(1), cjmp.nextEdge(1).which_pred());
	// now link CJMPs to PHIs.
	Quad.addEdge(cjmp, 0, phi0, 0);
	Quad.addEdge(cjmp, 1, phi1, 0);
	Quad.addEdge(qq2,  0, phi0, 1);
	Quad.addEdge(qq2,  1, phi1, 1);
	// done!
    }
}
