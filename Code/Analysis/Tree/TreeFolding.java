// TreeFolding.java, created Mon Apr  5 17:52:42 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.DataFlow.TreeSolver;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.BasicBlockInterf;
import harpoon.Analysis.DataFlow.ForwardDataFlowBasicBlockVisitor;
import harpoon.Analysis.DataFlow.ReversePostOrderIterator;
import harpoon.Analysis.EdgesIterator;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Temp;
import harpoon.Util.BitString;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The <code>TreeFolding</code> class performs tree folding on a 
 * tree code in canonical form.  Tree folding is used to remove 
 * superfluous temp definitions in the tree form.  For example,
 * in the tree expression:
 *
 * <PRE>
 *    SEQ(
 *     MOVE(t1, 5)
 *     CJUMP(t1, L-true, L-false)
 *    )
 * </PRE>
 *    
 * the superfluous temp t1 would be removed, and its use would be replaced
 * with the expression to which t1 was assigned:
 *
 * <PRE>
 *     CJUMP(5, L-true, L-false)
 * </PRE>
 *
 * 
 * The algorithm for tree folding is as follows: 
 *
 * <PRE>
 *   foreach TEMP t do
 *     if ((t has one DEF)             AND 
 *         (DEF(t) has one USE)        AND
 *         (RHS(DEF(t)) is available))
 *       remove DEF(t)
 *       replace t with RHS(DEF(t))
 * </PRE>
 *
 * <b>Issues:</b><p>
 * 
 * At this time, memory writes kill all expressions.  However, this is not
 * really necessary.  Furthermore, the algorithm is not especially efficient
 * either in time or in space.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeFolding.java,v 1.5 2002-04-10 03:02:06 cananian Exp $ 
 * 
 */
public class TreeFolding extends ForwardDataFlowBasicBlockVisitor {
    private       boolean        initialized = false;

    private final int            maxTreeID;
    private final Code           code;
    private final Map            bb2tfi;
    private final Map            DUChains, UDChains;
    private final Map            tempsToPrsvs;
    private final Stm            root;
    private final BasicBlock.Factory bbfactory;

    private CFGrapher grapher;
    private UseDefer  usedefer;

    /** Constructs a new <code>TreeFolding</code> object for the
     *  <code>code</code>.
     *  
     *  <BR> <B>requires:</B> <OL>
     *     <LI> <code>code</code> is a tree codeview in canonical form
     *          (<code>code.isCanonical()</code> should return 
     *          <code>true</code>).  
     *  <BR> <B>modifies:</B> nothing.
     *  <BR> <B>effects:</B> constructs a new
     *	      <code>BasicBlockVisitor</code> and initializes its
     *          internal datasets for analysis of the
     *          <code>BasicBlock</code>s in <code>code</code>.
     *          However, the <code>visit()</code> and <code>merge()</code>
     *          methods should not be called directly.  Rather, the folded
     *          tree code should be extracted by calling the calling the
     *          <code>fold()</code> method. 
     */	     
    public TreeFolding(harpoon.IR.Tree.Code code) { 
	Map tempsToDefs = new HashMap();

	assert code.isCanonical();

	this.code         = code;
	this.root         = (Stm)this.code.getRootElement();
	this.grapher      = code.getGrapher();
	this.usedefer     = code.getUseDefer();
	this.maxTreeID    = TreeSolver.getMaxID(RS(this.root));
	this.bb2tfi       = new HashMap();
	this.DUChains     = new HashMap();
	this.UDChains     = new HashMap();
	this.tempsToPrsvs = new HashMap();
	this.bbfactory    = new BasicBlock.Factory(code, grapher);
	BasicBlock rootbb = bbfactory.getRoot();
	
	initTempsToPrsvs(tempsToPrsvs);
	initTempsToDefs (tempsToDefs);
	
	for (Iterator i = new ReversePostOrderIterator(rootbb);i.hasNext();) { 
	    BasicBlock bb = (BasicBlock)i.next();
	    bb2tfi.put(bb, new TreeFoldingInfo(bb));
	}

	TreeSolver.forward_rpo_solver(rootbb, this);
	computeUseDef(bbfactory, tempsToDefs);

	initialized = true;
    }

