// AppelRegAllocClasses.java, created Tue Jun  5  0:02:27 2001 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Instr.AppelRegAlloc.Move;
import harpoon.Analysis.Instr.AppelRegAlloc.MoveList;

import harpoon.IR.Assem.Instr;

import harpoon.Temp.Temp;

import harpoon.Util.Util;
import harpoon.Util.Default;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Collections.LinearSet;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/** Collects various data structures used by AppelRegAlloc. 
 *  @author  Felix S. Klock II <pnkfelix@mit.edu>
 *  @version $Id: AppelRegAllocClasses.java,v 1.1.2.3 2001-06-17 22:29:48 cananian Exp $
 */
abstract class AppelRegAllocClasses extends RegAlloc {
    public AppelRegAllocClasses(harpoon.Backend.Generic.Code code) { 
	super(code); 
    }
    NodeSet precolored;
    NodeSet initial;
    NodeSet simplify_worklist;
    NodeSet freeze_worklist;
    NodeSet spill_worklist;
    NodeSet spilled_nodes;
    NodeSet coalesced_nodes;
    NodeSet colored_nodes;
    NodeSet select_stack;


    static class Web { 
	Temp temp; 
	Collection defs, uses;
	Web(Temp temp, Set defs, Set uses) { 
	    this.temp = temp; 
	    this.defs = new LinearSet(defs); 
	    this.uses = new LinearSet(uses);
	}
	public String toString() {
	    return "Web<"+temp+
		", defs:"+defs+
		", uses:"+uses+">";
	}
    }


    // these sets collectively partition the space of Nodes
    void initializeNodeSets() {
	precolored        = new NodeSet("precolored");
	initial           = new NodeSet("initial");
	simplify_worklist = new NodeSet("simplify_worklist");
	freeze_worklist   = new NodeSet("freeze_worklist");
	spill_worklist    = new NodeSet("spill_worklist");
	spilled_nodes     = new NodeSet("spilled_nodes");
	coalesced_nodes   = new NodeSet("coalesced_nodes");
	colored_nodes     = new NodeSet("colored_nodes");
	select_stack      = new NodeSet("select_stack");
    }

    class BackupNodeSetInfo {
	class BackupNodeInfo {
	    // keeps id/web saved
	    final Node origNode;
	    // don't need to save s_prev/s_next/s_rep
	    final NodeList origNeighbors; 
	    final Node origAlias;
	    final ArrayList origMoves; // List<Move>
	    final int origColor;

	    BackupNodeInfo( Node save ){
		origNode = save;
		origAlias = save.alias;
		origColor = save.color;
		origMoves = new ArrayList(save.moves.size);
		origNeighbors = new NodeList();
		for(Iterator moves=save.moves.iterator(); moves.hasNext();)
		    origMoves.add( moves.next() );
		for(NodeIter nodes = save.neighbors.iter(); nodes.hasNext();)
		    origNeighbors.add( nodes.next() );
	    }

	    void restore() {
		origNode.alias = origAlias;
		origNode.color = origColor;
		origNode.moves = new MoveList();
		for(Iterator moves=origMoves.iterator(); moves.hasNext(); )
		    origNode.moves.add( (Move) moves.next() );
		origNode.neighbors = new NodeList();
		for(NodeIter nodes = origNeighbors.iter();nodes.hasNext(); )
		    origNode.neighbors.add( nodes.next() );
	    }
	}
	
	final NodeSet origSet;
	final ArrayList savedState; // List<BackupNodeInfo>

	BackupNodeSetInfo(NodeSet save) {
	    this.origSet = save;
	    savedState = new ArrayList(origSet.size);
	    for(NodeIter nI=save.iter(); nI.hasNext(); ){
		savedState.add( new BackupNodeInfo( nI.next() ));
	    }
	}
    }

    BackupNodeSetInfo
	simplify_worklistI, freeze_worklistI,
	spill_worklistI, spilled_nodesI,
	coalesced_nodesI, colored_nodesI,
	select_stackI;

