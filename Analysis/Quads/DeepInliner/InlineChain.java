// InlineChain.java, created Tue Jul 26 06:09:22 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads.DeepInliner;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Collections;
import java.util.Collection;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;

import harpoon.Util.Util;

/**
   <code>InlineChain</code> models an arbitrary-length call path that
   needs to be inlined.  Basically, an <code>InlineChain</code> is a
   list of <code>CALL</code>s: <code>cs1</code>, <code>cs2</code>,
   ..., <code>csk</code> (where <code>cs</code>(i+1) is part of the
   method called by <code>cs</code>i, forall i).  Inlining such a
   chain mutates only the body of the method that contains
   <code>cs1</code>: if <code>m</code>i is the method called by
   <code>cs</code>i, <code>cs1</code> is replaced with a copy of
   <code>m1</code>, inside which <code>cs2</code> has been replaced
   with a copy of <code>m2</code>, inside which <code>cs3</code> has
   been replaced with a copy of <code>m3</code>, and so on.

   <p>Intuitively, the outcome of inlining a chain is similar to the
   result of inlining <code>cs1</code>, next inlining the copy of
   <code>cs2</code> from the copy of <code>m1</code> that replaced
   <code>cs1</code>, and so on.

   <p>The programmer may subclass <code>InlineChain</code> to specify
   an action to be performed after each individual call is inlined
   ({@link #action}) and an action to be performed after the entire call
   chain was inlined ({@see #finalAction}).

   The method <code>DeepInliner.inline</code> is able to inline all
   the inlining chains generated for the compiled program.

   @see DeepInliner#inline
 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: InlineChain.java,v 1.1 2005-08-09 22:40:35 salcianu Exp $ */
public abstract class InlineChain {

    public InlineChain(List<CALL> calls) {
	assert calls.size() > 0 : "cannot inline an empty chain of calls";
	this.calls = new LinkedList<CALL>(calls);
	this.targetMethod = Util.quad2method(this.calls.getFirst());
    }

    private final HMethod targetMethod;
    final HMethod getTargetMethod() { return targetMethod; }

    // Calls to be inlined, starting with the CALL inside the target method
    private final LinkedList<CALL> calls;

    /** @return Unmodifiable view of the <code>CALL</code>s from
        <code>this</code> inline chain. */
    public final Collection<CALL> calls() {
	return Collections.unmodifiableCollection(calls);
    }


    /** 
	@param cs The last call that has been inlined.

	@param calleeCode The code of the callee.  This is the
	original code, NOT its copy that was inlined in
	<code>cs</code>'s place.  The quads from
	<code>caleeCode</code> are the keys of the map passed as the
	<code>oldQuad2newQuad</code> param; still,
	<code>calleeCode</code> is not redundant, as it may contain
	other information besides the instructions (ex: the
	<code>AllocationInformation</code> map).

	@param oldQuad2newQuad A map from the original quads
	(instructions) of the last to be inlined method to their
	copies that are now part of the new body (<code>HCode</code>)
	of the target method.  */
    public void action(CALL cs, Code calleeCode, Map<Quad,Quad> oldQuad2newQuad) {
	// default implem: do nothing
    }


    public void finalAction() {
	// default implem: do nothing
    }
    

    final void applyInline(CALL cs, Code calleeCode, Map<Quad,Quad> oldQuad2newQuad) {
	boolean changed = false;

	for(ListIterator<CALL> lIter = calls.listIterator(); lIter.hasNext(); ) {
	    CALL cs2 = lIter.next();
	    if(cs2.equals(cs)) {
		changed = true;
		// cs2 has already been inlined, we can consider it done
		// and forget about it ...
		lIter.remove();
		// .. but not before executing the user action for it
		action(cs, calleeCode, oldQuad2newQuad);

		if(lIter.hasNext()) {
		    // If there is a next call in the inline chain,
		    // replace it with its clone
		    CALL csNext = lIter.next();
		    lIter.remove();
		    CALL newCsNext = (CALL) oldQuad2newQuad.get(csNext);
		    assert newCsNext != null : "wrong orig -> cloned";
		    lIter.add(newCsNext);
		}
		else {
		    finalAction();
		}
	    }
	}

	if(changed && DeepInliner.DEBUG) {
	    System.out.println("New inline chain: " + this);
	}
    }

    final boolean isEmpty()         { return calls.isEmpty(); }


    public String toString() {
	if(this.isEmpty()) {
	    // the chain may become null as a result of repeated re-adjustements
	    return "EMPTY INLINE CHAIN";
	}

	StringBuffer buff = new StringBuffer();	
	buff.append("\nInlineChain:");
	buff.append(callsToString(this.calls));
	return buff.toString();
    }


    public static String callsToString(LinkedList<CALL> calls) {
	StringBuffer buff = new StringBuffer("{");
	for(CALL cs : calls) {
	    buff.append("\n  ");
	    buff.append(Util.quad2method(cs));
	    buff.append("\n    ");
	    buff.append(Util.code2str(cs));
	}
	// print the last method
	buff.append("\n  ");
	buff.append(calls.getLast().method());
	// We are not sure the last call .method() is indeed the
	// called method (we would need a call graph for the virtual
	// calls).  We print a (*) to draw programmer's attention.
	buff.append(" (*)");
	buff.append("\n}");
	return buff.toString();	
    }

}
