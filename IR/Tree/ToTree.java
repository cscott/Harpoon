package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.UseDef;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
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
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * The ToTree class is used to translate low-quad-no-ssa code to tree code.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToTree.java,v 1.1.2.12 1999-05-10 02:07:40 duncan Exp $
 */
public class ToTree implements Derivation, TypeMap {
    private Derivation  m_derivation;
    private Tree        m_tree;
    private TypeMap     m_typeMap;
  
    /** Class constructor */
    public ToTree(final TreeFactory tf, LowQuadNoSSA code) {
	Util.assert(tf.getParent().getName().equals("tree"));

	final Hashtable dT = new Hashtable();
	
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

    private Tree translate(TreeFactory tf, LowQuadNoSSA code, Hashtable dT) {
	CloningTempMap                ctm;
	LowQuadMap                    lqm;
	LowQuadWithDerivationVisitor  dv;
	Stm                           tree;
	StmList                       stmList;
	TranslationVisitor            tv;
	
	// Clone the Quad graph
	//
	lqm = new LowQuadMap();
	dv = new CloningVisitor(code, code, code, lqm);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	    ((Quad)e.nextElement()).visit(dv);
	
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) {
	    Quad qTmp = (Quad)e.nextElement();
	    Edge[] el = qTmp.nextEdge();
	    for (int i=0; i<el.length; i++) 
		Quad.addEdge(lqm.get((Quad)el[i].from()),
			     el[i].which_succ(),
			     lqm.get((Quad)el[i].to()),
			     el[i].which_pred());
	}
	
	dv = new LabelingVisitor(dv.getDerivation(), code, dv.getTypeMap());
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	    lqm.get((Quad)e.nextElement()).visit(dv);
	
	ctm = new CloningTempMap
	    (((Quad)code.getRootElement()).getFactory().tempFactory(),
	     tf.tempFactory());
	
	// pass in typemap/deriv explicitly
	tv = new TranslationVisitor(tf, dv.getDerivation(), code, 
				    dv.getTypeMap(), ctm, dT);;
	for (Enumeration e = quadGraph(lqm.get((Quad)code.getRootElement()), tv);
	     e.hasMoreElements();)
	    ((Quad)e.nextElement()).visit(tv);
	
	stmList = tv.getStmtList();
	
	// Construct tree from StmList
	//
	if (stmList==null) return null;
	else if (stmList.tail == null) { return stmList.head; }
	else {
	    tree = new SEQ(tf, stmList.tail.head, stmList.tail.head, stmList.head);
	    stmList = stmList.tail.tail;
	    while (stmList != null) {
		tree  = new SEQ(tf, stmList.head, stmList.head, tree);
		stmList = stmList.tail;
	    }
	}
	return tree;
    }
    
    // Enumerates the Quad graph in depth-first order.  Additionally inserts
    // JUMP nodes into the generated Tree in the appropriate places.  This
    // is very hackish, and will be updated when I have more time.  
    //
    private Enumeration quadGraph(final Quad head, 
				  final TranslationVisitor tv) {
	return new Enumeration() {
	    boolean addJump = false;
	    harpoon.IR.Quads.LABEL jumpDst = null;

	    final TranslationVisitor v = tv;
	    Set  visited = new HashSet();
	    Stack s = new Stack();
	    { s.push(head); }
	    public boolean hasMoreElements() { return !s.isEmpty(); }
	    public Object nextElement() { 
		if (s.isEmpty()) throw new NoSuchElementException();
		Quad q = (Quad)s.pop();
		if (addJump) {
		    if (jumpDst==null) v.addEndJump(q);
		    else v.addJump(q, jumpDst);
		}

		Quad[] next = q.next();
		for (int i=0; i<next.length; i++) {
		    if (!visited.contains(next[i])) {
			s.push(next[i]);
			visited.union(next[i]);
			addJump = false;
			jumpDst = null;
		    }
		    /* CHECK IF THIS IS VALID */
		    else {
			if (!(q instanceof harpoon.IR.Quads.SIGMA)) {
			    addJump = true;
			    if (q.next(0) instanceof harpoon.IR.Quads.LABEL) {
				jumpDst = (harpoon.IR.Quads.LABEL)q.next(0);
			    }
			    else {
				Util.assert(q.next(0) instanceof 
					    harpoon.IR.Quads.FOOTER);
			    }
			}
		    }
			    
		}
		return q;
	    }
	};
    }
}
  
