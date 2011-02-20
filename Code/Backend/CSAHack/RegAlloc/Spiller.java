/*
 * RegAlloc/Spiller.java
 *  rewrite instruction list to spill overflow temporaries.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.StrongARM.TwoWordTemp;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.CSAHack.RegAlloc.Code.Access;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Properties.CFGEdge; 
import harpoon.Backend.CSAHack.Graph.NodeList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Temp.TempMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Rewrite instruction list to spill overflowing temporaries to the
 * local frame.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see RegAlloc.RegAlloc
 */
// XXX SPILLER SHOUD USE code.dg TO UPDATE DERIVATION WHILE IT SPILLS!
class Spiller {
  Code code;
  Map  spills;

  Spiller(Code c, TempList spilled) {
    this.code = c;
    // make an array of temporaries that need to be spilled
    // and allocate each of them some space in the frame.
    spills = new HashMap();
    for (int i=0; spilled!=null; spilled=spilled.tail, i++)
      spills.put(spilled.head, c.allocLocal());
  }
  // rewrite an instruction list.
  Instr rewrite(Instr instrs) { 
    final Map usespill = new HashMap(), defspill=new HashMap();
    Set instrsToSkip = new HashSet();
    for (Instr il=instrs; il!=null; il=il.getNext()) {
      if (instrsToSkip.contains(il)) continue;
      // find USE and DEFs of spilltemps.
      Temp d[] = il.def();
      for (int i=0; i<d.length; i++) {
	if (d[i] instanceof TwoWordTemp) {
	  // can't rename twowordtemps so just insert the pieces as themselves.
	  Temp low = ((TwoWordTemp)d[i]).getLow();
	  Temp high= ((TwoWordTemp)d[i]).getHigh();
	  if (spills.containsKey(low) &&
	      il.getAssem().indexOf("`d"+i+"l")!=-1) defspill.put(low, low);
	  if (spills.containsKey(high) &&
	      il.getAssem().indexOf("`d"+i+"h")!=-1) defspill.put(high, high);
	} else if (spills.containsKey(d[i]) && !defspill.containsKey(d[i]))
	  defspill.put(d[i], new Temp(il.getFactory().tempFactory()));
      }
      Temp u[] = il.use();
      for (int i=0; i<u.length; i++) {
	if (u[i] instanceof TwoWordTemp) {
	  Temp low = ((TwoWordTemp)u[i]).getLow();
	  Temp high= ((TwoWordTemp)u[i]).getHigh();
	  // can't rename twowordtemps so just insert the pieces as themselves.
	  if (spills.containsKey(low) &&
	      il.getAssem().indexOf("`s"+i+"l")!=-1)  usespill.put(low, low);
	  if (spills.containsKey(high) &&
	      il.getAssem().indexOf("`s"+i+"h")!=-1) usespill.put(high, high);
	} else if (spills.containsKey(u[i]) && !usespill.containsKey(u[i]))
	  usespill.put(u[i], new Temp(il.getFactory().tempFactory()));
      }
      // if no uses or defs of spilltemps, continue.
      if (defspill.size()==0 && usespill.size()==0) continue;
      // okay, rename the original instr with the new limited-lifespan temps.
      TempMap defMap =  new TempMap() { // defmap
	  public Temp tempMap(Temp t) {
	      return defspill.containsKey(t) ? (Temp)defspill.get(t) : t;
	  }
      }, useMap = new TempMap() { // usemap
	  public Temp tempMap(Temp t) {
	      return usespill.containsKey(t) ? (Temp)usespill.get(t) : t;
	  }
      };
      Instr ni = il.rename(il.getFactory(), defMap, useMap);
      // replace old instr with new instr
      Instr.replace(il, ni);  il=ni;
      instrsToSkip.add(ni);
      // insert loads before any usespills.
      for (Iterator it=usespill.entrySet().iterator(); it.hasNext(); ) {
	  Map.Entry e = (Map.Entry) it.next();
	  Temp olduse = (Temp) e.getKey(), newuse = (Temp) e.getValue();
	  Access a = (Access) spills.get(olduse);
	  for (Iterator it2=il.predC().iterator(); it2.hasNext(); ) {
	    Instr loadi = a.makeLoad(il.getFactory(), il, newuse);
	    loadi.insertAt((CFGEdge)it2.next());
	    instrsToSkip.add(loadi);
	  }
      }
      // treat next instruction and this one as atomic if next instr
      // string contains '@ dummy'
      if (il.succC().size()==1) {
	  Instr dummy = (Instr) ((CFGEdge)il.succC().iterator().next()).to();
	  if (dummy.getAssem().indexOf("@ dummy") >= 0) {
	      ni = dummy.rename(dummy.getFactory(), defMap, useMap);
	      Instr.replace(dummy, ni); il=ni;
	      instrsToSkip.add(ni);
	  }
      }
      // insert stores before any defspills.
      for (Iterator it=defspill.entrySet().iterator(); it.hasNext(); ) {
	  Map.Entry e = (Map.Entry) it.next();
	  Temp olddef = (Temp) e.getKey(), newdef = (Temp) e.getValue();
	  Access a = (Access) spills.get(olddef);
	  for (Iterator it2=il.succC().iterator(); it2.hasNext(); ) {
	    Instr storei = a.makeStore(il.getFactory(), il, newdef);
	    storei.insertAt((CFGEdge)it2.next());
	    instrsToSkip.add(storei);
	  }
      }
      // reset usespill and defspill for efficient re-use.
      usespill.clear(); defspill.clear();
      // done!
    }
    // fixup instrs
    while (instrs.getPrev()!=null) instrs = instrs.getPrev();
    return instrs;
  }
}
