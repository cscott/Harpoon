// ToTree.java, created Tue Feb 16 16:46:36 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.UseDef;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
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
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @version $Id: ToTree.java,v 1.1.2.25 1999-08-06 22:30:49 pnkfelix Exp $
 */
public class ToTree implements Derivation, TypeMap {
    private Derivation  m_derivation;
    private Tree        m_tree;
    private TypeMap     m_typeMap;
  
    /** Class constructor */
    public ToTree(final TreeFactory tf, LowQuadNoSSA code) {
	Util.assert(tf.getParent().getName().equals("tree"));

	final Map dT = new HashMap();
	
	m_tree = translate(tf, code, dT);
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		if ((hce==null)||(t==null)) return null;
		else {
		    Object deriv = dT.get(new Tuple(new Object[] { hce, t }));
		    if (deriv instanceof Error)
			throw (Error)((Error)deriv).fillInStackTrace();
		    else
			return (DList)deriv;
		}
	    }
	};
	m_typeMap = new TypeMap() {
	    public HClass typeMap(HCode hc, Temp t) {
		Util.assert(t.tempFactory()==tf.tempFactory());
		if (t==null) return null;
		else {
		    Object type = dT.get(t);   // Ignores hc parameter
		    if (type instanceof Error) 
			throw (Error)((Error)type).fillInStackTrace();
		    else                       
			return (HClass)type;
		}
	    }
	};
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
    public HClass typeMap(HCode hc, Temp t) {
	// Ignores HCode parameter
	return m_typeMap.typeMap(hc, t);
    }

    private Tree translate(TreeFactory tf, LowQuadNoSSA code, Map dT) {
	Set                           handlers;
	CloningTempMap                ctm;
	LowQuadMap                    lqm;
	LowQuadWithDerivationVisitor  dv;
	Quad                          root;
	Stm                           tree;
	StmList                       stmList;
	TranslationVisitor            tv;
	
	root = (Quad)code.getRootElement();
	ctm = new CloningTempMap
	    (root.getFactory().tempFactory(),tf.tempFactory());


	// Clone the Quad graph
	lqm = new LowQuadMap();
	dv = new CloningVisitor(code, code, code, lqm);
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    ((Quad)i.next()).visit(dv);
	
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
	dv = new LabelingVisitor(dv.getDerivation(), code, dv.getTypeMap());
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    lqm.get((Quad)i.next()).visit(dv);

	// *MODIFY* the cloned (and labeled) quad graph to 
	// a) Not explicitly check exceptional values.   
	// b) Accept an LABEL to exception handling code as a parameter
	dv = new CallConversionVisitor
	    (dv.getDerivation(), code, dv.getTypeMap());
	for (Iterator i=quadGraph(lqm.get(root)); i.hasNext();)
	    ((Quad)i.next()).visit(dv);
	
	// Construct a list of harpoon.IR.Tree.Stm objects
	handlers = ((CallConversionVisitor)dv).getHandlers();
	tv = new TranslationVisitor(tf, dv.getDerivation(), code, 
				    dv.getTypeMap(), ctm, dT);
	for (Iterator i=quadGraph(lqm.get(root),handlers,true);i.hasNext();) 
	    ((Quad)i.next()).visit(tv);
	
	return tv.getTree();
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
class TranslationVisitor extends ExtendedLowQuadVisitor {

    private LowQuadNoSSA      m_code;         // The codeview to translate
    private CloningTempMap    m_ctm;          // Clones Temps to new tf
    private Derivation        m_derivation;   // Old derivation info
    private Map               m_dT;           // Updated deriv & type info
    private Frame             m_frame;        // The machine-specific Frame
    private LabelMap          m_labelMap;     // Converts labels to Tree LABELs
    private OffsetMap         m_offm;         // Machine-specific offset map
    private List              m_stmList;      // Holds translated statements
    private TreeTempMap       m_tempMap;      // Maps Temps to Tree TEMPs
    private TreeFactory       m_tf;           // The new TreeFactory
    private TypeMap           m_typeMap;      // Old typing info
    private TEMP              m_handler = null; 
  
    public TranslationVisitor(TreeFactory tf, Derivation derivation, 
			      LowQuadNoSSA code, TypeMap typeMap, 
			      CloningTempMap ctm, Map dT) {
	m_code         = code;
	m_ctm          = ctm;
	m_derivation   = derivation;
	m_dT           = dT;
	m_frame        = tf.getFrame();
	m_labelMap     = new LabelMap();
	m_offm         = m_frame.getOffsetMap();
	m_tempMap      = new TreeTempMap();
	m_tf           = tf;
	m_typeMap      = typeMap;
	m_stmList      = new ArrayList();
    }

    Tree getTree() { return Stm.toStm(m_stmList); } 

    public void visit(Quad q) { /* Dont translate other quads */  }

    public void visit(harpoon.IR.Quads.ALENGTH q) {
	Stm s0 = new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new MEM  
	     (m_tf, q, Type.INT, // The "length" field is of type INT
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       MAP(q.objectref(), q),
	       new CONST(m_tf, q, m_offm.lengthOffset(type(q.objectref()))))));

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
	    loopHeaders[i]   = new LABEL(m_tf, q, new Label());
	    loopMid[i]       = new LABEL(m_tf, q, new Label());
	    loopFooters[i]   = new LABEL(m_tf, q, new Label());
	    arrayDims[i+1]   = MAP(q.dims(i), q);
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
		 m_frame.memAlloc
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
		   new CONST(m_tf, q, m_offm.classOffset(arrayClass)))), 
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
	    new MOVE(m_tf,q,MAP(q.dst(),q),arrayRefs[0]);
	addStmt(Stm.toStm(Arrays.asList(stms)));
    }

    public void visit(harpoon.IR.Quads.ARRAYINIT q) {
	Stm s0, s1, s2;

	// Create a pointer which we'll use to initialize the array
	TEMP  nextPtr = extra(q.objectref(), q, Type.POINTER);
	// Create derivation information for the new TEMP
	DList dl = new DList(MAP(q.objectref(), q).temp, true, null);

	s0 = new MOVE
	    (m_tf, q, 
	     nextPtr,
	     new BINOP
	     (m_tf, q, Type.POINTER, Bop.ADD,
	      MAP(q.objectref(), q),
	      new CONST
	      (m_tf, q, 
	       m_offm.elementsOffset(type(q.objectref())))));

	updateDT(nextPtr.temp, nextPtr, dl, null);
	updateDT(q.objectref(), q, MAP(q.objectref(), q));
	addStmt(s0);
    
	for (int i=0; i<q.value().length; i++) {
	    s0 = new MOVE
		(m_tf, q, 
		 new MEM(m_tf, q, maptype(q.type()), nextPtr), 
		 mapconst(q, q.value()[i], q.type()));
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
	Stm s0 = new CJUMP
	    (m_tf, q, MAP(q.test(), q),
	     (MAP((harpoon.IR.Quads.LABEL)q.next(1))).label,
	     (MAP((harpoon.IR.Quads.LABEL)q.next(0))).label);
    
	updateDT(q.test(), q, MAP(q.test(), q));
	addStmt(s0);
    }
  
    public void visit(harpoon.IR.Quads.COMPONENTOF q) {
	Stm s0 = new MOVE 
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     isInstanceOf(q, q.objectref(), 
			  type(q.arrayref()).getComponentType()));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.CONST q) {
	Stm s0 = new MOVE
	    (m_tf, q, MAP(q.dst(), q), mapconst(q, q.value(), q.type()));
    
	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }
  
    public void visit(harpoon.IR.Quads.INSTANCEOF q) {
	Stm s0 =new MOVE
	    (m_tf, q, MAP(q.dst(), q), 
	     isInstanceOf(q, q.src(), q.hclass()));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    public void visit(JMP q) { 
	addStmt(new JUMP(m_tf, q, MAP(q.label).label));
    }

    public void visit(harpoon.IR.Quads.LABEL q) {
	addStmt(MAP(q));
    }

    public void visit(harpoon.IR.Quads.METHOD q) {
	METHOD method;
	Temp   params[]  = q.params(); 
	TEMP   mParams[] = new TEMP[params.length+1];
	
	for (int i=0; i<params.length; i++) mParams[i+1] = MAP(params[i], q);
	Util.assert(m_handler==null);
	m_handler = mParams[0] = extra(q, Type.POINTER);
	method    = new METHOD(m_tf, q, mParams);
	updateDT
	    (m_handler.temp,method,new DList(m_handler.temp,true,null),null);
	addStmt(method);
    }

    public void visit(harpoon.IR.Quads.MONITORENTER q) {
	// Call to runtime libraries here
    }

    public void visit(harpoon.IR.Quads.MONITOREXIT q) {
	// Call to runtime libraries here
    }

    public void visit(harpoon.IR.Quads.MOVE q) {
	Stm s0 = new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.src(), q));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.src(), q, MAP(q.src(), q));
	addStmt(s0);
    }

    public void visit(harpoon.IR.Quads.NEW q) { 
	Stm s0, s1, s2;

	TEMP objectref = MAP(q.dst(), q);

	// Allocate memory for the new object
	s0 = new MOVE
	    (m_tf, q, 
	     objectref, 
	     m_frame.memAlloc(new CONST(m_tf, q, m_offm.size(q.hclass()))));

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
	       new CONST(m_tf, q, m_offm.classOffset(q.hclass())))),
	     new NAME(m_tf, q, m_offm.label(q.hclass())));
	
	updateDT(q.dst(), q, objectref);
	addStmt(Stm.toStm(Arrays.asList(new Stm[] { s0, s1, s2 })));
    }
	
    public void visit(harpoon.IR.Quads.RETURN q) {
	Exp retval;
    
	if (q.retval()==null) {
	    retval = new CONST(m_tf, q, 0);
	}
	else {
	    retval = MAP(q.retval(), q);
	    updateDT(q.retval(), q, retval);
	}

	Stm s0 = new RETURN(m_tf, q, retval);    
	addStmt(s0);
    }

    // Naive implementation
    public void visit(harpoon.IR.Quads.SWITCH q) { 
	Quad qNext;  CJUMP branch; LABEL lNext;
	TEMP discriminant = MAP(q.index(), q);
	for (int i=0; i<q.keysLength(); i++) {
	    qNext  = q.next(i); 
	    Util.assert(qNext instanceof harpoon.IR.Quads.LABEL);

	    lNext  = new LABEL(m_tf, q, new Label());
	    branch = new CJUMP
		(m_tf, q, new BINOP(m_tf, q, Type.LONG, Bop.CMPEQ, 
				    discriminant, 
				    new CONST(m_tf, q, q.keys(i))),
		 (MAP((harpoon.IR.Quads.LABEL)qNext)).label,
		 lNext.label);
	    addStmt(Stm.toStm(Arrays.asList(new Stm[] { branch, lNext })));
	}
	updateDT(q.index(), q, discriminant);
    }
  
    public void visit(harpoon.IR.Quads.THROW q) { 
	Util.assert(m_handler!=null);
	Stm s0 = new THROW(m_tf, q, MAP(q.throwable(), q), m_handler);
	updateDT(q.throwable(), q, MAP(q.throwable(), q));
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
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     new BINOP // Array offset are always INT types
	     (m_tf, q, Type.INT, Bop.MUL, 
	      new CONST
	      (m_tf, q, m_offm.size(q.arrayType().getComponentType())),
	      MAP(q.index(), q)));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.index(), q, MAP(q.index(), q));
	addStmt(s0);
    }

    public void visit(PARRAY q) {
	Stm s0  = 
	    new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.objectref(), q));

	updateDT(q.dst(), q, MAP(q.dst(), q));
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
	    retval = extra(q.ptr(), q, TYPE(q, q.retval()));
	    m_dT.put
		(retval, 
		 new Error
		 ("*** Return value of void function has no type"));
	    m_dT.put
		(new Tuple(new Object[]{q, retval}), 
		 new Error
		 ("*** Return value of void function has no derivation"));
	}
	else {
	    retval = MAP(q.retval(), q);
	    updateDT(q.retval(), q, retval);
	}
      
	// Assign TEMPs for the exceptional value and function pointer.
	// These can not be null.
	//
	retex = MAP(q.retex(), q);
	func  = MAP(q.ptr(), q);
	    
	qParams = q.params(); params = null; 
	for (int i=qParams.length-1; i >= 0; i--) {
	    params = new ExpList(MAP(qParams[i], q), params);      
	    updateDT(qParams[i], q, MAP(qParams[i], q));
	}
	    
	s0 = new CALL
	    (m_tf, q, 
	     retval, 
	     new NAME(m_tf, q, MAP(q.labelex).label),
	     func, params);

	updateDT(q.ptr(), q, func);
	addStmt(s0); 
    }

    public void visit(PFCONST q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new NAME(m_tf, q, m_offm.label(q.field())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    public void visit(PFIELD q) { 
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     MAP(q.objectref(), q));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.objectref(), q, MAP(q.objectref(), q));
	addStmt(s0);
    }
  
    public void visit(PFOFFSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     new CONST(m_tf, q, m_offm.offset(q.field())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    public void visit(PGET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     new MEM(m_tf, q, TYPE(q, q.dst()), MAP(q.ptr(), q)));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.ptr(), q, MAP(q.ptr(), q));
	addStmt(s0);
    }
  
    public void visit(PMCONST q) { 
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new NAME(m_tf, q, m_offm.label(q.method())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    public void visit(PMETHOD q) {
      HClass type = type(q.objectref());
      
      // FIXME: type of object should not be void!
      if (type==HClass.Void) type = HClass.forName("java.lang.Object");

      Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),              
	     new MEM
	     (m_tf, q, Type.POINTER,
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD, 
	       MAP(q.objectref(), q), 
	       new CONST
	       (m_tf, q, m_offm.classOffset(type)))));
    
	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.objectref(), q, MAP(q.objectref(), q));
	addStmt(s0);
    }

    public void visit(PMOFFSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new CONST(m_tf, q, m_offm.offset(q.method())));

	updateDT(q.dst(), q, ((MOVE)s0).dst);
	addStmt(s0);
    }

    public void visit(POPER q) {
	Exp oper = null; int optype; 
	Stm s0;
	Temp[] operands = q.operands();
  
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
	    throw new Error("Unknown optype in ToTree");
	}
    
	if (operands.length==1) {
	    oper = new UNOP
		(m_tf, q, TYPE(q, operands[0]), optype, 
		 MAP(operands[0], q)); 
		
	    updateDT(operands[0], q, MAP(operands[0], q));
	}
	else if (operands.length==2) {
	    oper = new BINOP
		(m_tf, q, 
		 MERGE_TYPE(TYPE(q, operands[0]), TYPE(q, operands[1])),
		 optype,
		 MAP(operands[0], q), 
		 MAP(operands[1], q)); 

	    updateDT(operands[0], q, MAP(operands[0], q));
	    updateDT(operands[1], q, MAP(operands[1], q));
	}
	else 
	    throw new Error("Unexpected # of operands: " + q);
    
	s0 = new MOVE(m_tf, q, MAP(q.dst(), q), oper);
	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }

    private void visitShiftOper(POPER q) { 
	int optype; OPER oper; Stm s0; 
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
	oper = new BINOP
	    (m_tf, q, 
	     // Don't merge type of shift exp with type of additive exp
	     TYPE(q, operands[0]),
	     optype,
	     MAP(operands[0], q), 
	     MAP(operands[1], q)); 
	
	updateDT(operands[0], q, MAP(operands[0], q));
	updateDT(operands[1], q, MAP(operands[1], q));
	
	s0 = new MOVE(m_tf, q, MAP(q.dst(), q), oper);
	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(s0);
    }
  
    public void visit(PSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     new MEM(m_tf, q, TYPE(q, q.src()), MAP(q.ptr(), q)),
	     MAP(q.src(), q));

	updateDT(q.src(), q, MAP(q.src(), q));
	updateDT(q.ptr(), q, MAP(q.ptr(), q));
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

    private TEMP extra(HCodeElement source, int type) {
	return new TEMP(m_tf, source, type, 
			new Temp(m_tf.tempFactory(), "tr_"));
    }

    private TEMP extra(Temp tOld, HCodeElement source, int type) {
	Temp tNew = new Temp(tOld.tempFactory(), "tr_");
	return m_tempMap.tempMap(tNew, source, type);
    }

    private LABEL MAP(harpoon.IR.Quads.LABEL label) { 
	return m_labelMap.labelMap(label); 
    }

    private TEMP MAP(Temp t, HCodeElement source) { 
	return MAP(t, source, TYPE(source, t));
    }

    private TEMP MAP(Temp t, HCodeElement source, int type) { 
	Util.assert(!hastype(source, t) || 
		    isValidMapping(type(t), type));
	return m_tempMap.tempMap(t, source, type); 
    }

    private HClass type(Temp t) { 
	return m_typeMap.typeMap(m_code, t); 
    }

    private int TYPE(HCodeElement src, Temp t) { 
	return hastype(src, t)?maptype(type(t)):Type.POINTER; 
    }

    private boolean hastype(HCodeElement hce, Temp t) { 
	return m_derivation.derivation(hce, t)==null; 
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
	boolean longptrs = m_frame.pointersAreLong();
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
	    constant = new CONST(m_tf, src, m_frame.pointersAreLong());
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
	    constant = new MEM
		(m_tf, src, Type.POINTER, 
		 new NAME(m_tf, src, m_offm.label((String)value)));
	else 
	    throw new Error("Bad type for CONST " + type); 
	return constant;
    }


    /* This version of UpdateDT is used for Temps which were created just
     * for the Tree form
     */
    private void updateDT(Temp tmp, Tree tNew, DList dl, HClass hc) { 
	Util.assert(tmp.tempFactory()==tNew.getFactory().tempFactory());
	Util.assert(dl==null ^ hc==null);

	if (dl!=null) { // If tmp is a derived ptr, update deriv info.
	    m_dT.put(new Tuple(new Object[] {tNew, tmp}), dl);
	    m_dT.put(tmp, 
		     new Error("*** Can't type a derived pointer: "+tmp));
	}
	else { // If the tmp is NOT a derived pointer, assign its type
	    m_dT.put(tmp, hc);
	}
    }           
  
    /* This version of UpdateDT is used for Temps which existed
     * in the LowQuad form.
     */
    private void updateDT(Temp tmp, Quad qOld, Tree tNew) {
	DList dl; HClass hc; 
    
	Util.assert(tmp.tempFactory()==qOld.getFactory().tempFactory());

	dl = DList.clone(m_derivation.derivation(qOld, tmp), m_ctm);
	if (dl!=null) { // If tmp is a derived ptr, update deriv info.
	    m_dT.put(new Tuple(new Object[] {tNew, MAP(tmp, qOld)}),dl);
	    m_dT.put
		(MAP(tmp, qOld), 
		 new Error("*** Can't type a derived pointer: " + 
			   MAP(tmp, qOld)));
	}
	else { // If the tmp is NOT a derived pointer, assign its type
	    hc = m_typeMap.typeMap(m_code, tmp);
	    if (hc!=null) {
		m_dT.put(MAP(tmp, qOld), hc);
	    }
	}
    }
  	
    class LabelMap {
	private final Map h = new HashMap();
	public LABEL labelMap(harpoon.IR.Quads.LABEL q) {
	    if (q==null) return null;
	    else if (h.containsKey(q)) return (LABEL)h.get(q);
	    else {
		String s = ("_"+m_tf.getMethod().toString()+"_"+q.label()).
		    replace(' ', '_');
		h.put(q, new LABEL(m_tf, q, new Label(s))); 
		return (LABEL)h.get(q); 
	    }
	}
    }

    class TreeTempMap {
	private final Map h = new HashMap();
	public TEMP tempMap(Temp t, HCodeElement source, int type) {
	    if (t==null) return null;
	    else if (h.containsKey(t)) return (TEMP)h.get(t);
	    else {	
		h.put(t, new TEMP(m_tf, source, type, m_ctm.tempMap(t)));
		return (TEMP)h.get(t);
	    }
	}
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                            *
     *                Run-time typechecking code                  *
     *                                                            *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/


    /** 
     *  The isInstantceOf check uses the casting conversion algorithm
     *  found in the Java Language Specification at 
     *  http://java.sun.com/docs/books/jls/html/5.doc.html#176921.
     *  Returns an Exp that evaluates to true if "q" can be cast to "type"
     *  with this algorithm.
     */
    private Exp isInstanceOf(Quad q, Temp src, HClass type) {
	TEMP srcTEMP = MAP(src, q);
	TEMP classPtr = extra(src, q, Type.POINTER);
	TEMP RESULT = extra(src, q, Type.INT);

	if (type.isPrimitive()) {
	    return new CONST(m_tf, q, maptype(type)==maptype(type(src))?1:0);
	}
	else {
	    LABEL isNonNull = new LABEL(m_tf, q, new Label());
	    LABEL isNull    = new LABEL(m_tf, q, new Label());
	    LABEL end       = new LABEL(m_tf, q, new Label());
	    
	    Stm s0 = new CJUMP
		(m_tf, q, 
		 new BINOP
		 (m_tf, q, Type.POINTER, Bop.CMPEQ,
		  srcTEMP, 
		  new CONST(m_tf, q, 0)),
		 isNull.label, 
		 isNonNull.label);
	    Stm s1 = isNull;
	    Stm s2 = new MOVE(m_tf, q, RESULT, new CONST(m_tf, q, 0));
	    Stm s3 = new JUMP(m_tf, q, end.label);
	    Stm s4 = isNonNull;
	    Stm s5 = new MOVE
	      (m_tf, q, 
	       RESULT,
	       new ESEQ
	       (m_tf, q, 
		new MOVE
		(m_tf, q,  
		 classPtr,  
		 new MEM 
		 (m_tf, q, Type.POINTER,
		  new BINOP
		  (m_tf, q, Type.POINTER, Bop.ADD,
		   new CONST(m_tf, q, m_offm.classOffset(type(src))),
		   srcTEMP))),
		isInstanceOf(q, classPtr, type)));
	    Stm s6 = end;
	    
	    return new ESEQ
	      (m_tf, q, 
	       Stm.toStm(Arrays.asList(new Stm[] {s0,s1,s2,s3,s4,s5,s6})), 
	       RESULT);
	}
    }
  
    private Exp isArray(TEMP tagBits) {
	return new BINOP
	    (m_tf, tagBits, Type.INT, Bop.CMPEQ,
	     tagBits,
	     new CONST(m_tf, tagBits, m_offm.arrayTag()));
    }

    private Exp isClass(TEMP tagBits) {
	return new BINOP
	    (m_tf, tagBits, Type.INT, Bop.CMPEQ,
	     tagBits, 
	     new CONST(m_tf, tagBits, m_offm.classTag()));
	
    }
    
    private Exp isInterface(TEMP tagBits) {
	return new BINOP
	    (m_tf, tagBits, Type.INT, Bop.CMPEQ,
	     tagBits, 
	     new CONST(m_tf, tagBits, m_offm.interfaceTag()));
    }

    private Exp classExtends(TEMP classPtr, HClass type) { 
	Util.assert(!type.isInterface() &&
		    !type.isArray() &&
		    !type.isPrimitive());

	Stm  s0, s1;

	NAME typeLabel  = new NAME(m_tf, classPtr, m_offm.label(type));
	TEMP classLabel = extra(classPtr, Type.POINTER);
	TEMP result     = extra(classPtr, Type.INT);

	s0 = new MOVE
	    (m_tf, classPtr, 
	     classLabel,
	     new MEM
	     (m_tf, classPtr, Type.POINTER,
	      new BINOP
	      (m_tf, classPtr, Type.POINTER, Bop.ADD,
	       classPtr,
	       new CONST(m_tf, classPtr, m_offm.displayOffset(type)))));

	s1 = new MOVE
	    (m_tf, classPtr, 
	     result,
	     new BINOP
	     (m_tf, classPtr, Type.POINTER, Bop.CMPEQ, 
	      classLabel, 
	      typeLabel));

	return new ESEQ
	    (m_tf,classPtr,Stm.toStm(Arrays.asList(new Stm[] {s0,s1})),result);
    }

    private Exp isImplemented(TEMP classPtr, HClass type) {
	Util.assert(type.isInterface());

	Stm   s0, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12;

	NAME  typeLabel      = new NAME(m_tf, classPtr, m_offm.label(type));
	TEMP  interfacePtr   = extra(classPtr, Type.POINTER);
	TEMP  interfaceLabel = extra(classPtr, Type.POINTER);
	TEMP  result         = extra(classPtr, Type.POINTER);

	LABEL endLabel      = new LABEL(m_tf, classPtr, new Label());
	LABEL loop          = new LABEL(m_tf, classPtr, new Label());
	LABEL next          = new LABEL(m_tf, classPtr, new Label());
	LABEL successLabel  = new LABEL(m_tf, classPtr, new Label());
	LABEL failureLabel  = new LABEL(m_tf, classPtr, new Label());
	
	// Assign interfacePtr to point to the block of memory 
	// directly before the first interface.
	// 
	s0 = new MOVE
	    (m_tf, classPtr,
	     interfacePtr,
	     new BINOP
	     (m_tf, classPtr, Type.POINTER, Bop.ADD,
	      new MEM
	      (m_tf, classPtr, Type.POINTER, 
	       new BINOP
	       (m_tf, classPtr, Type.POINTER, Bop.ADD,
		classPtr, 
		new CONST(m_tf, classPtr, m_offm.interfaceListOffset(type)))),
	      new CONST(m_tf, classPtr, -wordSize())));
	
	// Label the top of the loop
	//
	s1 = loop;

	// Increment the current interface ptr
	// 
	s2 = new MOVE
	    (m_tf, classPtr, 
	     interfacePtr,
	     new BINOP
	     (m_tf, classPtr, Type.POINTER, Bop.ADD,
	      interfacePtr,
	      new CONST(m_tf, classPtr, wordSize())));

	// Derefence the current interface ptr
	//
	s3 = new MOVE
	    (m_tf, classPtr, 
	     interfaceLabel, 
	     new MEM(m_tf, classPtr, Type.POINTER, interfacePtr));

	// Check if we have reached the end of the interface list
	//
	s4 = new CJUMP
	    (m_tf, classPtr, 
	     new BINOP
	     (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
	      interfaceLabel, 
	      new CONST(m_tf, classPtr, 0)),
	     failureLabel.label,
	     next.label);

	s5 = next;
	
	// See if we've found the correct label
	// 
	s6 = new CJUMP
	     (m_tf, classPtr, 
	      new BINOP
	      (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
	       typeLabel,
	       interfaceLabel),
	      successLabel.label,
	      loop.label);

	// We've reached the end of the interface list without finding 
	// a match.  Return 0.
	// 
	s7 = failureLabel;
	
	s8 = new MOVE(m_tf, classPtr, result, new CONST(m_tf, classPtr, 0));
	s9 = new JUMP(m_tf, classPtr, endLabel.label);

	// We found the interface label we wanted.  Yay!  Return 1.
	// 
	s10 = successLabel;
	s11 = new MOVE(m_tf, classPtr, result, new CONST(m_tf, classPtr, 1));

	s12 = endLabel;

	return new ESEQ
	    (m_tf, classPtr, 
	     Stm.toStm(Arrays.asList(new Stm[] 
		   {s0,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12})),
	     result);

    }

    private ESEQ componentType(TEMP classPtr) {
	return new ESEQ
	    (m_tf, classPtr, 
	     new MOVE
	     (m_tf, classPtr, 
	      classPtr, 
	      new MEM
	      (m_tf, classPtr, Type.POINTER,
	       new BINOP
	       (m_tf, classPtr, Type.POINTER, Bop.ADD,
		classPtr, 
		new CONST
		(m_tf, classPtr, 
		 m_offm.componentTypeOffset
		 (HClass.forDescriptor("[Ljava/lang/Object;")))))),
	     classPtr);
    }
	
    /** 
     *  The isInstantceOf check uses the casting conversion algorithm
     *  found in the Java Language Specification at 
     *  http://java.sun.com/docs/books/jls/html/5.doc.html#176921.
     *  Returns an Exp that evaluates to true if "q" can be cast to "type"
     *  with this algorithm.
     */
    private Exp isInstanceOf(Quad q, TEMP classPtr, HClass type) {
	Exp    classCheckResult, interfaceCheckResult, arrayCheckResult;
	Exp    result;
	LABEL  classCheck, interfaceCheck, arrayCheck, isInterfaceLabel, end;
	Stm    s0, s1, s2, s3, s4, s5;
	TEMP   tagBits;
	
	arrayCheck        = new LABEL(m_tf, q, new Label());
	classCheck        = new LABEL(m_tf, q, new Label());
	interfaceCheck    = new LABEL(m_tf, q, new Label());
	isInterfaceLabel  = new LABEL(m_tf, q, new Label());
	end               = new LABEL(m_tf, q, new Label());
	
	result            = extra(classPtr, Type.INT);
	tagBits           = extra(classPtr, Type.INT);

	s0 = new MOVE
	    (m_tf, q, 
	     tagBits,
	     new MEM
	     (m_tf, q, Type.INT, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       classPtr, 
	       new CONST(m_tf, q, m_offm.tagOffset(HClass.Void))))); 
	// FIX: eliminate hc param for static data
		     

	s1 = new CJUMP
	    (m_tf, q, isClass(tagBits), 
	     classCheck.label, 
	     isInterfaceLabel.label);
	
	s2 = new SEQ
	    (m_tf, q, 
	     isInterfaceLabel,
	     new CJUMP
	     (m_tf, q, isInterface(tagBits), 
	      interfaceCheck.label, 
	      arrayCheck.label));
	
	//
	// Now, make use of our compile-time type information:
	//

	// CASE 1: "type" is known to be an array type.  Simply return false.
	// 
	if (type.isArray()) {
	    classCheckResult     = new CONST(m_tf, q, 0);
	    interfaceCheckResult = new CONST(m_tf, q, 0);
	    if (type.getComponentType().isPrimitive()) {
		arrayCheckResult = new BINOP
		    (m_tf, q, Type.POINTER, Bop.CMPEQ,
		     componentType(classPtr),
		     new NAME(m_tf, q, m_offm.label(type.getComponentType())));
	    }
	    else {
		arrayCheckResult 
		    = isInstanceOf(q, 
				   (TEMP)componentType(classPtr).exp, 
				   type.getComponentType());
	    }
	}
	// CASE 2: "type" is known to be an interface type.  In this case,
	//         the type
	//
	else if (type.isInterface()) {
	    classCheckResult     = isImplemented(classPtr, type);
	    // Because checking if an interface extends another interface
	    // is the same as checking if a class implements some interface
	    interfaceCheckResult = classCheckResult;  
	    arrayCheckResult     = new CONST
		(m_tf, q, type==HClass.forName("java.lang.Cloneable")?1:0);
	}
	// CASE 3: "type is a class type
	//
	else {
	    classCheckResult     = classExtends(classPtr, type);
	    interfaceCheckResult = new CONST
		(m_tf, q, type==HClass.forName("java.lang.Object")?1:0);
	    arrayCheckResult     = interfaceCheckResult;  // Same
	}
	
	s3 = new SEQ
	    (m_tf, q, 
	     classCheck,
	     new SEQ
	     (m_tf, q, 
	      new MOVE(m_tf, q, result, classCheckResult),
	      new JUMP(m_tf, q, end.label)));

	s4 = new SEQ
	    (m_tf, q, 
	     interfaceCheck,
	     new SEQ
	     (m_tf, q, 
	      new MOVE(m_tf, q, result, interfaceCheckResult),
	      new JUMP(m_tf, q, end.label)));

	s5 = new SEQ
	    (m_tf, q, 
	     arrayCheck,
	     new MOVE(m_tf, q, result, arrayCheckResult));

	return new ESEQ
	    (m_tf, q, 
	     Stm.toStm(Arrays.asList(new Stm[] {s0,s1,s2,s3,s4,s5,end})), 
	     result);
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
abstract class LowQuadWithDerivationVisitor extends ExtendedLowQuadVisitor {
    private Map         m_dT;
    private Derivation  m_derivation;
    private HCode       m_code;
    private TypeMap     m_typeMap;

    protected LowQuadWithDerivationVisitor(Derivation derivation, 
					   HCode code,
					   TypeMap typeMap) {
	m_dT         = new HashMap();
	m_derivation = derivation;
	m_code       = code;
	m_typeMap    = typeMap;
    }

    Derivation getDerivation() {
	return new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		if ((hce==null)||(t==null)) return null;
		else {
		    Object deriv = 
			m_dT.get(new Tuple(new Object[] { hce, t }));
		    if (deriv instanceof Error)
			throw (Error)((Error)deriv).fillInStackTrace();
		    else {
			return (DList)deriv;
		    }
		}
	    }
	};
    }

    TypeMap getTypeMap() {
	return new TypeMap() {
	    public HClass typeMap(HCode hc, Temp t) {
		if (t==null) return null;
		else {
		    Object type = m_dT.get(t);   // Ignores hc parameter
		    if (type instanceof Error) 
			throw (Error)((Error)type).fillInStackTrace();
		    else                       
			return (HClass)type;
		}
	    }
	};
    }
    
    protected void updateDTInfo(Quad qOld, Quad qNew) {
	for (int i=0; i<2; i++) {
	    Temp[] tmps = (i==0)?qOld.def():qOld.use();
	    for (int j=0; j<tmps.length; j++) {
		if (m_derivation.derivation(qOld, tmps[j]) != null) {
		    m_dT.put(new Tuple(new Object[] { qNew, tmps[j] }),
			     DList.clone
			     (m_derivation.derivation(qOld, tmps[j])));
		    m_dT.put
			(tmps[j], 
			 new Error("*** Derived pointers have no type"));
		}
		else {
		    if (m_typeMap.typeMap(m_code, tmps[j]) != null) {
			m_dT.put(tmps[j], 
				 m_typeMap.typeMap(m_code, tmps[j]));
		    }
		}
	    }
	}
    }
}


