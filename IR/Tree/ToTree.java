// ToTree.java, created Tue Feb 16 16:46:36 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.AllocationInformationMap.AllocationPropertiesImpl;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.Analysis.SSxReachingDefsImpl;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadNoSSA;
import harpoon.IR.LowQuad.LowQuadSSA;
import harpoon.IR.LowQuad.LowQuadSSI;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.LQop;
import harpoon.IR.LowQuad.PAOFFSET;
import harpoon.IR.LowQuad.PARRAY;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PCONST;
import harpoon.IR.LowQuad.PFCONST;
import harpoon.IR.LowQuad.PFIELD;
import harpoon.IR.LowQuad.PFOFFSET;
import harpoon.IR.LowQuad.PGET;
import harpoon.IR.LowQuad.PMCONST;
import harpoon.IR.LowQuad.PMETHOD;
import harpoon.IR.LowQuad.PMOFFSET;
import harpoon.IR.LowQuad.POPER;
import harpoon.IR.LowQuad.PPTR;
import harpoon.IR.LowQuad.PSET;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.Default;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

/**
 * The ToTree class is used to translate low-quad code to tree code.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ToTree.java,v 1.1.2.91 2001-01-13 21:45:57 cananian Exp $
 */
class ToTree {
    private Tree        m_tree;
    private DerivationGenerator m_dg = new DerivationGenerator();
    // turning this option on enables assertion checking every dispatch
    // to ensure the target method is not null (which happens when the
    // method has been proven to be uncallable by the classhierarchy)
    private static boolean checkDispatch =
	!System.getProperty("harpoon.check.dispatch", "no")
	.equalsIgnoreCase("no");
   
    /** Class constructor.  Uses the default <code>EdgeOracle</code>
     *  and <code>ReachingDefs</code> for <code>LowQuadNoSSA</code>. */
    public ToTree(final TreeFactory tf, LowQuadNoSSA code) {
	this(tf, code,
	     code.getAllocationInformation(),
	     new ToTreeHelpers.DefaultEdgeOracle(),
	     new ToTreeHelpers.DefaultFoldNanny(),
	     new ReachingDefsImpl(code));
    }
    public ToTree(final TreeFactory tf, final LowQuadSSA code) {
	this(tf, code,
	     code.getAllocationInformation(),
	     new ToTreeHelpers.MinMaxEdgeOracle(code),
	     new ToTreeHelpers.SSXSimpleFoldNanny(code),
	     new SSxReachingDefsImpl(code));
    }
    public ToTree(final TreeFactory tf, final LowQuadSSI code) {
	this(tf, code,
	     code.getAllocationInformation(),
	     new ToTreeHelpers.MinMaxEdgeOracle(code),
	     new ToTreeHelpers.SSXSimpleFoldNanny(code),
	     new SSxReachingDefsImpl(code));
    }
    /** Class constructor. */
    public ToTree(final TreeFactory tf, harpoon.IR.LowQuad.Code code,
		  AllocationInformation ai,
		  EdgeOracle eo, FoldNanny fn, ReachingDefs rd) {
	Util.assert(((Code.TreeFactory)tf).getParent()
		    .getName().equals("tree"));
	if (ai==null)
	    ai = harpoon.Analysis.DefaultAllocationInformation.SINGLETON;
	translate(tf, code, ai, eo, fn, rd);
    }
    
    /** Returns a <code>TreeDerivation</code> object for the
     *  generated <code>Tree</code> form. */
    public TreeDerivation getTreeDerivation() { return m_dg; }

    /** Returns the root of the generated tree code */
    public Tree getTree() {
	return m_tree;
    }

    private void translate(TreeFactory tf, harpoon.IR.LowQuad.Code code,
			   AllocationInformation ai,
			   EdgeOracle eo, FoldNanny fn, ReachingDefs rd) {

	Quad root = (Quad)code.getRootElement();
	TempMap ctm = new CloningTempMap
	    (root.getFactory().tempFactory(),tf.tempFactory());

	// Construct a list of harpoon.IR.Tree.Stm objects
	TranslationVisitor tv = new TranslationVisitor
	    (tf, rd, code.getDerivation(), ai, eo, fn, m_dg, ctm);
						       
	// traverse, starting with the METHOD quad.
	dfsTraverse((harpoon.IR.Quads.METHOD)root.next(1), 0,
		    tv, new HashSet());

	// Assign member variables
	m_tree       = ((TranslationVisitor)tv).getTree();
    }
    
    // Translates the Quad graph in a depth-first order.
    private void dfsTraverse(Quad q, int which_pred,
			     TranslationVisitor tv, Set visited) {
	// if this is a phi function, translate by emitting appropriate MOVEs
	if (q instanceof harpoon.IR.Quads.PHI)
	    tv.emitPhiFixup((harpoon.IR.Quads.PHI)q, which_pred);
	// if we've already translated this quad, goto the translation.
	if (visited.contains(q)) {
	    tv.emitGoto(tv.label(q), /*for line # info:*/q.prev(which_pred));
	    return;
	} else visited.add(q);
	// label phis.
	if (q instanceof harpoon.IR.Quads.PHI)
	    tv.emitLabel(tv.label(q), /* for line # info:*/q);
	// translate this instruction.
	q.accept(tv);
	// translate successors.
	int n = q.nextLength();
	int def = tv.edgeOracle.defaultEdge(q);
	for (int i=0; i<n; i++) {
	    // permute edges such that default always comes first.
	    //  think:  0 1[2]3 4  (if 2 is the default)
	    //         [2]0 1 3 4
	    Edge edge = q.nextEdge((i==0)?def:(i<=def)?i-1:i);
	    if (q instanceof harpoon.IR.Quads.SIGMA) {
		// label sigma outputs, and emit proper fixup code
		tv.emitLabel(tv.label(edge), /*for line # info:*/q);
		tv.emitSigmaFixup((harpoon.IR.Quads.SIGMA)q,
				  edge.which_succ());
	    }
	    // recurse.
	    if (!(edge.to() instanceof harpoon.IR.Quads.FOOTER))
		dfsTraverse((Quad)edge.to(), edge.which_pred(), tv, visited);
	}
	// done, yay.
    }

    // don't close class: all of the following are inner classes,
    // even if they don't look that way.  I'm just don't feel like
    // reindenting all of this existing code. [CSA]
  
// Translates the LowQuadNoSSA code into tree form. 
//
static class TranslationVisitor extends LowQuadVisitor {
    private final TempMap     m_ctm;          // Clones Temps to new tf
    private final NameMap     m_nm;           // Runtime-specific label naming
    private final List        m_stmList;      // Holds translated statements
    private final TreeFactory m_tf;           // The new TreeFactory
    private Temp              m_handler = null; 
    private final Runtime.TreeBuilder m_rtb;

    private final Derivation quadDeriv;
    private final DerivationGenerator treeDeriv;
    public final AllocationInformation allocInfo;
    public final EdgeOracle edgeOracle;
    public final FoldNanny  foldNanny;
    public final ReachingDefs reachingDefs;

