package harpoon.Analysis.Instr;

import harpoon.Analysis.Instr.AppelRegAllocClasses.Web;
import harpoon.Analysis.Instr.AppelRegAllocClasses.Node;
import harpoon.Analysis.Instr.AppelRegAllocClasses.NodeIter;

import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.Loops.LoopFinder;

import harpoon.IR.Assem.Instr;

import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.CombineIterator;
import harpoon.Util.Util;

import harpoon.Temp.Temp;

import java.util.Collection;
import java.util.Map;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Abstracts away the heuristic used for deciding between
    spill candidates during graph simplification.
    <p>
    See "Spill code minimization techniques for optimizing
    compilers", Bernstein et. al
*/
public class SpillHeuristics {
    // Node n -> Set of Instr i, s.t. n is alive at i
    private GenericMultiMap nodeToLiveAt = new GenericMultiMap();
    private Map nestedLoopDepth;
    
    // shares state with the AppelRegAlloc class
    AppelRegAlloc regalloc;
    
    SpillHeuristics(AppelRegAlloc ara) {
	regalloc = ara;
    }
    
    /** Resets the state of this in preparation for analysis on 
	altered code.
    */
    void reset() {
	// FSK: Lazily build these mappings...
	nodeToLiveAt = null;
	nestedLoopDepth = null;
    }

    /** Instr -> Integer, val is loop nesting depth for the key (0 for
	non-looped code).  Constructed on demand; access through method
	call only. */
    Map nestedLoopDepth() {
	if (nestedLoopDepth == null)
	    nestedLoopDepth = buildNestedLoopDepth();
	return nestedLoopDepth;
    }
    
    private Map buildNestedLoopDepth() { 
	// builds the nestedLoopDepth map, doing a breadth-first
	// traversal of the loop tree.
	HashMap depthMap = new HashMap();
	LoopFinder root = new LoopFinder(regalloc.code);
	
	// List<Loops> where index of vector is the looping depth
	// (thus the 0th elem is the root itself)
	ArrayList level = new ArrayList();
	level.add(root);
	int depth = 0; // tracks the current level in the tree
	while( ! level.isEmpty()) {
	    Iterator levelIter = level.iterator();
	    level = new ArrayList();
	    Integer currDepth = new Integer( depth );
	    while( levelIter.hasNext() ){
		Loops curr = (Loops) levelIter.next();
		level.addAll( curr.nestedLoops() );
		
		Iterator instrs=curr.loopExcElements().iterator(); 
		while( instrs.hasNext() ){
		    Instr i = (Instr) instrs.next();
		    Util.assert( ! depthMap.keySet().contains( i ));
		    depthMap.put( i , currDepth );
		}
	    }
	    depth++;
	}

	return depthMap;
    }

    /** Returns a bunch of SpillHeuristic objects, with "cheap" ones 
	in front.
    */
    SpillHeuristic[] spillHeuristics() {
	SpillHeuristic[] hs = new SpillHeuristic[] { 

	    // ** SCOTT'S SPILL HEURISTIC
	    new SpillHeuristic() { double cost(Node m) {
		return (1000*(m.web.defs.size()+m.web.uses.size() ) ) / m.degree;  }}
	    ,

	    // ** CHAITIN'S SPILL HEURISTIC **
	    new SpillHeuristic() { double cost( Node m ) {  
		return chaitinCost(m) / m.degree; }}
	    , 
	    new SpillHeuristic() { double cost( Node m ) {  
		return chaitinCost(m) / (m.degree * m.degree); }} 
	    , 
	    new SpillHeuristic() { double cost( Node m ) { 
		return chaitinCost(m) / ( area(m) * m.degree ); }} 
	    , 
	    new SpillHeuristic() { double cost( Node m ) { 
		return chaitinCost(m) / ( area(m) * m.degree * m.degree ); }}, 
	    
	};
	
	return hs;
    }

    /** Returns the loop nested depth of <code>i</code>. */
    int depth(Instr i){ return((Integer)nestedLoopDepth().get(i)).intValue(); }
    /** Returns the number of temporaries live at <code>i</code>. */
    int width(Instr i){ return liveAt(i).size(); }


    // wrapper for dealing with union'ing a large bitset with a small
    // appendage set.
    private class ExtremityCollection extends AbstractCollection {
	ArrayList appendage;
	Collection body;
	// shares state with big, but not with small.  I bet remove()
	// is pretty sketch...
	ExtremityCollection(Collection big, Collection small) {
	    appendage = new ArrayList(small.size());
	    for(Iterator iter=small.iterator(); iter.hasNext();){
		Object s = iter.next();
		if (!big.contains(s)) {
		    appendage.add(s);
		}
	    }
	    body = big;
	}
	public Iterator iterator() { 
	    return new CombineIterator(body.iterator(), appendage.iterator());
	}
	public int size() { return body.size() + appendage.size(); }
    }
    