// Clones the quad graph, while preserving type/derivation information
// for the new quads
class CloningVisitor extends LowQuadWithDerivationVisitor {
    private LowQuadMap m_lqm;
    public CloningVisitor(Derivation d, HCode hc, 
			  TypeMap t, LowQuadMap lqm) {
	super(d, hc, t);
	m_lqm = lqm;
    }
    public void visit(Quad q) {
	Quad qm = (Quad)q.clone();
	m_lqm.put(q, qm);
	updateDTInfo(q, qm);
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
 
    public CallConversionVisitor(Derivation d, HCode hc, TypeMap t) { 
	super(d, hc, t);
    }

    public void visit(harpoon.IR.Quads.Quad quad) { 
	updateDTInfo(quad, quad);
	this.pcall=null; this.nconst=null; this.acmpeq=null; this.cjump=null; 
    }

    public void visit(harpoon.IR.Quads.CJMP cjump) { 
	updateDTInfo(cjump, cjump);
	if ((acmpeq != null) && (this.acmpeq.dst() == cjump.test())) {
	    this.cjump = cjump;
	    replaceExceptionCheck();
	}
	this.pcall=null; this.nconst=null; this.acmpeq=null; this.cjump=null;
    }

    public void visit(harpoon.IR.Quads.CONST nconst) { 
	updateDTInfo(nconst, nconst);
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
	updateDTInfo(pcall, pcall);
	this.pcall = pcall;
	this.nconst=null; this.acmpeq=null; this.cjump=null;
    }

    public void visit(POPER poper) { 
	updateDTInfo(poper, poper);
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
	updateDTInfo(pcall, newCall);
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
    public LabelingVisitor(Derivation d, HCode hc, TypeMap t) { 
	super(d, hc, t);
	m_QToL = new HashMap(); 
    }
    public void visit(Quad q) { updateDTInfo(q, q); } 
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
		updateDTInfo(q, newQ);

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
    public void visit(QuadVisitor v) { visit((ExtendedLowQuadVisitor)v); } 
    public void visit(ExtendedLowQuadVisitor v) { v.visit(this); }
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
