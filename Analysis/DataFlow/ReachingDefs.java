package harpoon.Analysis.DataFlow;

/**
 * ReachingDefs
 *
 *
 * Things that I want:
 * - Mapping from Quads to stuff (preferably unique, small integers)
 *   --> use getID?  how do we know the maximum number?
 * - Mapping from Temps to stuff (preferably unique, small integers)
 *
 * @author  John Whaley
 * @author Felix Klock (pnkfelix@mit.edu)
 */

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import harpoon.Analysis.EdgesIterator;
import harpoon.Util.*;
import harpoon.IR.Properties.Edges;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/** Performs Reaching Definitions analysis on any IR that implements
    HCodeElement, Edges, and UseDef.
*/
public class ReachingDefs extends ForwardDataFlowBasicBlockVisitor {

  Map bbToSets;
  Map tempsToPrsvs;
  int maxQuadID;

    /** ReachingDefs constructor.
	<BR> <B>requires:</B> <code>q</code> implements
	<code>HCodeElement</code> and <code>UseDef</code>.
    */
    public ReachingDefs(Edges q) {
	bbToSets = new Hashtable();
	this.maxQuadID = Solver.getMaxID((HCodeElement) q);
	initTempsToPrsvs(q);
    }
    
  /**
   * Initializes the map between temps and their preserve sets.  The
   * preserve sets contain all quads that do NOT define the given
   * temp.

   <BR> <B>requires:</B> <code>root</code> implements
   <code>HCodeElement</code> and <code>UseDef</code>.
   <BR> <B>effects:</B> TODO: fill in.
   */
  void initTempsToPrsvs(Edges root) {
    tempsToPrsvs = new HashMap();
    Enumeration q_en = new IteratorEnumerator(new EdgesIterator(root));
    while (q_en.hasMoreElements()) {
      UseDef q = (UseDef)q_en.nextElement();
      Temp[] defs = q.def();
      for (int i=0, n=defs.length; i<n; ++i) {
	Temp t = defs[i];
	BitString bs = (BitString)tempsToPrsvs.get(t);
	if (bs == null) {
	  tempsToPrsvs.put(t, bs = new BitString(maxQuadID));
	  bs.setUpTo(maxQuadID);
	}
	bs.clear( ((HCodeElement)q).getID());
      }
    }
  }

  /**
   * Merge function.
   *
   */
  public boolean merge(BasicBlock f, BasicBlock t) {
    ReachingDefInfo from_info = getInfo(f);
    Util.assert(from_info != null);
    BasicBlock from = f;
    BasicBlock to   = t;

    boolean result = false;
    ReachingDefInfo to_info = getInfo(to);
    if (to_info == null) {
	putInfo(to, to_info = new ReachingDefInfo(to, maxQuadID, tempsToPrsvs));
	result = true;
    }
    if (DEBUG) db("looking at in set of "+to+": "+to_info.inSet);
    if(to_info.mergePredecessor(from_info)) {
      if (DEBUG) db("in set of "+to+" changed to "+to_info.inSet);
      result = true;
    }
    return result;
  }

  /**
   * Visit function.  In our case, it simply updates the out set.
   *
   */
  public void visit(BasicBlock bb) {
    ReachingDefInfo info = getInfo(bb);
    if (info == null) {
	// FSK commented out the assertion since it wont work for a
	// generic ReachingDefs implementation
	// Util.assert(bb.getFirst() instanceof HEADER);
	putInfo(bb, info = new ReachingDefInfo(bb, maxQuadID, tempsToPrsvs));
    }
    info.updateOutSet();
  }

  public ReachingDefInfo getInfo(BasicBlock bb) {
    return (ReachingDefInfo)bbToSets.get(bb);
  }

  public void putInfo(BasicBlock bb, ReachingDefInfo info) {
    bbToSets.put(bb, info);
  }

  public String dump() {
    StringBuffer s = new StringBuffer();
    Iterator e = bbToSets.keySet().iterator();
    while (e.hasNext()) {
      BasicBlock bb = (BasicBlock)e.next();
      s.append("Basic block "+bb);
      ReachingDefInfo rdi = getInfo(bb);
      s.append("\n"+rdi);
    }
    return s.toString();
  }

  /*
  public static boolean setUnion(Hashtable to, Hashtable from) {
    boolean changed = false;
    for (Enumeration e = from.keys(); e.hasMoreElements(); ) {
      Object o = e.nextElement();
      if (!to.contains(o)) {
	changed = true; to.push(o);
      }
    }
    return changed;
  }
  */
  
}