    public TranslationVisitor(TreeFactory tf,
			      ReachingDefs reachingDefs,
			      Derivation quadDeriv,
			      AllocationInformation allocInfo,
			      EdgeOracle edgeOracle,
			      FoldNanny foldNanny,
			      DerivationGenerator treeDeriv,
			      TempMap ctm) {
	m_ctm          = ctm;
	m_tf           = tf; 
	m_nm           = m_tf.getFrame().getRuntime().nameMap;
	m_stmList      = new ArrayList();
	m_rtb	       = m_tf.getFrame().getRuntime().treeBuilder;
	this.quadDeriv      = quadDeriv;
	this.treeDeriv	    = treeDeriv;
	this.allocInfo	    = allocInfo;
	this.edgeOracle     = edgeOracle;
	this.foldNanny      = foldNanny;
	this.reachingDefs   = reachingDefs;
    }

    Tree getTree() { return Stm.toStm(m_stmList); } 

    // label maker ----------------
    private final Map labelMap = new HashMap() {
	public Object get(Object key) {
	    if (!containsKey(key)) { put(key, new Label()); }
	    return super.get(key);
	}
    };
    public Label label(Quad q) {
	Util.assert(q instanceof harpoon.IR.Quads.PHI);
	return (Label) labelMap.get(q);
    }
    public Label label(Edge e) {
	Util.assert(e.from() instanceof harpoon.IR.Quads.SIGMA);
	return (Label) labelMap.get(e);
    }
    // end label maker --------------

    // labels and phis and sigmas, oh my! ------------
    public void emitGoto(Label target, HCodeElement src) {
	addStmt(new JUMP(m_tf, src, target));
    }
    public void emitLabel(Label label, HCodeElement src) {
	addStmt(new LABEL(m_tf, src, label, false));
    }
    public void emitPhiFixup(harpoon.IR.Quads.PHI q, int which_pred) {
	for (int i=0; i<q.numPhis(); i++)
	    addMove(q, q.dst(i), _TEMPte(q.src(i, which_pred), q));
    }
    public void emitSigmaFixup(harpoon.IR.Quads.SIGMA q, int which_succ) {
	for (int i=0; i<q.numSigmas(); i++)
	    addMove(q, q.dst(i, which_succ), _TEMPte(q.src(i), q));
    }
    // end labels and phis and sigmas, oh my! --------

    public void visit(Quad q) { Util.assert(false); /* not handled! */ }

    public void visit(harpoon.IR.Quads.ALENGTH q) {
	addMove
	    (q, q.dst(),
	     m_rtb.arrayLength
	     (m_tf, q, treeDeriv, _TEMPte(q.objectref(), q))
	     );
    }

    public void visit(harpoon.IR.Quads.ANEW q) {
	// create and zero fill a multi-dimensional array.
	int dl = q.dimsLength();
	Util.assert(dl>0);
	// temps to hold each part of the array;
	// arrayTemps[i] holds an (dl-i)-dimensional array.
	// arrayClasses[i] is the type of arrayTemps[i]
	Temp[] arrayTemps = new Temp[dl+1];
	HClass[] arrayClasses = new HClass[dl+1];
	arrayTemps[0] = m_ctm.tempMap(q.dst());
	arrayClasses[0] = q.hclass();
	for (int i=1; i<=dl; i++) {
	    arrayTemps[i] = new Temp(arrayTemps[i-1]);
	    arrayClasses[i] = arrayClasses[i-1].getComponentType();
	    Util.assert(arrayClasses[i]!=null);
	}
	// temps standing for size of each dimension.
	Temp[] dimTemps = new Temp[dl];
	for (int i=0; i<dl; i++) {
	    dimTemps[i] = m_ctm.tempMap(q.dims(i));
	    // move (possibly folded) values into dimTemps, as we will
	    // be evaluating the dimensions multiple times.
	    addStmt(new MOVE(m_tf, q,
			     _TEMP(q, HClass.Int, dimTemps[i]),
			     _TEMP(q.dims(i), q)));
	}
	// temps used to index each dimension
	Temp[] indexTemps = new Temp[dl];
	for (int i=0; i<dl; i++)
	    indexTemps[i] = new Temp(m_tf.tempFactory(), "idx");
	// labels for loop top, test, and end.
	Label[] testLabel = new Label[dl];
	Label[] loopLabel = new Label[dl];
	Label[] doneLabel = new Label[dl];
	for (int i=0; i<dl; i++) {
	    testLabel[i] = new Label();
	    loopLabel[i] = new Label();
	    doneLabel[i] = new Label();
	}
	// okay.  Now do the translation:
	//  d1 = new array(ilen);
	//  for (i=0; i<ilen; i++) {
	//    d2 = d1[i] = new array(jlen);
	//    for (j=0; j<jlen; j++) {
	//      d2[i] = 0;
	//    }
	//  }
	for (int i=0; i<=dl; i++) { // write the loop tops.
	    Exp initializer;
	    if (i==dl) // bottom out with elements set to zero/null.
		initializer = constZero(q, arrayClasses[i]);
	    else
		initializer = m_rtb.arrayNew
		    (m_tf, q, treeDeriv,
		     MAP(allocInfo.query(q)),
		     arrayClasses[i],
		     new Translation.Ex
		     (_TEMP(q, HClass.Int, dimTemps[i]))).unEx(m_tf);
	    // output: d1[i] = d2 = initializer.
	    Stm s1 = new MOVE // d2 = initializer
		(m_tf, q,
		 _TEMP(q, arrayClasses[i], arrayTemps[i]),
		 initializer);
	    if (i>0) // suppress the "d1[i]" part for outermost
		s1 = new MOVE
		    (m_tf, q,
		     makeMEM // d1[i] = ...
		     (q, arrayClasses[i],
		      new BINOP
		      (m_tf, q, Type.POINTER, Bop.ADD,
		       m_rtb.arrayBase
		       (m_tf, q, treeDeriv,
			new Translation.Ex
			(_TEMP(q, arrayClasses[i-1], arrayTemps[i-1])))
		       .unEx(m_tf),
		       m_rtb.arrayOffset
		       (m_tf, q, treeDeriv, arrayClasses[i-1],
			new Translation.Ex
			(_TEMP(q, HClass.Int, indexTemps[i-1])))
			.unEx(m_tf)
			)),
		     new ESEQ // ... (d2 = initializer)
		     (m_tf, q, s1,
		      _TEMP(q, arrayClasses[i], arrayTemps[i])));
		      
	    addStmt(s1);
	    // for (i=0; i<ilen; i++) ...
	    if (i<dl) { // skip loop for innermost
		addStmt(new MOVE // i=0.
			(m_tf, q,
			 _TEMP(q, HClass.Int, indexTemps[i]),
			 new CONST(m_tf, q, 0)));
		addStmt(new JUMP(m_tf, q, testLabel[i]));
		addStmt(new LABEL(m_tf, q, loopLabel[i], false));
	    }
	}
	// okay, write the loop bottoms in reverse order.
	for (int i=dl-1; i>=0; i--) {
	    // increment the loop counter
	    addStmt(new MOVE // i++
		    (m_tf, q,
		     _TEMP(q, HClass.Int, indexTemps[i]),
		     new BINOP(m_tf, q, Type.INT, Bop.ADD,
			       _TEMP(q, HClass.Int, indexTemps[i]),
			       new CONST(m_tf, q, 1))));
	    // loop test label
	    addStmt(new LABEL(m_tf, q, testLabel[i], false));
	    // test and branch: if i<ilen goto LOOP else goto DONE;
	    addStmt(new CJUMP
		    (m_tf, q,
		     new BINOP(m_tf, q, Type.INT, Bop.CMPLT,
			       _TEMP(q, HClass.Int, indexTemps[i]),
			       _TEMP(q, HClass.Int, dimTemps[i])),
		     loopLabel[i]/*iftrue*/, doneLabel[i]/*iffalse*/));
	    addStmt(new LABEL(m_tf, q, doneLabel[i], false));
	}
	// ta-da!
	// add bogus move to placate folding code
	addMove(q, q.dst(), _TEMP(q, arrayClasses[0], arrayTemps[0]));
    }

