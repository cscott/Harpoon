// QuadLiveness.java, created Wed Oct 27 17:17:49 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Liveness;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.PHI;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cscott.jutil.Default;
import net.cscott.jutil.WorkSet;
/**
 * <code>QuadLiveness</code> performs live variable analysis for a given
 * <code>HCode</code>. Since it caches results, you should create a new
 * <code>QuadLiveness</code> if you have changed the <code>HCode</code>.
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: QuadLiveness.java,v 1.4 2004-02-08 01:53:14 cananian Exp $
 */
public class QuadLiveness extends Liveness<Quad> {
    final Map<Quad,Set<Temp>> livein, liveout;
    final Map<Quad,Temp[]> tempin, tempout, tempinout;

    /** Creates a <code>QuadLiveness</code>. Requires 
     *  that the <code>HCode</code> be quad-no-ssa.
     */
    public QuadLiveness(HCode<Quad> hc) {
	super(hc);
       	List<Map<Quad,Set<Temp>>> live = this.analyze();
	this.livein = live.get(0);
	this.liveout = live.get(1);
	this.tempin = new HashMap<Quad,Temp[]>();
	this.tempout = new HashMap<Quad,Temp[]>();
	this.tempinout = new HashMap<Quad,Temp[]>();
    }

    /** Returns the <code>Set</code> of <code>Temp</code>s 
     *  that are live-in at the <code>HCodeElement</code>. 
     *  Requires that the <code>HCodeElement</code> be in 
     *  quad-no-ssa form. Returns <code>null</code> if there
     *  no live-in variables.
     */
    public Set<Temp> getLiveIn(Quad hce) {
	return new WorkSet<Temp>(livein.get(hce));
    }
    /** Same as getLiveIn, but returns array of <code>Temp</code>s.  This
	array is guaranteed to have the Temp's in the same order for a given
	QuadLiveness object,Quad pair.**/

    public Temp[] getLiveInArray(Quad hce) {
	if (tempin.containsKey(hce))
	    return Util.safeCopy(Temp.arrayFactory, tempin.get(hce));
	else {
	    Set<Temp> set = this.livein.get(hce);
	    Temp[] retval = set.toArray(new Temp[set.size()]);
	    tempin.put(hce,retval);
	    return Util.safeCopy(Temp.arrayFactory, retval);
	}
    }
    /** Returns the <code>Set</code> of <code>Temp</code>s 
     *  that are live-out at the <code>HCodeElement</code>. 
     *  Requires that the <code>HCodeElement</code> be in 
     *  quad-no-ssa form. Returns <code>null</code> if there
     *  no live-in variables.
     */
    public Set<Temp> getLiveOut(Quad hce) {
	return new WorkSet<Temp>(liveout.get(hce));
    }

    /** Same as getLiveOut, but returns array of <code>Temp</code>s.
	Makes the same order guarantees as <code>getLiveInArray</code>.**/
    public Temp[] getLiveOutArray(Quad hce) {
	if (tempout.containsKey(hce))
	    return Util.safeCopy(Temp.arrayFactory, tempout.get(hce));
	else {
	    Set<Temp> set = this.liveout.get(hce);
	    Temp[] retval = set.toArray(new Temp[set.size()]);
	    tempout.put(hce, retval);
	    return Util.safeCopy(Temp.arrayFactory, retval);
	}
    }

    public Temp[] getLiveInandOutArray(Quad hce) {
	if (tempinout.containsKey(hce))
	    return Util.safeCopy(Temp.arrayFactory, tempinout.get(hce));
	else {
	    Set<Temp> set = this.liveout.get(hce);
	    Set<Temp> setin = this.livein.get(hce);
	    Iterator<Temp> iter=set.iterator();
	    int count=0;
	    while (iter.hasNext())
		if (setin.contains(iter.next()))
		    count++;
	    Temp[] retval=new Temp[count];
	    iter=set.iterator();
	    count=0;
	    while (iter.hasNext()) {
		Temp tt = iter.next();
		if (setin.contains(tt))
		    retval[count++]=tt;
	    }
	    tempinout.put(hce, retval);
	    return Util.safeCopy(Temp.arrayFactory, retval);
	}
    }

    private List<Map<Quad,Set<Temp>>> analyze() {
	//System.err.println("Entering QuadLiveness.analyze()");
	WorkSet<Quad> ws = new WorkSet<Quad>(this.hc.getElementsL());

	if (ws.isEmpty()) {
	    //System.err.println("Leaving QuadLiveness.analyze()");
	    return Collections.<Map<Quad,Set<Temp>>>nCopies
		(2, Default.<Quad,Set<Temp>>EMPTY_MAP());
	}

	Quad[] leaves = this.hc.getLeafElements();
	
	// For efficiency reasons, we want to start w/ a leaf
	// element if one exists. Otherwise, just start w/
	// any element.
	Quad hce = null;
	if (leaves != null) {
	    hce = leaves[0];
	    ws.remove(hce);
	} else {
	    hce = ws.removeLast();
	}

	Map<Quad,Set<Temp>> in = new HashMap<Quad,Set<Temp>>();
	Map<Quad,Set<Temp>> out = new HashMap<Quad,Set<Temp>>();

	while (true) {
	    Set<Temp> out_ = new WorkSet<Temp>();


	    for (int i=0;i<hce.nextLength();i++) {
		Quad successor=hce.next(i);
		if (in.containsKey(successor)) {
		    if (successor instanceof PHI) {
			WorkSet<Temp> w=new WorkSet<Temp>(out.get(successor));
			
			//search for successor
			int edge=hce.nextEdge(i).which_pred();
			PHI phi=(PHI)successor;
			
			for (int j=0;j<phi.numPhis();j++)
			    w.remove(phi.dst(j));
			
			for (int j=0;j<phi.numPhis();j++)
			    w.add(phi.src(j,edge));
			out_.addAll(w);
		    } else
			out_.addAll(in.get(successor));
		}
	    }

	    // Calculate "in" Set
	    Set<Temp> in_ = new WorkSet<Temp>(out_);
	    in_.removeAll(hce.defC());
	    in_.addAll(hce.useC());

	    // If we have grown our live-in or live-out variables,
	    // we need to update all of its predecessors.
	    // TRICK: doing a put on a Map returns the previous mapping
	    Set<Temp> old_in = in.put(hce, in_);
	    Set<Temp> old_out = out.put(hce, out_);
	    if ((old_in == null)|| (old_in.size() < in_.size())) {
		for (int i=0;i<hce.prevLength();i++)
		    ws.push(hce.prev(i));
	    } else if ((old_out == null)|| ( old_out.size() < out_.size())) {
		for (int i=0;i<hce.prevLength();i++)
		    ws.push(hce.prev(i));
	    }

	    if (ws.isEmpty()) break;
	    hce = ws.removeLast();
	}
	List<Map<Quad,Set<Temp>>> retval=new ArrayList<Map<Quad,Set<Temp>>>(2);
	retval.add(in); retval.add(out);
	return retval;
    }
}

