// BasicBlock.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Collections.LinearSet;
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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Collection;

/**
   BasicBlock collects a sequence of operations.  It is designed to
   abstract away specific operation and allow the compiler to focus on
   control flow at a higher level.  (It also allows for some analysis
   within the block to operate more efficiently by taking advantage of
   the fact that the elements of a BasicBlock have a total ordering of
   execution). 

   <P> Most BasicBlocks are a part of a larger piece of code, and thus
   a collection of BasicBlocks form a Control Flow Graph (where the
   nodes of the graph are the blocks and the directed edges of the
   graph indicate when one block is succeeded by another block).
   
   <P> Make sure to look at <code>BasicBlock.Factory</code>, since it
   acts as the central core for creating and keeping track of the
   <code>BasicBlock</code>s for a given <code>HCode</code>.

   <P> <B>NOTE:</B> right now <code>BasicBlock</code> only guarantees
   that it preserves the <i>Maximal Basic Block</i> property (where each
   block is the longest sequence of instructions such that only the
   first instruction may have more than one entry and only the last
   instruction may have more than one exit) if the graph of operations
   is not modified while the basic block is in use.  For that matter,
   some methods of BasicBlock may implicitly rely on the intermediate
   representation not changing while the blocks are in use.  However,
   most <b>but not all</b> Intermediate Representations in the Flex
   Compiler are immutable.  Therefore compilation passes modifying the
   intermediate representation must reconstruct the BasicBlocks for
   that intermediate representation if they wish to perform a second
   analysis pass.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: BasicBlock.java,v 1.3.2.3 2002-04-07 20:22:47 cananian Exp $ */
