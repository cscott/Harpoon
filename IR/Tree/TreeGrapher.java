// TreeGrapher2.java, created Sun Jul 16 18:23:48 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge; 
import harpoon.ClassFile.HCodeElement; 
import harpoon.IR.Properties.CFGrapher; 
import harpoon.Temp.Label;
import harpoon.Temp.LabelList; 
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Util; 

import java.util.Collection; 
import java.util.HashMap; 
import java.util.Iterator; 
import java.util.Map; 

/**
 * <code>TreeGrapher</code> provides a means to externally associate
 * control-flow graph information with elements of a canonical tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeGrapher.java,v 1.1.2.9 2000-07-16 23:40:39 cananian Exp $
 */
class TreeGrapher extends CFGrapher {
    Tree firstElement = null;
    final MultiMap predMap = new GenericMultiMap(Factories.arrayListFactory,
						 Factories.hashMapFactory);
    final MultiMap succMap = new GenericMultiMap(Factories.arrayListFactory,
						 Factories.hashMapFactory);
   /** Class constructor.  Don't call this directly -- use
    *  the getGrapher() method in <code>harpoon.IR.Tree.Code</code> instead.
    */ 
    TreeGrapher(Code code) {
	// Tree grapher only works on canonical trees. 
	Util.assert(code.getName().equals("canonical-tree"));
	Edger e = new Edger(code, new Labeler(code));
	// done.
    }
    public HCodeElement getFirstElement(HCode hcode) { return firstElement; }
    public Collection predC(HCodeElement hc) { return predMap.getValues(hc); }
    public Collection succC(HCodeElement hc) { return succMap.getValues(hc); }

    private static class Labeler {
	private final Map labelmap = new HashMap();
	Labeler(Code code) {
	    class LVisitor extends TreeVisitor {
		public void visit(Tree e) { /* no op */ }
		public void visit(LABEL l) { labelmap.put(l.label, l); }
	    }
	    TreeVisitor tv = new LVisitor();
	    for (Iterator it=code.getElementsI(); it.hasNext(); )
		((Tree)it.next()).accept(tv);
	}
	LABEL lookup(Label l) {
	    Util.assert(labelmap.containsKey(l));
	    return (LABEL)labelmap.get(l);
	}
    }
    private class Edger {
	void addEdge(final Tree from, final Tree to) {
	    HCodeEdge hce = new HCodeEdge() {
		public HCodeElement from() { return from; }
		public HCodeElement to() { return to; }
		public String toString() { return "Edge from "+from+" to "+to;}
	    };
	    predMap.add(to, hce);
	    succMap.add(from, hce);
	}
	Edger(Code code, final Labeler labeler) {
	    class EVisitor extends TreeVisitor {
		Tree last = null;
		void linkup(Stm s, boolean canFallThrough) {
		    if (firstElement==null) firstElement = s;
		    if (last!=null) addEdge(last, s);
		    last = canFallThrough ? s : null;
		}
		public void visit(Tree t) { /* ignore */ }
		public void visit(Stm s) { linkup(s, true); }
		public void visit(CALL c) {
		    // edge to handler; also fall-through.
		    addEdge(c, labeler.lookup(c.getHandler().label));
		    linkup(c, true);
		}
		public void visit(CJUMP c) {
		    // edges to iftrue and iffalse; no fall-through.
		    addEdge(c, labeler.lookup(c.iffalse));
		    addEdge(c, labeler.lookup(c.iftrue));
		    linkup(c, false);
		}
		public void visit(ESEQ e) {
		    Util.assert(false, "Not in canonical form!");
		}
		public void visit(JUMP j) {
		    // edges to targets list. no fall-through.
		    for (LabelList ll=j.targets; ll!=null; ll=ll.tail)
			addEdge(j, labeler.lookup(ll.head));
		    linkup(j, false);
		}
		public void visit(RETURN r) {
		    // no fall-through.
		    linkup(r, false);
		}
		public void visit(SEQ s) { /* ignore this guy! */ }
		public void visit(THROW t) {
		    // no fall-through.
		    linkup(t, false);
		}
	    }
	    TreeVisitor tv = new EVisitor();
	    // iterate in depth-first pre-order:
	    for (Iterator it=code.getElementsI(); it.hasNext(); )
		((Tree)it.next()).accept(tv);
	}
    }
}
