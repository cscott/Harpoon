// BasicBlock.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.WorkSet;
import harpoon.Util.Worklist;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCode;
import harpoon.IR.Properties.CFGrapher;

import harpoon.Analysis.DataFlow.ReversePostOrderEnumerator;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
   BasicBlock collects a serial list of operations.  It is designed to
   abstract away specific operation and allow the compiler to focus on
   control flow at a higher level.  (It also allows for some analysis
   within the block to operate more efficiently by taking advantage of
   the fact that the elements of a BasicBlock have a total ordering of
   execution). 
   
   <P> Make sure to look at <code>BasicBlock.Factory</code>, since it
   acts as the central core for creating and keeping track of the
   <code>BasicBlock</code>s for a given <code>HCode</code>.

   <P> <B>NOTE:</B> right now <code>BasicBlock</code> only guarantees
   that it preserves the Maximal Basic Block property (where the first
   element is the entry point and the last element is the exit point)
   if the graph of operations is not modified while the basic block is
   in use.  However, other pieces of code WILL modify the Beginning
   and End of a basic block (for example, the register allocator will
   add LOADs to the beginning and STOREs to the end).  Perhaps we can
   allow for some modification of the Control-Flow-Graph; check with
   group. 
 *
 * @author  John Whaley
 * @author  Felix Klock <pnkfelix@mit.edu> 
 * @version $Id: BasicBlock.java,v 1.1.2.25 2000-03-09 00:49:00 salcianu Exp $
*/
public class BasicBlock {
    
    static final boolean DEBUG = false;

    private HCodeElement first;
    private HCodeElement last;

    // BasicBlocks preceding and succeeding this block
    private Set pred_bb;
    private Set succ_bb;

    // unique id number for this basic block
    private int num;

    // number of statements in this block
    private int size;
    
    // factory that generated this block
    private Factory factory; 

    public HCodeElement getFirst() { return first; }
    public HCodeElement getLast() { return last; }
    
    private void addPredecessor(BasicBlock bb) { pred_bb.add(bb); }
    private void addSuccessor(BasicBlock bb) { succ_bb.add(bb); }
    
    public int prevLength() { return pred_bb.size(); }
    public int nextLength() { return succ_bb.size(); }
    public Enumeration prev() { return new IteratorEnumerator(pred_bb.iterator()); }
    public Enumeration next() { return new IteratorEnumerator(succ_bb.iterator()); }

    /** Returns all the predecessors of <code>this</code> basic block. */
    public BasicBlock[] getPrev() {
	return (BasicBlock[]) pred_bb.toArray(new BasicBlock[pred_bb.size()]);
    }

    /** Returns all the successors of <code>this</code> basic block. */
    public BasicBlock[] getNext() {
	return (BasicBlock[]) succ_bb.toArray(new BasicBlock[succ_bb.size()]);
    }

    /** Returns all the predecessors of <code>this</code>. 
	<BR> <B>effects:</B> returns a <code>Set</code> of
	     <code>BasicBlock</code>s which precede
	     <code>this</code>. 
     */
    public Set prevSet() { 
	return Collections.unmodifiableSet(pred_bb); 
    }
    
    /** Returns all the successors of <code>this</code>. 
	<BR> <B>effects:</B> returns a <code>Set</code> of
	     <code>BasicBlock</code>s which succeed
	     <code>this</code>. 
     */
    public Set nextSet() {
	return Collections.unmodifiableSet(succ_bb);
    }

