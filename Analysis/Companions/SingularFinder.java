// SingularFinder.java, created Sat May  3 20:00:14 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.WorkSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>SingularFinder</code> is an implementation of
 * <code>SingularOracle</code> for quad IRs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SingularFinder.java,v 1.1 2003-05-07 22:53:04 cananian Exp $
 */
public class SingularFinder implements SingularOracle<Quad> {
    private final HCodeFactory hcf;
    /** Creates a <code>SingularFinder</code>. */
    public SingularFinder(HCodeFactory hcf) {
        this.hcf = hcf;
    }
    
    private HCode<Quad> convert(HMethod m) {
	return (harpoon.IR.Quads.Code) hcf.convert(m);
    }

    public Set<Quad> genSites(HMethod m, StaticValue<Quad> sv) {
	if (!methodCache.containsKey(m)) computeSingularity(m);
	return methodCache.get(m).genSiteMap.get(sv);
    }

    public Set<Temp> conditionallySingular(HMethod m, StaticValue<Quad> sv) {
	if (!methodCache.containsKey(m)) computeSingularity(m);
	return methodCache.get(m).condSingMap.get(sv);
    }

    // xxx should cache this result.
    public Set<Temp> pairwiseSingular(HMethod m, StaticValue<Quad> sv1,
				      StaticValue<Quad> sv2) {
	Set<Temp> P1 = conditionallySingular(m, sv1);
	Set<Temp> P2 = conditionallySingular(m, sv2);
	// easy cases.
	if (P1==null || P2==null) return null; // sv1/sv2 not singular!
	Set<Temp> P = methodCache.get(m).tempSetFactory.makeSet();
	P.addAll(P1); P.addAll(P2);
	Set<Quad> gs1 = genSites(m, sv1);
	Set<Quad> gs2 = genSites(m, sv2);
	Set<Quad> intersection = methodCache.get(m).quadSetFactory.makeSet();
	intersection.addAll(gs1); intersection.retainAll(gs2);
	if (intersection.isEmpty()) return P;
	// hard case: use method summary, look for paths from sv1 to sv2
	// that don't pass through *all* the quads listed in 'intersection'
	// easy sub case: when there is no path from sv1 to sv2 or sv2 to sv1.
	assert false: "unimplemented";
	return P;
    }

    // xxx cache this result?
    public Set<Temp> mutuallySingular(HMethod m,
				      Collection<StaticValue<Quad>> svs) {
	assert svs.size()>0;
	List<StaticValue<Quad>> worklist = new ArrayList<StaticValue<Quad>>
	    (new HashSet<StaticValue<Quad>>(svs)); // make unique.
	// easy cases first.
	if (worklist.size()==1)
	    return conditionallySingular(m, worklist.get(0));
	if (worklist.size()==2)
	    return pairwiseSingular(m, worklist.get(0), worklist.get(1));
	// hard cases: quadratic in size of set =(
	Set<Temp> P = methodCache.get(m).tempSetFactory.makeSet(); // empty.
	for (int i=0; i<worklist.size(); i++) {
	    for (int j=i+1; j<worklist.size(); j++) {
		Set<Temp> Px = pairwiseSingular
		    (m, worklist.get(i), worklist.get(j));
		if (Px==null) return null; // not mutually singular!!
		P.addAll(Px);
	    }
	}
	return P;
    }

    private void computeSingularity(HMethod m) {
	assert !methodCache.containsKey(m);
	HCode<Quad> hc = convert(m);

	assert false : "unimplemented"; // xxx do this analysis.

	//methodCache.put(m, ...);
	assert methodCache.containsKey(m);
    }

    private final Map<HMethod, PerMethodInfo> methodCache =
	new HashMap<HMethod,PerMethodInfo>();

    static class PerMethodInfo {
	Map<StaticValue<Quad>,Set<Temp>> condSingMap;
	Map<StaticValue<Quad>,Set<Quad>> genSiteMap;
	// trimmed to include only temps mentioned in condSingMap
	SetFactory<Temp> tempSetFactory;
	// trimmed to include only quads named in genSiteMap or condSingMap
	SetFactory<Quad> quadSetFactory;
	Graph trimmedCFG; // CFG trimmed to universe of quadSetFactory.
    }


    // re-use in/gen/kill.  use aggregatemap for nout.
    // implement setfactory that reuses set objects where possible.
    private Map<Quad,Set<StaticValue<Quad>>> computeRU(HCode<Quad> hc) {
	Map<Quad,Set<StaticValue<Quad>>> inRU =
	    new HashMap<Quad,Set<StaticValue<Quad>>>();//xxx small (array) map?
	Map<Quad,Set<StaticValue<Quad>>> outRU =
	    new HashMap<Quad,Set<StaticValue<Quad>>>();//xxx small (array) map?

	WorkSet<Quad> worklist = new WorkSet<Quad>(hc.getElementsL());
	while (!worklist.isEmpty()) {
	    Quad n = worklist.removeFirst();
	    Set<StaticValue<Quad>> in =
		new HashSet<StaticValue<Quad>>(); // xxx small
	    Set<StaticValue<Quad>> gen =
		new HashSet<StaticValue<Quad>>(); // xxx small
	    Set<StaticValue<Quad>> kill =
		new HashSet<StaticValue<Quad>>(); // xxx small
	    Set<StaticValue<Quad>> out = outRU.get(n);
	    // compute in from union of predecessors out
	    for (Iterator<Quad> it=Arrays.asList(n.prev()).iterator();
		 it.hasNext(); )
		in.addAll(outRU.get(it.next()));
	    inRU.put(n, in);
	    // compute gen/kill
	    for (Iterator<Temp> it=n.useC().iterator(); it.hasNext(); )
		gen.add(new StaticValue<Quad>(it.next(), n));
	    for (Iterator<StaticValue<Quad>> it=in.iterator(); it.hasNext();) {
		StaticValue<Quad> sv = it.next();
		if (n.defC().contains(sv.left()))
		    kill.add(sv);
	    }
	    // recompute out.
	    Set<StaticValue<Quad>> nout =
		new HashSet<StaticValue<Quad>>(); // xxx small.
	    nout.addAll(in); nout.removeAll(kill); nout.addAll(gen);
	    if (!nout.equals(out)) {
		outRU.put(n, nout);
		// add all successors to worklist.
		worklist.addAll(Arrays.asList(n.next()));
	    }
	}
	// result is inRU map.
	return inRU;
    }
}
