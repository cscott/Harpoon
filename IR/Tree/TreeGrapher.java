// TreeGrapher.java, created Mon Dec 20  3:54:16 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeEdge; 
import harpoon.ClassFile.HCodeElement; 
import harpoon.IR.Properties.CFGrapher; 
import harpoon.IR.Tree.TreeVisitor; 
import harpoon.Temp.LabelList; 
import harpoon.Util.Util; 

import java.util.Collection; 
import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Iterator; 
import java.util.Map; 
import java.util.Set; 
import java.util.Stack; 


/**
 * <code>TreeGrapher</code> provides a means to externally associate
 * control-flow graph information with elements of an canonical tree. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeGrapher.java,v 1.1.2.5 2000-01-17 23:41:30 cananian Exp $
 */
class TreeGrapher extends CFGrapher { 
    private Map predecessors = new HashMap(); 
    private Map successors   = new HashMap(); 

    /** Class constructor.  Don't call this directly -- use
     *  the getGrapher() method in <code>harpoon.IR.Tree.Code</code> instead.
     */ 
    TreeGrapher(Code code) { 
	// Tree grapher only works on canonical trees. 
	Util.assert(code.getName().equals("canonical-tree")); 

	EdgeVisitor grapher = new EdgeVisitor
	    (code, this.predecessors, this.successors); 
    }

    /** Returns an array of all the edges to and from the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] edges(HCodeElement hc) { 
	return (HCodeEdge[])this.edgeC(hc).toArray(new HCodeEdge[0]); 
    }

    /** Returns an array of all the edges entering the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] pred(HCodeElement hc) { 
	return (HCodeEdge[])this.predC(hc).toArray(new HCodeEdge[0]); 
    }

    /** Returns an array of all the edges leaving the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] succ(HCodeElement hc) { 
	return (HCodeEdge[])this.succC(hc).toArray(new HCodeEdge[0]); 
    }

    /** Returns a <code>Collection</code> of all the edges to and from
     *  this <code>HCodeElement</code>. */
    public Collection edgeC(HCodeElement hc) { 
	Set edges = new HashSet(); 
	edges.addAll(this.predC(hc)); 
	edges.addAll(this.succC(hc)); 

	return edges; 
    }
    /** Returns a <code>Collection</code> of all the edges to
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return 
	<code>hc</code> in response to a call to <code>to()</code>;
	the actual predecessor will be returned from
	<code>from()</code>.  
     */
    public Collection predC(HCodeElement hc) { 
	return (Collection)this.predecessors.get(hc); 
    }

    /** Returns a <code>Collection</code> of all the edges from
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return
	<code>hc</code> in response to a call to
	<code>from()</code>; the actual successor to <code>this</code>
	will be returned from <code>to()</code>.
     */
    public Collection succC(HCodeElement hc) { 
	return (Collection)this.successors.get(hc); 
    }
	

    /* Only for canonical views, a class to initialize
     *  the edges representing the CFG of this Tree form
     */
    private static class EdgeVisitor extends TreeVisitor { 
	private Code   code; 
	private Map    labels = new HashMap();
	private Map    predecessors;
	private Map    successors;
	private Stack  nodes = new Stack();
	private Stm    nextNode;

	EdgeVisitor(Code code, Map predecessors, Map successors) { 
	    this.code = code; 
	    this.predecessors = predecessors; 
	    this.successors = successors; 
	    this.initSets(); 
	    this.computeEdges(); 
	}
	
	private void computeEdges() { 
	    this.nextNode = (Stm)this.code.getRootElement();
	    while (this.nextNode!=null) { 
		this.nextNode.accept(this); 
	    }
	}

	public void visit(Tree t) { throw new Error("No defaults here."); }
	public void visit(Exp e)  { /* Do nothing for Exps */ } 

	public void visit(final CALL s) { 
	    this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	    Util.assert(labels.containsKey(s.getHandler().label),
			"labels should contain Label:" + 
			s.getHandler().label);
	    Util.assert(this.nextNode!=null, 
			"nextNode shouldn't be null");
	    Util.assert(RS(this.nextNode)!=(Stm)labels.get(s.getHandler().label),
			new Util.LazyString() {
			    public String eval() {
				return "both normal and exceptional"+
				" return should not target same location"+
				" for "+Print.print(s);}});
	    
	    addEdge(s, RS(this.nextNode)); 
	    addEdge(s, (Stm)labels.get(s.getHandler().label));
	}

	public void visit(CJUMP s) { 
	    Util.assert(labels.containsKey(s.iftrue));
	    Util.assert(labels.containsKey(s.iffalse));
	    addEdge(s, (Stm)labels.get(s.iftrue));
	    addEdge(s, (Stm)labels.get(s.iffalse));
	    this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}
	
	public void visit(JUMP s) { 
	    for (LabelList l = s.targets; l!=null; l=l.tail) { 
		Util.assert(labels.containsKey(l.head));
		addEdge(s, (Stm)labels.get(l.head));
		this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	    }
	}

	public void visit(SEQ s) { 
	    Util.assert(s.getLeft()!=null && s.getRight()!=null);
	    nodes.push(s.getRight());
	    this.nextNode = s.getLeft();
	}

	public void visit(RETURN s) { 
	    this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}

	public void visit(THROW s) { 
	    this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}

	public void visit(Stm s) { 
	    this.nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	    if (this.nextNode!=null) addEdge(s, RS(this.nextNode)); 
	}

	private void initSets() {
	    for (Iterator i = this.code.getElementsI(); i.hasNext();) {
		Tree next = (Tree)i.next();
		// Add predecessor and successor sets for "next"
		this.successors.put(next, new HashSet()); 
		this.predecessors.put(next, new HashSet()); 

		// Map labels to their LABEL in the tree form. 
		if (next.kind() == TreeKind.LABEL) { 
		    labels.put(((LABEL)next).label, next); 
		}
	    }
	}
	
	private void addEdge(Stm from, Stm to) { 
	    Set pred, succ;
	    
	    pred = (Set)this.predecessors.get(to);
	    succ = (Set)this.successors.get(from);
	    pred.add(from);
	    succ.add(to);
	}	    

	private Stm RS(Stm seq) { 
	    while (seq.kind()==TreeKind.SEQ) seq = ((SEQ)seq).getLeft();  
	    return seq;
	}
    }
}
