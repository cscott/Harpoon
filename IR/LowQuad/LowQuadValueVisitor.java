// LowQuadValueVisitor.java, created Tue Jan 19 21:15:19 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

/**
 * <code>LowQuadValueVisitor</code> is a visitor class for low quads
 * that returns a parameterized value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadValueVisitor.java,v 1.1 2002-04-11 04:00:21 cananian Exp $
 */
public abstract class LowQuadValueVisitor<T>
    extends harpoon.IR.Quads.QuadValueVisitor<T> {
    /** if true, then throw an error if given any quads disallowed in lowquad
     *  form. */
    private final boolean strictLowQuad;
    /** Create a <code>LowQuadValueVisitor</code>. If <code>strictLowQuad</code>
     *  is true (the default), then the visitor will throw an error if
     *  any quads are visited which are disallowed in lowquad form. */
    protected LowQuadValueVisitor(boolean strictLowQuad) {
	this.strictLowQuad = strictLowQuad;
    }
    protected LowQuadValueVisitor() { this(true); }

    // DISALLOW certain Quads in LowQuad form.
    /** <code>AGET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.AGET q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>ASET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.ASET q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>CALL</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.CALL q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>GET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.GET q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>HANDLER</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.HANDLER q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>OPER</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.OPER q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    /** <code>SET</code> is disallowed in <code>LowQuad</code> form.
     *  This method throws an <code>Error</code> unless !strictLowQuad. */
    public T visit(harpoon.IR.Quads.SET q)
    { if (strictLowQuad) error(q); return super.visit(q); }
    // error function.
    private static final void error(harpoon.IR.Quads.Quad q) {
	throw new Error("Illegal LowQuad: "+q);
    }

    // Visitor functions for new LowQuads.
    public T visit(LowQuad q)    { return visit((harpoon.IR.Quads.Quad)q); }
    public T visit(POPER q)      { return visit((harpoon.IR.Quads.Quad)q); }
    public T visit(PCALL q)      { return visit((harpoon.IR.Quads.SIGMA)q); }
    public T visit(PGET q)       { return visit((LowQuad)q); }
    public T visit(PSET q)       { return visit((LowQuad)q); }

    // PPTR:
    public T visit(PPTR q)       { return visit((LowQuad)q); }
    public T visit(PARRAY q)     { return visit((PPTR)q); }
    public T visit(PFIELD q)     { return visit((PPTR)q); }
    public T visit(PMETHOD q)    { return visit((PPTR)q); }
    // PCONST:
    public T visit(PCONST q)     { return visit((LowQuad)q); }
    public T visit(PAOFFSET q)   { return visit((PCONST)q); }
    public T visit(PFOFFSET q)   { return visit((PCONST)q); }
    public T visit(PMOFFSET q)   { return visit((PCONST)q); }
    public T visit(PFCONST q)    { return visit((PCONST)q); }
    public T visit(PMCONST q)    { return visit((PCONST)q); }
}
