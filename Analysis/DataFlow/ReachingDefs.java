package harpoon.Analysis.DataFlow;

/**
 * ReachingDefs
 *
 * Note: this is only an example.  You should never actually do reaching defs
 * for SSA form like this.
 *
 * Things that I want:
 * - Mapping from Quads to stuff (preferably unique, small integers)
 *   --> use getID?  how do we know the maximum number?
 * - Mapping from Temps to stuff (preferably unique, small integers)
 *
 * @author  John Whaley
 */

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import harpoon.Util.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;

public class ReachingDefs extends ForwardDataFlowBasicBlockVisitor {

  Map bbToSets;
  Map tempsToPrsvs;
  int maxQuadID;

  public ReachingDefs(HEADER q) {
    bbToSets = new Hashtable();
    this.maxQuadID = Solver.getMaxID(q);
    initTempsToPrsvs(q);
  }

  /**
   * Initializes the map between temps and their preserve sets.  The
   * preserve sets contain all quads that do NOT define the given
   * temp.
   */
  void initTempsToPrsvs(Quad root) {
    tempsToPrsvs = new HashMap();
    QuadEnumerator q_en = new QuadEnumerator(root);
    while (q_en.hasMoreElements()) {
      Quad q = (Quad)q_en.nextElement();
      Temp[] defs = q.def();
      for (int i=0, n=defs.length; i<n; ++i) {
	Temp t = defs[i];
	BitString bs = (BitString)tempsToPrsvs.get(t);
	if (bs == null) {
	  tempsToPrsvs.put(t, bs = new BitString(maxQuadID));
	  bs.setUpTo(maxQuadID);
	}
	bs.clear(q.getID());
      }
    }
  }

  /**
   * Merge function.
   */
  public boolean merge(BasicBlock from, BasicBlock to) {
    ReachingDefInfo from_info = getInfo(from);
    Util.assert(from_info != null);
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
   */
  public void visit(BasicBlock bb) {
    ReachingDefInfo info = getInfo(bb);
    if (info == null) {
      Util.assert(bb.getFirst() instanceof HEADER);
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
