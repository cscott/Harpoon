// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>CALL</code> objects are statements which stand for 
 * java method invocations, using our runtime's calling convention.
 * <p>
 * The <code>retex</code> expression should typically be a
 * <code>Tree.NAME</code> specifying the label to which we should return
 * from this call if an exception occurs.  It is possible that 
 * <code>retex</code> specify an arbitrary expression instead, but the
 * usefulness of this is questionable and so this is strongly
 * discouraged.
 * <p>
 * Upon an exception in the callee, execution is returned at the
 * location specified by <code>retex</code> with the exception object
 * thrown placed in the location specified by <code>retval</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CALL.java,v 1.1.2.17 1999-08-04 05:52:29 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see INVOCATION
 * @see NATIVECALL
 */
public class CALL extends INVOCATION {
    /** Expression indicating the destination to which we should return
     *  if our caller throws an exception. */
    public Exp retex;

    /** Create a <code>CALL</code> object. */
    public CALL(TreeFactory tf, HCodeElement source,
		Exp retval, Exp retex, Exp func, ExpList args) {
	super(tf, source, retval, func, args);
	this.retex = retex;
    }
  
    public boolean isNative() { return false; }

    public ExpList kids() {
        return new ExpList
	    (retval, new ExpList(retex, new ExpList(func, args))); 
    }

    public int kind() { return TreeKind.CALL; }

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf && 
		    tf == kids.tail.head.tf &&
		    tf == kids.tail.tail.head.tf);
	for (ExpList e = kids.tail.tail.tail; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	return new CALL(tf, this,
			kids.head,            // retval
			kids.tail.head,       // retex
			kids.tail.tail.head,  // func
			kids.tail.tail.tail); // args
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CALL(tf, this, 
			(Exp)retval.rename(tf, ctm),
			(Exp)retex.rename(tf, ctm), 
			(Exp)func.rename(tf, ctm),
			ExpList.rename(args, tf, ctm));
  }

    protected Set defSet() { 
	Set def = super.defSet();
	if (retex.kind()==TreeKind.TEMP)  def.add(((TEMP)retex).temp);
	return def;
    }

    protected Set useSet() { 
	Set uses = super.useSet();
	if (!(retex.kind()==TreeKind.TEMP))  uses.addAll(retex.useSet());
	return uses;
    }

    public String toString() {
        ExpList list;
        StringBuffer s = new StringBuffer();
        s.append("CALL(#"+retval.getID()+", #"+retex.getID()+
                 ", #" + func.getID() + ", {");
        list = args;
        while (list != null) {
            s.append(" #"+list.head.getID());
            if (list.tail != null) {
                s.append(",");
            }
            list = list.tail;
        }
        s.append(" })");
        return new String(s);
    }
}
