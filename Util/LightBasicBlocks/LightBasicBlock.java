// LightBasicBlock.java, created Thu Mar 23 17:44:52 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.BasicBlockInterf;
import harpoon.Analysis.BasicBlockInterfVisitor;
import harpoon.Analysis.BasicBlockFactoryInterf;
import harpoon.Analysis.FCFGBasicBlock;
import harpoon.Util.UComp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.METHOD;

import harpoon.Util.Util;

/**
 * <code>LightBasicBlock</code> is designed as a compact version of a
 <code>BasicBlock</code>. The next and the previous basic blocks, as well
 as the composing instructions are stored in arrays, instead of expensive
 <code>Set</code>s. The traversal of these structures is far cheaper that
 the equaivalent operation on <code>BasicBlock</code>s: no
 <code>Iterator</code> object need to be dynamically created, it's juat an
 integer index.

 <p>
 <b>Use:</b> To obtain the <code>LightBasicBlock</code>s of a
 given method, you have to obtain a
 <code>BasicBlock.Factory</code> for that method and pass it to
 <code>LightBasicBlock.Factory</code>.

 <p>
 If the &quot;quad-with-try&quot; IR is used, the light basic
 blocks contain info about the handlers that handle the exceptions
 that could be thrown by the instructions from each light basic block.

 <p>
 <b>Note:</b> The interface might seem minimal but I <b>recommend</b> that
 you use it instead of adding some other methods. For example, one might
 complain that there are no methods to return the number of predecessors,
 nor the <code>i</code>-th predecessor. Here is why: you are expected to
 traverse the list of predecessors in the cheapest way: extract the array
 of predecessors and then do a for whose condition looks something like
 <code>i&lt;pred.length</code> instead of <code>i&lt;lbb.predLength()</code>
 (one method call per iteration!).<br>

 * 
 * @author  Alexandru SALCIANU <salcianu@mit.edu>
 * @version $Id: LightBasicBlock.java,v 1.1.2.10 2002-01-09 13:45:58 salcianu Exp $ */
public class LightBasicBlock implements java.io.Serializable {

    /** The user can place its annotations here.
	@deprecated */
    public Object user_info = null;

    /** Personal numeric ID */
    private int id;

    // See the TODO notice about Arrays.sort(...)
    public String str;

    /** The only way to produce <code>LightBasicBlock</code> is via 
	a <code>LightBasicBlock.Factory</code>. Outside this package, you
	cannot manufacture them. */
    LightBasicBlock(BasicBlockInterf bb) {
	// See the TODO notice about Arrays.sort(...)
	this.str = bb.toString();
	//LightBasicBlock(int id) {
	//this.id = id;
    }

    /** The successor basic blocks. */
    LightBasicBlock[] next = null;
    /** The predecessor basic blocks. */
    LightBasicBlock[] prev = null;
    /** The instructions in <code>this</code> basic block. */
    HCodeElement[] elements = null;

    /** The index in the next array where the exception handlers start. **/
    int handlerStartIndex;
    /** The index in the prev array where the protected lbbs start. */
    int protectedStartIndex;

    /** Returns the array of successor light basic blocks. Starting
	from position <code>getHandlerStartIndex()</code> on (including
	that position), the entries of the returned array point to
	light basic blocks corresponding to the handlers for the
	instructions of this (light) basic block. The handlers appear
	in the order in which they catch exceptions. The exit point of
	the method is considered to be the default handler (any
	uncaught exception is sent to the caller). */
    public final LightBasicBlock[] getNextLBBs() { return next; }

    /** Returns the index of the first handler into the array of next
        (light) basic blocks.
	@see getNextLBBs */
    public final int getHandlerStartIndex() { return handlerStartIndex; }


    /** Returns the array of predecessor light basic blocks. For basic
	blocks that start with a <code>HANDLER</code> instruction, the
	entries of the returned array starting from position
	<code>getProtectedStartIndex()</code> on (including that
	position) point to the light basic block composed of
	instructions which are protected by this <code>this</code>
	basic block. The end of the method is the only basic block
	that has both normal flow predecessors and protected basic
	blocks (any uncaught exception is passed down to the
	caller). */
    public final LightBasicBlock[] getPrevLBBs() { return prev; }

