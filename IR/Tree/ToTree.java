// ToTree.java, created Tue Feb 16 16:46:36 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadNoSSA;
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
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.IR.Properties.UseDef;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.HClassUtil;
import harpoon.Util.Tuple;
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
import java.util.Stack;

/**
 * The ToTree class is used to translate low-quad-no-ssa code to tree code.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToTree.java,v 1.1.2.51 1999-10-23 05:59:34 cananian Exp $
 */
public class ToTree implements Derivation, TypeMap {
    private Derivation  m_derivation;
    private Tree        m_tree;
    private TypeMap     m_typeMap;
   
    /** Class constructor */
    public ToTree(final TreeFactory tf, LowQuadNoSSA code) {
	Util.assert(((Code.TreeFactory)tf).getParent().getName().equals("tree"));
	translate(tf, code);
    }
    
    /** Returns the updated derivation information for the 
     *  specified <code>Temp</code>.  The <code>HCodeElement</code>
     *  parameter must be a <code>Tree</code> object in which the 
     *  <code>Temp</code> is found.
     */
    public DList derivation(HCodeElement hce, Temp t) {
	return m_derivation.derivation(hce, t);
    }

    /** Returns the root of the generated tree code */
    public Tree getTree() {
	return m_tree;
    }

    /** Returns the updated type information for the specified
     *  <code>Temp</code>.  The <code>HCode</code> paramter is
     *  ignored. */
    public HClass typeMap(HCodeElement hce, Temp t) {
	// Ignores HCode parameter
	return m_typeMap.typeMap(hce, t);
    }

    private void translate(TreeFactory tf, LowQuadNoSSA code) {
	Set                           handlers;
	CloningTempMap                ctm;
	LowQuadMap                    lqm;
	LowQuadWithDerivationVisitor  dv;
	Quad                          root;
	
	root = (Quad)code.getRootElement();
	ctm = new CloningTempMap
	    (root.getFactory().tempFactory(),tf.tempFactory());

	// Clone the Quad graph
	lqm = new LowQuadMap();
	dv = new CloningVisitor(code, code, lqm);
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    ((Quad)i.next()).accept(dv);
	
	for (Iterator i = code.getElementsI(); i.hasNext();) { 
	    Quad qTmp = (Quad)i.next();
	    Edge[] el = qTmp.nextEdge();
	    for (int n=0; n<el.length; n++) 
		Quad.addEdge(lqm.get((Quad)el[n].from()),
			     el[n].which_succ(),
			     lqm.get((Quad)el[n].to()),
			     el[n].which_pred());
	}

	// *MODIFY* the cloned quad graph to have explicit labels at the
	// destination of each branch.  
	dv = new LabelingVisitor(dv, dv);

	/* Replacing this Iterator code because the labelling visitor
	   screws up the internal representation for the quads.  It
	   would work if we were constructing a new quad graph or if
	   we didn't mess with the internal rep, but the quickest way
	   to fix everything is to use an Array instead of an
	   Iterator, since an Array is static while an Iterator will
	   update itself incrementally with each change.

	  for (Iterator i = code.getElementsI(); i.hasNext();)
	      lqm.get((Quad)i.next()).accept(dv);
	*/
	Quad[] qa = (Quad[]) code.getElements();
	for(int i=0; i<qa.length; i++) {
	    lqm.get(qa[i]).accept(dv);
	}

	// Construct a list of harpoon.IR.Tree.Stm objects
	TempRenameMap trm = new TempRenameMap();
	dv = new TranslationVisitor(tf, dv, dv, trm, ctm);
	for (Iterator i=quadGraph(lqm.get(root),true);i.hasNext();)
	    ((Quad)i.next()).accept(dv);

	// Assign member variables
	m_tree       = ((TranslationVisitor)dv).getTree();
	m_derivation = dv;
	m_typeMap    = dv;
    }
    
    private Iterator quadGraph(final Quad head) {
	return quadGraph(head, false);
    }

