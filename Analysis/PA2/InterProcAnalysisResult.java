// InterProcAnalysisResult.java, created Wed Jul  6 16:50:58 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Arrays;

import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.Pair;

import harpoon.ClassFile.HField;

/**
 * <code>InterProcAnalysisResult</code> models the analysis result
 * that (1) is computed for the end of a method <code>m</code>; and
 * (2) that is required in the inter-procedural analysis in order to
 * treat calls to <code>m</code>.  The information from an object of
 * this class is a method specification, from the point of view of the
 * pointer analysis.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: InterProcAnalysisResult.java,v 1.3 2005-09-05 16:47:19 salcianu Exp $ */
public abstract class InterProcAnalysisResult {

    /** Inside edges at the end of the method. */
    public abstract PAEdgeSet eomI();

    /** Reverse inside edges at the end of the method. */
    PAEdgeSet revEomI() {
	if(revEomI == null) {
	    revEomI = eomI().reverse();
	}
	return revEomI;
    }
    private PAEdgeSet revEomI;

    /** Certain methods of <code>this</code> object may use caching
        for internal performances.  However, every time one of the
        components (e.g., the set of inside edges) changes, the
        programmer needs to invoke this method. */
    public void invalidateCaches() {
	revEomI   = null;
	eomAllEsc = null;
    }

    /** Outside edges at the end of the method. */
    public abstract PAEdgeSet eomO();
    
    /** Nodes that escape *globally* at the end of the method and are
        relevant for the inter-proc analysis (we try to avoid
        including here nodes that are unreachable from the caller). */
    public abstract Set<PANode> eomDirGblEsc();
    
    /** All nodes that escape *globally* at the end of the method.
        Honestly, this is not necessary in the inter-procedural
        analysis (only <code>eomDirGblEsc</code> is); still, this
        class seemed the most appropriate location for this method. */
    public abstract Set<PANode> eomAllGblEsc();

    // TODO: move in PAUtil
    public Set<PANode> eomAllEsc() {
	if(eomAllEsc == null) {
	    Set<PANode> sources = DSFactories.nodeSetFactory.create();
	    sources.addAll(eomAllGblEsc());
	    sources.addAll(ret());
	    sources.addAll(ex());
	    for(PANode node : eomI().sources()) {
		if(PAUtil.trivialEscape(node)) {
		    sources.add(node);
		}
	    }
	    eomAllEsc = eomI().transitiveSucc(sources);
	}
	return eomAllEsc;
    }
    private Set<PANode> eomAllEsc;

    /** Nodes returned from the method. */
    public abstract Set<PANode> ret();
    
    /** Nodes thrown from the method as exceptions. */
    public abstract Set<PANode> ex();

    
    public String toString() {
	StringBuffer buff = new StringBuffer();
	if(!eomI().isEmpty()) {
	    buff.append("\nInside edges =");
	    buff.append(eomI());
	}
	if(!eomO().isEmpty()) {
	    buff.append("\nOutside edges =");
	    buff.append(eomO());
	}
	if(!ret().isEmpty()) {
	    buff.append("\nRet = ");
	    buff.append(ret());
	}
	if(!ex().isEmpty()) {
	    buff.append("\nEx = ");
	    buff.append(ex());
	}
	if(!eomDirGblEsc().isEmpty()) {
	    buff.append("\nDirGblEsc = ");
	    buff.append(eomDirGblEsc());
	}
	if(!eomAllGblEsc().isEmpty()) {
	    buff.append("\nAllGblEsc = ");
	    buff.append(eomAllGblEsc());
	}
	if(!eomAllEsc().isEmpty()) {
	    buff.append("\nAllEsc = ");
	    buff.append(eomAllEsc());
	}
	if(Flags.RECORD_WRITES && !eomWrites().isEmpty()) {
	    buff.append("\nMutated abstract field(s) = ");
	    buff.append(eomWrites());
	}
	return buff.toString();
    }


    public Iterable<PANode> getAllNodes() {
	return
	    DSUtil.unionIterable
	    (Arrays.<Iterable<PANode>>asList
	     (eomI().allNodes(),
	      eomO().allNodes(),
	      ret(),
	      ex(),
	      eomDirGblEsc()));
    }


    /** Abstract fields written by the method and its transitive
        callees.  Relevant only if <code>Flags.RECORD_WRITES</code> is
        turned on.  */
    public Set<Pair<PANode,HField>> eomWrites() {
	throw new Error("unimplemented");
    }



    public static class Chained extends InterProcAnalysisResult {
	public Chained(InterProcAnalysisResult ipar) {
	    this.ipar = ipar;
	}
	private final InterProcAnalysisResult ipar;

	public PAEdgeSet eomI() { return ipar.eomI(); }
	
	public void invalidateCaches() { ipar.invalidateCaches(); }

	public PAEdgeSet eomO() { return ipar.eomO(); }

	public Set<PANode> eomDirGblEsc() { return ipar.eomDirGblEsc(); }

	public Set<PANode> eomAllGblEsc() { return ipar.eomAllGblEsc(); }
	
	public Set<PANode> eomAllEsc() { return ipar.eomAllEsc(); }

	public Set<PANode> ret() { return ipar.ret(); }
    
	public Set<PANode> ex()  { return ipar.ex(); }

	public Set<Pair<PANode,HField>> eomWrites() { return ipar.eomWrites(); }

    }

}
