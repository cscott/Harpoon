// FCFGBasicBlock.java, created Fri Dec 14 11:03:15 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ListIterator;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;

import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.UnmodifiableListIterator;

import harpoon.Util.Util;

/**
 * <code>FCFGBasicBlock</code> is a basic block structure for the Factored
 Control Flow Graph (FCFG) representation. This representation is based on
 a factored and <i>implicit</i> representation of exceptions. It is described
 in detail in the paper
 <a href="http://www.research.ibm.com/jalapeno/pub/paste99.ps">Efficient and Precise Modeling of Exceptions for the Analysis of Java Programs</a>
 by Choi, Grove, Hind and Sarkar.

 <p>
 Each <code>FCFGBasicBlock</code> corresponds to a list of
 consecutive instructions, such that:
 <ul>
 <li>the normal (that is, in the absence of exceptions) flow of execution
 through them is a straight line, and
 <li>all the instructions are protected by the same set of handlers.
 </ul>

 Therefore, a <code>FCFGBasicBlock</code> has a single entry point, a
 single <i>normal</i> exit point and possibly many <i>lateral</i>
 exits due to exceptions. As all the instructions a
 <code>FCFGBasicBlock</code> are protected by the same set of handlers
 (in fact a list because the order the handlers are processed in is
 important), these lateral exits can be kept once for the entire basic
 block, hence the name <i>factored</i>.

 <p>
 Similar to <codeBasicBlock</code>s, the only way of producing
 <code>FCFGBasicBlock</code>s is by using
 <code>FCFGBasicBlock.Factory</code>. 

 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: FCFGBasicBlock.java,v 1.3.2.1 2002-02-27 08:30:23 cananian Exp $ */
public class FCFGBasicBlock implements BasicBlockInterf {
    
    /** Creates a <code>FCFGBasicBlock</code>. This method is called
     only by the <code>FCFGBasicBlock.Factory</code>. */
    protected FCFGBasicBlock(Quad q, Factory f) {
	id = f.BBnum++;
	first = q;
	size = 1;
	next_bb = new HashSet();
	prev_bb = new HashSet();
	protected_bb = new HashSet();
	handler_bb  = new LinkedList();
    }

    protected int id;

    /** Next BBs (according to the normal control flow). */
    protected Set next_bb;
    /** Previous BBs (according to the normal control flow). */
    protected Set prev_bb;

    /** Predecessor BBs in the exceptional control flow. */
    protected Set protected_bb;
    /** Successor BBs in the exceptional control flow. */
    protected List handler_bb;

    protected Quad first;
    protected Quad last;

    // number of instructions in this basic block
    int size;

    /** Returns the first instruction of <code>this</code> basic block. */
    public HCodeElement getFirst() {
	return first;
    }

    /** Returns the last instruction of <code>this</code> basic block. */
    public HCodeElement getLast() {
	return last;
    }
    
    /** Returns the set of predecessor BBs in the normal control
	flow. */
    public Set normalNextSet() {
	return next_bb;
    }

    /** Returns the set of successor BBs in the normal control
        flow. */
    public Set normalPrevSet() {
	return prev_bb;
    }


    // add the normal control flow edge <from,to>
    protected static void addNormalEdge
	(FCFGBasicBlock from, FCFGBasicBlock to) {
	from.next_bb.add(to);
	to.prev_bb.add(from);
    }

    /** Returns the set of predecessor BBs in the exceptional control
	flow. For a basic block that starts with a
	<code>HANDLER</code> instruction, this is the set of basic
	blocks whose instructions are protected by that
	handler. <code>FOOTER</code> is the default handler: the
	uncaught exceptions are sent to the caller. For any other
	basic block, this set is empty. */
    public Set protectedSet() {
	return protected_bb;
    }

    /** Returns the list of successor BBs in the exceptional control
	flow. These are the handlers that protect the instructions
	from this basic block. <code>FOOTER</code> is the default
	handler: the uncaught exceptions are sent to the caller.
	The order of the handlers in the list is the order in which the
	JVM tries to find an appropriate handler. */
    public List handlerList() {
	return handler_bb;
    }

    /** Returns <i>all</i> the predecessors of the basic block,
        according to the normal and the exceptional control flow. */
    public Set prevSet() {
	Set result = new HashSet();
	result.addAll(normalPrevSet());
	result.addAll(protectedSet());
	return result;
    }

    /** Returns <i>all</i> the successors of the basic block,
        according to the normal and the exceptional control flow. */
    public Set nextSet() {
	Set result = new HashSet();
	result.addAll(normalNextSet());
	result.addAll(handlerList());
	return result;
    }

