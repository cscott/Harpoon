// SingularFinder.java, created Sat May  3 20:00:14 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import net.cscott.jutil.Default;
import net.cscott.jutil.AggregateMapFactory;
import net.cscott.jutil.AggregateSetFactory;
import harpoon.Util.Collections.Graph;
import net.cscott.jutil.MapFactory;
import net.cscott.jutil.SetFactory;
import net.cscott.jutil.WorkSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * <code>SingularFinder</code> is an implementation of
 * <code>SingularOracle</code> for quad IRs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SingularFinder.java,v 1.5 2004-02-08 01:50:55 cananian Exp $
 */
public class SingularFinder implements SingularOracle<Quad> {
    private final HCodeFactory hcf;
    /** Creates a <code>SingularFinder</code>. */
    public SingularFinder(HCodeFactory hcf) {
        this.hcf = hcf;
    }
    
    public Set<Temp> conditionallySingular(HMethod m, StaticValue<Quad> sv) {
	return mutuallySingular(m, Collections.singleton(sv));
    }

    // XXX cache this result?
    public Set<Temp> mutuallySingular(HMethod m,
				      Collection<StaticValue<Quad>> svs) {
	if (!methodCache.containsKey(m))
	    computeSingularity(m);
	assert methodCache.containsKey(m);
	PerMethodInfo pmi = methodCache.get(m);
	Set<StaticValue<Quad>> svsset = new HashSet<StaticValue<Quad>>(svs);
	Set<Temp> result = new TreeSet<Temp>();// compactness?
	// mutually singular iff
	//  \forall <v,s> \in svs : RDin[s](v)!=\bot &&
	//              {} = (svs \intersect RUin[s][RDin[s](v)])
	for (Iterator<StaticValue<Quad>> it=svs.iterator(); it.hasNext(); ) {
	    StaticValue<Quad> sv = it.next();
	    assert pmi.rdResult.containsKey(sv.right());
	    // first condition.
	    Set<DefPoint> dps = pmi.rdResult.get(sv.right()).get(sv.left());
	    if (dps==null)
		return null; // this static value is not singular!
	    // second condition.  first, compute RUin[s] of all dp in dps.
	    for (Iterator<DefPoint> it2=dps.iterator(); it2.hasNext(); ) {
		DefPoint dp = it2.next();
		Set<StaticValue<Quad>> ru=pmi.ruResult.get(sv.right()).get(dp);
		// check that none are in svs
		for (Iterator<StaticValue<Quad>> it3=ru.iterator();
		     it3.hasNext(); )
		    if (svsset.contains(it3.next()))
			return null;//not mutually singular with some sv in svs
		// also keep track of P union {dps}
		if (dp.right() instanceof METHOD)
		    result.add(dp.left());
	    }
	}
	// well, these are mutually singular.  Boo-yah!
	return Collections.unmodifiableSet(result);
    }

    private void computeSingularity(HMethod m) {
	assert !methodCache.containsKey(m);
	QuadFlowGraph qfg = new QuadFlowGraph
	    ((harpoon.IR.Quads.Code) hcf.convert(m));
	Map<QNode,RDInfo> rdResult = new RDSolver().compute(qfg);
	Map<QNode,RUInfo> ruResult = new RUSolver(rdResult).compute(qfg);
	// trim these; XXX could be trimmed much further.
	// (for example, I think the only relevant keys are
	//  array/field stores and calls, and only the defpoints
	//  mentioned in the trimmed-down rdResult's RDInfos are
	//  relevant keys in the ruResult's RUInfo.)
	Map<Quad,RDInfo> _rdResult = new TreeMap<Quad,RDInfo>();
	Map<Quad,RUInfo> _ruResult = new TreeMap<Quad,RUInfo>();
	for (Iterator<QNode> it=qfg.nodes().iterator(); it.hasNext(); ) {
	    QNode qn = it.next();
	    if (qn.isPhiEntrance() || qn.isSigmaExit()) continue;
	    _rdResult.put(qn.baseQuad(), rdResult.get(qn));
	    _ruResult.put(qn.baseQuad(), ruResult.get(qn));
	}
	methodCache.put(m, new PerMethodInfo(_rdResult, _ruResult));
	assert methodCache.containsKey(m);
    }

    private final Map<HMethod, PerMethodInfo> methodCache =
	new HashMap<HMethod,PerMethodInfo>();

    static class PerMethodInfo {
	final Map<Quad,RDInfo> rdResult;
	final Map<Quad,RUInfo> ruResult;
	PerMethodInfo(Map<Quad,RDInfo> rdResult, Map<Quad,RUInfo> ruResult) {
	    this.rdResult = rdResult; this.ruResult = ruResult;
	}
    }