    /** Returns the index of the first protected basic block in the
	array of (light) basic blocks. 
	@see getPrevLBBs */
    public final int getProtectedStartIndex() { return protectedStartIndex; }

    /** Returns the inctructions in <code>this</code> basic block. */
    public final HCodeElement[] getElements() { return elements; }

    /** Returns the first instruction from <code>this</code> basic block. */
    public final HCodeElement getFirstElement() {
	if(elements.length > 0)
	    return elements[0];
	return null;
    }

    /** Returns the last instruction from <code>this</code> basic block. */
    public final HCodeElement getLastElement(){
	int len = elements.length;
	if(len > 0)
	    return elements[len - 1];
	return null;
    }

    /** String representation. */
    public final String toString(){
	// See the TODO notice about Arrays.sort(...)
	//return "LBB" + id;
	return str;
    }

    /** Converts the large, set based, basic blocks produced
	by a <code>BasicBlock.Factory</code> into smaller, array based,
	light basic blocks. */
    public static class Factory implements java.io.Serializable {
	
	private int count = 0;

	/** Creates a <code>LighBasicBlock.Factory</code> object.
	    It simply converts the large, set based, basic blocks produced
	    by <code>bbfact</code> into smaller, array based, light basic
	    blocks. */
	public Factory(BasicBlockFactoryInterf bbfact) {
	    // special case: method with inaccessible code (e.g. natives)
	    if(bbfact == null) return;
	    hcode = bbfact.getHCode();
	    // special case: method with inaccessible code (e.g. natives)
	    if(hcode == null) return;

	    Set bbs = bbfact.blockSet();
	    int nb_bbs = bbs.size();
	    lbbs = new LightBasicBlock[nb_bbs];
	    BasicBlockInterf[] l2b = new BasicBlockInterf[nb_bbs];
	    Map b2l = new HashMap();

	    create_lbbs(bbs, b2l, l2b);

	    // record the root basic block
	    root_lbb = (LightBasicBlock) b2l.get(bbfact.getRootBBInterf());

	    // set the links between the LightBasicBlocks:
	    // 1. succesors
	    set_next(b2l, l2b);
	    // 2. predecessors
	    set_prev(b2l, l2b);
	}

	// the underlying hcode
	HCode hcode = null;
	// all the light basic blocks 
	LightBasicBlock[] lbbs = null;
	// the root LightBasicBlock
	LightBasicBlock root_lbb = null;

	/** Returns the underlying <code>HCode</code>. This factory returns
	    <code>LightBasicBlock</code>s of the code returned by this
	    method. */
	public HCode getHCode() { return hcode; }

	/** Returns all the <code>LightBasicBlock</code>s of the underlying
	    <code>HCode</code>. */
	public LightBasicBlock[] getAllBBs() { return lbbs; }

	/** Returns the root <code>LightBasicBlock</code>. */
	public LightBasicBlock getRoot() { return root_lbb; }

	// map HCodeElement -> LightBasicBlock; computed "on demand"
	private Map hce2lbb = null;

	/** Returns the <code>LightBasicBlock</code> the instruction
	    <code>hce</code> belongs to. */
	public LightBasicBlock getBlock(HCodeElement hce) {
	    if(hce2lbb == null){
		// we need to compute hce2lbb (first time)
		hce2lbb = new HashMap();
		for(int i = 0; i< lbbs.length; i++){
		    LightBasicBlock lbb = lbbs[i];
		    HCodeElement[] hces = lbb.getElements();
		    for(int k = 0; k < hces.length; k++)
			hce2lbb.put(hces[k], lbb);
		}
	    }

	    return (LightBasicBlock) hce2lbb.get(hce);
	}

	private void create_lbbs(Set bbs, Map b2l, BasicBlockInterf[] l2b) {
	    // create the LightBasicBlocks
	    Iterator it = bbs.iterator();
	    for(int i = 0; i < lbbs.length; i++) {
		BasicBlockInterf bb = (BasicBlockInterf) it.next();
		LightBasicBlock lbb = new LightBasicBlock(bb);

		// store the basic block instructions
		List instrs = bb.statements();

		lbb.elements = (HCodeElement[])
		    instrs.toArray(new HCodeElement[instrs.size()]);

		lbbs[i] = lbb;
		// record the mapping Light Basic Block <-> Basic Block
		l2b[i]  = bb;
		b2l.put(bb, lbb);
	    }
	}