    // record the fact that the instructions from the basic block
    // <code>from</code> are controlled by the handler <code>to</code>.
    protected static void addExcpEdge(FCFGBasicBlock from, FCFGBasicBlock to) {
	from.handler_bb.add(to);
	to.protected_bb.add(from);
    }


    private class InstructionListIterator extends UnmodifiableListIterator {
	
	InstructionListIterator(Quad first_quad, int first_index) {
	    next  = first_quad;
	    index = first_index;
	}
	
	// element for next() to return
	Quad next;
	
	// where currently pointing?  
	// Invariant: 0 <= index /\ index <= size 
	int index; 
	
	public boolean hasNext() {
	    return index != size;
	}
	
	public Object next() {
	    if (index == size) {
		throw new NoSuchElementException();
	    }

	    index++;
	    Object ret = next;

	    if (index != size) {
		if(next instanceof HEADER)
		    next = next.next(1);
		else
		    next = next.next(0);
	    } else { 
		// keep 'next' the same, since previous()
		// needs to be able to return it
	    }

	    return ret;
	}
	
	
	public boolean hasPrevious() {
	    return index > 0;
	}
	
	public Object previous() {
	    if (index <= 0) {
		throw new NoSuchElementException();
	    }
	    
	    // special case: if index == size, then we just
	    // return <next>
	    if (index != size) {
		next = next.prev(0);
	    }
	    index--;
	    
	    return next;
	}

	public int nextIndex() {
	    return index;
	}
    };


    public List statements() {
	return new java.util.AbstractSequentialList() {
		public int size() { return size; }
		public ListIterator listIterator(int index) {
		    // check argument
		    if (index < 0) {
			throw new IndexOutOfBoundsException(index + "< 0"); 
		    } else if (index > size) {
			throw new
			    IndexOutOfBoundsException(index + " > " + size); 
		    }
		    
		    Quad first_quad = first;
		    int bound = (index == size) ? index - 1 : index ;
		    for(int i = 0; i < bound; i++)
			first_quad = first_quad.next(0);
		    
		    return new InstructionListIterator(first_quad, index);
		}
	    };
    }


    public void accept(BasicBlockInterfVisitor v) {
	v.visit(this);
    }

    /** Returns a string identifying <code>this</code>
	<code>FCFGBasicBlock</code>. All the
	<code>FCFGBasicBlock</code>s generated for a given method have
	different string representations. */
    public String toString() {
	return "FCFG" + id;
    }

    /** Factory structure for generating <code>FCFGBasicBlock</code>
	views of an <code>HCode</code>.  */
    public static class Factory implements BasicBlockFactoryInterf {

	protected HCode hcode;
	protected Set blocks;
	protected Set leaves;
	protected FCFGBasicBlock root;
	protected Map quadToBB;

	int BBnum = 0;
	
	/** Produces a <code>FCFGBasicBlock</code> view of
	    <code>hcode</code> <code>hcode</code> must be in
	    &quot;quad-with-try&quot; format. */
	public Factory(HCode hcode) {
	    assert hcode.getName().equals("quad-with-try") : "FCFG works only for quad-with-try";

	    this.hcode = hcode;

	    quadToBB = new HashMap();
	    leaves = new HashSet();
	    blocks = new HashSet();

	    METHOD method = Util.getMETHOD(hcode);
	    Set[] controlled = get_controlled(method);

	    Worklist w = new WorkSet();
	    HEADER header = (HEADER) hcode.getRootElement();
	    root = new FCFGBasicBlock(header, this);
	    blocks.add(root);
	    quadToBB.put(header, root);
	    w.push(root);

	    while(!w.isEmpty()) {
		FCFGBasicBlock current = (FCFGBasicBlock) w.pull();
		
		// 'last' is our guess on which elem will be the last;
		// thus we start with the most conservative guess
		Quad last = (Quad) current.getFirst();
		boolean foundEnd = false;

		while(!foundEnd) {
		    Quad succs[] = last.next();

		    // for METHOD, eliminate the bogus edges toward HANDLERs 
		    if(last == method)
			succs = new Quad[]{succs[0]};
		    // for HEADER eliminate the bogus edge toward FOOTER
		    else if(last == header)
			succs = new Quad[]{succs[1]};

		    if((succs.length > 1) ||  // split point 
		       (succs.length == 0)) { // end of method code
			end_basic_block(current, last, succs, w);
			foundEnd = true;
			if(succs.length == 0)
			    leaves.add(current);
		    } else { // one successor
			Quad next = succs[0];
			if((next.prev().length > 1) || // join point 
			   different_handlers(last, next, controlled)) {
			    end_basic_block(current, last, succs, w);
			    foundEnd = true;
			} else {
			    quadToBB.put(next, current);
			    last = next;
			    current.size++;
			}
		    }
		}

		current.last = last;
	    }
	}

