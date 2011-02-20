// LowQuadVisitor.java, created Tue Jan 19 21:15:19 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

/**
 * <code>LowQuadVisitor</code> is another design pattern.  We live to
 * make Martin happy.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadVisitor.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public abstract class LowQuadVisitor extends harpoon.IR.Quads.QuadVisitor {
    /** if true, then throw an error if given any quads disallowed in lowquad
     *  form. */
    private final boolean strictLowQuad;
    /** Create a <code>LowQuadVisitor</code>. If <code>strictLowQuad</code>
     *  is true (the default), then the visitor will throw an error if
     *  any quads are visited which are disallowed in lowquad form. */
    protected LowQuadVisitor(boolean strictLowQuad) {
	this.strictLowQuad = strictLowQuad;
    }
    protected LowQuadVisitor() { this(true); }

    // DISALLOW certain Quads in LowQuad form.
    /** <code>AGET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.AGET q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>ASET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.ASET q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>CALL</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.CALL q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>GET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.GET q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>HANDLER</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.HANDLER q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>OPER</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.OPER q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    /** <code>SET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public void visit(harpoon.IR.Quads.SET q)
    { if (strictLowQuad) error(q); else super.visit(q); }
    // error function.
    private static final void error(harpoon.IR.Quads.Quad q) {
	throw new Error("Illegal LowQuad: "+q);
    }

    // Visitor functions for new LowQuads.
    public void visit(LowQuad q)    { visit((harpoon.IR.Quads.Quad)q); }
    public void visit(POPER q)      { visit((harpoon.IR.Quads.Quad)q); }
    public void visit(PCALL q)      { visit((harpoon.IR.Quads.SIGMA)q); }
    public void visit(PGET q)       { visit((LowQuad)q); }
    public void visit(PSET q)       { visit((LowQuad)q); }

    // PPTR:
    public void visit(PPTR q)       { visit((LowQuad)q); }
    public void visit(PARRAY q)     { visit((PPTR)q); }
    public void visit(PFIELD q)     { visit((PPTR)q); }
    public void visit(PMETHOD q)    { visit((PPTR)q); }
    // PCONST:
    public void visit(PCONST q)     { visit((LowQuad)q); }
    public void visit(PAOFFSET q)   { visit((PCONST)q); }
    public void visit(PFOFFSET q)   { visit((PCONST)q); }
    public void visit(PMOFFSET q)   { visit((PCONST)q); }
    public void visit(PFCONST q)    { visit((PCONST)q); }
    public void visit(PMCONST q)    { visit((PCONST)q); }
}
