// Visitor.java, created Fri Sep 11 12:59:44 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Visitor</code> is a Design Pattern, courtesy of Martin.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Visitor.java,v 1.1 1998-09-11 17:13:58 cananian Exp $
 */

public abstract class Visitor  {
    /** Visit a quad q. */
    abstract void visit(Quad q);
    void visit(AGET q)		{ visit((Quad)q); }
    void visit(ALENGTH q)	{ visit((Quad)q); }
    void visit(ANEW q)		{ visit((Quad)q); }
    void visit(ASET q)		{ visit((Quad)q); }
    void visit(CALL q)		{ visit((Quad)q); }
    void visit(CJMP q)		{ visit((Quad)q); }
    void visit(COMPONENTOF q)	{ visit((Quad)q); }
    void visit(CONST q)		{ visit((Quad)q); }
    void visit(FOOTER q)	{ visit((Quad)q); }
    void visit(GET q)		{ visit((Quad)q); }
    void visit(HEADER q)	{ visit((Quad)q); }
    void visit(INSTANCEOF q)	{ visit((Quad)q); }
    void visit(METHODHEADER q)	{ visit((Quad)q); }
    void visit(MONITOR q)	{ visit((Quad)q); }
    void visit(MOVE q)		{ visit((Quad)q); }
    void visit(NEW q)		{ visit((Quad)q); }
    void visit(NOP q)		{ visit((Quad)q); }
    void visit(OPER q)		{ visit((Quad)q); }
    void visit(PHI q)		{ visit((Quad)q); }
    void visit(RETURN q)	{ visit((Quad)q); }
    void visit(SET q)		{ visit((Quad)q); }
    void visit(SWITCH q)	{ visit((Quad)q); }
    void visit(THROW q)		{ visit((Quad)q); }
}
