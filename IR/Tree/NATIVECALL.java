// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>NATIVECALL</code> objects are statements which stand for
 * function calls using standard C calling convention.  These are
 * typically used to implement parts of the runtime system
 * (for example, to invoke the garbage collector) and <i>not</i>
 * for java native method calls (which must use the standard java
 * method calling convention).
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NATIVECALL.java,v 1.3 2002-02-26 22:46:10 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see INVOCATION
 */
public class NATIVECALL extends INVOCATION {
    /** Constructor. */
    public NATIVECALL(TreeFactory tf, HCodeElement source,
		      TEMP retval, Exp func, ExpList args) {
	super(tf, source, retval, func, args, 0);
    }

    public boolean isNative() { return true; }

    public int kind() { return TreeKind.NATIVECALL; }

    public Stm build(TreeFactory tf, ExpList kids) {
	for (ExpList e = kids; e!=null; e=e.tail)
	    Util.ASSERT(tf == e.head.tf);
	Util.ASSERT(tf==this.tf, "cloning retval not yet implemented");
	return new NATIVECALL(tf, this, getRetval()/*XXX needs to clone*/,
			      kids.head,  // func
			      kids.tail); // args
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new NATIVECALL
			   (tf, this, 
			    (this.getRetval()==null) ? null :
			    (TEMP)this.getRetval().rename(tf, tm, cb),
			    (Exp)this.getFunc().rename(tf, tm, cb),
			    ExpList.rename(this.getArgs(), tf, tm, cb)),
			   tm);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NATIVECALL(");
	if (this.getRetval()!=null) 
	    { s.append(" # "+this.getRetval().getID()+","); } 
	s.append(" #"+this.getFunc().getID()+", {");
        for (ExpList list = this.getArgs(); list != null; list=list.tail) {
            s.append(" #" + list.head.getID());
            if (list.tail != null) s.append(",");
        }
        s.append(" })");
        return new String(s);
    }
}
