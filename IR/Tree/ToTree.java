// ToTree.java, created Tue Feb 16 16:46:36 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
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
 * @version $Id: ToTree.java,v 1.1.2.41 1999-09-09 21:43:04 cananian Exp $
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
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    lqm.get((Quad)i.next()).accept(dv);

	// *MODIFY* the cloned (and labeled) quad graph to 
	// a) Not explicitly check exceptional values.   
	// b) Accept an LABEL to exception handling code as a parameter
	NameMap nm = new NameMap();
	dv = new CallConversionVisitor(dv, dv, nm);
	for (Iterator i=quadGraph(lqm.get(root)); i.hasNext();)
	    ((Quad)i.next()).accept(dv);
	
	// Construct a list of harpoon.IR.Tree.Stm objects
	handlers = ((CallConversionVisitor)dv).getHandlers();
	dv = new TranslationVisitor(tf, dv, dv, nm, ctm);
	for (Iterator i=quadGraph(lqm.get(root),handlers,true);i.hasNext();)
	    ((Quad)i.next()).accept(dv);

	// Assign member variables
	m_tree       = ((TranslationVisitor)dv).getTree();
	m_derivation = dv;
	m_typeMap    = dv;
    }
    
    private Iterator quadGraph(final Quad head) {
	return quadGraph(head, new HashSet(), false);
    }

    // Enumerates the Quad graph in depth-first order.  
    // The handlers parameter refers to exception handling code not reachable
    // from head through any set of edges, but referred to through the 
    // "retex" parameter of a function.  
    // If explicitJumps is true, adds a "JMP" quad (defined in this file)
    // when quads connected by an edge are not 
    //       a) iterated contiguously            
    // AND   b) not connected by a SIGMA node
    // 
    private Iterator quadGraph(final Quad    head,
			       final Set     handlers, 
			       final boolean explicitJumps) { 
	return new Iterator() {
	    private QuadFactory qf      = head.getFactory();
	    private Iterator    H       = handlers.iterator();
	    private Set         visited = new HashSet();
	    private Stack       s       = new Stack();
	    { s.push(head); }
	    public void remove() { throw new UnsupportedOperationException(); }
	    public boolean hasNext() { return !s.isEmpty() || H.hasNext(); }
	    public Object next() { 
		if (s.isEmpty()) {
		    if (!H.hasNext()) { throw new NoSuchElementException(); }
		    else { 
			Object l = H.next();
			Util.assert(!visited.contains(l));
			s.push(l); visited.add(l);
		    }
		}
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
}
  
// Translates the LowQuadNoSSA code into tree form. 
//
class TranslationVisitor extends LowQuadWithDerivationVisitor {
    // Keep track of which strings we have allocated 
    private static Set        m_strings = new HashSet();

    private CloningTempMap    m_ctm;          // Clones Temps to new tf
    private OffsetMap         m_offm;         // Machine-specific offset map
    private List              m_stmList;      // Holds translated statements
    private TreeFactory       m_tf;           // The new TreeFactory
    private NameMap           m_nm;           // Temp renaming
    private TEMP              m_handler = null; 
    private Map               m_labels  = new HashMap();
  
    public TranslationVisitor(TreeFactory tf, Derivation derivation, 
			      TypeMap typeMap,NameMap nm,CloningTempMap ctm) {
	super(derivation, typeMap);
	m_ctm          = ctm;
	m_tf           = tf; 
	m_offm         = m_tf.getFrame().getOffsetMap();
	m_nm           = nm;
	m_stmList      = new ArrayList();
    }

    Tree getTree() { return Stm.toStm(m_stmList); } 

    public void visit(Quad q) { /* Dont translate other quads */  }

    public void visit(harpoon.IR.Quads.ALENGTH q) {
	Stm s0 = new MOVE
	    (m_tf, q, 
	     _TEMP(q.dst(), q),
	     new MEM  
	     (m_tf, q, Type.INT, // The "length" field is of type INT
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       _TEMP(q.objectref(), q),
	       new CONST
	       (m_tf, q, m_offm.lengthOffset(typeMap(q,q.objectref()))))));

	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.ANEW q) {
	Exp classPtr, hashcode, length;
	HClass arrayClass;
	int instructionsPerIter = 12;
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
	    stms[base++] = new MOVE
		(m_tf, q, arrayRefs[i], 
		 m_tf.getFrame().memAlloc
		 (new BINOP
		  (m_tf, q, Type.INT, Bop.ADD, 
		   // Add space for hashcode, length, and finalization info
		   new CONST(m_tf, q, m_offm.size(HClass.Int) * 3),
		   new BINOP
		   (m_tf, q, Type.INT, Bop.MUL,
		    arrayDims[i+1],
		    new CONST(m_tf, q, wordSize())))));

	    hashcode  = new UNOP(m_tf, q, Type.INT, Uop._2I, arrayRefs[i]);
	    length    = arrayDims[i+1];
	    arrayClass = q.hclass();
	    for (int n=0; n<i; n++) arrayClass = arrayClass.getComponentType();
	    classPtr  = new NAME(m_tf, q, m_offm.label(arrayClass));
	
	    // Assign the array a hashcode
	    //
	    stms[base++] = new MOVE
		(m_tf, q, 
		 new MEM
		 (m_tf, q, Type.INT, 
		  new BINOP
		  (m_tf, q, Type.POINTER, Bop.ADD,
		   arrayRefs[i], 
		   new CONST(m_tf, q, m_offm.hashCodeOffset(arrayClass)))),
		 hashcode);
	    
	    // Assign the array's length field
	    //
	    stms[base++] = new MOVE
		(m_tf, q,
		 new MEM
		 (m_tf, q, Type.INT, 
		  new BINOP
		  (m_tf, q, Type.POINTER, Bop.ADD,
		   arrayRefs[i], 
		   new CONST(m_tf, q, m_offm.lengthOffset(arrayClass)))),
		 length);
	    
	    // Assign the array a class ptr
	    //
	    stms[base++] = new MOVE
		(m_tf, q, 
		 new MEM
		 (m_tf, q, Type.POINTER, 
		  new BINOP
		  (m_tf, q, Type.POINTER, Bop.ADD,
		   arrayRefs[i], 
		   new CONST(m_tf, q, m_offm.clazzPtrOffset(arrayClass)))), 
		 classPtr);

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

    
	// Make a reference to the array we are going to create
	//
	stms[instructionsPerIter*q.dimsLength()] = 
	    new MOVE(m_tf,q,_TEMP(q.dst(),q),arrayRefs[0]);
	addStmt(Stm.toStm(Arrays.asList(stms)));
    }

    public void visit(harpoon.IR.Quads.ARRAYINIT q) {
	Stm s0, s1, s2;

	// Create a pointer which we'll use to initialize the array
	TEMP  nextPtr = extra(q, Type.POINTER);
	TEMP  objTemp = _TEMP(q.objectref(), q);
	// Create derivation information for the new TEMP
	DList dl = new DList(objTemp.temp, true, null);

	s0 = new MOVE
	    (m_tf, q, 
	     nextPtr,
	     new BINOP
	     (m_tf, q, Type.POINTER, Bop.ADD,
	      objTemp, 
	      new CONST
	      (m_tf, q, 
	       m_offm.elementsOffset(typeMap(q,q.objectref())))));

	addDT(nextPtr.temp, nextPtr, dl, null);
	updateDT(q.objectref(), q, objTemp.temp, objTemp);
	addStmt(s0);
    
	for (int i=0; i<q.value().length; i++) {
	    Exp c = mapconst(q, q.value()[i], q.type());
	    MEM m;
	    if (q.type().equals(HClass.Boolean) ||
		q.type().equals(HClass.Byte))
		m = new MEM(m_tf, q, 8, true, nextPtr);
	    else if (q.type().equals(HClass.Char))
		m = new MEM(m_tf, q, 16, false, nextPtr);
	    else if (q.type().equals(HClass.Short))
		m = new MEM(m_tf, q, 16, true, nextPtr);
	    else 
		m = new MEM(m_tf, q, maptype(q.type()), nextPtr);
	    s0 = new MOVE(m_tf, q, m, c);
	    s1 = new MOVE
		(m_tf, q, 
		 nextPtr, 
		 new BINOP
		 (m_tf, q, Type.POINTER, Bop.ADD, 
		  nextPtr, 
		  new CONST(m_tf, q, m_offm.size(q.type()))));

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
	Stm s0 = new MOVE 
	    (m_tf, q, 
	     dst, 
	     isInstanceOf(q, q.objectref(), 
			  typeMap(q,q.arrayref()).getComponentType()));

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
	Stm s0   = new MOVE(m_tf, q, dst, isInstanceOf(q,q.src(),q.hclass()));
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
		     m_ctm.tempMap(m_nm.tempMap(params[i])), mParams[i+1]);
	}
	Util.assert(m_handler==null);
	m_handler = mParams[0] = extra(q, Type.POINTER);
	method    = new METHOD(m_tf, q, mParams);
	addDT(m_handler.temp,method,new DList(m_handler.temp,true,null),null);
	addStmt(segment);
	addStmt(method);
    }

    public void visit(harpoon.IR.Quads.MONITORENTER q) {
	// Call to runtime libraries here
    }

    public void visit(harpoon.IR.Quads.MONITOREXIT q) {
	// Call to runtime libraries here
    }

    public void visit(harpoon.IR.Quads.MOVE q) {
	TEMP dst = _TEMP(q.dst(), q), src = _TEMP(q.src(), q);
	Stm  s0  = new MOVE(m_tf, q, dst, src);
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.src(), q, src.temp, src);
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.NEW q) { 
	Stm s0, s1, s2;

	TEMP objectref = _TEMP(q.dst(), q);

	// Allocate memory for the new object
	s0 = new MOVE
	    (m_tf, q, 
	     objectref, 
	     m_tf.getFrame().
	     memAlloc(new CONST(m_tf, q, m_offm.size(q.hclass()))));

	// Assign the new object a hashcode
	s1 = new MOVE
	    (m_tf, q, 
	     new MEM
	     (m_tf, q, Type.INT, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       objectref,
	       new CONST(m_tf, q, m_offm.hashCodeOffset(q.hclass())))),
	     new UNOP(m_tf, q, Type.POINTER, Uop._2I, objectref));
	
	// Assign the new object a class pointer
	s2 = new MOVE
	    (m_tf, q,
	     new MEM
	     (m_tf, q, Type.POINTER,
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       objectref,
	       new CONST(m_tf, q, m_offm.clazzPtrOffset(q.hclass())))),
	     new NAME(m_tf, q, m_offm.label(q.hclass())));
	
	updateDT(q.dst(), q, objectref.temp, objectref);
	addStmt(Stm.toStm(Arrays.asList(new Stm[] { s0, s1, s2 })));
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
	     new BINOP // Array offset are always INT types
	     (m_tf, q, Type.INT, Bop.MUL, 
	      new CONST
	      (m_tf, q, m_offm.size(q.arrayType().getComponentType())),
	      index));

	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.index(), q, index.temp, index);
	addStmt(s0);
    }

    public void visit(PARRAY q) {
	TEMP dst = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);
	Stm  s0  = new MOVE(m_tf, q, dst, objectref);

	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }


    public void visit(PCALL q) { 
	throw new Error("Should not have unlabeled PCALLs in the quad graph");
    }

    public void visit(PCALL_WITH_LABEL q) {
	ExpList params; Temp[] qParams; TEMP retval, retex, func; 
	Stm s0;

	Util.assert(q.retex()!=null && q.ptr()!=null);

	// If q.retval() is null, create a dummy TEMP for the retval
	//
	if (q.retval()==null) {
	    retval = extra(q, TYPE(q, q.retval()));
	    addDT(retval.temp, retval, null, HClass.Void);
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
	    
	qParams = q.params(); params = null; 
	for (int i=qParams.length-1; i >= 0; i--) {
	    params = new ExpList(_TEMP(qParams[i], q), params);      
	    updateDT(qParams[i], q, ((TEMP)params.head).temp, params.head);
	}
	    
	s0 = new CALL
	    (m_tf, q, 
	     retval, 
	     new NAME(m_tf, q, _LABEL(q.labelex).label),
	     func, params);

	updateDT(q.retex(), q, retex.temp, retex);
	updateDT(q.ptr(), q, func.temp, func);
	addStmt(s0); 
    }

    public void visit(PFCONST q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     dst,
	     new NAME(m_tf, q, m_offm.label(q.field())));

	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(PFIELD q) { 
	TEMP dst = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);
	Stm s0 = new MOVE(m_tf, q, dst, objectref);
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }
  
    public void visit(PFOFFSET q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE(m_tf, q, dst, new CONST(m_tf,q,m_offm.offset(q.field())));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(PGET q) {
	TEMP dst = _TEMP(q.dst(), q), ptr = _TEMP(q.ptr(), q);
	Stm s0 = new MOVE(m_tf, q, dst, new MEM(m_tf,q,TYPE(q,q.dst()),ptr));
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.ptr(), q, ptr.temp, ptr);
	addStmt(s0);
    }
  
    public void visit(PMCONST q) { 
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE(m_tf, q, dst, new NAME(m_tf,q,m_offm.label(q.method())));
	updateDT(q.dst(), q, dst.temp, dst);
	addStmt(s0);
    }

    public void visit(PMETHOD q) {
	HClass type = typeMap(q,q.objectref());
	TEMP   dst  = _TEMP(q.dst(), q), objectref = _TEMP(q.objectref(), q);

	// FIXME: type of object should not be void!
	if (type==HClass.Void) type = HClass.forName("java.lang.Object");
	
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     dst, 
	     new MEM
	     (m_tf, q, Type.POINTER,
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD, 
	       objectref, 
	       new CONST
	       (m_tf, q, m_offm.clazzPtrOffset(type)))));
	
	updateDT(q.dst(), q, dst.temp, dst);
	updateDT(q.objectref(), q, objectref.temp, objectref);
	addStmt(s0);
    }

    public void visit(PMOFFSET q) {
	TEMP dst = _TEMP(q.dst(), q);
	Stm s0 = 
	    new MOVE(m_tf,q,dst,new CONST(m_tf,q,m_offm.offset(q.method())));
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
	Stm s0 = new MOVE(m_tf, q, new MEM(m_tf,q,TYPE(q,q.src()),ptr),src);
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
		    isValidMapping(typeMap(source,t), type));
	Temp nTmp = m_ctm.tempMap(m_nm.tempMap(t));
	return new TEMP(m_tf, source, type, nTmp);
    }

    private int TYPE(HCodeElement src, Temp t) { 
	return hastype(src, t)?maptype(typeMap(src,t)):Type.POINTER; 
    }

    private boolean hastype(HCodeElement hce, Temp t) { 
	return derivation(hce, t)==null; 
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
	else if (type==HClass.forName("java.lang.String")) {
	    constant = new MEM
		(m_tf, src, Type.POINTER, 
		 new NAME(m_tf, src, m_offm.label((String)value)));
	    if (!m_strings.contains(value)) { allocString((String)value); } 
	}
	else 
	    throw new Error("Bad type for CONST: " + type); 
	return constant;
    }

    // Allocates an instanceof of java.lang.String somewhere in 
    // the string segments.  
    private void allocString(String str) { 
	HClass    HCstring  = HClass.forName("java.lang.String");
	HClass    HCcharA   = HClass.forDescriptor("[C");
	Label     strClsPtr = m_offm.label(HCstring);
	Label     caClsPtr  = m_offm.label(HCcharA);
	HField    count     = HCstring.getField("count");
	HField    offset    = HCstring.getField("offset");
	HField    value     = HCstring.getField("value");

	ArrayList u         = new ArrayList();
	ArrayList d         = new ArrayList();
	List      stms      = new ArrayList();

	//
	// STEP 1: Construct the String object

	// Assign the String a HashCode
	add(m_offm.hashCodeOffset(HCstring),
	    new DATA(m_tf,null,new CONST(m_tf,null,str.hashCode())),u,d);
	// Assign the String a clazz ptr
	add(m_offm.clazzPtrOffset(HCstring),
	    new DATA(m_tf,null,new NAME(m_tf,null,strClsPtr)),u,d);
	// Set the "count" field to the length of the string constant
	add(m_offm.offset(count),
	    new DATA(m_tf,null,new CONST(m_tf,null,str.length())),u,d);
	// Set the "offset" field to 0
	add(m_offm.offset(offset),
	    new DATA(m_tf,null,new CONST(m_tf,null,0)),u,d);
	// Set the "value" field to point to a char array (created below)
	add(m_offm.offset(value),
	    new DATA
	    (m_tf,null,
	     new NAME
	     (m_tf,null,_LABEL(str+"CA",null).label)),u,d);
	
	Collections.reverse(u);
	u.add(0, new LABEL(m_tf, null, m_offm.label(str), true));
	u.add(0, new SEGMENT(m_tf, null, SEGMENT.STRING_CONSTANTS));
	stms.addAll(u);
	stms.addAll(d);

	u.clear(); d.clear();
	//
	// STEP 2: Construct the character array the string points to
	// Assign the character array a HashCode
	add(m_offm.lengthOffset(HCcharA),
	    new DATA(m_tf,null, 
		     new CONST(m_tf,null,str.length())),u,d);
	add(m_offm.hashCodeOffset(HCcharA),
	    new DATA(m_tf,null,
		     new CONST(m_tf,null,str.toCharArray().hashCode())),u,d);
	// Assign the character array a clazz ptr
	add(m_offm.clazzPtrOffset(HCcharA),
	    new DATA(m_tf,null,new NAME(m_tf,null,caClsPtr)),u,d);
	for (int i=0; i<str.length(); i++) { 
	    add(m_offm.elementsOffset(HCcharA)+i, 
		new DATA
		(m_tf, null, 
		 new CONST(m_tf, null, 16, true, (int)str.charAt(i))),u,d);
	}
	Collections.reverse(u);
	u.add(0, _LABEL(str+"CA", null));
	u.add(0, new SEGMENT(m_tf, null, SEGMENT.STRING_DATA));
	stms.addAll(u);
	stms.addAll(d);
	stms.add(new SEGMENT(m_tf, null, SEGMENT.CODE));
	
	addStmt(Stm.toStm(stms));
    }
    
    private void add(int index, Tree elem, ArrayList up, ArrayList down) { 
	int requiredSize;
	if (index<0) { 
	    requiredSize = (-index);
	    if (requiredSize > up.size()) { 
		up.ensureCapacity(requiredSize);
		for (int i=up.size(); i<requiredSize; i++) 
		    up.add(new DATA(elem.getFactory(), elem, Type.POINTER));
	    }	    
	    up.set(-index-1, elem);
	}
	else {
	    requiredSize = index+1;
	    if (requiredSize > down.size()) { 
		down.ensureCapacity(requiredSize);
		for (int i=down.size(); i<requiredSize; i++) 
		    down.add(new DATA(elem.getFactory(), elem, Type.POINTER));
	    }	    
	    down.set(index, elem);
	}
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                            *
     *                Run-time typechecking code                  *
     *                                                            *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    
    // Returns an Exp that evaluates to true if "q" can be cast to "type"
    // with this algorithm.  This algorithm is now substantially different
    // than the recursive casting conversion algorithm
    // found in the Java Language Specification at 
    // http://java.sun.com/docs/books/jls/html/5.doc.html#176921.  
    // 
    // FIXME:  <insert description of algorithm here>
    private Exp isInstanceOf(Quad q, Temp src, HClass type) {

	TEMP SRC1     = _TEMP(src, q);
	TEMP SRC2     = _TEMP(src, q);
	TEMP RESULT1  = extra(q, Type.INT);
	TEMP RESULT2  = _TEMP(RESULT1);
	TEMP RESULT3  = _TEMP(RESULT1);
	
	updateDT(src, q, SRC1.temp, SRC1);
	updateDT(src, q, SRC2.temp, SRC2);
	addDT(RESULT1.temp, RESULT1, null, HClass.Int);
	updateDT(RESULT1.temp, RESULT1, RESULT2.temp, RESULT2);
	updateDT(RESULT1.temp, RESULT1, RESULT3.temp, RESULT3);

	if (type.isPrimitive()) {
	    return new CONST(m_tf,q,type==typeMap(q,src)?1:0);
	}
	else {
	    Util.assert(TYPE(q,src)==Type.POINTER);
	    LABEL isNonNull = new LABEL(m_tf, q, new Label(), false);
	    LABEL isNull    = new LABEL(m_tf, q, new Label(), false);
	    LABEL end       = new LABEL(m_tf, q, new Label(), false);

	    return new ESEQ
		(m_tf, q, 
		 Stm.toStm
		 (Arrays.asList
		  (new Stm[] {
		      // null instanceof X  is always false
		      new CJUMP
			  (m_tf, q, 
			   new BINOP
			   (m_tf, q, Type.POINTER, Bop.CMPEQ,
			    SRC1, 
			    new CONST(m_tf, q, 0)),
			   isNull.label, 
			   isNonNull.label),

		      // we've done instanceof on a null pointer, return false
		      isNull,
		      new MOVE(m_tf, q, RESULT1, new CONST(m_tf, q, 0)),
		      new JUMP(m_tf, q, end.label),

		      // the pointer is not null, use our run-time 
		      // type-checking algorithm
		      isNonNull,
		      new MOVE
			  (m_tf, q, 
			   RESULT2,
			   HClassUtil.baseClass(type).isInterface() ? 
			   isImplemented
			   (new MEM 
			    (m_tf, q, Type.POINTER,
			     new BINOP
			     (m_tf, q, Type.POINTER, Bop.ADD,
			      new CONST
			      (m_tf, q, 
			       m_offm.clazzPtrOffset(typeMap(q,src))),
			      SRC2)), 
			    type) : 
			   classExtends
			   (new MEM 
			    (m_tf, q, Type.POINTER,
			     new BINOP
			     (m_tf, q, Type.POINTER, Bop.ADD,
			      new CONST
			      (m_tf, q, 
			       m_offm.clazzPtrOffset(typeMap(q,src))),
			      SRC2)),
			    type)), 
		      end})),
		 RESULT3);
	}
    }
  
    private Exp classExtends(Exp classPtr, HClass type) { 
	Util.assert(!(HClassUtil.baseClass(type).isInterface() || 
		      HClassUtil.baseClass(type).isPrimitive()));

	NAME typeLabel   = new NAME(m_tf, classPtr, m_offm.label(type));
	return new BINOP
	    (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
	     typeLabel,
	     new MEM
	     (m_tf, classPtr, Type.POINTER,
	      new BINOP
	      (m_tf, classPtr, Type.POINTER, Bop.ADD,
	       classPtr,
	       new CONST
	       (m_tf, classPtr, m_offm.offset(type)))));
    }

    private Exp isImplemented(Exp classPtr, HClass type) {
	Util.assert(HClassUtil.baseClass(type).isInterface());

	NAME  typeLabel   = new NAME(m_tf, classPtr, m_offm.label(type));
	TEMP  IFACEPTR1   = extra(classPtr, Type.POINTER);
	TEMP  IFACEPTR2   = _TEMP(IFACEPTR1);
	TEMP  IFACEPTR3   = _TEMP(IFACEPTR1);
	TEMP  IFACEPTR4   = _TEMP(IFACEPTR1);
	TEMP  IFACELABEL1 = extra(classPtr, Type.POINTER);
	TEMP  IFACELABEL2 = _TEMP(IFACELABEL1);
	TEMP  IFACELABEL3 = _TEMP(IFACELABEL1);
	TEMP  RESULT1     = extra(classPtr, Type.INT);
	TEMP  RESULT2     = _TEMP(RESULT1);
	TEMP  RESULT3     = _TEMP(RESULT1);

	// FIXME: add deriv info 

	LABEL endLabel      = new LABEL(m_tf, classPtr, new Label(), false);
	LABEL loop          = new LABEL(m_tf, classPtr, new Label(), false);
	LABEL next          = new LABEL(m_tf, classPtr, new Label(), false);
	LABEL successLabel  = new LABEL(m_tf, classPtr, new Label(), false);
	LABEL failureLabel  = new LABEL(m_tf, classPtr, new Label(), false);
	
	return new ESEQ
	    (m_tf, classPtr, 
	     Stm.toStm
	     (Arrays.asList
	      (new Stm[] {

		  // Assign IFACEPTR to point to the block of memory 
		  // directly before the first interface.
		  // 
		  new MOVE
		      (m_tf, classPtr,
		       IFACEPTR1,
		       new MEM
		       (m_tf, classPtr, Type.POINTER, 
			new BINOP
			(m_tf, classPtr, Type.POINTER, Bop.ADD,
			 classPtr, 
			 new CONST
			 (m_tf, classPtr, 
			  m_offm.interfaceListOffset(type)-wordSize())))),
		  
		  // Label the top of the loop
		  //
		  loop,

		  // Increment the current interface ptr
	          // 
		  new MOVE
		      (m_tf, classPtr, 
		       IFACEPTR2,
		       new BINOP
		       (m_tf, classPtr, Type.POINTER, Bop.ADD,
			IFACEPTR3,
			new CONST(m_tf, classPtr, wordSize()))),

		  new MOVE
		      (m_tf, classPtr,
		       IFACELABEL1, 
			new MEM(m_tf, classPtr, Type.POINTER, IFACEPTR4)),

		  // Check if we have reached the end of the interface list
		  //
		  new CJUMP
		      (m_tf, classPtr, 
		       new BINOP
		       (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
			IFACELABEL2, 
			new CONST(m_tf, classPtr)),
		       failureLabel.label,
		       next.label),
		  
		  next,
	
		  // See if we've found the correct label
		  // 
		  new CJUMP
		      (m_tf, classPtr, 
		       new BINOP
		       (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
			typeLabel,
			IFACELABEL3),
		       successLabel.label,
		       loop.label),
		  
		  // We've reached the end of the interface list without
		  // finding a match.  Return 0.
		  // 
		  failureLabel,
	
		  new MOVE
		      (m_tf, classPtr, 
		       RESULT1, 
		       new CONST(m_tf, classPtr, 0)),

		  new JUMP(m_tf, classPtr, endLabel.label),

		  // We found the interface label we wanted.  Yay!  Return 1.
		  // 
		  successLabel,
		  new MOVE
		      (m_tf, classPtr, 
		       RESULT2, 
		       new CONST(m_tf, classPtr, 1)),
		  
		  endLabel
		      })),
	     RESULT3);

    }
	
    private int wordSize() {
	int size = m_offm.size(HClass.forName("java.lang.Object"));
	return size;
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
abstract class LowQuadWithDerivationVisitor 
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
		    if (typeMap(qOld, tmps[j]) != null) {
			tT.put(new Tuple(new Object[] { qNew, tmps[j] }), 
			       typeMap(qOld, tmps[j]));
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
	    if (typeMap(hOld, tOld) != null) {
		tT.put(new Tuple(new Object[]{hNew,tNew}),typeMap(hOld, tOld));
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
class CloningVisitor extends LowQuadWithDerivationVisitor {
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


// Updates the CFG to remove explicit exception checks.  Additionally
// converts PCALL quads into PCALL_WITH_LABEL quads.  The LABEL associated
// with these quads is a pointer to the exception handling code of the
// PCALL.  
//
// FIXME:  needs to deal with PCALLs that do not match this simple pattern
//
class CallConversionVisitor extends LowQuadWithDerivationVisitor { 
    private Set                    handlers = new HashSet();
    private PCALL                  pcall;
    private harpoon.IR.Quads.CONST nconst;
    private POPER                  acmpeq;
    private harpoon.IR.Quads.CJMP  cjump;
    private NameMap                nm;
 
    public CallConversionVisitor(Derivation d, TypeMap t, NameMap nm) {
	super(d, t);
	this.nm = nm;
    }

    public void visit(harpoon.IR.Quads.Quad quad) { 
	this.pcall=null; this.nconst=null; this.acmpeq=null; this.cjump=null; 
    }

    public void visit(harpoon.IR.Quads.CJMP cjump) { 
	if ((acmpeq != null) && (this.acmpeq.dst() == cjump.test())) {
	    this.cjump = cjump;
	    replaceExceptionCheck();
	}
	this.pcall=null; this.nconst=null; this.acmpeq=null; this.cjump=null;
    }

    public void visit(harpoon.IR.Quads.CONST nconst) { 
	if ((this.pcall != null) && (nconst.value() == null)) { 
	    this.nconst = nconst; 
	    this.acmpeq = null; this.cjump = null;
	}
	else {   
	    this.pcall=null; this.nconst=null; 
	    this.acmpeq=null; this.cjump=null; 
	}
    }

    public void visit(PCALL pcall) { 
	// If the pcall does not have a place for a return value, give it one
	if (pcall.retval()==null) { 
	    Temp  retval = new Temp(pcall.retex().tempFactory());
	    PCALL rCall  = new PCALL((LowQuadFactory)pcall.getFactory(), pcall,
				     pcall.ptr(), pcall.params(), retval, 
				     pcall.retex());
	    updateDT(pcall, rCall);
	    addDT(retval,rCall,null,typeMap(pcall,pcall.retex()));
	    Quad.replace(pcall, rCall);  pcall=rCall;
	}
	this.pcall = pcall;
	this.nconst=null; this.acmpeq=null; this.cjump=null;
	nm.map(this.pcall.retex(), this.pcall.retval());
    }

    public void visit(POPER poper) { 
	if ((this.nconst != null)                       && 
	    (poper.opcode() == Qop.ACMPEQ)              &&
	    (this.pcall.retex() == poper.operands()[0]) &&
	    (this.nconst.dst() == poper.operands()[1])) {
	    this.acmpeq = poper;
	    this.cjump  = null;
	}
	else { 	
	    this.pcall=null; this.nconst=null;
	    this.acmpeq=null; this.cjump = null; 
	}
    }

    // Return a set of LABELs which precede each block of exception handling
    // code. 
    Set getHandlers() { return this.handlers; } 

    // Convert   pcall --> nconst --> acmpeq --> cjump(-->true, -->false)
    // to        pcall --> true
    //             
    private void replaceExceptionCheck() { 
	PCALL newCall    = 
	    new PCALL_WITH_LABEL((LowQuadFactory)pcall.getFactory(), pcall, 
				 pcall.ptr(),pcall.params(), pcall.retval(), 
				 pcall.retex(),
				 (harpoon.IR.Quads.LABEL)cjump.next(0));
	updateDT(pcall, newCall);
	Quad.replace(pcall, newCall);
	Quad.addEdge(newCall, 0, 
		     cjump.next()[1], cjump.nextEdge(1).which_pred());
	handlers.add(cjump.next(0));
    }
}
    
// Adds LABELs to the destination of every branch.  This actually modifies
// the supplied Quad graph, so it is imperative that a previous 
// transformation clones the graph prior to using this visitor.    
//
class LabelingVisitor extends LowQuadWithDerivationVisitor {  
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
	harpoon.IR.Quads.LABEL label;
    
	label  = (harpoon.IR.Quads.LABEL)m_QToL.get(q);
	if (label==null) {
	    if (q instanceof harpoon.IR.Quads.PHI) {
		label = new harpoon.IR.Quads.LABEL
		    (q.getFactory(), 
		     (harpoon.IR.Quads.PHI)q, 
		     new Label().toString());
		Quad.replace(q, label);
		m_QToL.put(q, label);  // Don't replace same quad twice!
	    }
	    else {
		Quad newQ = (Quad)q.clone();  // IS THIS CORRECT????
		updateDT(q, newQ);

		label = new harpoon.IR.Quads.LABEL
		    (q.getFactory(), q, new Label().toString(), 
		     new Temp[] {}, q.prevEdge().length);
		Edge[] el = q.prevEdge();
		for (int i=0; i<el.length; i++) 
		    Quad.addEdge((Quad)el[i].from(), el[i].which_succ(),
				 label, el[i].which_pred());
		Quad.addEdge(label, 0, newQ, 0);
		el = q.nextEdge();
		for (int i=0; i<el.length; i++) 
		    Quad.addEdge(newQ, el[i].which_succ(),
				 (Quad)el[i].to(), el[i].which_pred());
		m_QToL.put(q, label); // Don't replace same quad twice!
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

// A simple extension of PCALL, provides an additional field, labelex,
// which is the location of the PCALL's exception handler.
//
class PCALL_WITH_LABEL extends PCALL { 
    harpoon.IR.Quads.LABEL labelex;
    PCALL_WITH_LABEL(LowQuadFactory qf, HCodeElement source,
		     Temp ptr, Temp[] params, Temp retval, Temp retex, 
		     harpoon.IR.Quads.LABEL labelex) {
	super(qf, source, ptr, params, retval, retex);
	this.labelex = labelex;
    }
    public int kind() { return QuadKind.min(); }  
    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	throw new Error("Should not use this method");
    }
    public void visit(QuadVisitor v) { visit((ExtendedLowQuadVisitor)v); } 
    public void visit(ExtendedLowQuadVisitor v) { v.visit(this); }
    public String toString() {
	return "PCALL_WITH_LABEL: "+labelex.toString()+"\n"+super.toString();
    }
}

// Although explicit jump instructions are not necessary in quad form, 
// they are necessary in tree form.  The JMP quad instructions indicates 
// that a corresponding IR.Tree.JUMP instruction must be added in the 
// Tree form.
//
class JMP extends harpoon.IR.Quads.Quad { 
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
abstract class ExtendedLowQuadVisitor extends LowQuadVisitor { 
    protected ExtendedLowQuadVisitor() { } 
    public void visit(PCALL_WITH_LABEL q) { visit((PCALL)q); } 
    public void visit(JMP q)              { visit((Quad)q); }
}


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//                                                            //
//                    Utility classes                         //
//                                                            //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

class LowQuadMap {
    final private Map h = new HashMap();
    void put(Quad qOld, Quad qNew)  { h.put(qOld, qNew); }
    Quad get(Quad old)              { return (Quad)h.get(old); }
    boolean contains(Quad old)      { return h.containsKey(old); }
}

class NameMap implements TempMap {
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