	// set the successors for the code views with explicit
	// treatment of exception; straightforward
	private void set_next
	    (final Map b2l, final BasicBlockInterf[] l2b) {

	    class BBInterfVisitorNext extends BasicBlockInterfVisitor {

		LightBasicBlock lbb = null;
		
		public void visit(BasicBlock bb) {
		    LightBasicBlock[] lnext = 
			new LightBasicBlock[bb.nextLength()];
		    
		    int k = 0;
		    for(Iterator it = bb.nextSet().iterator();
			it.hasNext(); k++)
			lnext[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    // Because there is no explicit loop view for the
		    // moment, it helps the dataflow analysis to have
		    // "next" sorted (the basic blocks closer to the
		    // header of the method (loop header) will be in
		    // the first positions.  TODO: this should be
		    // eliminated once a decent loop view of the code
		    // is implemented.
		    Arrays.sort(lnext, UComp.uc);
		    
		    lbb.handlerStartIndex = k;
		    // no handlers
		    lbb.next = lnext;
		}
		
		public void visit(FCFGBasicBlock bb) {
		    LightBasicBlock[] lnext = 
			new LightBasicBlock[bb.normalNextSet().size() + 
					   bb.handlerList().size()];
		    int k = 0;
		    for(Iterator it = bb.normalNextSet().iterator();
			it.hasNext(); k++)
			lnext[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    lbb.handlerStartIndex = k;
		    for(Iterator it = bb.handlerList().iterator();
			it.hasNext(); k++)
			lnext[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    lbb.next = lnext;
		}
		
		public void visit(BasicBlockInterf bb) {
		    Util.assert(false, "Unknown BasicBlockInterf!");
		}
	    };
	    
	    BBInterfVisitorNext visitor = new BBInterfVisitorNext();

	    for(int i = 0; i < lbbs.length; i++) {
		// lbbs[i] is the ith basic block,
		// l2b[i] is the corresponding light basic block
		visitor.lbb = lbbs[i];
		// visit l2b[i] and set lbbs[i].next
		l2b[i].accept(visitor);
	    }
	}

	// set the successors for the code views with explicit
	// treatment of exception; straightforward
	private void set_prev(final Map b2l, final BasicBlockInterf[] l2b) {

	    class BBInterfVisitorPrev extends BasicBlockInterfVisitor {

		LightBasicBlock lbb = null;
		
		public void visit(BasicBlock bb) {
		    LightBasicBlock[] lprev = 
			new LightBasicBlock[bb.prevLength()];
		    
		    int k = 0;
		    for(Iterator it = bb.prevSet().iterator();
			it.hasNext(); k++)
			lprev[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    lbb.protectedStartIndex = k;
		    // no protected set
		    lbb.prev = lprev;
		}
		
		public void visit(FCFGBasicBlock bb) {
		    LightBasicBlock[] lprev = 
			new LightBasicBlock[bb.normalPrevSet().size() + 
					   bb.protectedSet().size()];
		    int k = 0;
		    for(Iterator it = bb.normalPrevSet().iterator();
			it.hasNext(); k++)
			lprev[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    lbb.protectedStartIndex = k;
		    for(Iterator it = bb.protectedSet().iterator();
			it.hasNext(); k++)
			lprev[k] = (LightBasicBlock) b2l.get(it.next());
		    
		    lbb.prev = lprev;
		}
		
		public void visit(BasicBlockInterf bb) {
		    Util.assert(false, "Unknown BasicBlockInterf!");
		}
	    };
	    
	    BBInterfVisitorPrev visitor = new BBInterfVisitorPrev();

	    for(int i = 0; i < lbbs.length; i++) {
		// lbbs[i] is the ith basic block,
		// l2b[i] is the corresponding light basic block
		visitor.lbb = lbbs[i];
		// visit l2b[i] and set lbbs[i].prev
		l2b[i].accept(visitor);
	    }
	}

    }

}