    /** Returns an unmodifiable <code>List</code> for the
	<code>HCodeElement</code>s within <code>this</code>.  
	<BR> <B>effects:</B> Generates a new <code>List</code> of
	<code>HCodeElement</code>s ordered according to the order
	mandated by the <code>CFGrapher</code> used in the call to
	<code>computeBasicBlocks</code> that generated
	<code>this</code>. 
    */
    public List statements() {
	Util.assert(size > 0, "BasicBlock class breaks on empty bbs");
	return new java.util.AbstractSequentialList() {
	    public int size() { return size; }
	    public ListIterator listIterator(int index) {
		// note that index *can* equal the size of the list,
		// in which case we start the iterator past the last
		// element of the list. 

		// check argument
		if (index < 0 || index > size) {
		    throw new IndexOutOfBoundsException
			(index +"< 0 || "+index+" > "+size);
		}
		
		// iterate to correct starting point
		HCodeElement curr = first;
		int i;

		// slight rep inconsistency in upper bound; we'll keep
		// next pointing at curr, even though that's the
		// prev-elem, not the next one.  See implementation
		// below for details 
		int bound = Math.min(index, size-1);
		for(i=0; i < bound; i++) {
		    curr = factory.grapher.succ(curr)[0].to();
		}
		
		// new final vars to be passed to ListIterator
		final HCodeElement fcurr = curr;
		final int fi = index;

		if (false) System.out.println
			       (" generating listIterator("+index+")"+
				" next: "+fcurr+
				" ind: "+fi);

		return new harpoon.Util.UnmodifiableListIterator() {
		    HCodeElement next = fcurr; //elem for next() to return
		    int ind = fi; //where currently pointing?

		    public boolean hasNext() {
			return ind!=size;
		    }
		    public Object next() {
			if (!hasNext()) {
			    throw new NoSuchElementException();
			}
			ind++;
			Object ret = next;
			Util.assert(ind <= size, 
				    "ind > size:"+ind+", "+size);
			if (ind != size) {
			    HCodeEdge[] succs = factory.grapher.succ(next);
			    Util.assert(succs.length == 1,
					next+" has wrong succs:" + 
					java.util.Arrays.asList(succs)+
					" (ind: "+ind+")");
			    next = succs[0].to(); 

			} else { 
			    // keep 'next' the same, since previous()
			    // needs to be able to return it
			}
			return ret;
		    }
		    
		    
		    public boolean hasPrevious() {
			if (ind == size) {
			    return true;
			} else {
			    return factory.grapher.predC(next).size() == 1;
			}
		    }
		    public Object previous() {
			if (!hasPrevious()) {
			    throw new NoSuchElementException();
			}
			if (ind != size) {
			    next = factory.grapher.pred(next)[0].from();
			}
			ind--;
			return next;
		    } 
		    public int nextIndex() {
			return ind;
		    }
		};
	    }
	};
    }

    /** Accept a visitor. */
    public void accept(BasicBlockVisitor v) { v.visit(this); }
    
    protected BasicBlock(HCodeElement h, Factory f) {
	Util.assert(h!=null);
	first = h; 
	last = null; // note that this MUST be updated by 'f'
	pred_bb = new HashSet(); succ_bb = new HashSet();
	size = 1;
	this.factory = f;
	num = factory.BBnum++;
    }

    private static void addEdge(BasicBlock from, BasicBlock to) {
	from.addSuccessor(to);
	to.addPredecessor(from);
    }
    
    public String toString() {
	return "BB"+num;
    }

    public String dumpElems() {
	StringBuffer s = new StringBuffer();
	Iterator iter = statements().listIterator();
	while(iter.hasNext()) {	    
	    s.append(iter.next() + "\n");
	}
	return s.toString();
    }
    
    /** Factory structure for generating BasicBlock views of
	<code>HCode</code>s.  	
    */
    public static class Factory { 
	// the underlying HCode
	private final HCode hcode;

	private final Map hceToBB;

	private final CFGrapher grapher;
	private final BasicBlock root;
	private final Set leaves;
	private final Set blocks;


	// tracks the current id number to assign to the next
	// generated basic block
	private int BBnum = 0;

	/** Returns the root <code>BasicBlock</code>.
	    <BR> <B>effects:</B> returns the <code>BasicBlock</code>
	         that is at the start of the set of
		 <code>HCodeElement</code>s being analyzed.
	*/
	public BasicBlock getRoot() {
	    return root;
	}

	/** Returns the leaf <code>BasicBlock</code>s.
	    <BR> <B>effects:</B> returns a <code>Set</code> of
	         <code>BasicBlock</code>s that are at the ends of the
		 <code>HCodeElement</code>s being analyzed.
	*/
	public Set getLeaves() {
	    return leaves;
	}

	/** Returns the <code>HCode</code> that <code>this</code> factory
	    produces basic blocks of. */
	public HCode getHCode(){
	    return hcode;
	}

	/** Returns the <code>BasicBlock</code>s constructed by
	    <code>this</code>.
	*/
	public Set blockSet() {
	    return blocks;
	}
	
	/** Generates an <code>Iterator</code> that traverses over all
	    of the blocks generated by this <code>BasicBlock.Factory</code>.
	*/
	public Iterator blocksIterator() {
	    return blockSet().iterator();
	}

	/** Returns the <code>BasicBlock</code> containing
	    <code>hce</code>. 
	*/
	public BasicBlock getBlock(HCodeElement hce) {
	    return (BasicBlock) hceToBB.get(hce);
	}

