// Stm.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

/**
 * <code>Stm</code> objects are statements which perform side effects and
 * control flow.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Stm.java,v 1.1.2.13 2000-01-05 04:06:35 duncan Exp $
 */
abstract public class Stm extends Tree {
    protected Stm(TreeFactory tf, harpoon.ClassFile.HCodeElement source) {
	super(tf, source);
    }
    
    protected Stm(TreeFactory tf, harpoon.ClassFile.HCodeElement source,
		  int next_arity) {
	super(tf, source, next_arity);
    }

    /** Build an <code>Stm</code> of this type from the given list of
     *  subexpressions. */
    abstract public Stm build(ExpList kids);
    abstract public Stm build(TreeFactory tf, ExpList kids);
    
    // Overridden by MOVE and INVOCATION
    protected Set defSet() { return Collections.EMPTY_SET; }
    protected Set useSet() { return ExpList.useSet(kids()); }

    public abstract Tree rename(TreeFactory tf, CloningTempMap ctm);

    /** Returns a tree-based representation of <code>list</code>.  
     *
     * <br><b>Requires:</b> foreach element, <code>l</code>, of 
     *                      <code>list</code>, 
     *                      <code>(l != null) && (l instanceof Stm)</code>.
     * <br><b>Modifies:</b>
     * <br><b>Effects: </b> returns a tree-based representation of 
     *                      <code>list</code>.  If <code>list</code> is null,
     *                      returns null.  
     */
    public static Stm toStm(List list) { 
	if (list==null) return null;
	int size = list.size();
	if      (size==0) { return null; }
	else if (size==1) { return (Stm)list.get(0); } 
	else { 
	    Stm          hce = (Stm)list.get(0); 
	    TreeFactory  tf  = hce.getFactory();
	    SEQ s=new SEQ(tf,hce,(Stm)list.get(size-2),(Stm)list.get(size-1));
	    for (ListIterator li=list.listIterator(size-2);li.hasPrevious();) {
		Stm previous = (Stm)li.previous();
		Util.assert(previous.getFactory()==tf);
		s = new SEQ(tf, hce, previous, s);
	    }
	    return s;
	}		
    }

    /**
     * Returns a <code>Stm</code> such that all <code>SEQ</code> objects
     * contained within the <code>Stm</code> object are on the top level.
     */
    public static Stm linearize(Stm stm) { 
	List  l = new ArrayList();
	Stack s = new Stack();
	s.push(stm);

	while (!s.isEmpty()) { 
	    Stm next = (Stm)s.pop();
	    if (next.kind() == TreeKind.SEQ) { 
		SEQ seq = (SEQ)next; 
		s.push(seq.right);
		s.push(seq.left);
	    } 
	    else { 
		l.add(next);
	    } 
	}
	return toStm(l);
    }

    /** 
     * Returns true if <code>stm</code> has no effect. 
     */
    public static boolean isNop(Stm stm) { 
	return (stm.kind()==TreeKind.EXP) && 
	    ((((EXP)stm).exp).kind()==TreeKind.CONST);
    }
}