    // Enumerates the Quad graph in depth-first order.  
    // If explicitJumps is true, adds a "JMP" quad (defined in this file)
    // when quads connected by an edge are not 
    //       a) iterated contiguously            
    // AND   b) not connected by a SIGMA node
    // 
    private Iterator quadGraph(final Quad    head,
			       final boolean explicitJumps) { 
	return new Iterator() {
	    private QuadFactory qf      = head.getFactory();
	    private Set         visited = new HashSet();
	    private Stack       s       = new Stack();
	    { s.push(head); }
	    public void remove() { throw new UnsupportedOperationException(); }
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Object next() { 
		if (s.isEmpty()) throw new NoSuchElementException();
		Quad q = (Quad)s.pop();
		Quad[] next = q.next();
		for (int i=0; i<next.length; i++) {
		    if (!visited.contains(next[i])) {
			s.push(next[i]);
			visited.add(next[i]);
		    }
		    else if (explicitJumps) {
			JMP                    jmp   = null;
			harpoon.IR.Quads.SIGMA sig   = null;
			harpoon.IR.Quads.LABEL label = null;
			try { sig = (harpoon.IR.Quads.SIGMA)q; } 
			catch (ClassCastException cce1) { 
			    try { label = (harpoon.IR.Quads.LABEL)q.next(0); }
			    catch (ClassCastException cce2) { 
				Util.assert(q.next(0).kind()==QuadKind.FOOTER);
				continue;
			    }
			    try { jmp = (JMP)q; }
			    catch (ClassCastException cce2) { 
				s.push(new JMP(qf, q, label));
				continue;
			    }
			    continue;
			}
		    }
		}
		return q;
	    }
	};
    }
    // don't close class: all of the following are inner classes,
    // even if they don't look that way.  I'm just don't feel like
    // reindenting all of this existing code. [CSA]
  
// Translates the LowQuadNoSSA code into tree form. 
//
static class TranslationVisitor extends LowQuadWithDerivationVisitor {
    private CloningTempMap    m_ctm;          // Clones Temps to new tf
    private NameMap           m_nm;           // Runtime-specific label naming
    private List              m_stmList;      // Holds translated statements
    private TreeFactory       m_tf;           // The new TreeFactory
    private TempRenameMap     m_trm;          // Temp renaming
    private TEMP              m_handler = null; 
    private Map               m_labels  = new HashMap();
    private Runtime.TreeBuilder m_rtb;
  
    public TranslationVisitor(TreeFactory tf, Derivation derivation, 
			      TypeMap typeMap, TempRenameMap trm,
			      CloningTempMap ctm) {
	super(derivation, typeMap);
	m_ctm          = ctm;
	m_tf           = tf; 
	m_nm           = m_tf.getFrame().getRuntime().nameMap;
	m_trm          = trm;
	m_stmList      = new ArrayList();
	m_rtb	       = m_tf.getFrame().getRuntime().treeBuilder;
    }

    Tree getTree() { return Stm.toStm(m_stmList); } 

    public void visit(Quad q) { /* Dont translate other quads */  }

    public void visit(harpoon.IR.Quads.ALENGTH q) {
	Stm s0 = new MOVE
	    (m_tf, q, 
	     _TEMP(q.dst(), q),
	     m_rtb.arrayLength
	     (m_tf, q, new Translation.Ex(_TEMP(q.objectref(), q))).unEx(m_tf)
	     );
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.ANEW q) {
	Exp classPtr, hashcode, length;
	HClass arrayClass;
	int instructionsPerIter = 9;
	Stm[] stms = new Stm[instructionsPerIter * q.dimsLength() + 1];

	LABEL[] loopHeaders   = new LABEL[q.dimsLength()];
	LABEL[] loopMid       = new LABEL[q.dimsLength()];
	LABEL[] loopFooters   = new LABEL[q.dimsLength()];
	Exp[]   arrayDims     = new Exp[q.dimsLength()+1];
	TEMP[]  arrayRefs     = new TEMP[q.dimsLength()];
	TEMP[]  inductionVars = new TEMP[q.dimsLength()];
	for (int i=0; i<q.dimsLength(); i++) { 
	    loopHeaders[i]   = new LABEL(m_tf, q, new Label(), false);
	    loopMid[i]       = new LABEL(m_tf, q, new Label(), false);
	    loopFooters[i]   = new LABEL(m_tf, q, new Label(), false);
	    arrayDims[i+1]   = _TEMP(q.dims(i), q);
	    arrayRefs[i]     = extra(q, Type.POINTER);
	    inductionVars[i] = extra(q, Type.INT);
	}
	arrayDims[0] = new CONST(m_tf, q, 1);
	
	for (int i=0; i<q.dimsLength(); i++) { 
	    int base = i*9;
	    int rear = (instructionsPerIter*q.dimsLength()) - (i*3) - 1;
	    
	    stms[base++] = 
		new MOVE(m_tf, q, inductionVars[i], new CONST(m_tf, q, 0));
		 
	    stms[base++] = loopHeaders[i]; 

	    stms[base++] = new CJUMP
		(m_tf, q, 
		 new BINOP
		 (m_tf, q, Type.INT, Bop.CMPGE,
		  inductionVars[i], 
		  arrayDims[i]),
		 loopFooters[i].label, 
		 loopMid[i].label);

	    stms[base++] = loopMid[i];
		  
	    // Step 1: allocate memory needed for the array. 
	    length    = arrayDims[i+1];
	    arrayClass = q.hclass();
	    for (int n=0; n<i; n++) arrayClass = arrayClass.getComponentType();

	    stms[base++] = new MOVE
		(m_tf, q, arrayRefs[i], 
		 m_rtb.arrayNew(m_tf, q, arrayClass,
				new Translation.Ex(length)).unEx(m_tf));

	    // Move the pointer to the new array into the appropriate 
	    // memory location.	    
	    
	    if (i>0) { 
		stms[base++] = new MOVE
		    (m_tf, q, 
		     new MEM
		     (m_tf, q, Type.POINTER,
		      new BINOP
		      (m_tf, q, Type.POINTER, Bop.ADD, 
		       arrayRefs[i-1],
		       inductionVars[i])),
		     arrayRefs[i]);
	    }
	    else { 
		stms[base++] = new EXP(m_tf, q, new CONST(m_tf, q, 0));
	    }
	    
	    // Increment the correct induction variable
	    stms[rear-2] = new MOVE
		(m_tf, q, 
		 inductionVars[i], 
		 new BINOP
		 (m_tf, q, Type.INT, Bop.ADD, 
		  inductionVars[i],
		  new CONST(m_tf, q, 1)));
	    
	    stms[rear-1] = new JUMP(m_tf, q, loopHeaders[i].label);
	    stms[rear]   = loopFooters[i];
	}

	// FIXME: ZERO FILL! (arrayNew doesn't initialize elements)
	// only need to zero fill leaf arrays; all others are initialized.
    
	// Make a reference to the array we are going to create
	//
	stms[instructionsPerIter*q.dimsLength()] = 
	    new MOVE(m_tf,q,_TEMP(q.dst(),q),arrayRefs[0]);
	addStmt(Stm.toStm(Arrays.asList(stms)));
    }