    /**
     * Performs the tree-folding optimization on this class's tree code.
     *
     * <BR>  <B>Requires:</B>
     * <BR>  <B>Modifies:</B> this class's tree code
     * <BR>  <B>Effects:</B> Performs the tree-folding optimization on 
     *                       this class's tree code, and returns the resulting
     *                       tree code.  Preserves the <code>CFGraphable</code>
     *                       interface correctly by calling the code's 
     *                       <code>recomputeEdges()</code> method. 
     */
    public harpoon.IR.Tree.Code fold() { 
	Map IDsToTrees;
	initIDsToTrees(IDsToTrees = new HashMap());
	fold(bbfactory.getRoot(), IDsToTrees, this.DUChains, this.UDChains);
	return this.code;
    }


    /** Visit (Transfer) function.  
     *  <pre>  OUT(bb)     = GEN(bb) union (IN(bb) intersect PRSV(bb))  </pre>
     *  <pre>  OUT_mem(bb) = IN_mem(bb) union KILL_mem(bb)  </pre>.
     *
     *  <B>Note:</B> this method should never be called directly, and will 
     *  cause an assertion failure if it is called from outside of the
     *  <code>TreeFolding</code> class.
     */
    public void visit(BasicBlock bb) {
	assert bb!=null;
	assert !initialized;

	//if (DEBUG) db("Visiting: " + bb);
	TreeFoldingInfo info = (TreeFoldingInfo)bb2tfi.get(bb);

	assert info!=null;
	info.outSet[0].clearUpTo(maxTreeID);
	info.outSet[0].or (info.prsvSet[0]);
	info.outSet[0].and(info.inSet[0]);
	info.outSet[0].or (info.genSet[0]);
	info.outSet[1].clearUpTo(maxTreeID);
	info.outSet[1].or(info.prsvSet[1]);
	info.outSet[1].or(info.inSet[1]);
	info.outSet[1].and(info.genSet[1]);
    }
  
