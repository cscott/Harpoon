package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>QuadList</code> represents a list of <code>Quad</code>s.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadList.java,v 1.2 1998-08-08 00:43:22 cananian Exp $
 */
public abstract interface QuadList {
    public Quad[] next();
    public Quad[] prev();
}