    public void visit(harpoon.IR.Quads.ARRAYINIT q) {
	HClass arrayType = HClassUtil.arrayClass(q.getFactory().getLinker(),
						 q.type(), 1);
	Stm s0, s1, s2;

	// Create a pointer which we'll use to initialize the array
	Temp  nextPtr = new Temp(m_tf.tempFactory(), "nxt");
	// make sure base pointer lives in a treetemp. (may be folded instead)
	Temp   objTemp = m_ctm.tempMap(q.objectref());
	addStmt(new MOVE(m_tf, q, 
			 _TEMP(q, arrayType, objTemp), 
			 _TEMP(q.objectref(), q)));
	// Create derivation information for the new TEMP
	DList dl = new DList(objTemp, true, null);

	// set nextPtr to point to arrayBase + q.offset() * size.
	s0 = new MOVE
	    (m_tf, q, 
	     _TEMP(q, dl, nextPtr),
	     new BINOP
	     (m_tf, q, Type.POINTER, Bop.ADD,
	      m_rtb.arrayBase
	      (m_tf, q, treeDeriv,
	       new Translation.Ex(_TEMP(q, arrayType, objTemp)))
	      .unEx(m_tf),
	      m_rtb.arrayOffset
	      (m_tf, q, treeDeriv, arrayType,
	       new Translation.Ex(new CONST(m_tf, q, q.offset()))).unEx(m_tf)
	      ));

	addStmt(s0);
    
	Object[] qvalue = q.value();
	if (qvalue.length <= 5 /* magic number */ || !q.type().isPrimitive()) {
	    // explicit element-by-element initialization
	    for (int i=0; i<qvalue.length; i++) {
		Exp c = mapconst(q, qvalue[i], q.type()).unEx(m_tf);
		MEM m = makeMEM(q, q.type(), _TEMP(q, dl, nextPtr));
		s0 = new MOVE(m_tf, q, m, c);
		s1 = new MOVE
		    (m_tf, q, 
		     _TEMP(q, dl, nextPtr), 
		     new BINOP
		     (m_tf, q, Type.POINTER, Bop.ADD, 
		      _TEMP(q, dl, nextPtr), 
		      m_rtb.arrayOffset
		      (m_tf, q, treeDeriv, arrayType,
		       new Translation.Ex(new CONST(m_tf, q, 1))).unEx(m_tf)
		      ));
		
		addStmt(new SEQ(m_tf, q, s0, s1));
	    }
	} else {
	    // initialize array from a constant table.

	    // create labels for our constant table
	    Label constTblStart = new Label(), constTblEnd = new Label();
	    // and labels for loop
	    Label looptop=new Label(),looptst=new Label(),loopend=new Label();
	    // create a pointer into our constant table...
	    Temp constPtr = new Temp(m_tf.tempFactory(), "cnst");
	    // for (constPtr=constTblStart; constPtr < constTblEnd; constPtr++)
	    // loop header:
	    addStmt(new MOVE
		    (m_tf, q,
		     _TEMP(q, HClass.Void, constPtr),
		     new NAME(m_tf, q, constTblStart)));
	    addStmt(new JUMP(m_tf, q, looptst));
	    addStmt(new LABEL(m_tf, q, looptop, false));
	    // loop body: { *nextptr = *constPtr; }
	    addStmt(new MOVE
		    (m_tf, q,
		     makeMEM(q, q.type(), _TEMP(q, dl, nextPtr)),
		     makeMEM(q, q.type(), _TEMP(q, HClass.Void, constPtr))));
	    // loop increment:
	    addStmt(new MOVE
		    (m_tf, q,
		     _TEMP(q, dl, nextPtr),
		     new BINOP
		     (m_tf, q, Type.POINTER, Bop.ADD,
		      _TEMP(q, dl, nextPtr),
		      m_rtb.arrayOffset
		      (m_tf, q, treeDeriv, arrayType,
		       new Translation.Ex(new CONST(m_tf, q, 1))).unEx(m_tf)
		      )));
	    addStmt(new MOVE
		    (m_tf, q,
		     _TEMP(q, HClass.Void, constPtr),
		     new BINOP
		     (m_tf, q, Type.POINTER, Bop.ADD,
		      _TEMP(q, HClass.Void, constPtr),
		      new CONST(m_tf, q, sizeof(q.type())))));
	    // loop test:
	    addStmt(new LABEL(m_tf, q, looptst, false));
	    addStmt(new CJUMP
		    (m_tf, q,
		     new BINOP
		     (m_tf, q, Type.POINTER, Bop.CMPLE,
		      _TEMP(q, HClass.Void, constPtr),
		      new NAME(m_tf, q, constTblEnd)),
		     looptop/*iftrue*/, loopend/*iffalse*/));
	    // okay, now output constant data table.
	    addStmt(new ALIGN(m_tf, q, sizeof(q.type())));
	    addStmt(new LABEL(m_tf, q, constTblStart, false));
	    Util.assert(qvalue.length>0);
	    for (int i=0; i<qvalue.length; i++) {
		if (i==qvalue.length-1)
		    addStmt(new LABEL(m_tf, q, constTblEnd, false));
		addStmt(new DATUM(m_tf, q, _CONST(q, q.type(), qvalue[i])));
	    }
	    // and jump back here when loop's done.
	    addStmt(new ALIGN(m_tf, q, 8/* safe value for alignment */));
	    addStmt(new LABEL(m_tf, q, loopend, false));
	}
	// done.
    }

    public void visit(harpoon.IR.Quads.CJMP q) { 
	addStmt(_TEMPte(q.test(), q).unCx
		(m_tf, label(q.nextEdge(1)), label(q.nextEdge(0))));
    }
  
    public void visit(harpoon.IR.Quads.COMPONENTOF q) {
	addMove(q, q.dst(),
		m_rtb.componentOf(m_tf, q, treeDeriv,
				  _TEMPte(q.arrayref(), q),
				  _TEMPte(q.objectref(), q)));
    }

