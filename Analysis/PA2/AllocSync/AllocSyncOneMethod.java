// AllocSyncOneMethod.java, created Tue Jul 26 13:49:39 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

import jpaul.Misc.Function;
import jpaul.Misc.Predicate;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.NodeRepository.INode;
import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.PAUtil;

import harpoon.Analysis.PA2.Flags;
import harpoon.Analysis.PA2.AnalysisPolicy;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.ChainedAllocationProperties;

import harpoon.Analysis.Quads.DeepInliner.InlineChain;

import harpoon.Util.Util;

/**
 * <code>AllocSyncOneMethod</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: AllocSyncOneMethod.java,v 1.2 2005-08-16 22:41:57 salcianu Exp $
 */
class AllocSyncOneMethod {

    /** 

	@param hm 
	
     */
    AllocSyncOneMethod(PointerAnalysis pa, HMethod hm,
		       CachingCodeFactory ccf,
		       LoopDetector loopDet,
		       AllCallers allCallers,
		       int MAX_SA_INLINE_LEVEL,
		       boolean SA_IN_LOOPS) {
	this.pa  = pa;
	this.ccf = ccf;
	this.loopDet = loopDet;
	this.allCallers = allCallers;
	this.MAX_SA_INLINE_LEVEL = MAX_SA_INLINE_LEVEL;
	this.SA_IN_LOOPS = SA_IN_LOOPS;

	if(PAUtil.isNative(hm) || PAUtil.isAbstract(hm)) return;

	// get the inside nodes corresponding to the interesting allocation sites
	Collection<Quad>   allocs = getAllAllocs(hm, ccf);
	Collection<PANode> inodes = getInsideNodes(allocs);
	if(inodes.isEmpty()) return;

	if(ASFlags.VERBOSE) {
	    System.out.println("\n\nAllocSyncOneMethod for " + hm);
	    System.out.println("allocs = " + allocs);
	    System.out.println("inodes = " + inodes);
	}
	
	// try to stack allocate them
	stackAllocateWithInlining(hm, inodes);
    }

    // the underlying pointer analysis
    private final PointerAnalysis pa;
    // the code factory used to generate the code of the method
    private final CachingCodeFactory ccf;
    // auxiliary object that detects whether a quad is in a loop
    private final LoopDetector loopDet;
    // auxiliary object to navigate the call graph backwards
    private final AllCallers allCallers;

    private final int MAX_SA_INLINE_LEVEL;
    private final boolean SA_IN_LOOPS;

    // will store the generated inlining chains
    private final List<InlineChain> ics = new LinkedList<InlineChain>();
    Collection<InlineChain> getICS() { return ics; }

    // get all allocation sites from the code of hm (ccf.convert(hm))
    private Collection<Quad> getAllAllocs(HMethod hm, CachingCodeFactory ccf) {
	Collection<Quad> news = new LinkedList<Quad>();
	Code code = (Code) ccf.convert(hm);
	for(Quad q : code.getElements()) {
	    if(isAlloc(q)) {
		news.add(q);
	    }
	}
	return news;
    }

    // get the inside nodes corresponding to the allocation sites from "allocs"
    private Collection<PANode> getInsideNodes(Collection<Quad> allocs) {
	return 
	    DSUtil.mapColl2
	    (allocs,
	     new Function<Quad,PANode>() {
		public PANode f(Quad q) {
		    return pa.getNodeRep().getInsideNode(q); 
		}
	     },
	     new LinkedList<PANode>());
    }



    private LinkedList<CALL> calls = new LinkedList<CALL>();
    private String buff = "";

    private void stackAllocateWithInlining(HMethod hm, Collection<PANode> candidates) {	
	if(ASFlags.VERBOSE) {
	    System.out.println(buff + "hm = " + hm);
	    System.out.println(buff + "candidates = " + candidates);
	}

	Pair<Collection<PANode>,Collection<PANode>> pair = identifySAOps(hm, candidates);
	Collection<PANode> toStackAllocate = filterAllocs(pair.left);
	Collection<PANode> escOnlyInCaller = filterAllocs(pair.right);

	if(!toStackAllocate.isEmpty()) {
	    if(calls.size() == 0) {
		if(ASFlags.VERBOSE) System.out.print(buff + "Directly ");
		doStackAllocation(iNodes2Quads(toStackAllocate));
	    }
	    else {
		ics.add(new StackAllocInlineChain(calls, iNodes2Quads(toStackAllocate)));
	    }
	}
	if(escOnlyInCaller.isEmpty()) return;
	if(calls.size() == MAX_SA_INLINE_LEVEL) return;

	String buffOld = null;
	if(ASFlags.VERBOSE) { 
	    buffOld = buff; buff += " "; 
	}
	// enumerate call sites that may invoke hm
	for(HMethod caller : allCallers.getCallers(hm)) {
	    // exceptions are seldom called, so inlining through them is a waste of time
	    if(PAUtil.isException(caller.getDeclaringClass())) continue;
	    for(CALL cs : allCallers.getCALLs(caller, hm)) {
		// do not inline calls with multiple possible callees
		if(!allCallers.monoCALL(cs)) continue;
		if(!pa.hasAnalyzedCALL(caller, cs, hm)) continue;
		if(!SA_IN_LOOPS && loopDet.inLoop(cs)) {
		    if(ASFlags.VERBOSE) {
			System.out.println(buff + "Cannot inline in-loop cs=" + Util.code2str(cs));
		    }
		    continue;
		}
		if(ASFlags.VERBOSE) {
		    System.out.println(buff + "Examine cs=" + Util.code2str(cs));
		}
		calls.addFirst(cs);
		if(acceptable(calls)) {
		    stackAllocateWithInlining(caller, escOnlyInCaller);
		}
		calls.removeFirst();		
	    }
	}
	if(ASFlags.VERBOSE) buff = buffOld;
    }