    static class DefPoint extends Default.PairList<Temp,Quad> {
	DefPoint(Temp v, Quad def) { super(v, def); }
    }
    static abstract class Info<I extends Info<I,K,V>,K,V> {
	final MapFactory<K,Set<V>> mf;
	final Map<K,Set<V>> map;
	Info(MapFactory<K,Set<V>> mf, Map<K,Set<V>> map) {
	    this.mf = mf;
	    this.map = map;
	}
	protected abstract I thisInfo();
	protected abstract I newInfo(MapFactory<K,Set<V>> mf, Map<K,Set<V>> map);
	Set<V> get(K k) {
	    if (map.containsKey(k)) return map.get(k);
	    return Collections.EMPTY_SET;
	}
	I put(K k, Set<V> s) {
	    if (s==null || s.size()>0) {
		if (map.containsKey(k) &&
		    (s==null ? null==map.get(k) : s.equals(map.get(k))))
		    return thisInfo(); // no change necessary.
		I result = newInfo(this.mf, mf.makeMap(this.map));
		result.map.put(k, s);
		return result;
	    } else return removeKey(k);
	}
	I removeKey(K k) {
	    if (!map.containsKey(k)) return thisInfo();
	    I result = newInfo(this.mf, mf.makeMap(this.map));
	    result.map.remove(k);
	    return result;
	}
	I add(SetFactory<V> sf, K k, V v) {
	    Set<V> s = get(k);
	    if (s.contains(v)) return thisInfo();
	    s = sf.makeSet(s);
	    s.add(v);
	    return put(k, s);
	}
	public int hashCode() { return map.hashCode(); }
	public boolean equals(Object o) {
	    if (this==o) return true;
	    if (!(o instanceof Info)) return false;
	    return this.map.equals(((Info)o).map);
	}
    }
    static class RDInfo extends Info<RDInfo,Temp,DefPoint> {
	RDInfo(MapFactory<Temp,Set<DefPoint>> mf) {
	    this(mf, mf.makeMap());
	}
	private RDInfo(MapFactory<Temp,Set<DefPoint>> mf, 
		       Map<Temp,Set<DefPoint>> map) {
	    super(mf, map);
	}
	protected RDInfo thisInfo() { return this; }
	protected RDInfo newInfo(MapFactory<Temp,Set<DefPoint>> mf,
				 Map<Temp,Set<DefPoint>> map) {
	    return new RDInfo(mf, map);
	}
	static RDInfo join(MapFactory<Temp,Set<DefPoint>> mf,
			   SetFactory<DefPoint> sf,
			   RDInfo rd1, RDInfo rd2) {
	    RDInfo result = new RDInfo
		(mf, multiMapJoin(mf, sf, rd1.map, rd2.map));
	    if (result.equals(rd1)) return rd1; // reuse if possible
	    if (result.equals(rd2)) return rd2; // reuse if possible
	    return result;
	}
    }
    static class RDSolver extends DataFlowSolver.Forward<QNode,QEdge,RDInfo> {
	MapFactory<Temp,Set<DefPoint>> mf =
	    new AggregateMapFactory<Temp,Set<DefPoint>>();
	SetFactory<DefPoint> sf =
	    new AggregateSetFactory<DefPoint>();
	RDInfo EMPTY = new RDInfo(mf);

