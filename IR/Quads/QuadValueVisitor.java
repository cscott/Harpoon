// QuadValueVisitor.java, created by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>QuadValueVisitor</code> is a visitor class that returns
 * a (parameterized) value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadValueVisitor.java,v 1.1 2002-04-11 04:00:34 cananian Exp $
 */
public abstract class QuadValueVisitor<T>  {
    protected QuadValueVisitor() { }

    /** Visit a quad q. */
    public abstract T visit(Quad q);
    public T visit(AGET q)		{ return visit((Quad)q); }
    public T visit(ALENGTH q)		{ return visit((Quad)q); }
    public T visit(ANEW q)		{ return visit((Quad)q); }
    public T visit(ARRAYINIT q)		{ return visit((Quad)q); }
    public T visit(ASET q)		{ return visit((Quad)q); }
    public T visit(CALL q)		{ return visit((SIGMA)q); }
    public T visit(CJMP q)		{ return visit((SIGMA)q); }
    public T visit(COMPONENTOF q)	{ return visit((Quad)q); }
    public T visit(CONST q)		{ return visit((Quad)q); }
    public T visit(DEBUG q)		{ return visit((Quad)q); }
    public T visit(FOOTER q)		{ return visit((Quad)q); }
    public T visit(GET q)		{ return visit((Quad)q); }
    public T visit(HEADER q)		{ return visit((Quad)q); }
    public T visit(INSTANCEOF q)	{ return visit((Quad)q); }
    public T visit(LABEL q)		{ return visit((PHI)q); }
    public T visit(HANDLER q)		{ return visit((Quad)q); }
    public T visit(METHOD q)		{ return visit((Quad)q); }
    public T visit(MONITORENTER q)	{ return visit((Quad)q); }
    public T visit(MONITOREXIT q)	{ return visit((Quad)q); }
    public T visit(MOVE q)		{ return visit((Quad)q); }
    public T visit(NEW q)		{ return visit((Quad)q); }
    public T visit(NOP q)		{ return visit((Quad)q); }
    public T visit(OPER q)		{ return visit((Quad)q); }
    public T visit(PHI q)		{ return visit((Quad)q); }
    public T visit(RETURN q)		{ return visit((Quad)q); }
    public T visit(SET q)		{ return visit((Quad)q); }
    public T visit(SIGMA q)		{ return visit((Quad)q); }
    public T visit(SWITCH q)		{ return visit((SIGMA)q); }
    public T visit(THROW q)		{ return visit((Quad)q); }
    public T visit(TYPECAST q)		{ return visit((NOP)q); }
    public T visit(TYPESWITCH q) 	{ return visit((SIGMA)q); }
    public T visit(XI q)		{ return visit((PHI)q); }
}