    void saveNodeSets() {
	simplify_worklistI = new BackupNodeSetInfo(  simplify_worklist );
	freeze_worklistI   = new BackupNodeSetInfo(  freeze_worklist   );
	spill_worklistI	   = new BackupNodeSetInfo(  spill_worklist    );
	spilled_nodesI	   = new BackupNodeSetInfo(  spilled_nodes  );
	coalesced_nodesI   = new BackupNodeSetInfo(  coalesced_nodes  );
	colored_nodesI	   = new BackupNodeSetInfo(  colored_nodes  );
	select_stackI	   = new BackupNodeSetInfo(  select_stack  );
    }
    void restoreNodeSets() {
	// helper array to make traversal code easy
	BackupNodeSetInfo[] infos = new BackupNodeSetInfo[]{
	    simplify_worklistI, freeze_worklistI,
	    spill_worklistI, spilled_nodesI,
	    coalesced_nodesI, colored_nodesI,
	    select_stackI 
	};

	// must clear all origSets before restoring them
	for(int i=0; i<infos.length; i++)
	    infos[i].origSet.clear();
	for(int i=0; i<infos.length; i++)
	    for(Iterator nI=infos[i].savedState.iterator();nI.hasNext();)
		infos[i].origSet.add( (Node) nI.next() );
    }


    abstract static class NodeIter {
	public abstract boolean hasNext();
	public abstract Node next();
    }
    NodeIter combine(final NodeIter[] iters) {
	return new NodeIter() {
		int i=0;
		public boolean hasNext() { 
		    while(i < iters.length) {
			if (iters[i].hasNext()) {
			    return true;
			} else {
			    i++;
			    continue;
			}
		    }
		    return false;
		}
		public Node next() { 
		    hasNext(); 
		    return iters[i].next(); }
	    };
    }

    final class NodeSet  {
	String name; // for debugging
	int size;
	Node head; // dummy element

	public String toString() { return "NS<"+name+">"; }
	void checkRep() { 
	    Util.assert(size >= 0);
	    Util.assert(head != null);
	    Node curr = head;
	    int sz = -1;
	    do { 
		Util.assert( curr.s_prev.s_next == curr );
		Util.assert( curr.s_next.s_prev == curr );
		sz++;
		curr = curr.s_next;
	    } while (curr != head);
	    Util.assert(sz == size);
	}


	NodeSet(String s) { 
	    name = s; 
	    head = new Node(null); 
	    size = 0; 
	    checkRep(); 
	}

	NodeIter iter() { 
	    checkRep();
	    return new NodeIter() {
		    Node curr = head;
		    public boolean hasNext() { return curr.s_next != head; }
		    public Node next() { checkRep();
		    Util.assert(hasNext());
		    Node ret = curr.s_next; curr = ret; 
		    checkRep();
		    return ret; 
		    }
		};
	}

	Node pop() {
	    checkRep(); 
	    Node n = head.s_next; 
	    remove(n); 
	    checkRep(); 
	    return n;
	}

	boolean isEmpty() { return size == 0; }
	void clear() { while( !isEmpty() ) pop(); }

	void remove(Node n) {
	    checkRep();
	    Util.assert(n.s_rep == this, 
			this.name + 
			" tried to remove a node that is in "+
			n.s_rep.name);

	    Util.assert(! n.locked, "node "+n+" should not be locked" );

	    n.s_prev.s_next = n.s_next;
	    n.s_next.s_prev = n.s_prev;
	    size--;
	    n.s_next = n;
	    n.s_prev = n;
	    n.s_rep = null;

	    checkRep();
	}
	void add(Node n) {
	    // If select_stack is going to be implemented as a
	    // NodeSet, .add(..) needs to "push" Nodes on FRONT of
	    // set, not the end.

	    checkRep();
	    Util.assert(n.s_prev == n);
	    Util.assert(n.s_next == n);	    
	    Util.assert(n.s_rep == null);

	    Util.assert(! n.locked, "node "+n+" should not be locked" );

	    head.s_next.s_prev = n;
	    n.s_next = head.s_next;
	    n.s_prev = head;
	    head.s_next = n;
	    n.s_rep = this;
	    size++;

	    n.nodeSet_history.addFirst(this);

	    checkRep();
	}

	Set asSet() { return new java.util.AbstractSet() {
		public int size() { return size; }
		public Iterator iterator() {
		    final NodeIter ni = iter();
		    return new harpoon.Util.UnmodifiableIterator() {
			    public boolean hasNext() {return ni.hasNext();}
			    public Object next() {return ni.next();}
			};
		}
	    };
	}

    }

    NodeIter adjacent(Node n) { 
	final NodeIter internIter = n.neighbors.iter();
	// filter out ( selectStack U coalescedNodes )
	return new NodeIter() {
		Node next = null;
		public boolean hasNext() {
		    if (next == null) {
			while(internIter.hasNext()) {
			    next = internIter.next();
			    if (next.isSelectStack() || next.isCoalesced()) {
				continue;
			    } else {
				return true;
			    }
			}
			return false;
		    } else {
			return true;
		    }
		}
		public Node next() {
		    hasNext();
		    Node rtrn = next; 
		    next = null; 
		    return rtrn;
		}
	    };
    }