public class BasicBlock<HCE extends HCodeElement>
    implements BasicBlockInterf<HCE,BasicBlock<HCE>>, java.io.Serializable {
    
    static final boolean DEBUG = false;
    static final boolean TIME = false;
    
    static boolean CHECK_INSTRS = false;

    private HCE first;
    private HCE last;

    // BasicBlocks preceding and succeeding this block (we store the
    // CFG implicityly in the basic block objects; if necessary this
    // information can be migrated to BasicBlock.Factory and
    // maintained there)
    private Set<BasicBlock<HCE>> pred_bb;
    private Set<BasicBlock<HCE>> succ_bb;

    // unique id number for this basic block; used only for BasicBlock.toString()
    private int num;

    // number of statements in this block
    private int size;
    
    // factory that generated this block
    private Factory<HCE> factory; 

    /** Returns the first <code>HCodeElement</code> in the sequence. 
	@deprecated Use the standard List view provided by statements() instead
	@see BasicBlock#statements
     */
    public HCE getFirst() { return first; }

    /** Returns the last <code>HCodeElement</code> in the sequence. 
	@deprecated Use the standard List view provided by statements() instead
	@see BasicBlock#statements
     */
    public HCE getLast() { return last; }
    
    /** Adds <code>bb</code> to the set of predecessor basic blocks
	for <code>this</code>.  Meant to be used during construction.
	FSK: probably should take this out; it adds little to the
	class 
    */
    private void addPredecessor(BasicBlock<HCE> bb) { pred_bb.add(bb); }

    /** Adds <code>bb</code> to the set of successor basic blocks for
	<code>this</code>.  Meant to be used during construction.
	FSK: probably should take this out; it adds little to the
	class 
    */
    private void addSuccessor(BasicBlock<HCE> bb) { succ_bb.add(bb); }
    
    /** Returns the number of basic blocks in the predecessor set for
	<code>this</code>. 
	@deprecated Use prevSet() instead
	@see BasicBlock#prevSet
    */
    public int prevLength() { return pred_bb.size(); }

    /** Returns the number of basic blocks in the successor set for
	<code>this</code>. 
	@deprecated Use nextSet() instead
	@see BasicBlock#nextSet
    */
    public int nextLength() { return succ_bb.size(); }

    /** Returns an Enumeration that iterates over the predecessors for
	<code>this</code>. 
	@deprecated Use prevSet() instead
	@see BasicBlock#prevSet
    */
    public Enumeration<BasicBlock<HCE>> prev() { return new IteratorEnumerator<BasicBlock<HCE>>(pred_bb.iterator()); }

    /** Returns an Enumeration that iterates over the successors for
	<code>this</code>. 
	@deprecated Use nextSet() instead
	@see BasicBlock#prevSet
    */
    public Enumeration<BasicBlock<HCE>> next() { return new IteratorEnumerator<BasicBlock<HCE>>(succ_bb.iterator()); }

    /** Returns all the predecessors of <code>this</code> basic
	block. 
	@deprecated Use prevSet() instead
	@see BasicBlock#prevSet
    */
    public BasicBlock<HCE>[] getPrev() {
	return pred_bb.toArray(new BasicBlock<HCE>[pred_bb.size()]);
    }

    /** Returns all the successors of <code>this</code> basic block. 
	@deprecated Use nextSet() instead
	@see BasicBlock#nextSet
    */
    public BasicBlock<HCE>[] getNext() {
	return succ_bb.toArray(new BasicBlock<HCE>[succ_bb.size()]);
    }

    /** Returns all the predecessors of <code>this</code>. 
	<BR> <B>effects:</B> returns a <code>Set</code> of
	     <code>BasicBlock</code>s which precede
	     <code>this</code>. 
     */
    public Set<BasicBlock<HCE>> prevSet() { 
	return Collections.unmodifiableSet(pred_bb); 
    }
    
    /** Returns all the successors of <code>this</code>. 
	<BR> <B>effects:</B> returns a <code>Set</code> of
	     <code>BasicBlock</code>s which succeed
	     <code>this</code>. 
     */
    public Set<BasicBlock<HCE>> nextSet() {
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
    public List<HCE> statements() {

	// FSK: this is dumb; why not just return an empty list in
	// this case?  I suspect this is an attempt to fail-fast, but
	// still... 
	assert size > 0 : "BasicBlock class breaks on empty BBs";

	return new java.util.AbstractSequentialList<HCE>() {
	    public int size() { return size; }
	    public ListIterator<HCE> listIterator(int index) {
		// note that index *can* equal the size of the list,
		// in which case we start the iterator past the last
		// element of the list. 

		// check argument
		if (index < 0) {
		    throw new IndexOutOfBoundsException(index +"< 0"); 
		} else if (index > size) {
		    throw new IndexOutOfBoundsException(index+" > "+size); 
		}
		
		// iterate to correct starting point
		HCE curr;

		// FSK: put better code in here for choosing starting pt
		if (index < size) {
		    curr = first;
		    int bound = Math.min(index, size-1);
		    for(int i=0; i < bound; i++) {
			curr = factory.grapher.succ(curr)[0].to();
		    }
		} else {
		    curr = last;
		}

		// new final vars to be passed to ListIterator
		final HCE fcurr = curr;
		final int fi = index;

		if (false) System.out.println
			       (" generating listIterator("+index+")"+
				" next: "+fcurr+
				" ind: "+fi);

		return new harpoon.Util.UnmodifiableListIterator<HCE>() {
		    //elem for next() to return
		    HCE next = fcurr; 
		    
		    // where currently pointing?  
		    // Invariant: 0 <= ind /\ ind <= size 
		    int ind = fi; 

		    // checks rep of `this' (for debugging)
		    private void repOK() {
			repOK("");
		    }

		    // checks rep of `this' (for debugging)
		    private void repOK(String s) {
			assert 0 <= ind : (s+" (0 <= ind), ind:"+ind);
			assert ind <= size : (s+" (ind <= size), ind:"+ind+", size:"+size);
			assert ((ind==0)?next==first:true) : (s+" (ind==0 => next==first), next:"+next+", first:"+first);

			assert ((ind==(size-1))?next==last:true) : (s+" (ind==(size-1) => next==last), next:"+next+", last:"+last);

			assert ((ind==size)?next==last:true) : (s+" (ind==size => next==last), next:"+next+", last:"+last);
		    }

		    public boolean hasNext() {
			if (DEBUG) repOK();
			return ind != size;
		    }
		    public HCE next() {
			if (DEBUG) repOK("beginning");			
			if (ind == size) {
			    throw new NoSuchElementException();
			}
			ind++;
			HCE ret = next;
			if (ind != size) {
			    Collection<HCodeEdge<HCE>> succs = factory.grapher.succC(next);
			    assert succs.size() == 1 : (true)?" wrong succs:":
					next+" has wrong succs:" + succs
					+" (ind:"+ind+", size:"+size+")";
			    next = succs.iterator().next().to(); 

			} else { 
			    // keep 'next' the same, since previous()
			    // needs to be able to return it
			}

			if (DEBUG) repOK("end");
			return ret;
		    }
		    
		    
		    public boolean hasPrevious() {
			if (DEBUG) repOK();
			return ind > 0;

		    }
		    public HCE previous() {
			if (DEBUG) repOK();

			if (ind <= 0) {
			    throw new NoSuchElementException();
			}

			// special case: if ind == size, then we just
			// return <next>
			if (ind != size) {
			    next = factory.grapher.pred(next)[0].from();
			}
			ind--;

			if (DEBUG) repOK();
			return next;
		    } 
		    public int nextIndex() {
			if (DEBUG) repOK();
			return ind;
		    }
		};
	    }
	};
    }

    /** Accept a visitor. 
	FSK: is this really useful?  John put this in with the thought
	that we'd have many different types of BasicBlocks, but I'm
	not sure about that actually being a useful set of subtypes
     */
    public void accept(BasicBlockInterfVisitor v) { v.visit(this); }
    
    /** Constructs a new BasicBlock with <code>h</code> as its first
	element.  Meant to be used only during construction.
    */
    protected BasicBlock(HCE h, Factory f) {
	assert h!=null;
	first = h; 
	last = null; // note that this MUST be updated by 'f'
	pred_bb = new HashSet<BasicBlock<HCE>>();
	succ_bb = new HashSet<BasicBlock<HCE>>();
	size = 1;
	this.factory = f;
	num = factory.BBnum++;
    }

    /** Constructs an edge from <code>from</code> to
	</code>to</code>. 
    */
    private static <HCE extends HCodeElement> void addEdge(BasicBlock<HCE> from, BasicBlock<HCE> to) {
	from.addSuccessor(to);
	to.addPredecessor(from);
    }
    
    public String toString() {
	return "BB"+num+"{"+first+"}";
    }

    /** Returns a String composed of the statements comprising
	<code>this</code>. 
    */
    public String dumpElems() {
	List stms = statements();
	StringBuffer s = new StringBuffer(stms.size()*16);
	Iterator iter = stms.listIterator();
	while(iter.hasNext()) {	    
	    s.append(iter.next() + "\n");
	}
	return s.toString();
    }
    
    /** Factory structure for generating BasicBlock views of
	an <code>HCode</code>.  	
    */
    public static class Factory<HCE extends HCodeElement> 
	implements BasicBlockFactoryInterf<HCE,BasicBlock<HCE>>,
		   java.io.Serializable { 

	// the underlying HCode
	private final HCode<HCE> hcode;

	private final Map<HCE,BasicBlock<HCE>> hceToBB;

	private final CFGrapher<HCE> grapher;
	private final BasicBlock<HCE> root;
	private final Set<BasicBlock<HCE>> leaves;
	private final Set<BasicBlock<HCE>> blocks;


	// tracks the current id number to assign to the next
	// generated basic block
	private int BBnum = 0;

	/** Returns the root <code>BasicBlock</code>.
	    <BR> <B>effects:</B> returns the <code>BasicBlock</code>
	         that is at the start of the set of
		 <code>HCodeElement</code>s being analyzed.
	*/
	public BasicBlock<HCE> getRoot() {
	    return root;
	}

	/** Does the same thing as <code>getRoot</code>.
	    Work around Java's weak typing system. */
	public BasicBlock<HCE> getRootBBInterf() {
	    return getRoot();
	}

	/** Returns the leaf <code>BasicBlock</code>s.
	    <BR> <B>effects:</B> returns a <code>Set</code> of
	         <code>BasicBlock</code>s that are at the ends of the
		 <code>HCodeElement</code>s being analyzed.
	*/
	public Set<BasicBlock<HCE>> getLeaves() {
	    return leaves;
	}

	/** Does the same thing as <code>getLeaves</code>.
	    Work around Java's weak typing system. */
	public Set<BasicBlock<HCE>> getLeavesBBInterf() {
	    return getLeaves();
	}

	/** Returns the <code>HCode</code> that <code>this</code> factory
	    produces basic blocks of. */
	public HCode<HCE> getHCode(){
	    return hcode;
	}

	/** Returns the <code>BasicBlock</code>s constructed by
	    <code>this</code>.
	*/
	public Set<BasicBlock<HCE>> blockSet() {
	    return blocks;
	}
	
	/** Generates an <code>Iterator</code> that traverses over all
	    of the blocks generated by this <code>BasicBlock.Factory</code>.
	*/
	public Iterator<BasicBlock<HCE>> blocksIterator() {
	    return postorderBlocksIter();
	}

	/** Generates an <code>Iterator</code> that traverses over all
	    of the blocks generated by this
	    <code>BasicBlock.Factory</code> in Preorder (root first,
	    then subtrees).
	*/
	public Iterator<BasicBlock<HCE>> preorderBlocksIter() {
	    LinkedList<Iterator<BasicBlock<HCE>>> iters =
		new LinkedList<Iterator<BasicBlock<HCE>>>();
	    LinkedList<BasicBlock<HCE>> bbs=new LinkedList<BasicBlock<HCE>>();
	    LinkedList<BasicBlock<HCE>> order=new LinkedList<BasicBlock<HCE>>();
	    Set<BasicBlock<HCE>> done = new HashSet<BasicBlock<HCE>>();
	    BasicBlock<HCE> start = getRoot();
	    done.add(start);
	    bbs.addLast(start);
	    iters.addLast(start.nextSet().iterator());
	    while(!bbs.isEmpty()) {
		assert bbs.size() == iters.size();
		for (Iterator<BasicBlock<HCE>> i = iters.removeLast();
		     i.hasNext(); ) {
		    BasicBlock<HCE> bb2 = i.next();
		    if (!done.contains(bb2)) {
			done.add(bb2);
			bbs.addFirst(bb2);
			iters.addLast(i);
			i = bb2.nextSet().iterator();
		    }
		}
		
		// reverses order
		order.addLast(bbs.removeLast());
	    }

	    return order.iterator();
	}
	
	/** Generates an <code>Iterator</code> that traverses over all
	    of the blocks generated by this
	    <code>BasicBlock.Factory</code> in Postorder (subtrees
	    first, then root).
	*/
	public Iterator<BasicBlock<HCE>> postorderBlocksIter() {
	    LinkedList<Iterator<BasicBlock<HCE>>> iters =
		new LinkedList<Iterator<BasicBlock<HCE>>>();
	    LinkedList<BasicBlock<HCE>> bbs=new LinkedList<BasicBlock<HCE>>();
	    LinkedList<BasicBlock<HCE>> order=new LinkedList<BasicBlock<HCE>>();
	    Set<BasicBlock<HCE>> done = new HashSet<BasicBlock<HCE>>();
	    BasicBlock<HCE> start = getRoot();
	    done.add(start);
	    bbs.addLast(start);
	    iters.addLast(start.nextSet().iterator());
	    while(!bbs.isEmpty()) {
		assert bbs.size() == iters.size();
		for (Iterator<BasicBlock<HCE>> i = iters.removeLast();
		     i.hasNext(); ) {
		    BasicBlock<HCE> bb2 = i.next();
		    if (!done.contains(bb2)) {
			done.add(bb2);
			bbs.addLast(bb2);
			iters.addLast(i);
			i = bb2.nextSet().iterator();
		    }
		}

		// reverses order
		order.addLast(bbs.removeLast());
	    }

	    return order.iterator();
	}
	
	/** Returns the <code>BasicBlock</code> containing
	    <code>hce</code>. 
	    <BR> <B>requires:</B> hce is present in the code for
	         <code>this</code>. 
            <BR> <B>effects:</B> returns the BasicBlock that contains
	         <code>hce</code>, or <code>null</code> if
		 <code>hce</code> is unreachable.
	*/
	public BasicBlock<HCE> getBlock(HCE hce) {
	    return hceToBB.get(hce);
	}

	/** Does the same thing as <code>getBlock</code>.
	    Work around Java's weak typing system. */
	public BasicBlock<HCE> getBBInterf(HCE hce) {
	    return getBlock(hce);
	}
	
	/** Constructs a <code>BasicBlock.Factory</code> using the
	    implicit control flow provided by <code>code</code>.
	    <BR> <B>requires:</B> elements of <code>code</code>
	         implement <code>CFGraphable</code>.
	    <BR> <B>effects:</B> constructs a
	         <code>BasicBlock.Factory</code> using
		 <code>this(code, CFGrapher.DEFAULT);</code> 
	*/
	public Factory(HCode<HCE> code) {
	    this(code, CFGrapher.DEFAULT);
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
	public Factory(HCode<HCE> hcode, final CFGrapher<HCE> grapher) {
	    if (TIME) System.out.print("bldBB");

	    // maps HCodeElement 'e' -> BasicBlock 'b' starting with 'e'
	    Map<HCE,BasicBlock<HCE>> h = new HashMap<HCE,BasicBlock<HCE>>(); 
	    // stores BasicBlocks to be processed
	    Worklist<BasicBlock<HCE>> w = new WorkSet<BasicBlock<HCE>>();

	    HCE head = grapher.getFirstElement(hcode);
	    this.grapher = grapher;
	    this.hcode   = hcode;

	    // modifable util classes for construction use only
	    Set<BasicBlock<HCE>> myLeaves = new HashSet<BasicBlock<HCE>>();
	    Map<HCE,BasicBlock<HCE>> myHceToBB =
		new HashMap<HCE,BasicBlock<HCE>>();

	    BasicBlock<HCE> first = new BasicBlock<HCE>(head, this);
	    h.put(head, first);
	    myHceToBB.put(head, first);
	    w.push(first);
	    
	    root = first;

	    while(!w.isEmpty()) {
		BasicBlock<HCE> current = w.pull();
		
		// 'last' is our guess on which elem will be the last;
		// thus we start with the most conservative guess
		HCE last = current.getFirst();
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
			    HCE e_n = grapher.succ(last)[i].to();
			    BasicBlock<HCE> bb = h.get(e_n);
			    if (bb == null) {
				h.put(e_n, bb=new BasicBlock<HCE>(e_n, this));
				myHceToBB.put(e_n, bb);
				w.push(bb);
			    }
			    BasicBlock.addEdge(current, bb);
			}
			foundEnd = true;
			
		    } else { // one successor
			assert n == 1 : "must have one successor";
			HCE next = grapher.succ(last)[0].to();
			int m = grapher.predC(next).size();
			if (m > 1) { // control flow join
			    if(DEBUG) System.out.println("found join:  "+next);
			    
			    BasicBlock<HCE> bb = h.get(next);
			    if (bb == null) {
				bb = new BasicBlock<HCE>(next, this);
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

		final HCE flast = last;
		final BasicBlock<HCE> fcurr = current;
		assert grapher.succC(last).size() != 1 ||
			     grapher.predC(grapher.succ(last)[0].
				      to()).size() > 1 : "succC invariant broken";

	    }

	    // efficiency hacks: make various immutable Collections
	    // array-backed sets, and make them unmodifiable at
	    // construction time rather than at accessor time.
	    leaves = Collections.unmodifiableSet(new LinearSet<BasicBlock<HCE>>(myLeaves));
	    hceToBB = Collections.unmodifiableMap(myHceToBB);
	    blocks = Collections.unmodifiableSet(new LinearSet<BasicBlock<HCE>>
						 (new HashSet<BasicBlock<HCE>>(hceToBB.values())));
	    Iterator<BasicBlock<HCE>> bbIter = blocks.iterator();
	    while (bbIter.hasNext()) {
		BasicBlock<HCE> bb = bbIter.next();
		bb.pred_bb = new LinearSet<BasicBlock<HCE>>(bb.pred_bb);
		bb.succ_bb = new LinearSet<BasicBlock<HCE>>(bb.succ_bb);

		// FSK: debug checkBlock(bb);
	    }

	    if (CHECK_INSTRS) {
		// check that all instrs map to SOME block
		Iterator<HCE> hceIter = hcode.getElementsI();
		while(hceIter.hasNext()) {
		    HCE hce = hceIter.next();
		    System.out.println("BB Check: "+hce);
		    if (!(hce instanceof harpoon.IR.Assem.InstrLABEL) &&
			!(hce instanceof harpoon.IR.Assem.InstrDIRECTIVE)&&
			!(hce instanceof harpoon.IR.Assem.InstrJUMP) &&
			
		        (getBlock(hce) == null)) {
			
			Set<HCE> s = new HashSet<HCE>();
			List<HCodeEdge<HCE>> t = new ArrayList<HCodeEdge<HCE>>();
			t.addAll(grapher.predC(hce));
			for(int i=0; i<t.size(); i++) {
			    HCE p = t.get(i).from();
			    if(!s.contains(p)) {
				// System.out.println("visiting "+p);
				s.add(p);
				t.addAll(grapher.predC(p));
				if (grapher.predC(p).size() == 0) {
				    System.out.println
					("no preds for "+p);
				}
				if (getBlock(p) != null) {
				    System.out.println
					("there IS a BB for pred:"+p);
				}
			    }
			}
			System.out.println("no BB for "+hce+"\n");
		    }
		}
	    }
		

	    if (TIME) System.out.print("#");	    
	}

	private void checkBlock(BasicBlock<HCE> block) {
	    List<HCE> blockL = block.statements();
	    int sz = blockL.size();
	    Iterator<HCE> iter = blockL.iterator();
	    HCE curr = null;
	    while(iter.hasNext()) {
		HCE h = iter.next();
		if (curr == null) {
		    assert h == block.first;

		    curr = h;
		} else {
		    assert grapher.succC(curr).size() == 1;
		    assert grapher.succ(curr)[0].to() == h;

		    curr = h;
		}
		sz--;
	    }
	    assert curr == block.last;
	    assert sz == 0;
	}
	public void dumpCFG() { dumpCFG(root); }
	
	public String toString() { return "BasicBlock.Factory : \n"+getCFG(root); }

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

	public static String getCFG(BasicBlock start) {
	    Enumeration e = new ReversePostOrderEnumerator(start);
	    StringBuffer sb = new StringBuffer();
	    while (e.hasMoreElements()) {
		BasicBlock bb = (BasicBlock)e.nextElement();
		sb.append("Basic block "+bb + " size:"+ bb.size);
		sb.append("\n");
		sb.append("BasicBlock in : "+bb.pred_bb);
		sb.append("\n");
		sb.append("BasicBlock out: "+bb.succ_bb);
		sb.append("\n");
	    }
	    return sb.toString();
	}
	
    }
}