    private Collection liveAt(Instr i) {
	// live-at(i) should be live-in(i) U defs(i), according to scott
	Collection liveTempC = regalloc.liveTemps.getLiveBefore(i);
	return new ExtremityCollection( liveTempC, i.defC() );
    }

    private void buildNodeToLiveAt() {
	nodeToLiveAt.clear();
	for(Iterator instrs = regalloc.instrs(); instrs.hasNext(); ){
	    Instr i = (Instr) instrs.next();
	    Collection liveAt = liveAt(i);
	    for(Iterator temps = liveAt.iterator(); temps.hasNext();) {
		Temp t = (Temp) temps.next();
		Collection rdefC = regalloc.rdefs.reachingDefs(i, t);
		Collection webC = regalloc.tempToWebs.getValues( t );
		for(Iterator webs=webC.iterator(); webs.hasNext(); ){
		    Web w = (Web) webs.next();
		    boolean intersect = false;
		    for(Iterator rti = rdefC.iterator(); rti.hasNext(); ){
			Instr def = (Instr) rti.next();
			if(w.defs.contains( def )){
			    intersect = true;
			    break;
			}
		    }
		    if ( intersect ) {
			// w has a def that reaches i, thus w is live-at i
			Collection nodeC = (Collection) regalloc.nodesFor(w);
			for(Iterator nodes=nodeC.iterator();nodes.hasNext();){
			    Node n = (Node) nodes.next();
			    nodeToLiveAt.add( n, i );
			}
		    }
		}
	    }
	}
    }

    protected abstract class SpillHeuristic {
	public String toString() { 
	    return "SpillHeuristic<"
		+"accumExpCost:"+accumExpCost 
		+" maxExpSpills:"+maxExpSpills
		+" actualCost:"+actualCost
		+" actualSpills:"+actualSpills
		+">";
	}

	double accumExpCost = 0.0;
	int maxExpSpills = 0;

	double actualCost = 0.0;
	int actualSpills = 0;

	HashMap instrToAreaCache = new HashMap();
	HashMap nodeToAreaCache = new HashMap();    

	void reset() { 
	    accumExpCost = 0.0; 
	    maxExpSpills = 0;
	    actualCost = 0.0; 
	    actualSpills = 0; 
	    instrToAreaCache.clear();
	    nodeToAreaCache.clear();
	}
	void expectSpill( Node m ) { 
	    // IMPORTANT: don't confuse "accumExpCost" here (which is called
	    // "h_i" in the paper) with "cost" in the paper (which is
	    // called chaitinCost here)
	    accumExpCost += chaitinCost( m ); 
	    maxExpSpills++;
	}

	
	/** called when spill code is added for n . */
	void reallySpill( NodeIter ni ){
	    while(ni.hasNext()) 
		reallySpill(ni.next());
	}
	void reallySpill( Node n ){
	    actualCost += chaitinCost(n);
	    actualSpills++;
	}

	abstract double cost( Node m );
	
	double chaitinCost( Node m ) {
	    double sum = 0.0;
	    for(Iterator ds = m.web.defs.iterator(); ds.hasNext(); ){
		Instr i = (Instr) ds.next();
		sum += Math.pow( 10.0, depth(i));
	    }
	    for(Iterator us = m.web.uses.iterator(); us.hasNext(); ){
		Instr i = (Instr) us.next();
		sum += Math.pow( 10.0, depth(i));
	    }
	    return sum;
	}
	double area( Node m ) {
	    if (nodeToLiveAt == null) {
		nodeToLiveAt = new GenericMultiMap();
		buildNodeToLiveAt();
	    }

	    if (nodeToAreaCache.containsKey(m)) {
		return ((Double)nodeToAreaCache.get(m)).doubleValue();
	    } else {
		double sum = 0.0;
		Collection instrC = nodeToLiveAt.getValues( m );
		for(Iterator instrs = instrC.iterator(); instrs.hasNext(); ){
		    Instr i = (Instr) instrs.next();
		    
		    if (instrToAreaCache.containsKey(i)) {
			sum += ((Double)instrToAreaCache.get(i)).doubleValue();
		    } else {
			double val = (Math.pow(5.0, depth(i)) * width(i));
			sum += val;
			instrToAreaCache.put(i, new Double(val) );
		    }
		}
		nodeToAreaCache.put(m, new Double(sum));
		return sum;
	    }
	}

    }
}