// Translates the LowQuadNoSSA code
//
class TranslationVisitor extends LowQuadVisitor {

    private LABEL END;
    private LowQuadNoSSA      m_code;         // The codeview to translate
    private CloningTempMap    m_ctm;          // Clones Temps to new tf
    private Derivation        m_derivation;   // Old derivation info
    private Hashtable         m_dT;           // Updated deriv & type info
    private Frame             m_frame;        // The machine-specific Frame
    private LabelMap          m_labelMap;     // Converts labes to Tree LABELs
    private OffsetMap         m_offm;         // Machine-specific offset map
    private StmList           m_stmList;      // Holds translated statements
    private TreeTempMap       m_tempMap;      // Maps Temps to Tree TEMPs
    private TreeFactory       m_tf;           // The new TreeFactory
    private TypeMap           m_typeMap;      // Old typing info
  
    public TranslationVisitor(TreeFactory tf, Derivation derivation, 
			      LowQuadNoSSA code, TypeMap typeMap, 
			      CloningTempMap ctm, Hashtable dT) {
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
	END = new LABEL(m_tf, m_code.getRootElement(), new Label());
	m_stmList      = null;

    }

    public StmList getStmtList() { return new StmList(END, m_stmList); }

    public void visit(Quad q) { /* Dont translate other quads */  }