    public void visit(harpoon.IR.Quads.CONST q) {
	addMove(q, q.dst(), mapconst(q, q.value(), q.type()));
    }
  
    public void visit(harpoon.IR.Quads.INSTANCEOF q) {
	addMove
	    (q, q.dst(),
	     m_rtb.instanceOf(m_tf, q, treeDeriv,
			      _TEMPte(q.src(), q), q.hclass()));
    }

    public void visit(harpoon.IR.Quads.METHOD q) {
	METHOD method; SEGMENT segment;
	Temp   paramsQ[]  = q.params(); // quad temps
	Temp   paramsT[] = new Temp[paramsQ.length]; // tree temps
	TEMP   mParams[] = new TEMP[paramsQ.length+1];
	
	// compute types of TEMPs
	HMethod hm = q.getFactory().getMethod();
	List types = new ArrayList(Arrays.asList(hm.getParameterTypes()));
	if (!q.isStatic()) types.add(0, hm.getDeclaringClass());
	HClass paramType[] = (HClass[])types.toArray(new HClass[types.size()]);
	// make tree temps for quad temps
	for (int i=0; i<paramsQ.length; i++)
	    paramsT[i] = m_ctm.tempMap(paramsQ[i]);

	segment = new SEGMENT(m_tf, q, SEGMENT.CODE);
	for (int i=0; i<paramsT.length; i++) { 
	    mParams[i+1] = _TEMP(q, paramType[i], paramsT[i]);
	}
	Util.assert(m_handler==null);
	m_handler = new Temp(m_tf.tempFactory(), "handler");
	mParams[0] = _TEMP(q, HClass.Void, m_handler);
	int rettype = (hm.getReturnType()==HClass.Void) ? -1 :
	    TYPE(hm.getReturnType());
	method    = new METHOD(m_tf, q, m_nm.label(hm), rettype, mParams);
	addStmt(segment);
	addStmt(method);
	// deal with possible folding
	for (int i=0; i<paramsQ.length; i++)
	    addMove(q, paramsQ[i], _TEMP(q, paramType[i], paramsT[i]));
    }

    public void visit(harpoon.IR.Quads.MONITORENTER q) {
	addStmt(m_rtb.monitorEnter(m_tf, q, treeDeriv, _TEMPte(q.lock(), q))
		     .unNx(m_tf));
    }

    public void visit(harpoon.IR.Quads.MONITOREXIT q) {
	addStmt(m_rtb.monitorExit(m_tf, q, treeDeriv, _TEMPte(q.lock(), q))
		     .unNx(m_tf));
    }

    public void visit(harpoon.IR.Quads.MOVE q) {
	addMove(q, q.dst(), _TEMPte(q.src(), q));
    }

    public void visit(harpoon.IR.Quads.NEW q) { 
	addMove(q, q.dst(),
		m_rtb.objectNew(m_tf, q, treeDeriv, MAP(allocInfo.query(q)),
				q.hclass(), true));
    }
	
    public void visit(harpoon.IR.Quads.PHI q) {
	// do nothing!
    }

    public void visit(harpoon.IR.Quads.RETURN q) {
	Exp retval;
    
	if (q.retval()==null) {
	    retval = new CONST(m_tf, q, 0);
	}
	else {
	    retval = _TEMP(q.retval(), q);
	}

	Stm s0 = new RETURN(m_tf, q, retval);    
	addStmt(s0);
    }

    public void visit(final harpoon.IR.Quads.SWITCH q) { 
	// move (possibly folded) discriminant into Temp, since we'll be
	// evaluating it multiple times.
	final Temp index = m_ctm.tempMap(q.index());
	addStmt(new MOVE(m_tf, q,
			 _TEMP(q, HClass.Int, index),
			 _TEMP(q.index(), q)));
	// sort keys by inserting into TreeMap (n ln n time)
	final SortedMap cases = new TreeMap();
	for (int i=0; i<q.keysLength(); i++) {
	    Object chk=cases.put(new Integer(q.keys(i)), label(q.nextEdge(i)));
	    Util.assert(chk==null, "duplicate key in switch statement!");
	}
	final Label deflabel = label(q.nextEdge(q.keysLength()));
	// bail out of zero-key case.
	if (cases.size()==0) {
	    addStmt(new JUMP(m_tf, q, deflabel));
	    return;
	}
	// select translation depending on size and sparsity of the keys array.
	// (note that we now know that keysLength() is greater than 0)
	int min_key = ((Integer)cases.firstKey()).intValue();
	int max_key = ((Integer)cases.lastKey()).intValue();
	double sparsity = ((double)max_key - min_key) / q.keysLength();

	// SWITCH TRANSLATIONS:

	// for small numbers of keys, it's most efficient to test each.
	if ( (q.keysLength() > 5/* this is a magic number*/) &&
	     (sparsity < 3/* oh, look! another magic number! */)) {
	    // DIRECT JUMP TABLE
	    // first check if < min or > max (this means default!)
	    Label l1 = new Label(), l2 = new Label(), l3 = new Label();
	    addStmt(new CJUMP
		    (m_tf, q, new BINOP(m_tf, q, Type.INT, Bop.CMPLT,
					_TEMP(q, HClass.Int, index),
					new CONST(m_tf, q, min_key)),
		     deflabel, l1));
	    addStmt(new LABEL(m_tf, q, l1, false));
	    addStmt(new CJUMP
		    (m_tf, q, new BINOP(m_tf, q, Type.INT, Bop.CMPGT,
					_TEMP(q, HClass.Int, index),
					new CONST(m_tf, q, max_key)),
		     deflabel, l2));
	    addStmt(new LABEL(m_tf, q, l2, false));
	    // construct LabelList of possible targets
	    LabelList targets = new LabelList(deflabel, null);
	    for (Iterator it=cases.values().iterator(); it.hasNext(); )
		targets = new LabelList((Label)it.next(), targets);
	    // okay, do the jump!
	    boolean pointersAreLong = Type.isDoubleWord(m_tf, Type.POINTER);
	    addStmt(new JUMP
		    (m_tf, q,
		     makeMEM
		     (q, HClass.Void,
		      new BINOP(m_tf, q, Type.POINTER, Bop.ADD,
				new NAME(m_tf, q, l3),
				new BINOP
				(m_tf, q, Type.INT, Bop.SHL,
				 new BINOP(m_tf, q, Type.INT, Bop.ADD,
					   _TEMP(q, HClass.Int, index),
					   new CONST(m_tf, q, -min_key)),
				 new CONST(m_tf, q, pointersAreLong?3:2)))),
		     targets));
	    // and lastly, emit the jump table.
	    addStmt(new ALIGN(m_tf, q, 8/* safe alignment*/));
	    addStmt(new LABEL(m_tf, q, l3, false));
	    int expected=min_key;
	    for (Iterator it=cases.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry thiscase = (Map.Entry) it.next();
		int thiskey = ((Integer)thiscase.getKey()).intValue();
		Label thislabel = (Label)thiscase.getValue();
		while (expected++ < thiskey)
		    addStmt(new DATUM(m_tf, q, new NAME(m_tf, q, deflabel)));
		addStmt(new DATUM(m_tf, q, new NAME(m_tf, q, thislabel)));
	    }
	    // align for code again.
	    addStmt(new ALIGN(m_tf, q, 8/* safe alignment*/));
	    // done
	} else {
	    // BINARY-SEARCH THROUGH JUMP TABLE
	    // this is used for sparse or small switch statements.
	    class SwitchState { // class to help out our recursion.
		private final Map.Entry[] keya = (Map.Entry[])
		    cases.entrySet().toArray(new Map.Entry[cases.size()]);
		int key(int i) {return ((Integer)keya[i].getKey()).intValue();}
		Label label(int i) { return (Label)keya[i].getValue(); }

		private void emit(int low, int high) {
		    if (high-low < 2/*what did we say about magic numbers?*/) {
			// emit simple equality checks for "small" ranges.
			for (int i=low; i<=high; i++) {
			    Label lNext = (i!=high) ? new Label() : deflabel;
			    addStmt(new CJUMP
				    (m_tf, q,
				     new BINOP
				     (m_tf, q, Type.INT, Bop.CMPEQ,
				      _TEMP(q, HClass.Int, index),
				      new CONST(m_tf, q, key(i))),
				     label(i), lNext));
			    if (i!=high)
				addStmt(new LABEL(m_tf, q, lNext, false));
			}
		    } else {
			// divide and conquer for "larger" ranges.
			int mid = (high+low)/2;
			// emit equality test against keys(mid)
			Label lNext = new Label();
			addStmt(new CJUMP
				(m_tf, q,
				 new BINOP
				 (m_tf, q, Type.INT, Bop.CMPEQ,
				  _TEMP(q, HClass.Int, index),
				  new CONST(m_tf, q, key(mid))),
				 label(mid), lNext));
			addStmt(new LABEL(m_tf, q, lNext, false));
			// apply pigeonhole principle:
			boolean gtP = pigeonhole(mid+1, high);
			Label gtL = gtP ? label(high) : new Label();
			boolean ltP = pigeonhole(low, mid-1);
			Label ltL = ltP ? label(low) : new Label();
			// test lower than key
			addStmt(new CJUMP
				(m_tf, q,
				 new BINOP
				 (m_tf, q, Type.INT, Bop.CMPLT,
				  _TEMP(q, HClass.Int, index),
				  new CONST(m_tf, q, key(mid))),
				 /*< and > labels*/ltL, gtL));
			// greater-than case
			if (!gtP) { //(sometimes we can skip this)
			    addStmt(new LABEL(m_tf, q, gtL, false));
			    emit(mid+1, high); // i love recursion.
			}
			// less-than case
			if (!ltP) { //(sometimes we can skip this)
			    addStmt(new LABEL(m_tf, q, ltL, false));
			    emit(low, mid-1); // really, i do!
			}
			// done!
		    }
		}
		boolean pigeonhole(int low, int high) {
		    // sometimes we can skip a test due to pigeonholing.
		    return (low==high) && (low>0) && (high < keya.length-1) &&
			/*pigeonhole:*/((key(high+1) - key(low-1)) == 2);
		}
	    }
	    // use helper to recurse, emitting the test-and-branch chains.
	    new SwitchState().emit(0, cases.size()-1);
	}
    }
  