	/** Constructs a <code>BasicBlock.Factory</code> and generates
	    <code>BasicBlock</code>s for a given <code>HCode</code>.
	    <BR> <B>requires:</B> 
	         <code>grapher.getFirstElement(hcode)</code>
	         is an appropriate entry point for a 
		 basic block.
	    <BR> <B>effects:</B>  Creates a set of
	         <code>BasicBlock</code>s corresponding to the blocks
		 implicitly contained in
		 <code>grapher.getFirstElement(hcode)</code> and the
		 <code>HCodeElement</code> objects that this
		 points to, and returns the
		 <code>BasicBlock</code> that
		 <code>grapher.getFirstElement(hcode)</code> is an
		 instruction in.  The <code>BasicBlock</code> returned
		 is considered to be the root (entry-point) of the set
		 of <code>BasicBlock</code>s created.   
	*/
	public Factory(HCode hcode, final CFGrapher grapher) {
	    // maps HCodeElement 'e' -> BasicBlock 'b' starting with 'e'
	    HashMap h = new HashMap(); 
	    // stores BasicBlocks to be processed
	    Worklist w = new WorkSet();

	    HCodeElement head = grapher.getFirstElement(hcode);
	    this.grapher = grapher;
	    this.hcode   = hcode;

	    // modifable util classes for construction use only
	    HashSet myLeaves = new HashSet();
	    HashMap myHceToBB = new HashMap();

	    BasicBlock first = new BasicBlock(head, this);
	    h.put(head, first);
	    myHceToBB.put(head, first);
	    w.push(first);
	    
	    root = first;



	    while(!w.isEmpty()) {
		BasicBlock current = (BasicBlock) w.pull();
		
		// 'last' is our guess on which elem will be the last;
		// thus we start with the most conservative guess
		HCodeElement last = current.getFirst();
		boolean foundEnd = false;
		while(!foundEnd) {
		    int n = grapher.succC(last).size();
		    if (n == 0) {
			if(DEBUG) System.out.println("found end:   "+last);
			
			foundEnd = true;
			myLeaves.add(current); 
			
		    } else if (n > 1) { // control flow split
			if(DEBUG) System.out.println("found split: "+last);
			
			for (int i=0; i<n; i++) {
			    HCodeElement e_n = grapher.succ(last)[i].to();
			    BasicBlock bb = (BasicBlock) h.get(e_n);
			    if (bb == null) {
				h.put(e_n, bb=new BasicBlock(e_n, this));
				myHceToBB.put(e_n, bb);
				w.push(bb);
			    }
			    BasicBlock.addEdge(current, bb);
			}
			foundEnd = true;
			
		    } else { // one successor
			Util.assert(n == 1, "must have one successor");
			HCodeElement next = grapher.succ(last)[0].to();
			int m = grapher.predC(next).size();
			if (m > 1) { // control flow join
			    if(DEBUG) System.out.println("found join:  "+next);
			    
			    BasicBlock bb = (BasicBlock) h.get(next);
			    if (bb == null) {
				bb = new BasicBlock(next, this);
				h.put(next, bb);
				myHceToBB.put(next, bb);
				w.push(bb);
			    }
			    BasicBlock.addEdge(current, bb);
			    foundEnd = true;
			    
			} else { // no join; update our guess
			    if(DEBUG) System.out.println("found line:  "+
							 last+", "+ next);
			    
			    current.size++;
			    myHceToBB.put(next, current);
			    last = next;
			}
		    }
		}

		current.last = last;

		final HCodeElement flast = last;
		final BasicBlock fcurr = current;
		Util.assert( grapher.succC(last).size() != 1 ||
			     grapher.predC(grapher.succ(last)[0].
				      to()).size() > 1,
			     new Object() { 
				 public String toString() {
				     return 
				     "last elem: "+flast+" of "+ 
				     fcurr+" breaks succC "+
				     "invariant: "+grapher.succC(flast)+
				     " BB: " + fcurr.dumpElems();
				 }
			     });

	    }

	    // efficiency hack; make various immutable Collections
	    // unmodifable at construction time rather than at
	    // accessor time.
	    leaves = Collections.unmodifiableSet(myLeaves);
	    hceToBB = Collections.unmodifiableMap(myHceToBB);
	    blocks = 
		Collections.unmodifiableSet(new HashSet(hceToBB.values()));
	
	    
	}

	public static void dumpCFG(BasicBlock start) {
	    Enumeration e = new ReversePostOrderEnumerator(start);
	    while (e.hasMoreElements()) {
		BasicBlock bb = (BasicBlock)e.nextElement();
		System.out.println("Basic block "+bb + " size:"+ bb.size);
		System.out.println("BasicBlock in : "+bb.pred_bb);
		System.out.println("BasicBlock out: "+bb.succ_bb);
		System.out.println();
	    }
	}
    }
}