    public void visit(harpoon.IR.Quads.ARRAYINIT q) {
	HClass arrayType = HClassUtil.arrayClass(q.type(), 1);
	Stm s0, s1, s2;

	// Create a pointer which we'll use to initialize the array
	TEMP  nextPtr = extra(q, Type.POINTER);
	TEMP  objTemp = _TEMP(q.objectref(), q);
	// Create derivation information for the new TEMP
	DList dl = new DList(objTemp.temp, true, null);

	// set nextPtr to point to arrayBase + q.offset() * size.
	s0 = new MOVE
	    (m_tf, q, 
	     nextPtr,
	     new BINOP
	     (m_tf, q, Type.POINTER, Bop.ADD,
	      m_rtb.arrayBase
	      (m_tf, q, new Translation.Ex(objTemp)).unEx(m_tf),
	      m_rtb.arrayOffset
	      (m_tf, q, arrayType,
	       new Translation.Ex(new CONST(m_tf, q, q.offset()))).unEx(m_tf)
	      ));

	addDT(nextPtr.temp, nextPtr, dl, null);
	updateDT(q.objectref(), q, objTemp.temp, objTemp);
	addStmt(s0);
    
	for (int i=0; i<q.value().length; i++) {
	    Exp c = mapconst(q, q.value()[i], q.type());
	    MEM m = makeMEM(q, q.type(), nextPtr);
	    s0 = new MOVE(m_tf, q, m, c);
	    s1 = new MOVE
		(m_tf, q, 
		 nextPtr, 
		 new BINOP
		 (m_tf, q, Type.POINTER, Bop.ADD, 
		  nextPtr, 
		  m_rtb.arrayOffset
		  (m_tf, q, arrayType,
		   new Translation.Ex(new CONST(m_tf, q, 1))).unEx(m_tf)
		  ));

	    addStmt(Stm.toStm(Arrays.asList(new Stm[] { s0, s1 })));
	}
    }

    public void visit(harpoon.IR.Quads.CJMP q) { 
	Util.assert(q.next().length==2 && 
		    q.next(0) instanceof harpoon.IR.Quads.LABEL &&
		    q.next(1) instanceof harpoon.IR.Quads.LABEL);

	TEMP test = _TEMP(q.test(), q);
	Stm s0 = new CJUMP
	    (m_tf, q, test, 
	     (_LABEL((harpoon.IR.Quads.LABEL)q.next(1))).label,
	     (_LABEL((harpoon.IR.Quads.LABEL)q.next(0))).label);
    
	updateDT(q.test(), q, test.temp, test);
	addStmt(s0);
    }
  