    final static class NodePairSet {
	HashSet pairs = new HashSet();
	public boolean contains(Node a, Node b) { 
	    return pairs.contains(Default.pair(a,b));
	}
	public void add(Node a, Node b) { 
	    pairs.add(Default.pair(a, b)); 
	}
    }

    final static class NodeList { 
	final static class Cons { Node elem; Cons next;  }
	int size = 0;
	Cons first, last;
	void add(Node n) {
	    Cons c = new Cons();
	    c.elem = n;
	    if (first == null) {
		first = last = c;
	    } else {
		c.next = first;
		first = c;
	    }
	    size++;
	}
	/** INVALIDATES 'l' */
	void append(NodeList l) {
	    last.next = l.first;
	    last = l.last;
	    l.first = null; l.last = null;
	    size += l.size;
	}
	void checkRep() {
	    Util.assert(last.next == null);
	    for(Cons curr = first; curr != last; curr = curr.next ) 
		Util.assert(curr != null);
	}
	NodeIter iter() {
	    return new NodeIter() {
		    Cons c = first;
		    public boolean hasNext() { return c != null; }
		    public Node next() {
			Node n = c.elem; c = c.next; return n;
		    }
		};
	}
    }

    int nextId = 1;
    final class Node {
	final int id;

	// prev and next pointers for this node's set
	Node s_prev, s_next;

	// Set Representative
	NodeSet s_rep;

	// the degree of this in the interference graph
	int degree = 0;

	// linked list of neighbors of this in interference graph
	NodeList neighbors = new NodeList();

	// when a move (u,v) has been coalesced and v put into
	// coalescedNodes, then v.alias = u
	Node alias = null;

	// list of moves this node is associated with
	MoveList moves = new MoveList();

	// color of this, ( 0 <= color < K when assigned )
	int color = -1;

	// Web for this (null if this is dummy header element)
	final Web web; 

	// for debugging purposes: a sequence of the node sets this
	// has been a member of during its lifetime
	java.util.LinkedList nodeSet_history = new java.util.LinkedList();

	// special case for dummy nodes (for which 'w == null')
	public Node(Web w) { 
	    id = nextId; nextId++; s_prev = s_next = this; web = w;} 

	public Node(NodeSet which, Web w) { 
	    this(w); 
	    which.add(this); 
	}

	boolean locked = false;

	// machine registers, preassigned a color
	public boolean isPrecolored()      { 
	    boolean r = s_rep == precolored;        
	    if (r) Util.assert(color != -1);
	    return r;
	}

	// temporary registers, not precolored and not yet processed 
	public boolean isInitial()       { return s_rep == initial; }

	// list of low-degree non-move-related nodes
	public boolean isSimplifyWorkL() { return s_rep == simplify_worklist;}

	// low-degree move-related nodes
	public boolean isFreezeWorkL()   { return s_rep == freeze_worklist;  }

	// high-degree nodes
	public boolean isSpillWorkL()    { return s_rep == spill_worklist;   }

	// nodes marked for spilling during this round
	public boolean isSpilled()       { return s_rep == spilled_nodes;    }

	// registers that have been coalesced; when u <- v is
	// coalesced, v is added ot this set and u put back on some
	// work-list (or vice versa)
	public boolean isCoalesced()     { return s_rep == coalesced_nodes;  }

	// nodes successfully colored
	public boolean isColored() { 
	    boolean r = s_rep == colored_nodes;     
	    if (r) Util.assert(color != -1); 
	    return r;
	}

	// nodes on select stack
	public boolean isSelectStack()   { return s_rep == select_stack;     }

	public String toString() {
	    return "Node<"+
		"id:"+id+
		", deg:"+degree+
		", alias:"+alias+
		", temp:"+((web==null)?"none":web.temp+"")+
		", history:"+nodeSet_history+
		">";
	}
    }

    static class ColorSet {
	boolean[] colors;
	int size;
	public ColorSet(int numColors) {
	    size = numColors;
	    colors = new boolean[numColors];
	    for(int i=0; i < colors.length; i++) {
		colors[i] = true;
	    }
	}
	/** requires: ! this.isEmpty() */
	public int available() {
	    for(int i=0; i<colors.length; i++) {
		if (colors[i]) 
		    return i;
	    }
	    throw new RuntimeException();
	}
	public void remove(int color) {
	    if (color < colors.length) {
		if ( colors[ color ] )
		    size--;
		colors[ color ] = false;
	    } else {
		// removing an unused register-only color; NOP
	    }
	}
	public boolean isEmpty() { return size == 0; }
    }



}
