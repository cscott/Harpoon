// LightBasicBlock.java, created Thu Mar 23 17:44:52 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.BasicBlock;
import harpoon.Util.UComp;

/**
 * <code>LightBasicBlock</code> is designed as a compact version of a
 <code>BasicBlock</code>. The next and the previous basic blocks, as well
 as the composing instructions are stored in arrays, instead of expensive
 <code>Set</code>s. The traversal of these structures is far cheaper that
 the equaivalent operation on <code>BasicBlock</code>s: no
 <code>Iterator</code> object need to be dynamically created, it's juat an
 integer index.<br>
 <b>Note:</b> The interface might seem minimal but I <b>insist</b> that
 you use it instead of adding some other methods. For example, one might
 complain that there are no methods to return the number of predecessors,
 nor the <code>i</code>-th predecessor. Here is why: you are expect to
 traverse the list of predecessors in the cheapest way: extract the array
 of predecessors and then do a for whose condition looks something like
 <code>i&lt;pred.length</code> instead of <code>i&lt;lbb.predLength()</code>
 (one method call per iteration!).
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: LightBasicBlock.java,v 1.1.2.8 2001-06-17 22:37:13 cananian Exp $
 */
public class LightBasicBlock implements java.io.Serializable {

    /** The user can place its annotations here. */
    public Object user_info = null;

    /** Personal numeric ID */
    private int id;

    // See the TODO notice about Arrays.sort(...)
    public String str;

    /** The only way to produce <code>LightBasicBlock</code> is via 
	a <code>LightBasicBlock.Factory</code>. Outside this package, you
	cannot manufacture them. */
    LightBasicBlock(BasicBlock bb) {
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

    /** Returns the successor light basic blocks. */
    public final LightBasicBlock[] getNextLBBs() { return next; }

    /** Returns the predecessor basic blocks. */
    public final LightBasicBlock[] getPrevLBBs() { return prev; }

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
	public Factory(BasicBlock.Factory bbfact){
	    convert(bbfact);
	}
	
	// the underlying hcode
	HCode hcode;
	// all the light basic blocks 
	LightBasicBlock[] lbbs = null;
	// the root LightBasicBlock
	LightBasicBlock root_lbb;

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

	// convert from the set based BasicBlocks produced by bbfact
	// to the array based LightBasicBlocks.
	final void convert(BasicBlock.Factory bbfact) {
	    hcode = bbfact.getHCode();
	    Set bbs = bbfact.blockSet();
	    int nb_bbs = bbs.size();
	    lbbs = new LightBasicBlock[nb_bbs];
	    BasicBlock[] l2b = new BasicBlock[nb_bbs];
	    Map b2l = new HashMap();

	    // create the LightBasicBlocks
	    Iterator it = bbs.iterator();
	    for(int i = 0; i < nb_bbs; i++){
		BasicBlock bb = (BasicBlock) it.next();

		//LightBasicBlock lbb = new LightBasicBlock(count++);
		LightBasicBlock lbb = new LightBasicBlock(bb);

		// store the instructions of the basic block
		List instrs = bb.statements();
		lbb.elements = (HCodeElement[])
		    instrs.toArray(new HCodeElement[instrs.size()]);

		lbbs[i] = lbb;
		// record the mapping Light Basic Block <-> Basic Block
		l2b[i]  = bb;
		b2l.put(bb, lbb);
	    }

	    // setting the links between the LightBasicBlocks
	    for(int i = 0; i < nb_bbs; i++){
		LightBasicBlock lbb = lbbs[i];
		BasicBlock bb = l2b[i];

		LightBasicBlock[] lnext = new LightBasicBlock[bb.nextLength()];
		int k = 0;
		for(Enumeration en = bb.next(); en.hasMoreElements(); k++)
		    lnext[k] = (LightBasicBlock) b2l.get(
				    (BasicBlock)en.nextElement());

		LightBasicBlock[] lprev = new LightBasicBlock[bb.prevLength()];
		k = 0;
		for(Enumeration ep = bb.prev(); ep.hasMoreElements(); k++)
		    lprev[k] = (LightBasicBlock) b2l.get(
				    (BasicBlock)ep.nextElement());

		// Because there is no explicit loop view for the moment, it
		// helps the dataflow analysis to have "next" sorted (the
		// basic blocks closer to the header of the method (loop
		// header) will be in the first positions.
		// TODO: this should be eliminated once a decent loop view
		//  of the code is implemented.
		Arrays.sort(lnext, UComp.uc);

		lbb.next = lnext;
		lbb.prev = lprev;
	    }

	    // recording the root of the basic block structure
	    root_lbb = (LightBasicBlock) b2l.get(bbfact.getRoot());
	}

    }

}
