// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>NATIVECALL</code> objects are statements which stand for
 * function calls using standard C calling convention.  These are
 * typically used to implement java "native" method calls, although
 * they can also be used to implement part of the runtime system
 * (for example, to invoke the garbage collector).
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NATIVECALL.java,v 1.1.2.10 1999-08-04 05:52:30 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see INVOCATION
 */
public class NATIVECALL extends INVOCATION {
    /** Constructor. */
    public NATIVECALL(TreeFactory tf, HCodeElement source,
		Exp retval, Exp func, ExpList args) {
	super(tf, source, retval, func, args);
    }

    public boolean isNative() { return true; }
  
    public ExpList kids() { 
        return new ExpList
	    (retval, new ExpList(func, args));
    }

    public int kind() { return TreeKind.NATIVECALL; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
  
    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf && 
		    tf == kids.tail.head.tf &&
		    tf == kids.tail.tail.head.tf);
	for (ExpList e = kids.tail.tail.tail; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	return new NATIVECALL(tf, this,
			      kids.head,       // retval
			      kids.tail.head,  // func
			      kids.tail.tail); // args
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new NATIVECALL(tf, this, 
			      (Exp)retval.rename(tf, ctm),
			      (Exp)func.rename(tf, ctm),
			      ExpList.rename(args, tf, ctm));
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NATIVECALL(# "+retval.getID()+", #"+func.getID()+", {");
        for (ExpList list = args; list != null; list=list.tail) {
            s.append(" #" + list.head.getID());
            if (list.tail != null) s.append(",");
        }
        s.append(" })");
        return new String(s);
    }
}