	private Set[] get_controlled(METHOD method) {
	    int nb_handlers = method.nextLength() - 1;
	    Set[] controlled = new Set[nb_handlers];
	    for(int i = 0; i < nb_handlers; i++)
		controlled[i] = ((HANDLER) method.next(i+1)).protectedSet();
	    return controlled;
	}

	// The basic block "current" ends at "last". We examine "last"'s
	// succesors (from succs[]) and, if necessary, create new
	// basic blocks for them. These new basic blocks are put into
	// the worklist w.
	private void end_basic_block
	    (FCFGBasicBlock current, Quad last, Quad succs[], Worklist w) {
	    // examine the normal successors 
	    for(int i = 0; i < succs.length; i++) {
		// the edges between METHOD and HANDLERs are bogus
		if(succs[i] instanceof HANDLER) continue;
		FCFGBasicBlock bb = get_bb(succs[i], w);
		FCFGBasicBlock.addNormalEdge(current, bb);
	    }

	    // find the relevant handlers
	    METHOD method = Util.getMETHOD(hcode);
	    Quad handlers[] = method.next();
	    int nb_handlers = handlers.length - 1;
	    for(int i = 0; i < nb_handlers; i++) {
		HANDLER handler = (HANDLER) handlers[i+1];
		if(handler.protectedSet().contains(last)) {
		    FCFGBasicBlock bb = get_bb(handler, w);
		    FCFGBasicBlock.addExcpEdge(current, bb);
		}
	    }
	}

	// Get the basic block that starts with the quad "succ". If
	// such a basic block does not exist yet, create a new one and
	// add it to the worklist w (in order for it to be processed
	// later).
	private FCFGBasicBlock get_bb(Quad succ, Worklist w) {
	    FCFGBasicBlock bb = (FCFGBasicBlock) quadToBB.get(succ);
	    if(bb == null) {
		// new basic block -> create it and put it into w
		bb = new FCFGBasicBlock(succ, this);
		blocks.add(bb);
		quadToBB.put(succ, bb);
		w.push(bb);
	    }
	    return bb;
	}

	// checks whether a and b are protected by different sets of handlers
	// requires: controlled[i] = protectedSet() of the ith handler.
	private boolean different_handlers(Quad a, Quad b, Set[] controlled) {
	    for(int i = 0; i < controlled.length; i++)
		if(controlled[i].contains(a) ^ controlled[i].contains(b))
		    return true;
	    return false;
	}

	/** Returns the <code>HCode</code> that <code>this</code> factory
	    produces FCFG basic blocks of. */
	public HCode getHCode() {
	    return hcode;
	}

	/** Returns the root <code>FCFGBasicBlock</code>.
	    <BR> <B>effects:</B> returns the <code>FCFGBasicBlock</code>
	         that is at the start of the set of
		 <code>HCodeElement</code>s being analyzed.
	*/
	public FCFGBasicBlock getRoot() {
	    return root;
	}

	/** Does the same thing as <code>getRoot</code>.
	    Work around Java's weak typing system. */
	public BasicBlockInterf getRootBBInterf() {
	    return getRoot();
	}

	/** Returns the <code>FCFGBasicBlock</code>s constructed by
	    <code>this</code> factory.
	*/
	public Set blockSet() {
	    return blocks;
	}

	/** Returns the leaf <code>FCFGBasicBlock</code>s.  <BR>
	    <B>effects:</B> returns a <code>Set</code> of terminal
	    <code>FCFGBasicBlock</code>s for the underlying
	    <code>HCode</code>. Usually, this set will contain a
	    single element, the <code>FCFGBasicBlock</code> for the
	    <code>FOOTER</code>. */
	public Set getLeaves() {
	    return leaves;
	}

	/** Does the same thing as <code>getLeaves</code>.
	    Work around Java's weak typing system. */
	public Set getLeavesBBInterf() {
	    return getLeaves();
	}

	/** Returns the <code>FCFGBasicBlock</code> containing
	    <code>hce</code>. 
	    <BR> <B>requires:</B> hce is present in the code for
	    <code>this</code>. 
	    <BR> <B>effects:</B> returns the basic block that contains
	    <code>hce</code>, or <code>null</code> if
	    <code>hce</code> is unreachable.
	*/
	public FCFGBasicBlock getBlock(HCodeElement hce) {
	    return (FCFGBasicBlock) quadToBB.get(hce);
	}
	
	/** Does the same thing as <code>getBlock</code>.
	    Work around Java's weak typing system. */
	public BasicBlockInterf getBBInterf(HCodeElement hce) {
	    return getBlock(hce);
	}

    };
    
}