    public void visit(harpoon.IR.Quads.ALENGTH q) {
	Stm s0 = new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new MEM
	     (m_tf, q, Type.POINTER, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       MAP(q.objectref(), q),
	       new CONST(m_tf, q, m_offm.lengthOffset(type(q.objectref()))))));

	addStmt(q, s0);
    }

    public void visit(harpoon.IR.Quads.ANEW q) {
	Exp classPtr, hashcode, length;
	Stm s0, s1, s2, s3; TEMP arrayref;
	
	// Create a reference to the array we are going to create
	//
	arrayref = MAP(q.dst(), q);

	// Create the fields with which we'll initialize the array
	// 
	hashcode  = new UNOP(m_tf, q, Type.INT, Uop._2I, arrayref);
	length    = MAP(q.dims(0), q);
	classPtr  = new NAME(m_tf, q, m_offm.label(q.hclass()));

	// Allocate memory for the array
	// FIX:  needs to allocate memory for hashcode, classptr, length, and 
	//       finalization info
	//
	s0 = new MOVE
	    (m_tf, q, 
	     arrayref, 
	     m_frame.memAlloc(new BINOP
			      (m_tf, q, Type.INT, Bop.MUL,
			       length,
			       new CONST
			       (m_tf, q, 
				m_offm.size(q.hclass().getComponentType())))));
    
	// Assign the array a hashcode
	//
	s1 = new MOVE
	    (m_tf, q, 
	     new MEM
	     (m_tf, q, Type.INT, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       arrayref, 
	       new CONST(m_tf, q, m_offm.hashCodeOffset(q.hclass())))),
	     hashcode);
    
	// Assign the array's length field
	//
	s2 = new MOVE
	    (m_tf, q,
	     new MEM
	     (m_tf, q, Type.INT, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       arrayref, 
	       new CONST(m_tf, q, m_offm.lengthOffset(q.hclass())))),
	     length);
    
	// Assign the array a class ptr
	//
	s3 = new MOVE
	    (m_tf, q, 
	     new MEM
	     (m_tf, q, Type.POINTER, 
	      new BINOP
	      (m_tf, q, Type.POINTER, Bop.ADD,
	       arrayref, 
	       new CONST(m_tf, q, m_offm.classOffset(q.hclass())))), 
	     classPtr);

	// Update derivation & type info
	//
	updateDT(q.dst(), q, arrayref);    
	addStmt(q, new Stm[] { s0, s1, s2, s3 });      
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

	       //	       (q.value().length * 
	       //		m_offm.size(q.type())))));

	updateDT(nextPtr.temp, q, nextPtr, dl, null);
	updateDT(q.objectref(), q, MAP(q.objectref(), q));
	addStmt(q, s0);
    
	for (int i=0; i<q.value().length; i++) {
	    s0 = new MOVE
		(m_tf, q, 
		 new MEM(m_tf, q, Type.POINTER, nextPtr), 
		 mapconst(q, q.value()[i], q.type()));
	    s1 = new MOVE
		(m_tf, q, 
		 nextPtr, 
		 new BINOP
		 (m_tf, q, Type.POINTER, Bop.ADD, 
		  nextPtr, 
		  new CONST(m_tf, q, m_offm.size(q.type()))));

	    addStmt(q, new Stm[] { s0, s1 });
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
	addStmt(q, s0);
    }
  
    public void visit(harpoon.IR.Quads.COMPONENTOF q) {
	Stm s0 = new MOVE 
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     isInstanceOf(q, q.objectref(), 
			  type(q.arrayref()).getComponentType()));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }

    public void visit(harpoon.IR.Quads.CONST q) {
	Stm s0 = new MOVE
	    (m_tf, q, MAP(q.dst(), q), mapconst(q, q.value(), q.type()));
    
	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }
  
    public void visit(harpoon.IR.Quads.INSTANCEOF q) {
	Stm s0 =new MOVE
	    (m_tf, q, MAP(q.dst(), q), 
	     isInstanceOf(q, q.src(), q.hclass()));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }
  
    public void visit(harpoon.IR.Quads.LABEL q) {
	addStmt(q, MAP(q));
    }

    public void visit(harpoon.IR.Quads.METHOD q) {
	Temp params[] = q.params(); 
	Temp mappedParams[] = new Temp[params.length];
	for (int i = 0; i < params.length; i++)
	    mappedParams[i] = m_ctm.tempMap(params[i]);
	Stm s0 = m_frame.procPrologue(m_tf, q, mappedParams);
	if (s0 != null) addStmt(q, s0);
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
	addStmt(q, s0);
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
	addStmt(q, new Stm[] { s0, s1, s2 });
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
	addStmt(q, s0);
    }

    /* Naive implementation */
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
	    //addStmt(q, new Stm[] { MAP((harpoon.IR.Quads.LABEL)qNext), 
	    //		    branch });
	    addStmt(q, new Stm[] { branch, lNext } );
	}
	updateDT(q.index(), q, discriminant);
    }
  
    public void visit(harpoon.IR.Quads.THROW q) { 
	Stm s0 = new THROW(m_tf, q, MAP(q.throwable(), q));
    
	updateDT(q.throwable(), q, MAP(q.throwable(), q));
	addStmt(q, s0);
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
	TEMP dst = MAP(q.dst(), q), index = MAP(q.index(), q);

	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     dst, 
	     new BINOP
	     (m_tf, q, Type.POINTER, Bop.MUL,
	      new CONST
	      (m_tf, q, m_offm.size(q.arrayType().getComponentType())),
	      index));

	updateDT(q.dst(), q, dst);
	updateDT(q.index(), q, index);
	addStmt(q, s0);
    }

    public void visit(PARRAY q) {
	Stm s0  = 
	    new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.objectref(), q));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }

    public void visit(PCALL q) {
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
      
	// Should we dereference the method pointer here? 
	s0 = new CALL(m_tf, q, retval, retex, func, params);      

	updateDT(q.retex(), q, retex);
	updateDT(q.ptr(), q, func);
	addStmt(q, s0); 
    }

    public void visit(PFCONST q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new NAME(m_tf, q, m_offm.label(q.field())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }

    public void visit(PFIELD q) { 
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     MAP(q.objectref(), q));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.objectref(), q, MAP(q.objectref(), q));
	addStmt(q, s0);
    }
  
    public void visit(PFOFFSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     new CONST(m_tf, q, m_offm.offset(q.field())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }

    public void visit(PGET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q), 
	     new MEM(m_tf, q, TYPE(q, q.dst()), MAP(q.ptr(), q)));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	updateDT(q.ptr(), q, MAP(q.ptr(), q));
	addStmt(q, s0);
    }
  
    public void visit(PMCONST q) { 
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new NAME(m_tf, q, m_offm.label(q.method())));

	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
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
	addStmt(q, s0);
    }

    public void visit(PMOFFSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     MAP(q.dst(), q),
	     new CONST(m_tf, q, m_offm.offset(q.method())));

	updateDT(q.dst(), q, ((MOVE)s0).dst);
	addStmt(q, s0);
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
	case Qop.LSHL: optype = Bop.SHL; break;
	case Qop.ISHR:
	case Qop.LSHR: optype = Bop.SHR; break;
	case Qop.IUSHR:
	case Qop.LUSHR: optype = Bop.USHR; break; 
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
		(m_tf, q, TYPE(q, operands[0]), optype,
		 MAP(operands[0], q), 
		 MAP(operands[1], q)); 

	    updateDT(operands[0], q, MAP(operands[0], q));
	    updateDT(operands[1], q, MAP(operands[1], q));
	}
	else 
	    throw new Error("Unexpected # of operands: " + q);
    
	s0 = new MOVE(m_tf, q, MAP(q.dst(), q), oper);
	updateDT(q.dst(), q, MAP(q.dst(), q));
	addStmt(q, s0);
    }
  
    public void visit(PSET q) {
	Stm s0 = 
	    new MOVE
	    (m_tf, q, 
	     new MEM(m_tf, q, TYPE(q, q.src()), MAP(q.ptr(), q)),
	     MAP(q.src(), q));

	updateDT(q.src(), q, MAP(q.src(), q));
	updateDT(q.ptr(), q, MAP(q.ptr(), q));
	addStmt(q, s0);
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Utility Functions                      *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    void addJump(Quad q, harpoon.IR.Quads.LABEL l) {
	m_stmList = new StmList(new JUMP(m_tf, q, MAP(l).label), m_stmList);
    }

    void addEndJump(Quad q) {
	m_stmList = new StmList(new JUMP(m_tf, q, END.label), m_stmList);
    }

    private void addStmt(Quad q, Stm stm) { 
        m_stmList = new StmList(stm, m_stmList); 
	if (!(q instanceof harpoon.IR.Quads.SIGMA)) {

	    if (q.next(0) instanceof harpoon.IR.Quads.LABEL) {
		m_stmList = new StmList
		    (new JUMP
		     (m_tf, q, MAP((harpoon.IR.Quads.LABEL)q.next(0)).label),
		     m_stmList);
	    }
	}
    }

    private void addStmt(Quad q, Stm[] stm) { 
	for (int i=0; i<stm.length-1; i++)
	    m_stmList = new StmList(stm[i], m_stmList);
	
	addStmt(q, stm[stm.length-1]); 
    }

    private Stm toStm(Stm[] stm) {
	if (stm.length==0) return null;
	else if (stm.length==1) return stm[0];
	else {
	    Stm s = new SEQ
		(m_tf, stm[stm.length-2], 
		 stm[stm.length-2], 
		 stm[stm.length-1]);

	    for (int i=stm.length-3; i>=0; i--) 
		s = new SEQ(m_tf, stm[i], stm[i], s);

	    return s;
	}
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

    private Exp mapconst(HCodeElement src, Object value, HClass type) {
	Exp constant;

	if (type==HClass.Boolean)
	    constant = new CONST
		(m_tf, src, ((Boolean)value).booleanValue()?1:0);
	else if (type==HClass.Byte)
	    constant = new CONST(m_tf, src, ((Byte)value).intValue()); 
	else if (type==HClass.Char)
	    constant = new CONST 
		(m_tf, src, 
		 Character.getNumericValue(((Character)value).charValue())); 
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
	else if (type==HClass.Void)
	    constant = new CONST(m_tf, src, 0); 
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
    private void updateDT(Temp tmp, Quad qOld, Tree tNew, 
			  DList dl, HClass hc) { 
	Util.assert(tmp.tempFactory()==tNew.getFactory().tempFactory());
	    
	if (dl!=null) { // If tmp is a derived ptr, update deriv info.
	    m_dT.put(new Tuple(new Object[] {tNew, tmp}), dl);
	    m_dT.put(tmp, 
		     new Error("*** Can't type a derived pointer: "+tmp));
	}
	else { // If the tmp is NOT a derived pointer, assign its type
	    if (hc!=null) {
		m_dT.put(tmp, hc);
	    }
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
	private final Hashtable h = new Hashtable();
	public LABEL labelMap(harpoon.IR.Quads.LABEL q) {
	    if (q==null) return null;
	    else if (h.containsKey(q)) return (LABEL)h.get(q);
	    else {
		h.put(q, new LABEL(m_tf, q, new Label()));
		return (LABEL)h.get(q);
	    }
	}
    }

    class TreeTempMap {
	private final Hashtable h = new Hashtable();
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
	      (m_tf, q, toStm(new Stm[] {s0, s1, s2, s3, s4, s5, s6}), RESULT);
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
	TEMP result     = extra(classPtr, Type.POINTER);

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

	return new ESEQ(m_tf, classPtr, toStm(new Stm[] { s0, s1 }), result);
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

	
	s4 = new CJUMP
	    (m_tf, classPtr, 
	     new BINOP
	     (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
	      interfaceLabel, 
	      new CONST(m_tf, classPtr, 0)),
	     failureLabel.label,
	     next.label);

	s5 = next;

	s6 = new CJUMP
	     (m_tf, classPtr, 
	      new BINOP
	      (m_tf, classPtr, Type.POINTER, Bop.CMPEQ,
	       typeLabel,
	       interfaceLabel),
	      successLabel.label,
	      loop.label);

	s7 = failureLabel;
	
	s8 = new MOVE(m_tf, classPtr, result, new CONST(m_tf, classPtr, 0));
	s9 = new JUMP(m_tf, classPtr, endLabel.label);

	s10 = successLabel;
	s11 = new MOVE(m_tf, classPtr, result, new CONST(m_tf, classPtr, 1));

	s12 = endLabel;

	return new ESEQ
	    (m_tf, classPtr, 
	     toStm(new Stm[] 
		   { s0, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12 }),
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
		 (HClass.forName("java.lang.Object[]")))))),
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
	     toStm(new Stm[] { s0, s1, s2, s3, s4, s5, end }), 
	     result);
    }
    
    // FIX THIS PLEASE
    private int wordSize() {
	int size = m_offm.size(HClass.forName("java.lang.Object"));
	System.out.println("Returning word size: " + size);
	return size;
    }
}