    public void visit(harpoon.IR.Quads.THROW q) { 
	Util.assert(m_handler!=null);
	addStmt(new THROW(m_tf, q,
			  _TEMP(q.throwable(), q),
			  _TEMP(q, HClass.Void, m_handler)));
    }

    public void visit(harpoon.IR.Quads.TYPECAST q) {
	throw new Error("Use INSTANCEOF instead of TYPECAST");
    }

    public void visit(harpoon.IR.Quads.TYPESWITCH q) {
	throw new Error("Direct translation of TYPESWITCH is unimplemented."+
			" Use TypeSwitchRemover class.");
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   LowQuad Translator                     *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		  
    public void visit(PAOFFSET q) {
	addMove
	    (q, q.dst(),
	     m_rtb.arrayOffset(m_tf, q, treeDeriv, q.arrayType(),
			       _TEMPte(q.index(), q)));
    }

    public void visit(PARRAY q) {
	addMove
	    (q, q.dst(),
	     m_rtb.arrayBase(m_tf, q, treeDeriv,
			     _TEMPte(q.objectref(), q)));
    }


    // runtime-independent
    public void visit(PCALL q) { 
	ExpList params; Temp[] qParams;
	Temp retval, retex; // tree temps for destination variables
	TEMP retvalT, retexT; // TEMP expressions for the above.
	Exp func, ptr;

	Util.assert(q.retex()!=null && q.ptr()!=null);

	// If q.retval() is null, the 'retval' in Tree.CALL is also null.
	if (q.retval()==null) {
	    retval = null;
	    retvalT = null;
	}
	else {
	    retval = m_ctm.tempMap(q.retval()); // a tree temp.
	    // (return value should never have a derived type)
	    Util.assert(quadDeriv.typeMap(q, q.retval())!=null);
	    retvalT = _TEMP(q, quadDeriv.typeMap(q, q.retval()), retval);
	}
      
	// clone & type retex.
	retex = m_ctm.tempMap(q.retex());
	// (exception value should never have a derived type)
	Util.assert(quadDeriv.typeMap(q, q.retex())!=null);
	retexT= _TEMP(q, quadDeriv.typeMap(q, q.retex()), retex);

	// deal with function pointer.
	func  = _TEMP(q.ptr(), q);
	ptr = q.isVirtual() ? // extra dereference for virtual functions.
	    (Exp) makeMEM(q, HClass.Void, func) : (Exp) func;
	    
	qParams = q.params(); params = null; 
	for (int i=qParams.length-1; i >= 0; i--) {
	    params = new ExpList(_TEMP(qParams[i], q), params);      
	}

	// if debugging option enabled, check that this dispatch pointer is
	// non-null, and call a special reporting function if it is.
	if (checkDispatch) {
	    Temp pT = new Temp(m_tf.tempFactory(), "ptr");
	    addStmt(new MOVE(m_tf, q, _TEMP(q, HClass.Void, pT), ptr));
	    emitAssert(m_tf, q,
		       new BINOP(m_tf, q, Type.POINTER, Bop.CMPEQ,
				 _TEMP(q, HClass.Void, pT),new CONST(m_tf, q)),
		       false, "method pointer != null");
	    ptr = _TEMP(q, HClass.Void, pT);
	}
	addStmt(new CALL
	    (m_tf, q, 
	     retvalT, retexT,
	     ptr, params,
	     new NAME(m_tf, q, label(q.nextEdge(1/*exception edge*/))),
	     q.isTailCall()));
	if (edgeOracle.defaultEdge(q)!=0)
	    addStmt(new JUMP(m_tf, q, label(q.nextEdge(0))));

	// RESULTS OF CALLS SHOULD NEVER BE FOLDED! (assert this here?)
    }

    // just refer to the runtime's NameMap
    public void visit(PFCONST q) {
	addMove
	    (q, q.dst(),
	     new NAME(m_tf, q, m_nm.label(q.field())));
    }

    public void visit(PFIELD q) { 
	addMove
	    (q, q.dst(),
	     m_rtb.fieldBase(m_tf, q, treeDeriv,
			     _TEMPte(q.objectref(), q)));
    }
  
    public void visit(PFOFFSET q) {
	addMove
	    (q, q.dst(),
	     m_rtb.fieldOffset(m_tf, q, treeDeriv, q.field()));
    }

    // runtime-independent
    public void visit(PGET q) {
	MEM m = makeMEM(q, q.type(), _TEMP(q.ptr(), q));
	addMove(q, q.dst(), m);
    }
  
    // just refer to the runtime's NameMap
    public void visit(PMCONST q) { 
	addMove(q, q.dst(),
		new NAME(m_tf, q, m_nm.label(q.method())));
    }

    public void visit(PMETHOD q) {
	addMove
	    (q, q.dst(),
	     m_rtb.methodBase(m_tf, q, treeDeriv,
			      _TEMPte(q.objectref(), q)));
    }

    public void visit(PMOFFSET q) {
	addMove(q, q.dst(),
		m_rtb.methodOffset(m_tf, q, treeDeriv, q.method()));
    }

    public void visit(POPER q) {
	Exp oper = null; int optype; 
	Stm s0;
	Temp[] operands = q.operands();
	TEMP dst;
  
	// Convert optype to a Bop or a Uop
	switch(q.opcode()) {
	case Qop.ACMPEQ:
	case Qop.DCMPEQ:
	case Qop.FCMPEQ:
	case Qop.ICMPEQ:
	case Qop.LCMPEQ:
	case LQop.PCMPEQ: optype = Bop.CMPEQ; break;
	case Qop.D2F:
	case Qop.I2F:
	case Qop.L2F: optype = Uop._2F; break;
	case Qop.D2I:
	case Qop.F2I:
	case Qop.L2I: optype = Uop._2I; break;
	case Qop.D2L:
	case Qop.F2L:
	case Qop.I2L: optype = Uop._2L; break; 
	case Qop.I2D:
	case Qop.F2D:
	case Qop.L2D: optype = Uop._2D; break;
	case Qop.DADD: 
	case Qop.FADD:
	case Qop.IADD:
	case Qop.LADD: 
	case LQop.PADD: optype = Bop.ADD; break;
	case Qop.DCMPGE:
	case Qop.FCMPGE: optype = Bop.CMPGE; break;
	case Qop.DCMPGT:
	case Qop.FCMPGT:
	case Qop.ICMPGT:
	case Qop.LCMPGT: 
	case LQop.PCMPGT: optype = Bop.CMPGT; break;
	case Qop.DDIV:
	case Qop.FDIV:
	case Qop.IDIV:
	case Qop.LDIV: optype = Bop.DIV; break;
	case Qop.DMUL:
	case Qop.FMUL:
	case Qop.IMUL:
	case Qop.LMUL: optype = Bop.MUL; break;
	case Qop.DNEG:   
	case Qop.FNEG: 
	case Qop.INEG:
	case Qop.LNEG:
	case LQop.PNEG: optype = Uop.NEG; break;
	case Qop.DREM:
	case Qop.FREM:
	case Qop.IREM:
	case Qop.LREM: optype = Bop.REM; break;
	case Qop.I2B: optype = Uop.I2B; break;
	case Qop.I2C: optype = Uop.I2C; break;
	case Qop.I2S: optype = Uop.I2S; break;
	case Qop.IAND:
	case Qop.LAND: optype = Bop.AND; break;
	case Qop.IOR:
	case Qop.LOR: optype = Bop.OR; break;
	case Qop.IXOR:
	case Qop.LXOR: optype = Bop.XOR; break;
	case Qop.ISHL:
	case Qop.LSHL: 
	case Qop.ISHR:
	case Qop.LSHR: 
	case Qop.IUSHR:
	case Qop.LUSHR: 
	    visitShiftOper(q); return;  // Special case
 	default: 
	    throw new Error("Unknown optype in ToTree: "+q.opcode());
	}
    
	if (operands.length==1) {
	    Exp op0 = _TEMP(operands[0], q);
	    oper = new UNOP(m_tf, q, op0.type(), optype, op0);
	}
	else if (operands.length==2) {
	    Exp op0 = _TEMP(operands[0], q), op1 = _TEMP(operands[1], q);
	    oper = new BINOP
		(m_tf, q, 
		 MERGE_TYPE(op0.type(), op1.type()),
		 optype,
		 op0, 
		 op1);
	}
	else 
	    throw new Error("Unexpected # of operands: " + q);
    
	addMove(q, q.dst(), oper);
    }

    private void visitShiftOper(POPER q) { 
	int optype; OPER oper;
	Temp[] operands = q.operands();

	switch (q.opcode()) { 
	case Qop.ISHL:
	case Qop.LSHL: 
	    optype = Bop.SHL; break;
	case Qop.LUSHR: 
	case Qop.IUSHR:
	    optype = Bop.USHR; break;
	case Qop.ISHR:
	case Qop.LSHR: 
	    optype = Bop.SHR; break;
	default: 
	    throw new Error("Not a shift optype: " + q.opcode());
	}

	Exp op0 = _TEMP(operands[0], q), op1 = _TEMP(operands[1], q);
	oper = new BINOP(m_tf, q, op0.type(), optype, op0, op1);
	addMove(q, q.dst(), oper);
    }
  
    public void visit(PSET q) {
	Exp src = _TEMP(q.src(), q), ptr = _TEMP(q.ptr(), q);
	MEM m = makeMEM(q, q.type(), ptr);
	Stm s0 = new MOVE(m_tf, q, m, src);
	addStmt(s0);
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Utility Functions                      *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    private void emitAssert(TreeFactory m_tf, HCodeElement src,
			    Exp cond, boolean condShouldBeTrue,
			    String assertion) {
	Label Lsafe = new Label(), Lunsafe = new Label();
	Label Lassert = new Label(), Lfile = new Label();
	addStmt(new CJUMP(m_tf, src, cond, 
			  condShouldBeTrue ? Lsafe : Lunsafe,
			  condShouldBeTrue ? Lunsafe : Lsafe));
	addStmt(new LABEL(m_tf, src, Lunsafe, false));
	// unsafe jump!  call reporting function.
	Label Lreporter = new Label(m_nm.c_function_name("__assert_fail"));
	addStmt(new NATIVECALL(m_tf, src, null, new NAME(m_tf, src, Lreporter),
			       new ExpList
			       (new NAME(m_tf, src, Lassert), new ExpList
				(new NAME(m_tf, src, Lfile), new ExpList
				 (new CONST(m_tf, src, src.getLineNumber()),
				  new ExpList(new CONST(m_tf, src), null))))
			       ));
	    addStmt(new LABEL(m_tf, src, Lassert, false));
	    emitString(m_tf, src, assertion);
	    addStmt(new LABEL(m_tf, src, Lfile, false));
	    emitString(m_tf, src, src.getSourceFile());
	    addStmt(new LABEL(m_tf, src, Lsafe, false));
    }
    private void emitString(TreeFactory m_tf, HCodeElement src, String s) {
	for (int i=0; i<s.length(); i++)
	    addStmt(new DATUM(m_tf, src, new CONST(m_tf, src, 8, false,
						   (int) s.charAt(i))));
	// null-terminate:
	addStmt(new DATUM(m_tf, src, new CONST(m_tf, src, 8, false, 0)));
	// align to proper word boundary.  (be safe, align 8)
	addStmt(new ALIGN(m_tf, src, 8));
    }
    // do appropriate temp mapping on allocationproperties object
    private AllocationProperties MAP(final AllocationProperties ap) {
	if (ap.allocationHeap()==null) return ap;
	else return new AllocationPropertiesImpl(ap, m_ctm);
    }
			   
    private void addStmt(Stm stm) { 
        m_stmList.add(stm);
    }

    // foldable _TEMP
    private Translation.Exp _TEMPte(Temp quadTemp, Quad useSite) {
	// this constructor takes quad temps.
	Util.assert(quadTemp.tempFactory()!=m_tf.tempFactory(),
		    "Temp should be from LowQuad factory, not Tree factory.");
	// use reachingDefs to find definition sites.
	Set defSites = reachingDefs.reachingDefs(useSite, quadTemp);
	if (defSites.size()==1) {
	    HCodeElement hce = (HCodeElement) defSites.iterator().next();
	    if (foldNanny.canFold(hce, quadTemp)) {
		// fold this use!
		Util.assert(foldMap.containsKey(Default.pair(hce,quadTemp)));
		return (Translation.Exp)
		    foldMap.remove(Default.pair(hce,quadTemp));
	    }
	}
	TypeBundle tb = mergeTypes(quadTemp, defSites);
	Temp treeTemp = m_ctm.tempMap(quadTemp);
	TEMP result = new TEMP(m_tf, useSite, tb.simpleType, treeTemp);
	if (tb.classType!=null)
	    treeDeriv.putTypeAndTemp(result, tb.classType, treeTemp);
	else
	    treeDeriv.putDerivation(result, tb.derivation);
	return new Translation.Ex(result);
    }
    // creates a properly typed TEMP -- may fold this use!
    private Exp _TEMP(Temp quadTemp, Quad useSite) {
	return _TEMPte(quadTemp, useSite).unEx(m_tf);
    }
    private TEMP _TEMP(HCodeElement src, HClass type, Temp treeTemp) {
	// this constructor takes TreeTemps.
	Util.assert(treeTemp.tempFactory()==m_tf.tempFactory(),
		    "Temp should be from Tree factory.");
	TEMP result = new TEMP(m_tf, src, TYPE(type), treeTemp);
	treeDeriv.putTypeAndTemp(result, type, treeTemp);
	return result;
    }
    private TEMP _TEMP(HCodeElement src, DList deriv, Temp treeTemp) {
	// this constructor takes TreeTemps.
	Util.assert(treeTemp.tempFactory()==m_tf.tempFactory(),
		    "Temp should be from Tree factory.");
	TEMP result = new TEMP(m_tf, src, Type.POINTER, treeTemp);
	treeDeriv.putDerivation(result, deriv);
	return result;
    }
    // make a move.  unless, of course, the expression should be folded.
    private void addMove(Quad defSite, Temp quadTemp, Translation.Exp value) {
	// this constructor takes quad temps.
	Util.assert(quadTemp.tempFactory()!=m_tf.tempFactory(),
		    "Temp should be from LowQuad factory, not Tree factory.");
	if (foldNanny.canFold(defSite, quadTemp)) {
	    Util.assert(!foldMap.containsKey(Default.pair(defSite, quadTemp)));
	    foldMap.put(Default.pair(defSite, quadTemp), value);
	    return;
	}
	// otherwise... make Tree.MOVE
	HClass type = quadDeriv.typeMap(defSite, quadTemp);
	Temp treeTemp = m_ctm.tempMap(quadTemp);
	TEMP dst = new TEMP(m_tf, defSite, TYPE(type), treeTemp);
	MOVE m = new MOVE(m_tf, defSite, dst, value.unEx(m_tf));
	if (type!=null)
	    treeDeriv.putTypeAndTemp(dst, type, treeTemp);
	else
	    treeDeriv.putDerivation(dst, 
				    quadDeriv.derivation(defSite, quadTemp));
	addStmt(m);
	return;
    }
    private void addMove(Quad defSite, Temp quadTemp, Exp value) {
	addMove(defSite, quadTemp, new Translation.Ex(value));
    }
    // storage for folded definitions.
    private final Map foldMap = new HashMap();

    private TypeBundle mergeTypes(Temp t, Set defSites) {
	Util.assert(defSites.size() > 0);
	
	TypeBundle tb = null;
	for (Iterator it=defSites.iterator(); it.hasNext(); ) {
	    Quad def = (Quad) it.next();
	    TypeBundle tb2 = (quadDeriv.typeMap(def, t)!=null) ?
		new TypeBundle(quadDeriv.typeMap(def, t)) :
		new TypeBundle(quadDeriv.derivation(def, t));
	    tb = (tb==null) ? tb2 : tb.merge(tb2);
	}
	return tb;
    }

    // make a properly-sized MEM
    private MEM makeMEM(HCodeElement source, HClass type, Exp ptr) {
	MEM result;
	if (type.equals(HClass.Boolean) || type.equals(HClass.Byte))
	    result = new MEM(m_tf, source, 8, true, ptr);
	else if (type.equals(HClass.Char))
	    result = new MEM(m_tf, source, 16, false, ptr);
	else if (type.equals(HClass.Short))
	    result = new MEM(m_tf, source, 16, true, ptr);
	else
	    result = new MEM(m_tf, source, maptype(type), ptr);
	treeDeriv.putType(result, type);// update type information!
	return result;
    }

    // Implmentation of binary numeric promotion found in the Java
    // language spec. 
    private int MERGE_TYPE(int type1, int type2) { 
	boolean longptrs = m_tf.getFrame().pointersAreLong();
	if (type1==type2) return type1;
	else { 
	    if (type1==Type.DOUBLE || type2==Type.DOUBLE) { 
		Util.assert(type1!=Type.POINTER && type2!=Type.POINTER);
		return Type.DOUBLE;
	    }
	    else if (type1==Type.FLOAT || type2==Type.FLOAT) { 
		Util.assert(type1!=Type.POINTER && type2!=Type.POINTER);
		return Type.FLOAT;
	    }
	    else if (type1==Type.LONG || type2==Type.LONG) { 
		if (type1==Type.POINTER || type2==Type.POINTER) { 
		    Util.assert(longptrs); return Type.POINTER;
		}
		return Type.LONG;
	    }
	    else if (type1==Type.POINTER || type2==Type.POINTER) { 
		return Type.POINTER;
	    }
	    else {
		return Type.INT;  // Should not get this far
	    }
	}
    }

    /** Return the sizeof a given type. */
    private int sizeof(HClass type) {
	if (type==HClass.Boolean || type==HClass.Byte) return 1;
	if (type==HClass.Char || type==HClass.Short) return 2;
	if (type==HClass.Int || type==HClass.Float) return 4;
	if (type==HClass.Long|| type==HClass.Double) return 8;
	return Type.isDoubleWord(m_tf, Type.POINTER) ? 8 : 4;
    }
    /** map constant value into precisely-typed CONST expression */
    // (string initializers are not allowed)
    private CONST _CONST(HCodeElement src, HClass type, Object value) {
	if (type==HClass.Void) return new CONST(m_tf, src); // null constant
	// sub-int types seen in ARRAYINIT
	if (type==HClass.Boolean)
	    return new CONST(m_tf, src, 8/*booleans are bytes*/, false,
			     ((Boolean)value).booleanValue()?1:0);
	if (type==HClass.Byte)
	    return new CONST(m_tf, src, 8, true, ((Byte)value).intValue());
	if (type==HClass.Char)
	    return new CONST(m_tf, src, 16, false, (int)
			     ((Character)value).charValue());
	if (type==HClass.Short)
	    return new CONST(m_tf, src, 16, true, ((Short)value).intValue());
	if (type==HClass.Int)
	    return new CONST(m_tf, src, ((Integer)value).intValue());
	if (type==HClass.Long)
	    return new CONST(m_tf, src, ((Long)value).longValue());
	if (type==HClass.Float)
	    return new CONST(m_tf, src, ((Float)value).floatValue());
	if (type==HClass.Double)
	    return new CONST(m_tf, src, ((Double)value).doubleValue());
	throw new Error("Bad type for CONST: " + type);
    }
    /** map constant value into (non-precisely-typed) CONST expression */
    private Translation.Exp mapconst(HCodeElement src,
				     Object value, HClass type) {
	Exp constant;

	if (type==HClass.Void) // HClass.Void reserved for null constants
	    constant = new CONST(m_tf, src);
	/* CSA: Sub-int types only seen in ARRAYINIT */
	else if (type==HClass.Boolean)
	    return new ExCONST
		(m_tf, src, ((Boolean)value).booleanValue()?1:0);
	else if (type==HClass.Byte)
	    return new ExCONST(m_tf, src, ((Byte)value).intValue()); 
	else if (type==HClass.Char)
	    return new ExCONST 
		(m_tf, src, 
		 (int)(((Character)value).charValue())); 
	else if (type==HClass.Short)
	    return new ExCONST(m_tf, src, ((Short)value).intValue()); 
	else if(type==HClass.Int) 
	    return new ExCONST(m_tf, src, ((Integer)value).intValue()); 
	else if (type==HClass.Long)
	    constant = new CONST(m_tf, src, ((Long)value).longValue());
	else if (type==HClass.Float)
	    constant = new CONST(m_tf, src, ((Float)value).floatValue()); 
	else if (type==HClass.Double)
	    constant = new CONST(m_tf, src, ((Double)value).doubleValue());
	else if (type.getName().equals("java.lang.String"))
	    return m_rtb.stringConst(m_tf, src, treeDeriv, (String)value);
	else if (type.getName().equals("java.lang.Class"))
	    return m_rtb.classConst(m_tf, src, treeDeriv, (HClass)value);
	else if (type.getName().equals("java.lang.reflect.Field"))
	    return m_rtb.fieldConst(m_tf, src, treeDeriv, (HField)value);
	else if (type.getName().equals("java.lang.reflect.Method"))
	    return m_rtb.methodConst(m_tf, src, treeDeriv, (HMethod)value);
	else 
	    throw new Error("Bad type for CONST: " + type); 
	return new Translation.Ex(constant);
    }
    private static class ExCONST extends Translation.Ex {
	final int val;
	public ExCONST(TreeFactory tf, HCodeElement src, int val) {
	    super(new CONST(tf, src, val));
	    this.val = val;
	}
	protected Stm unCxImpl(TreeFactory tf, Label iftrue, Label iffalse) {
	    if (val==0) return new JUMP(tf, exp, iffalse);
	    else return new JUMP(tf, exp, iftrue);
	}
    }
    private CONST constZero(HCodeElement src, HClass type) {
	if (type==HClass.Boolean || type==HClass.Byte ||
	    type==HClass.Char || type==HClass.Short ||
	    type==HClass.Int) 
	    return new CONST(m_tf, src, (int)0);
	else if (type==HClass.Long)
	    return new CONST(m_tf, src, (long)0);
	else if (type==HClass.Float)
	    return new CONST(m_tf, src, (float)0);
	else if (type==HClass.Double)
	    return new CONST(m_tf, src, (double)0);
	else
	    return new CONST(m_tf, src); // null.
    }
}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//                                                            //
//                    Utility classes                         //
//                                                            //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    /** An edge oracle tells you which edge out of an
     *  <code>HCodeElement</code> wants to be the default
     *  (ie, non-branching) edge. */
    static interface EdgeOracle {
	public int defaultEdge(HCodeElement hce);
    }
    /** A fold nanny tells you whether or not you can fold a
     *  particular definition of a <code>Temp</code>. */
    static interface FoldNanny {
	public boolean canFold(HCodeElement defSite, Temp t);
    }
	
    /** A <code>TypeBundle</code> rolls together the <code>HClass</code>
     *  type or <code>DList</code> derivation of a value, along with
     *  the integer <code>Tree.Type</code>. */
    private static class TypeBundle {
	final int simpleType;
	final HClass classType;
	final DList derivation;
	TypeBundle(HClass hc) {
	    this.simpleType = TYPE(hc);
	    this.classType = hc;
	    this.derivation = null;
	}
	TypeBundle(DList deriv) {
	    this.simpleType = Type.POINTER;
	    this.classType = null;
	    this.derivation = deriv;
	}
	TypeBundle merge(TypeBundle tb) {
	    if (this.derivation!=null) {
		Util.assert(this.equals(tb));
		return this;
	    }
	    Util.assert(false);return null;
	}
	public boolean equals(Object o) {
	    if (!(o instanceof TypeBundle)) return false;
	    TypeBundle tb = (TypeBundle) o;
	    if (this.simpleType != tb.simpleType) return false;
	    if (this.classType != null)
		return (this.classType == tb.classType);
	    Util.assert(this.derivation != null);
	    if (tb.derivation == null) return false;
	    return this.derivation.equals(tb.derivation);
	}
    }

    // UTILITY METHODS........

    private static int TYPE(HClass hc) { 
	if (hc==null || hc==HClass.Void) return Type.POINTER;
	return maptype(hc);
    }

    private static int maptype(HClass hc) {
	if (hc==HClass.Boolean ||
	    hc==HClass.Byte    ||
	    hc==HClass.Char    ||
	    hc==HClass.Short   ||
	    hc==HClass.Int)          return Type.INT;
	else if (hc==HClass.Long)    return Type.LONG;
	else if (hc==HClass.Float)   return Type.FLOAT;
	else if (hc==HClass.Double)  return Type.DOUBLE;
	else                         return Type.POINTER;
    }

} // end of public class ToTree