	// initialize to \q. \v. {} (except for method quad)
	protected RDInfo init(QNode qn) {
	    Quad q = qn.baseQuad();
	    if (!(q instanceof METHOD)) return EMPTY;
	    METHOD m = (METHOD) q;
	    RDInfo result = new RDInfo(mf);
	    for (int i=0; i<m.paramsLength(); i++)
		result.put(m.params(i),
			   Collections.singleton(new DefPoint(m.params(i),m)));
	    return result;
	}
	// join rule for RDInfo.
	protected RDInfo join(RDInfo rd1, RDInfo rd2) {
	    return RDInfo.join(mf, sf, rd1, rd2);
	}
	protected RDInfo out(QNode qn, RDInfo in) {
	    Quad q = qn.baseQuad();
	    RDInfo out = in;
	    if (qn.isPhiEntrance() || qn.isSigmaExit()) {
		Iterator<Temp> useI = qn.useC().iterator();
		Iterator<Temp> defI = qn.defC().iterator();
		while (useI.hasNext() && defI.hasNext())
		    out = doMove(out, defI.next(), useI.next());
		assert !useI.hasNext();
		assert !defI.hasNext();
	    } else if (q instanceof MOVE) {
		MOVE move = (MOVE) q;
		out = doMove(out, move.dst(), move.src());
	    } else if (q instanceof NEW) {
		NEW n = (NEW) q;
		out = out.put(n.dst(), Collections.singleton
			      (new DefPoint(n.dst(), n)));
	    } else if (q instanceof ANEW) {
		ANEW n = (ANEW) q;
		out = out.put(n.dst(), Collections.singleton
			      (new DefPoint(n.dst(), n)));
	    } else {
		for (Iterator<Temp> it=qn.defC().iterator(); it.hasNext(); )
		    out = out.put(it.next(), null);
	    }
	    return out;
	}
	RDInfo doMove(RDInfo in, Temp dst, Temp src) {
	    return in.put(dst, in.get(src));
	}
    }
    static class RUInfo extends Info<RUInfo,DefPoint,StaticValue<Quad>> {
	RUInfo(MapFactory<DefPoint,Set<StaticValue<Quad>>> mf) {
	    this(mf, mf.makeMap());
	}
	private RUInfo(MapFactory<DefPoint,Set<StaticValue<Quad>>> mf, 
		       Map<DefPoint,Set<StaticValue<Quad>>> map) {
	    super(mf, map);
	}
	protected RUInfo thisInfo() { return this; }
	protected RUInfo newInfo(MapFactory<DefPoint,Set<StaticValue<Quad>>> mf,
				 Map<DefPoint,Set<StaticValue<Quad>>> map) {
	    return new RUInfo(mf, map);
	}
	static RUInfo join(MapFactory<DefPoint,Set<StaticValue<Quad>>> mf,
			   SetFactory<StaticValue<Quad>> sf,
			   RUInfo ru1, RUInfo ru2) {
	    RUInfo result = new RUInfo
		(mf, multiMapJoin(mf, sf, ru1.map, ru2.map));
	    if (result.equals(ru1)) return ru1; // reuse if possible
	    if (result.equals(ru2)) return ru2; // reuse if possible
	    return result;
	}
    }
    static class RUSolver extends DataFlowSolver.Forward<QNode,QEdge,RUInfo> {
	Map<QNode,RDInfo> rdResult;
	MapFactory<DefPoint,Set<StaticValue<Quad>>> mf =
	    new AggregateMapFactory<DefPoint,Set<StaticValue<Quad>>>();
	SetFactory<StaticValue<Quad>> sf =
	    new AggregateSetFactory<StaticValue<Quad>>();
	RUInfo EMPTY = new RUInfo(mf);

	RUSolver(Map<QNode,RDInfo> rdResult) { this.rdResult=rdResult; }

	// initialize to \q. \sv. {}
	protected RUInfo init(QNode qn) { return EMPTY; }
	// join rule for RUInfo.
	protected RUInfo join(RUInfo ru1, RUInfo ru2) {
	    return RUInfo.join(mf, sf, ru1, ru2);
	}
	RDInfo getRD(QNode qn) { assert false:"unimplemented";return null; }
	protected RUInfo out(QNode qn, RUInfo in) {
	    Quad q = qn.baseQuad();
	    RUInfo out = in;
	    if (qn.isPhiEntrance() || qn.isSigmaExit() ||
		q instanceof MOVE) {
		// out = in.
	    } else {
		// use QNode's useC/defC for CALL/TYPESWITCH/SWITCH/CJMP
		RDInfo rd = rdResult.get(qn);
		// first, gen for uses.
		for (Iterator<Temp> it=qn.useC().iterator(); it.hasNext(); ) {
		    Temp u = it.next();
		    Set<DefPoint> s = rd.get(u);
		    if (s!=null)
			for (Iterator<DefPoint> it2=s.iterator();
			     it2.hasNext(); ) {
			    DefPoint dp = it2.next();
			    out = out.add(sf, dp, new StaticValue<Quad>(u, q));
			}
		}
		// last, kill for defs.
		for (Iterator<Temp> it=qn.defC().iterator(); it.hasNext(); )
		    out = out.removeKey(new DefPoint(it.next(), q));
	    }
	    return out;
	}
    }

    private static <V> Set<V> union(SetFactory<V> sf, Set<V> s1, Set<V> s2) {
	// try to reuse sets.
	if (s1.size()==0) return s2;
	if (s2.size()==0) return s1;
	// XXX these tests are slow =(
	if (s1.containsAll(s2)) return s1;
	if (s2.containsAll(s1)) return s2;
	// okay, have to create new set.
	Set<V> result = sf.makeSet(s1); result.addAll(s2);
	return result;
    }
    private static <K,V> Map<K,Set<V>> multiMapJoin(MapFactory<K,Set<V>> mf,
						    SetFactory<V> sf,
						    Map<K,Set<V>> m1,
						    Map<K,Set<V>> m2) {
	Map<K,Set<V>> result = mf.makeMap(m1);
	for (Iterator<K> it= m2.keySet().iterator(); it.hasNext(); ) {
	    K key = it.next();
	    Set<V> s1 = result.get(key);
	    Set<V> s2 = m2.get(key);
	    assert m2.containsKey(key);
	    if (!result.containsKey(key))
		result.put(key, s2);
	    else if (s1==null || s2==null)
		result.put(key, null); // bottom.
	    else
		result.put(key, union(sf, s1, s2));
	}
	if (result.equals(m1)) return m1; // reuse maps if possible
	if (result.equals(m2)) return m2; // reuse maps if possible.
	return result;
    }
}