// Abstract visitor class which preserves derivation and type information.
// Currently only the LabelingVisitor and the CloningVisitor extend this
// class, but eventually TranslationVisitor will as well.
//
abstract class LowQuadWithDerivationVisitor extends LowQuadVisitor {
  
    private Hashtable   m_dT;
    private Derivation  m_derivation;
    private HCode       m_code;
    private TypeMap     m_typeMap;

    protected LowQuadWithDerivationVisitor(Derivation derivation, 
					   HCode code,
					   TypeMap typeMap) {
	m_dT         = new Hashtable();
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

/* Clones the quad graph, while preserving type/derivation information
     * for the new quads */
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
    
/* Adds LABELs to the destination of every branch.  This actually modifies
     * the supplied Quad graph, so it is imperative that a previous 
     * transformation clones the graph prior to using this visitor.    
     */
class LabelingVisitor extends LowQuadWithDerivationVisitor {  
    private Hashtable m_QToL;
    public LabelingVisitor(Derivation d, HCode hc, TypeMap t) { 
	super(d, hc, t);
	m_QToL = new Hashtable(); 
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
		Quad newQ = (Quad)q.clone();  /* IS THIS CORRECT???? */
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

class LowQuadMap {
    final private Hashtable h = new Hashtable();
    void put(Quad qOld, Quad qNew)  { h.put(qOld, qNew); }
    Quad get(Quad old)              { return (Quad)h.get(old); }
    boolean contains(Quad old)      { return h.containsKey(old); }
}
  



