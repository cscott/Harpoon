package harpoon.Analysis.DataFlow;

/**
 * Solver
 *
 * Solves data flow equations, baby.
 * @author  John Whaley
 */

import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import harpoon.IR.Quads.*;
import harpoon.IR.Properties.Edges;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Worklist;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Analysis.EdgesIterator;

public abstract class Solver {

  public static boolean DEBUG = false;
  public static void db(String s) { System.out.println(s); }

  public static void forward_rpo_solver(Quad root, DataFlowQuadVisitor v) {

    ReversePostOrderEnumerator rpo = new ReversePostOrderEnumerator(root);

    do {
      v.changed = false;

      ReversePostOrderEnumerator iter = rpo.copy();
      while (iter.hasMoreElements()) {
	Quad q = (Quad)iter.next();
	if (DEBUG) db("visiting: "+q);
	q.visit(v);
	for (int i=0, n=q.nextLength(); i<n; ++i) {
	  Quad qn = q.next(i);
	  if (DEBUG) db("doing edge "+q+" -> "+qn);
	  if (v.merge(q, qn)) v.changed = true;
	}
      }

    } while (v.changed);

  }

  public static void worklist_solver(Quad root, DataFlowQuadVisitor v) {

    Worklist W = new HashSet();
    W.push(root);
    while (!W.isEmpty()) {
      v.changed = false;
      Quad q = (Quad) W.pull();
      if (DEBUG) db("visiting: "+q);
      q.visit(v);
      v.addSuccessors(W, q);
    }

  }

  public static void forward_rpo_solver(BasicBlock root, DataFlowBasicBlockVisitor v) {

    ReversePostOrderEnumerator rpo = new ReversePostOrderEnumerator(root);

    do {
      v.changed = false;

      ReversePostOrderEnumerator iter = rpo.copy();
      while (iter.hasMoreElements()) {
	BasicBlock q = (BasicBlock)iter.next();
	if (DEBUG) db("visiting: "+q);
	q.visit(v);
	for (Enumeration e=q.next(); e.hasMoreElements(); ) {
	  BasicBlock qn = (BasicBlock)e.nextElement();
	  if (DEBUG) db("doing edge "+q+" -> "+qn);
	  if (v.merge(q, qn)) v.changed = true;
	}
      }

    } while (v.changed);

  }

  public static void worklist_solver(BasicBlock root, DataFlowBasicBlockVisitor v) {

    Worklist W = new HashSet();
    W.push(root);
    while (!W.isEmpty()) {
      v.changed = false;
      BasicBlock q = (BasicBlock) W.pull();
      if (DEBUG) db("visiting: "+q);
      q.visit(v);
      v.addSuccessors(W, q);
    }

  }
    
    /** Function that finds the maximum ID for a set of HCodeElements.
	<BR> <B>requires:</B> <code>root</code> and all
	<code>HCodeElement</code>s reachable from <code>root</code>
	implement <code>Edges</code>. 
	<BR> <B>effects:</B> returns the largest ID of all the
	<code>HCodeElement</code>s reachable from <code>root</code>.
     */
    public static int getMaxID(HCodeElement root) {
    // this is utterly bogus. <-- Whaley, not FSK
    int max = 0;
    Enumeration q_en = new IteratorEnumerator(new EdgesIterator((Edges)root));
    while (q_en.hasMoreElements()) {
      int id = ((HCodeElement)(q_en.nextElement())).getID();
      Util.assert(id >= 0);
      max = (id > max) ? id : max;
    }
    if (DEBUG) db("max quad ID is "+max);
    return max;
  }

  /*
  public static void worklist_solver(Visitable root, Visitor v) {
    Worklist W = new HashSet();
    W.push(root);
    while (!W.isEmpty()) {
      Visitable q = (Visitable) W.pull();
      if (DEBUG) db("visiting: "+q);
      q.visit(v);
    }
  }
  */

}
