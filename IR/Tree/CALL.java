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
 * @version $Id: CALL.java,v 1.1.2.20 1999-10-23 05:59:34 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see INVOCATION
 * @see NATIVECALL
 */
public class CALL extends INVOCATION {
    /** Destination for any exception which the callee might throw.
     *  Must be non-null. */
    public TEMP retex;
    /** Expression indicating the destination to which we should return
     *  if our caller throws an exception. */
    public NAME handler;
    /** Whether this invocation should be performed as a tail call. */
    public boolean isTailCall;

    /** Create a <code>CALL</code> object. */
    public CALL(TreeFactory tf, HCodeElement source,
		TEMP retval, TEMP retex, Exp func, ExpList args,
		NAME handler, boolean isTailCall) {
	super(tf, source, retval, func, args, 2); // this.next_arity()==2
	Util.assert(retex != null && handler != null);
	Util.assert(retex.tf == tf);
	this.retex = retex;
	this.handler = handler;
	this.isTailCall = isTailCall;
    }
  
    public boolean isNative() { return false; }

    public ExpList kids() {
	ExpList result = new ExpList(retex, new ExpList
				     (handler, new ExpList(func, args))); 
	if (retval==null) return result;
	else return new ExpList(retval, result);
    }

    public int kind() { return TreeKind.CALL; }

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) {
	TEMP kids_retval = null;
	for (ExpList e = kids; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	if (kids.tail.head.kind()==TreeKind.TEMP) { // non-null retval!
	    kids_retval = (TEMP) kids.head;
	    kids = kids.tail;
	}
	return new CALL(tf, this, kids_retval,
			(TEMP)kids.head, // retex
			kids.tail.tail.head,  // func
			kids.tail.tail.tail,  // args
			(NAME)kids.tail.head, // handler
			isTailCall); 
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CALL(tf, this, 
			(TEMP)retval.rename(tf, ctm),
			(TEMP)retex.rename(tf, ctm), 
			(Exp)func.rename(tf, ctm),
			ExpList.rename(args, tf, ctm),
			(NAME)handler.rename(tf, ctm),
			isTailCall);
  }

    protected Set defSet() { 
	Set def = super.defSet();
	//if (retex.kind()==TreeKind.TEMP)  def.add(((TEMP)retex).temp);
	return def;
    }

    protected Set useSet() { 
	Set uses = super.useSet();
	//if (!(retex.kind()==TreeKind.TEMP))  uses.addAll(retex.useSet());
	return uses;
    }

    public String toString() {
        ExpList list;
        StringBuffer s = new StringBuffer();
        s.append("CALL(");
	if (retval==null) s.append("null"); else s.append("#"+retval.getID());
	s.append(", #"+retex.getID()+
                 ", #" + func.getID() + ", {");
        list = args;
        while (list != null) {
            s.append(" #"+list.head.getID());
            if (list.tail != null) {
                s.append(",");
            }
            list = list.tail;
        }
        s.append(" }");
	s.append(", #"+handler.getID());
	s.append(")");
	if (isTailCall) s.append(" [tail call]");
        return new String(s);
    }
}