    /*
    private boolean additionalCheck() {
	if(true) return true;

	// DEBUG: eliminate
	CALL cs = DSUtil.getFirst(calls);
	if(!cs.method().getName().equals("<init>")) return false;

	HMethod targetMethod = Util.quad2method(DSUtil.getFirst(calls));
	String targetClassName = targetMethod.getDeclaringClass().getName();
	if(
	   !(targetClassName.startsWith("spec.benchmarks._228_jack.NfaState") &&
	     targetMethod.getName().startsWith("AddMove"))
	   ) {
	    System.out.println("DEBUG: no inlining through " + targetMethod);
	    return false;
	}
	return true;
    }
    */


    private boolean acceptable(List<CALL> calls) {
	int totalSize = getCodeSize(Util.quad2method(DSUtil.getFirst(calls)));
	for(CALL cs : calls) {
	    HMethod caller = Util.quad2method(cs);
	    HMethod callee = pa.getCallGraph().calls(caller, cs)[0];
	    int calleeSize = getCodeSize(callee);
	    if(calleeSize > ASFlags.MAX_INLINABLE_METHOD_SIZE) {
		if(ASFlags.VERBOSE) {
		    System.out.println("\t\t\t\tCALLEE TOO BIG");
		}
		return false;
	    }
	    totalSize += calleeSize;
	    if(totalSize > ASFlags.MAX_METHOD_SIZE) {
		if(ASFlags.VERBOSE) {
		    System.out.println("\t\t\t\tTOTAL SIZE TOO BIG");
		}
		return false;
	    }
	}
	return true;
    }

    private int getCodeSize(HMethod hm) {
	return ccf.convert(hm).getElements().length;
    }

    // Filter out the allocation sites that should not be stack allocated (even if captured)
    // E.g., allocations inside loops.
    private Collection<PANode> filterAllocs(Collection<PANode> inodes) {
	// DEBUG for binary search of wrongly stack-allocated sites
	if(ASFlags.SA_MIN_LINE != -1) {
	    inodes = DSUtil.filterColl(inodes, 
				       new Predicate<PANode>() {
					   public boolean check(PANode inode) {
					       Quad q = ((INode) inode).getQuad();
					       return 
					       (q.getLineNumber() >= ASFlags.SA_MIN_LINE) &&
					       (q.getLineNumber() <= ASFlags.SA_MAX_LINE);						
					   }
					},
					new LinkedList<PANode>());
	}

	return 
	    DSUtil.<PANode>filterColl
	    (inodes,
	     new Predicate<PANode>() {
		public boolean check(PANode node) {
		    Quad quad = ((INode) node).getQuad();
		    if(!SA_IN_LOOPS && loopDet.inLoop(quad)) {
			if(ASFlags.VERBOSE) {
			    System.out.println(Util.code2str(quad) + " cannot be stack-allocated: in loop");
			}
			return false;
		    }
		    return true;
		}
	    },
	    new LinkedList<PANode>());
    }


    // Split inodes into three sets: 
    //   1. nodes that escape globally - thrown away
    //   2. nodes that can be stack allocated
    //   3. nodes that escape only in the caller(s)
    // Returns the pair of the sets 2 and 3
    private Pair<Collection<PANode>,Collection<PANode>> identifySAOps(HMethod hm, Collection<PANode> inodes) {
	Collection<PANode> canBeStackAlloc = new LinkedList<PANode>();
	Collection<PANode> escOnlyInCaller = new LinkedList<PANode>();

	// The null policy demands the best already computed result
	InterProcAnalysisResult ipar = pa.getInterProcResult(hm, null);

	for(PANode inode : inodes) {
	    if(ipar.eomAllGblEsc().contains(inode)) {
		// globally escaped node; no point to continue
		continue;
	    }
	    if(!ipar.eomAllEsc().contains(inode)) {
		canBeStackAlloc.add(inode);
	    }
	    else {
		// inode escapes only in the caller
		escOnlyInCaller.add(inode);
	    }
	}
	if(ASFlags.VERBOSE) {
	    System.out.println(buff + "identifySAOps " + inodes);
	    System.out.println(buff + "  canBeStackAlloc = " + canBeStackAlloc);
	    System.out.println(buff + "  escOnlyInCaller = " + escOnlyInCaller);
	}
	return new Pair<Collection<PANode>,Collection<PANode>>(canBeStackAlloc, escOnlyInCaller);
    }