    /**
     * Merges operation on the from and to basic block.  Returns true if
     * the to basic block changes.
     *        
     * <BR> <B>NOTE:</B> "changes" above refers to our knowledge about
     * the basic block changing, not the contents of the basic block
     * itself, which shouldn't be modified during Analysis.  Thus, an
     * appropriate "change" would be a variable being added to the
     * IN-set of 'to' during Forward Dataflow Analysis
     */
    public boolean merge(BasicBlockInterf from, BasicBlockInterf to) { 
	BitString        fOUT, fOUT_mem, tIN, tIN_mem;
	TreeFoldingInfo  fInfo, tInfo;
	boolean          result;

	assert !initialized;
	
	//if (DEBUG) db("Merging: " + from + ", " + to);
	
	fInfo     = (TreeFoldingInfo)bb2tfi.get(from);
	tInfo     = (TreeFoldingInfo)bb2tfi.get(to);
	
	fOUT      = fInfo.outSet[0];
	tIN       = tInfo.inSet[0];
	result    = tIN.or_upTo(fOUT, maxTreeID);

	fOUT_mem  = fInfo.outSet[1];
	tIN_mem   = tInfo.inSet[1];
	result    = result || tIN_mem.or_upTo(fOUT_mem, maxTreeID);

	return result;
	
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                            *
     *                 Initialization routines                    *
     *                                                            *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    // Maps tree IDs to Tree objects
    private void initIDsToTrees(Map IDsToTrees) { 
	for (Iterator i = new EdgesIterator(RS(root),grapher); i.hasNext();) { 
	    Stm stm = (Stm)i.next();
	    int ID  = stm.getID();
	    IDsToTrees.put(new Integer(ID), stm);
	}
    }

    // Maps Temps to BitStrings representing the Tree IDs of the Trees 
    // they preserve
    private void initTempsToPrsvs(Map tempsToPrsvs) { 
	for (Iterator i = ((Code.TreeFactory)root.getFactory()).getParent().getElementsI();
	     i.hasNext();) { 
	    Tree u = (Tree)i.next();
	    Temp[] tmps = (u instanceof Stm)?usedefer.def(u):usedefer.use(u);
	    for (int n=0; n<tmps.length; n++) { 
		BitString bs = (BitString)tempsToPrsvs.get(tmps[n]);
		if (bs==null) { 
		    tempsToPrsvs.put(tmps[n], bs=new BitString(maxTreeID+1));
		    bs.setAll();
		}
		bs.clear(u.getID());
	    }		
	}
    }

    // Maps Temps to the Tree IDs of the statements where they are defined
    private void initTempsToDefs(Map tempsToDefs) { 
	for (Iterator i = new EdgesIterator(RS(root),grapher); i.hasNext();) {
	    Stm stm = (Stm)i.next();
	    if (!((stm.kind()==TreeKind.CALL) || 
		  (stm.kind()==TreeKind.NATIVECALL))) { 
		Temp[] defs = usedefer.def(stm);  assert defs.length<=1;
		if (defs.length==1) { 
		    MAP_TO_SET(defs[0], new Integer(stm.getID()), tempsToDefs);
		}
	    }
	}
    }

    // Computes UD and DU chains based on the computed IN and OUT sets
    // FIX: this method could probably be cleaned up substantially
    // 
    private void computeUseDef(BasicBlock.Factory bbf, Map tempsToDefs) { 
	for (Iterator i = bbf.blocksIterator(); i.hasNext();) { 
	    BasicBlock      bb      = (BasicBlock)i.next();
	    TreeFoldingInfo bbInfo  = (TreeFoldingInfo)bb2tfi.get(bb);
	    BitString       bbIn    = (BitString)((bbInfo.inSet[0]).clone());
	    for (Iterator j = bb.statements().listIterator(); j.hasNext();) {
		Stm      stm   = (Stm)j.next();
		Integer  useID = new Integer(stm.getID());
		Temp[]   uses  = usedefer.use(stm);
		for (int n=0; n<uses.length; n++) { 
		    Temp use    = uses[n]; 
		    Set  defIDs = (Set)tempsToDefs.get(use);
		    //assert tempsToDefs.containsKey(use);
		    if (tempsToDefs.containsKey(use)) { 
			for (Iterator k = defIDs.iterator(); k.hasNext();) { 
			    Integer defID = (Integer)k.next();
			    if (bbIn.get(defID.intValue())) { 
				Tuple defT=new Tuple(new Object[]{defID,use});
				Tuple useT=new Tuple(new Object[]{useID,use});
				MAP_TO_SET(useT, defT, UDChains);
				MAP_TO_SET(defT, useT, DUChains);
			    }
			}	   
		    }
		}
		// Update IN set
		Temp[] defs = usedefer.def(stm); 
		assert defs.length<=1 || 
			    stm.kind()==TreeKind.CALL ||
			    stm.kind()==TreeKind.NATIVECALL;
		if (defs.length==1) { 
		    bbIn.and((BitString)tempsToPrsvs.get(defs[0]));
		    bbIn.set(stm.getID());
		}
	    }
	}
    }
    
    // Performs the folding operation, using the suppiled DU and UD chains.
    // FIX: this method could probably be cleaned up substantially
    private void fold(BasicBlock root, Map IDsToTrees, 
		      Map DUChains, Map UDChains) { 
	Map tm = new HashMap();
	BasicBlock bb;
	TreeFoldingInfo tfi;
	BitString dataIn, memIn;
	for (Iterator i = new ReversePostOrderIterator(root); i.hasNext();) { 
	    bb  = (BasicBlock)i.next();
	    tfi = (TreeFoldingInfo)bb2tfi.get(bb);
	    dataIn = (BitString)tfi.inSet[0].clone();
	    memIn = (BitString)tfi.inSet[1].clone();
	    
	    for (Iterator bbi = bb.statements().listIterator(); bbi.hasNext();) { 
		Stm stm = (Stm)bbi.next();
		Temp[] uses = usedefer.use(stm);
		for (int j=0; j<uses.length; j++) { 
		    Tuple useT = 
			new Tuple(new Object[] 
				  { new Integer(stm.getID()), uses[j] });
		    Set D = (Set)UDChains.get(useT);
		    // Is there only one DEF for this USE?
		    if (D!=null && D.size()==1) { 
			Tuple defT = (Tuple)(D.iterator().next()); 
			Set U = (Set)DUChains.get(defT);
			// is there only one USE for this DEF?
			if (U.size()==1) { 
			    //assert U contains use;
			    assert IDsToTrees.containsKey(defT.proj(0));
			    
			    // Is memory valid here? 
			    if (!memIn.get
				(((Integer)defT.proj(0)).intValue())) { 
				// Is the expression we want available?
				if (dataIn.get
				    (((Integer)defT.proj(0)).intValue())) { 
				    Stm DStm = 
					(Stm)(IDsToTrees.get(defT.proj(0)));
				    DStm = (Stm)GET_TREE(tm, DStm);
				    code.remove(DStm);
				    Stm foldedStm = 
					stm.build
					(replace(((MOVE)DStm).getSrc(), 
						 GET_TREE(tm, stm).kids(), 
						 uses[j]));
				    GET_TREE(tm, stm).replace(foldedStm);
				    MAP_TREE(tm, GET_TREE(tm, stm), foldedStm);
				}
			    }
			}
		    }
		}
		if (mayWriteMem(stm)) 
		    memIn.setAll();
		else if (stm.kind()==TreeKind.MOVE) { 
		    dataIn.and
			((BitString)tempsToPrsvs.get(usedefer.def(stm)[0]));
		    dataIn.set(stm.getID());
		    memIn.clear(stm.getID()); 
		}
	    }
	}
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Utility functions                      *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
    
    private void MAP_TREE(Map table, Tree key, Tree value) { 
	table.put(key, value);
    }
    
    private Tree GET_TREE(Map table, Tree key) {
	while (table.containsKey(key)) key = (Tree)table.get(key);
	return key;
    }

    private void MAP_TO_SET(Object key, Object value, Map map) { 
	Set set;
	if (!map.containsKey(key)) { 
	    set = new HashSet();
	    map.put(key, set);
	}
	else { 
	    set = (Set)map.get(key);
	}
	set.add(value);
    }

    
    private Stm RS(Stm s) { 
	try { while (true) s = ((SEQ)s).getLeft(); }
	catch (ClassCastException e) { return s; }
    }

    private ExpList replace(Exp src, ExpList use, Temp tUse) { 
	if (use==null||use.head==null) return null;
	else { 
	    if (use.head.kind()==TreeKind.TEMP) { 
		TEMP temp = (TEMP)use.head;
		// Only replaces one instance of the temp
		if (temp.temp==tUse) return new ExpList(src, use.tail);
		else return new ExpList(temp, replace(src, use.tail, tUse));
	    }
	    else return new ExpList
		     (use.head.build(replace(src, use.head.kids(), tUse)),
		      replace(src, use.tail, tUse));
	}
    }
    
    // TreeFoldingInfo is a record type grouping together four sets: 
    class TreeFoldingInfo { 
	final BitString[] genSet   = new BitString[2];  
	final BitString[] prsvSet  = new BitString[2];
	final BitString[] inSet    = new BitString[2];
	final BitString[] outSet   = new BitString[2];
	
	TreeFoldingInfo(BasicBlock bb) {
	    this.genSet[0]  = new BitString(maxTreeID+1);
	    this.genSet[1]  = new BitString(maxTreeID+1);
	    this.inSet[0]   = new BitString(maxTreeID+1);
	    this.inSet[1]   = new BitString(maxTreeID+1);
	    this.outSet[0]  = new BitString(maxTreeID+1);
	    this.outSet[1]  = new BitString(maxTreeID+1);
	    this.prsvSet[0] = new BitString(maxTreeID+1);
	    this.prsvSet[1] = new BitString(maxTreeID+1);
	    
	    computeGenPrsvSets(bb);
	}
	
	// Initializes the GEN and PRSV sets of this TreeFoldingInfo
	// This method should be called just once after the object is 
	// created.
	private void computeGenPrsvSets(BasicBlock bb) {
	    prsvSet[0].setAll();
	    prsvSet[1].setAll();
	    genSet[1].setAll();
	    for (Iterator i = bb.statements().listIterator(); i.hasNext();) { 
		Stm stm = (Stm)i.next();
		if (mayWriteMem(stm)) { 
		    prsvSet[1].setAll();
		}
		else { 
		    if (stm.kind()==TreeKind.MOVE) { 
			prsvSet[0].and
			    ((BitString)tempsToPrsvs.get(usedefer.def(stm)[0]));
			genSet[0].set(stm.getID());
			genSet[1].clear(stm.getID());
		    }
		}
	    }
	}
		
	public String toString() {
	    StringBuffer s = new StringBuffer();
	    s.append(   "tGen  set: "+genSet[0].toString());
	    s.append("\n\tPrsv set: "+prsvSet[0].toString());
	    s.append("\n\tIn   set: "+inSet[0].toString());
	    s.append("\n\tOut  set: "+outSet[0].toString()+"\n");
	    return s.toString();
	}
    }

    

    // Returns true if the parameter is a statement that might perform
    // a memory write. 
    private static boolean mayWriteMem(Stm s) { 
	int s_kind = s.kind();
	return 
	    (s_kind==TreeKind.CALL) ||
	    (s_kind==TreeKind.NATIVECALL) ||
	    ((s_kind==TreeKind.MOVE) && 
	     ((((MOVE)s).getDst()).kind()==TreeKind.MEM)); 
    }
}