    public void visit(harpoon.IR.Quads.COMPONENTOF q) {
	TEMP dst = _TEMP(q.dst(), q);
	TEMP aref= _TEMP(q.arrayref(), q);
	TEMP oref= _TEMP(q.objectref(), q);
	Stm s0 = new MOVE 
	    (m_tf, q, 
	     dst, 
	     m_rtb.componentOf(m_tf, q,
			       new Translation.Ex(aref),
			       new Translation.Ex(oref)).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.CONST q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm  s0  = new MOVE(m_tf, q, dst, mapconst(q, q.value(), q.type()));
    	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }
  
    public void visit(harpoon.IR.Quads.INSTANCEOF q) {
	TEMP dst = _TEMP(q.dst(), q);
	TEMP src = _TEMP(q.src(), q);
	Stm s0   = new MOVE
	    (m_tf, q,
	     dst,
	     m_rtb.instanceOf(m_tf, q, 
			      new Translation.Ex(src), q.hclass()).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(JMP q) { 
	addStmt(new JUMP(m_tf, q, _LABEL(q.label).label));
    }

    public void visit(harpoon.IR.Quads.LABEL q) {
	addStmt(_LABEL(q));
    }

    public void visit(harpoon.IR.Quads.METHOD q) {
	METHOD method; SEGMENT segment;
	Temp   params[]  = q.params(); 
	TEMP   mParams[] = new TEMP[params.length+1];
	
	segment = new SEGMENT(m_tf, q, SEGMENT.CODE);
	for (int i=0; i<params.length; i++) { 
	    mParams[i+1] = _TEMP(params[i], q);
	    updateDT(params[i], q, 
		     m_ctm.tempMap(m_trm.tempMap(params[i])), mParams[i+1]);
	}
	Util.assert(m_handler==null);
	m_handler = mParams[0] = extra(q, Type.POINTER);
	method    = new METHOD(m_tf, q, mParams);
	addDT(m_handler.temp,method,new DList(m_handler.temp,true,null),null);
	addStmt(segment);
	addStmt(method);
    }

    public void visit(harpoon.IR.Quads.MONITORENTER q) {
	TEMP obj = _TEMP(q.lock(), q);
	addStmt(m_rtb.monitorEnter(m_tf, q, new Translation.Ex(obj))
		     .unNx(m_tf));
    }

    public void visit(harpoon.IR.Quads.MONITOREXIT q) {
	TEMP obj = _TEMP(q.lock(), q);
	addStmt(m_rtb.monitorExit(m_tf, q, new Translation.Ex(obj))
		     .unNx(m_tf));
    }

    public void visit(harpoon.IR.Quads.MOVE q) {
	TEMP dst = _TEMP(q.dst(), q), src = _TEMP(q.src(), q);
	Stm  s0  = new MOVE(m_tf, q, dst, src);
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.src(), q, src.temp, src);
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.NEW q) { 
	TEMP objectref = _TEMP(q.dst(), q);
	addStmt(new MOVE(m_tf, q, objectref,
			 m_rtb.objectNew(m_tf, q, q.hclass()).unEx(m_tf)));
	updateDT(q.dst(), q, objectref.temp, objectref);
    }
	
    public void visit(harpoon.IR.Quads.RETURN q) {
	Exp retval;
    
	if (q.retval()==null) {
	    retval = new CONST(m_tf, q, 0);
	}
	else {
	    retval = _TEMP(q.retval(), q);
	    updateDT(q.retval(), q, ((TEMP)retval).temp, retval);
	}

	Stm s0 = new RETURN(m_tf, q, retval);    
	addStmt(s0);
    }

    // Naive implementation
    public void visit(harpoon.IR.Quads.SWITCH q) { 
	Quad qNext;  CJUMP branch; LABEL lNext;
	TEMP discriminant = _TEMP(q.index(), q);
	for (int i=0; i<q.keysLength(); i++) {
	    qNext  = q.next(i); 
	    Util.assert(qNext instanceof harpoon.IR.Quads.LABEL);

	    lNext  = new LABEL(m_tf, q, new Label(), false);
	    branch = new CJUMP
		(m_tf, q, new BINOP(m_tf, q, Type.LONG, Bop.CMPEQ, 
				    discriminant, 
				    new CONST(m_tf, q, q.keys(i))),
		 (_LABEL((harpoon.IR.Quads.LABEL)qNext)).label,
		 lNext.label);
	    addStmt(Stm.toStm(Arrays.asList(new Stm[] { branch, lNext })));
	}
	updateDT(q.index(), q, discriminant.temp, discriminant);
    }
  
    public void visit(harpoon.IR.Quads.THROW q) { 
	Util.assert(m_handler!=null);
	TEMP throwable = _TEMP(q.throwable(), q);
	Stm  s0        = new THROW(m_tf, q, throwable, m_handler);
	updateDT(q.throwable(), q, throwable.temp, throwable);
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.TYPECAST q) {
	throw new Error("Use INSTANCEOF instead of TYPECAST");
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   LowQuad Translator                     *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		  
    public void visit(PAOFFSET q) {
	TEMP dst = _TEMP(q.dst(), q), index = _TEMP(q.index(), q);
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     dst, 
	     m_rtb.arrayOffset(m_tf, q, q.arrayType(),
			       new Translation.Ex(index)).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.index(), q, index.temp, index);
	addStmt(s0);
    }

    public void visit(PARRAY q) {
	TEMP dst = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);
	Stm  s0  = new MOVE
	    (m_tf, q, dst, 
	     m_rtb.arrayBase(m_tf, q,
			     new Translation.Ex(objectref)).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }


    // runtime-independent
    public void visit(PCALL q) { 
	ExpList params; Temp[] qParams; TEMP retval, retex, func; 
	Exp ptr;
	Stm s0, s1;

	Util.assert(q.retex()!=null && q.ptr()!=null);

	// If q.retval() is null, the 'retval' in Tree.CALL is also null.
	if (q.retval()==null) {
	    retval = null;
	}
	else {
	    retval = _TEMP(q.retval(), q);
	    updateDT(q.retval(), q, retval.temp, retval);
	}
      
	// Assign TEMPs for the exceptional value and function pointer.
	// These can not be null.
	//
	retex = _TEMP(q.retex(), q);
	func  = _TEMP(q.ptr(), q);
	
	ptr = q.isVirtual() ? // extra dereference for virtual functions.
	    (Exp) new MEM(m_tf, q, Type.POINTER, func) : (Exp) func;
	    
	qParams = q.params(); params = null; 
	for (int i=qParams.length-1; i >= 0; i--) {
	    params = new ExpList(_TEMP(qParams[i], q), params);      
	    updateDT(qParams[i], q, ((TEMP)params.head).temp, params.head);
	}
	    
	// both out edges should be LABELs, right?
	harpoon.IR.Quads.LABEL Lrv = ((harpoon.IR.Quads.LABEL)q.next(0));
	harpoon.IR.Quads.LABEL Lex = ((harpoon.IR.Quads.LABEL)q.next(1));

	s0 = new CALL
	    (m_tf, q, 
	     retval, retex,
	     ptr, params,
	     new NAME(m_tf, q, _LABEL(Lex).label),
	     q.isTailCall());
	s1 = new JUMP
	    (m_tf, q, _LABEL(Lrv).label);

	updateDT(q.retex(), q, retex.temp, retex);
	updateDT(q.ptr(), q, func.temp, func);
	addStmt(s0); 
	addStmt(s1); 
    }

    // just refer to the runtime's NameMap
    public void visit(PFCONST q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     dst,
	     new NAME(m_tf, q, m_nm.label(q.field())));

	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(PFIELD q) { 
	TEMP dst = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);
	Stm  s0  = new MOVE
	    (m_tf, q, dst, 
	     m_rtb.fieldBase(m_tf, q,
			     new Translation.Ex(objectref)).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }
  
    public void visit(PFOFFSET q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = new MOVE
	    (m_tf, q,
	     dst,
	     m_rtb.fieldOffset(m_tf, q, q.field()).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    // runtime-independent
    public void visit(PGET q) {
	TEMP dst = _TEMP(q.dst(), q), ptr = _TEMP(q.ptr(), q);
	MEM m = makeMEM(q, q.type(), ptr);
	Stm s0 = new MOVE(m_tf, q, dst, m);
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.ptr(), q, ptr.temp, ptr);
	addStmt(s0);
    }
  
    // just refer to the runtime's NameMap
    public void visit(PMCONST q) { 
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE(m_tf, q, dst, new NAME(m_tf,q,m_nm.label(q.method())));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(PMETHOD q) {
	HClass type = this.typeMap(q,q.objectref());
	TEMP   dst  = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);

	Stm s0 = new MOVE
	    (m_tf, q, 
	     dst,
	     m_rtb.methodBase(m_tf, q,
			      new Translation.Ex(objectref)).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }

    public void visit(PMOFFSET q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE(m_tf,q,dst,
		     m_rtb.methodOffset(m_tf,q,q.method()).unEx(m_tf));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(POPER q) {
	Exp oper = null; int optype; 
	Stm s0;
	Temp[] operands = q.operands();
	TEMP dst, op0, op1;
  
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
	case Qop.I2B: optype = Uop._2B; break;
	case Qop.I2C: optype = Uop._2C; break;
	case Qop.I2S: optype = Uop._2S; break;
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
	    op0 = _TEMP(operands[0], q);
	    oper = new UNOP(m_tf, q, TYPE(q, operands[0]), optype, op0);
	    updateDT(operands[0], q, op0.temp, op0);
	}
	else if (operands.length==2) {
	    op0 = _TEMP(operands[0], q); op1 = _TEMP(operands[1], q);
	    oper = new BINOP
		(m_tf, q, 
		 MERGE_TYPE(TYPE(q, operands[0]), TYPE(q, operands[1])),
		 optype,
		 op0, 
		 op1);
	    updateDT(operands[0], q, op0.temp, op0);
	    updateDT(operands[1], q, op1.temp, op1);
	}
	else 
	    throw new Error("Unexpected # of operands: " + q);
    
	dst = _TEMP(q.dst(), q);
	s0 = new MOVE(m_tf, q, dst, oper);
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    private void visitShiftOper(POPER q) { 
	int optype; OPER oper; Stm s0; 
	Temp[] operands = q.operands();
	TEMP op0, op1, dst;

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

	op0=_TEMP(operands[0],q);op1=_TEMP(operands[1],q);dst=_TEMP(q.dst(),q);
	oper = new BINOP(m_tf, q, TYPE(q, operands[0]), optype, op0, op1);
	s0   = new MOVE(m_tf, q, dst, oper);
	updateDT(operands[0], q, op0.temp, op0);
	updateDT(operands[1], q, op1.temp, op1);
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }
  
    public void visit(PSET q) {
	TEMP src = _TEMP(q.src(), q), ptr = _TEMP(q.ptr(), q);
	MEM m = makeMEM(q, q.type(), ptr);
	Stm s0 = new MOVE(m_tf, q, m, src);
	updateDT(q.src(), q, src.temp, src);
	updateDT(q.ptr(), q, ptr.temp, ptr);
	addStmt(s0);
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Utility Functions                      *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    private void addStmt(Stm stm) { 
        m_stmList.add(stm);
    }

    private TEMP extra(HCodeElement src, int type) {
	return new TEMP(m_tf, src, type, new Temp(m_tf.tempFactory(), "tr_"));
    }

    // Used to correctly map local labels
    private LABEL _LABEL(String label, HCodeElement src) { 
	if (!m_labels.containsKey(label))
	    m_labels.put(label, new Label());
	return new LABEL(m_tf, src, (Label)m_labels.get(label), false);
    }

    private LABEL _LABEL(harpoon.IR.Quads.LABEL label) { 
	return _LABEL(label.label(), label);
    }

    private LABEL _LABEL(LABEL label) { 
	return new LABEL(m_tf, label, label.label, label.exported);
    }

    private TEMP _TEMP(TEMP t) { 
	return new TEMP(m_tf, t, t.type(), t.temp);
    }

    private TEMP _TEMP(Temp t, HCodeElement source) { 
	return _TEMP(t, source, TYPE(source, t));
    }

    private TEMP _TEMP(Temp t, HCodeElement source, int type) { 
	Util.assert(!hastype(source, t) || 
		    isValidMapping(this.typeMap(source,t), type));
	Temp nTmp = m_ctm.tempMap(m_trm.tempMap(t));
	return new TEMP(m_tf, source, type, nTmp);
    }

    private int TYPE(HCodeElement src, Temp t) { 
	return hastype(src, t)?maptype(this.typeMap(src,t)):Type.POINTER; 
    }

    private boolean hastype(HCodeElement hce, Temp t) { 
	return this.derivation(hce, t)==null; 
    }
  
    private boolean isValidMapping(HClass hc, int type) {
	return ((hc==HClass.Boolean&&type==Type.INT)   ||
		(hc==HClass.Byte&&type==Type.INT)      ||
		(hc==HClass.Char&&type==Type.INT)      ||
		(hc==HClass.Short&&type==Type.INT)     ||
		(hc==HClass.Int&&type==Type.INT)       ||
		(hc==HClass.Long&&type==Type.LONG)     ||
		(hc==HClass.Float&&type==Type.FLOAT)   ||
		(hc==HClass.Double&&type==Type.DOUBLE) ||
		(hc==HClass.Void&&type==Type.POINTER)  ||
		(!hc.isPrimitive()&&type==Type.POINTER));
    }

    private int maptype(HClass hc) {
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

    // make a properly-sized MEM
    private MEM makeMEM(HCodeElement source, HClass type, Exp ptr) {
	if (type.equals(HClass.Boolean) || type.equals(HClass.Byte))
	    return new MEM(m_tf, source, 8, true, ptr);
	if (type.equals(HClass.Char))
	    return new MEM(m_tf, source, 16, false, ptr);
	if (type.equals(HClass.Short))
	    return new MEM(m_tf, source, 16, true, ptr);
	return new MEM(m_tf, source, maptype(type), ptr);
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

    private Exp mapconst(HCodeElement src, Object value, HClass type) {
	Exp constant;

	if (type==HClass.Void) // HClass.Void reserved for null constants
	    constant = new CONST(m_tf, src);
	/* CSA: Sub-int types only seen in ARRAYINIT */
	else if (type==HClass.Boolean)
	    constant = new CONST
		(m_tf, src, ((Boolean)value).booleanValue()?1:0);
	else if (type==HClass.Byte)
	    constant = new CONST(m_tf, src, ((Byte)value).intValue()); 
	else if (type==HClass.Char)
	    constant = new CONST 
		(m_tf, src, 
		 (int)(((Character)value).charValue())); 
	else if (type==HClass.Short)
	    constant = new CONST(m_tf, src, ((Short)value).intValue()); 
	else if(type==HClass.Int) 
	    constant = new CONST(m_tf, src, ((Integer)value).intValue()); 
	else if (type==HClass.Long)
	    constant = new CONST(m_tf, src, ((Long)value).longValue());
	else if (type==HClass.Float)
	    constant = new CONST(m_tf, src, ((Float)value).floatValue()); 
	else if (type==HClass.Double)
	    constant = new CONST(m_tf, src, ((Double)value).doubleValue());
	else if (type==HClass.forName("java.lang.String"))
	    constant = m_rtb.stringConst(m_tf, src, (String)value).unEx(m_tf);
	else 
	    throw new Error("Bad type for CONST: " + type); 
	return constant;
    }
}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//                                                            //
// The following classes are auxiliary translation passes,    //
// required before the actual transformation into Tree form.  //
//                                                            //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//


// Abstract visitor class which preserves derivation and type information.
// This is used by all of the auxiliary translation visitors. 
//
static abstract class LowQuadWithDerivationVisitor // inner class
    extends ExtendedLowQuadVisitor implements Derivation, TypeMap {
    private Map         dT, tT;
    private Derivation  derivation;
    private TypeMap     typeMap;

    protected LowQuadWithDerivationVisitor(Derivation derivation, 
					   TypeMap typeMap) {
	this.dT         = new HashMap();
	this.tT         = new HashMap();
	this.derivation = derivation;
	this.typeMap    = typeMap;
    }

    public DList derivation(HCodeElement hce, Temp t) { 
	Util.assert(hce!=null && t!=null);
	DList deriv = 
	    (DList)dT.get(new Tuple(new Object[] { hce, t }));
	return (deriv==null)?derivation.derivation(hce, t):deriv;
    }
    
    public HClass typeMap(HCodeElement hce, Temp t) { 
	Util.assert(hce!=null && t!=null);
	Object type = tT.get(new Tuple(new Object[]{ hce, t }));
 	try { 
	    return type==null?typeMap.typeMap(hce,t):(HClass)type; 
	}
	catch (ClassCastException cce) { 
	    throw (Error)((Error)type).fillInStackTrace(); 
	}
    }

    protected void updateDT(Quad qOld, Quad qNew) { 
	for (int i=0; i<2; i++) {
	    Temp[] tmps = (i==0)?qOld.def():qOld.use();
	    for (int j=0; j<tmps.length; j++) {
		if (derivation(qOld, tmps[j]) != null) {
		    dT.put(new Tuple(new Object[] { qNew, tmps[j] }),
			   DList.clone
			   (derivation(qOld, tmps[j])));
		    tT.put(new Tuple(new Object[] { qNew, tmps[j] }),
			   new Error("*** Derived _pointers_ have no type: " +
				     tmps[j]));;
		}
		else {
		    if (this.typeMap(qOld, tmps[j]) != null) {
			tT.put(new Tuple(new Object[] { qNew, tmps[j] }), 
			       this.typeMap(qOld, tmps[j]));
		    }
		}
	    }
	}
    }

    protected void updateDT(Temp tOld, HCodeElement hOld,
			    Temp tNew, HCodeElement hNew) {
	if (derivation(hOld, tOld) != null) {
	    dT.put(new Tuple(new Object[] { hNew, tNew }),
		   DList.clone(derivation(hOld, tOld)));
	    tT.put(new Tuple(new Object[] { hNew, tNew }),
		   new Error("*** Derived _pointers_ have no type: " + tOld));
			     
	}
	else {
	    if (this.typeMap(hOld, tOld) != null) {
		tT.put(new Tuple(new Object[]{hNew,tNew}),this.typeMap(hOld, tOld));
	    }
	}
    }

    protected void addDT(Temp tmp, HCodeElement hNew, DList dl, HClass hc) {
	Util.assert(tmp != null && hNew != null);
	Util.assert(dl==null ^ hc==null,
		    "Temp "+tmp+" has derivation "+dl+" and type "+hc);
	
	if (dl!=null) { // If tmp is a derived ptr, update deriv info.
	    dT.put(new Tuple(new Object[] {hNew, tmp}), dl);
	    tT.put(new Tuple(new Object[] {hNew, tmp}), 
		   new Error("*** Can't type a derived pointer: "+tmp));
	}
	else { // If the tmp is NOT a derived pointer, assign its type
	    tT.put(new Tuple(new Object[] {hNew, tmp}), hc);
	}
    }
}


// Clones the quad graph, while preserving type/derivation information
// for the new quads
static class CloningVisitor extends LowQuadWithDerivationVisitor { //inner
    private LowQuadMap m_lqm;
    public CloningVisitor(Derivation d, TypeMap t, LowQuadMap lqm) {
	super(d, t);
	m_lqm = lqm;
    }
    public void visit(Quad q) {
	Quad qm = (Quad)q.clone();
	m_lqm.put(q, qm);
	updateDT(q, qm);
    }
}


// Adds LABELs to the destination of every branch.  This actually modifies
// the supplied Quad graph, so it is imperative that a previous 
// transformation clones the graph prior to using this visitor.    
//
static class LabelingVisitor extends LowQuadWithDerivationVisitor {//inner
    private Map m_QToL;
    public LabelingVisitor(Derivation d, TypeMap t) { 
	super(d, t);
	m_QToL = new HashMap(); 
    }
    public void visit(Quad q) { } 
    public void visit(harpoon.IR.Quads.SIGMA q) {
	Quad[] successors = q.next();
	for (int i=0; i < successors.length; i++)
	    toLabel(successors[i]);
    }
    
    public void visit(harpoon.IR.Quads.PHI q) {
	toLabel(q);
    }

    private void toLabel(Quad q) {
	Util.assert(q != null, "quad q should not equal null");

	harpoon.IR.Quads.LABEL label;
    
	label  = (harpoon.IR.Quads.LABEL)m_QToL.get(q);
	if (label==null) {
	    if (q instanceof harpoon.IR.Quads.PHI) {
		label = new harpoon.IR.Quads.LABEL
		    (q.getFactory(), 
		     (harpoon.IR.Quads.PHI)q, 
		     new Label().name);
		Quad.replace(q, label);
		m_QToL.put(q, label);  // Don't replace same quad twice!
		m_QToL.put(label, label);
	    } else {
		Util.assert(q.prevEdge().length == 1, "q:"+q+" should have arity 1");
		Quad newQ = (Quad)q.clone();  // IS THIS CORRECT????
		updateDT(q, newQ);

		label = new harpoon.IR.Quads.LABEL
		    (q.getFactory(), q, new Label().name, 
		     new Temp[] {}, q.prevEdge().length);
	        Edge prevEdge  = q.prevEdge(0);
		Quad.addEdge((Quad)prevEdge.from(), prevEdge.which_succ(),
				 label, prevEdge.which_pred());
		Quad.addEdge(label, 0, newQ, 0);
		Edge[] el = q.nextEdge();
		for (int i=0; i<el.length; i++) 
		    Quad.addEdge(newQ, el[i].which_succ(),
				 (Quad)el[i].to(), el[i].which_pred());
		m_QToL.put(q, label); // Don't replace same quad twice!
		m_QToL.put(newQ, label);
		m_QToL.put(label, label);
	    }
	}
    }
}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//                                                            //
// The following are definitions of extra quads used by the   //
// ToTree translator.  Additionally, a abstract visitor class //
// is defined to allow for the incorporation of these new     //
// quads into a visitor pattern.                              //
//                                                            //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

// Although explicit jump instructions are not necessary in quad form, 
// they are necessary in tree form.  The JMP quad instructions indicates 
// that a corresponding IR.Tree.JUMP instruction must be added in the 
// Tree form.
//
static class JMP extends harpoon.IR.Quads.Quad { // inner class
    harpoon.IR.Quads.LABEL label;
    JMP(QuadFactory qf, HCodeElement source, harpoon.IR.Quads.LABEL label) { 
	super(qf, source);
	this.label = label;
    }
    public Quad[] next() { return new Quad[] { label }; } 
    public Quad next(int i) { Util.assert(i==0); return label; } 
    public int kind() { return QuadKind.min(); }
    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	throw new Error("Should not use this method");
    }
    public void accept(QuadVisitor v) { accept((ExtendedLowQuadVisitor)v); } 
    public void accept(ExtendedLowQuadVisitor v) { v.visit(this); }
    public String toString() {return "JMP: " + label.toString();}
}

// An extension of LowQuadVisitor to handle the extra Quads
//
static abstract class ExtendedLowQuadVisitor extends LowQuadVisitor { // inner class
    protected ExtendedLowQuadVisitor() { } 
    public void visit(JMP q)              { this.visit((Quad)q); }
}


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//                                                            //
//                    Utility classes                         //
//                                                            //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

static class LowQuadMap { // inner class
    final private Map h = new HashMap();
    void put(Quad qOld, Quad qNew)  { h.put(qOld, qNew); }
    Quad get(Quad old)              { return (Quad)h.get(old); }
    boolean contains(Quad old)      { return h.containsKey(old); }
}

static class TempRenameMap implements TempMap { // inner class
    Map h = new HashMap();
    public Temp tempMap(Temp t) {
	while (h.containsKey(t)) { t = (Temp)h.get(t); }
	return t;
    }
    public void map(Temp Told, Temp Tnew) { 
	Util.assert(Tnew!=null);
	h.put(Told, Tnew); 
    }
}

} // end of public class ToTree
