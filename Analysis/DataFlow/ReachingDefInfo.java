package harpoon.Analysis.DataFlow;

import harpoon.Util.*;
import harpoon.Temp.Temp;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import java.util.Enumeration;
import java.util.Map;

/** <code>ReachingDefInfo</code> is a data structure storing
    information about the sets associated with a
    <code>BasicBlock</code> in Reaching Definitions Analysis.
*/ 
class ReachingDefInfo {

  BitString genSet;
  BitString prsvSet;

  BitString inSet;
  BitString outSet;

  int maxQuadID;

  ReachingDefInfo(BasicBlock bb, int maxQuadID, Map tempsToPrsvs) {
    inSet = new BitString(maxQuadID);
    outSet = new BitString(maxQuadID);

    genSet = new BitString(maxQuadID);
    prsvSet = new BitString(maxQuadID);

    this.maxQuadID = maxQuadID;

    calculateGenPrsvSets(tempsToPrsvs, bb);
  }

    /** Calculates Gen and Preserve sets.
	<BR> <B>requires:</B> elements of <code>bb</code> implement
	<code>UseDef</code> and <code>HCodeElement</code>. 
    */
    void calculateGenPrsvSets(Map tempsToPrsvs, BasicBlock bb) {
	
	prsvSet.setUpTo(maxQuadID);
	for (Enumeration e = bb.elements(); e.hasMoreElements(); ) {
	    UseDef q = (UseDef) e.nextElement();
	    Temp[] defs = q.def();
	    for (int i=0, n=defs.length; i<n; ++i) {
		Temp t = defs[i];
		BitString prsv2 = (BitString)tempsToPrsvs.get(t);
		prsvSet.and(prsv2);
		/*
		  for (int j=0, o=kills.length; j<o; ++j) {
		  int id = kills[j].getID();
		  prsvSet.clear(id);
		  }
		*/
		genSet.set(((HCodeElement)q).getID());
	    }
	}
    }

  public boolean mergePredecessor(ReachingDefInfo pred) {
    return inSet.or_upTo(pred.outSet, maxQuadID);
  }

  public void updateOutSet() {
    outSet.copyBits(inSet);
    outSet.and(prsvSet);
    outSet.or(genSet);
  }

  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append("\tIn   set: "+inSet);
    s.append("\n\tGen  set: "+genSet);
    s.append("\n\tOut  set: "+outSet+"\n");
    return s.toString();
  }

}