    private void doStackAllocation(final Collection<Quad> onStack) {
	Code targetBody = DSUtil.getFirst(onStack).getFactory().getParent();

	if(ASFlags.VERBOSE) {
	    System.out.println("Stack allocate in " + targetBody.getMethod());
	    for(Quad alloc : onStack) {
		System.out.println("\tSA: " + Util.code2str(alloc) + "\t inode = " +
				   pa.getNodeRep().getInsideNode(alloc));
	    }
	}

	final AllocationInformation<Quad> oldAI = targetBody.getAllocationInformation();
	
	targetBody.setAllocationInformation
	    (new AllocationInformation<Quad>() {
		public AllocationProperties query(final Quad q) {
		    return new ChainedAllocationProperties(getAP(oldAI, q)) {
			public boolean canBeStackAllocated() {
			    if(onStack.contains(q)) {
				return true;
			    }
			    return super.canBeStackAllocated();
			}
		    };
		}
	    });	
    }

    private Collection<Quad> iNodes2Quads(Collection<PANode> iNodes) {
	return 
	    DSUtil.mapColl
	    (iNodes,
	     new Function<PANode,Quad>() {
		public Quad f(PANode node) { return ((INode) node).getQuad(); }
	     },
	     new HashSet<Quad>());
    }

    private class StackAllocInlineChain extends InlineChain {
	public StackAllocInlineChain(List<CALL> calls, Collection<Quad> allocs) {
	    super(calls);
	    this.allocs = allocs;
	    if(ASFlags.VERBOSE) {
		this.copyCALLs = new LinkedList<CALL>(calls);
		System.out.println("new inline chain " + this);
	    }
	}
	private LinkedList<CALL> copyCALLs;
	private Collection<Quad> allocs;
	

	public void action(CALL cs, Code calleeCode, Map<Quad,Quad> oldQuad2newQuad) {
	    Code targetBody = cs.getFactory().getParent();
	    final AllocationInformation<Quad> oldAI = targetBody.getAllocationInformation();
	    final AllocationInformation<Quad> calleeAI = calleeCode.getAllocationInformation();
	    final Map<Quad,Quad> newAlloc2oldAlloc = getNewAlloc2oldAlloc(oldQuad2newQuad);

	    targetBody.setAllocationInformation
		(new AllocationInformation<Quad>() {
		    public AllocationProperties query(Quad q) {
			Quad oldQuad = newAlloc2oldAlloc.get(q);
			if(oldQuad != null) {
			    // return AllocationProperties for oldQuad
			    return getAP(calleeAI, oldQuad);
			}
			// default to oldAI
			return getAP(oldAI, q);
		    }
		});

	    // map the set of stack-allocatable alloc sites, if necessary
	    if(oldQuad2newQuad.containsKey(DSUtil.getFirst(allocs))) {
		this.allocs = DSUtil.<Quad,Quad>mapColl(allocs, oldQuad2newQuad, new LinkedList<Quad>());
	    }
	}

	public void finalAction() {
	    if(ASFlags.VERBOSE) {
		System.out.println("Stack-allocation after inlining " + InlineChain.callsToString(copyCALLs));
	    }

	    doStackAllocation(new HashSet<Quad>(allocs));

	    if(ASFlags.VERBOSE) {
		System.out.println();
	    }
	}

	private Map<Quad,Quad> getNewAlloc2oldAlloc(Map<Quad,Quad> oldQuad2newQuad) {
	    final Map<Quad,Quad> new2old = new HashMap<Quad,Quad>();
	    for(Map.Entry<Quad,Quad> entry : oldQuad2newQuad.entrySet()) {
		Quad oldQuad = entry.getKey();
		if(!isAlloc(oldQuad)) continue;
		Quad newQuad = entry.getValue();
		new2old.put(newQuad, oldQuad);	    
	    }
	    return new2old;
	}

	public String toString() {
	    StringBuffer buff = new StringBuffer(super.toString());
	    buff.append("\nTo stack allocate ");
	    for(Quad cs : allocs) {
		buff.append("\n  ");
		buff.append(Util.code2str(cs));
	    }
	    return buff.toString();
	}
    }

    private static boolean isAlloc(Quad q) {
	return (q instanceof NEW) || (q instanceof ANEW);
    }

    private static AllocationProperties getAP(AllocationInformation ai, Quad q) {
	if(ai != null)
	    return ai.query(q);
	return harpoon.Analysis.DefaultAllocationInformation.SINGLETON.query(q);
    }

}
