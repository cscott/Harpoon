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

public class ToTree implements Derivation, TypeMap {
  private Derivation  m_derivation;
  private Tree        m_tree;
  private TypeMap     m_typeMap;

  public ToTree(TreeFactory tf, LowQuadNoSSA code, Frame frame)
    {
      final Hashtable dT = new Hashtable();

      m_tree = translate(tf, code, frame, dT);
      m_derivation = new Derivation() {
	public DList derivation(HCodeElement hce, Temp t) {
	  return (DList)dT.get(new Tuple(new Object[] { hce, t }));
	}
      };
      m_typeMap = new TypeMap() {
	public HClass typeMap(HCode hc, Temp t) {
	  return (HClass)dT.get(t);
	}
      };
    }

  public DList derivation(HCodeElement hce, Temp t) {
    return m_derivation.derivation(hce, t);
  }

  public Tree getTree() {
    return m_tree;
  }

  public HClass typeMap(HCode hc, Temp t) {
    // Ignores HCode parameter
    return m_typeMap.typeMap(hc, t);
  }

  private Tree translate(TreeFactory tf, LowQuadNoSSA code, 
			 Frame frame, Hashtable dT)
			 
    {
      CloningTempMap  ctm;
      LowQuadMap      lqm;
      LowQuadVisitor  v;;
      Stm             tree;
      StmList         stmList;

      // Clone the Quad graph
      //
      lqm = new LowQuadMap();
      v = new CloningVisitor(lqm);
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	((Quad)e.nextElement()).visit(v);

      for (Enumeration e = code.getElementsE(); e.hasMoreElements();) {
	Quad qTmp = (Quad)e.nextElement();
	Edge[] el = qTmp.nextEdge();
	for (int i=0; i<el.length; i++) 
	  Quad.addEdge(lqm.get((Quad)el[i].from())[0],
		       el[i].which_succ(),
		       lqm.get((Quad)el[i].to())[0],
		       el[i].which_pred());
      }
	      
      v = new LabelingVisitor();
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	lqm.get((Quad)e.nextElement())[0].visit(v);
      

      ctm = new CloningTempMap
	(((Quad)code.getRootElement()).getFactory().tempFactory(),
	 tf.tempFactory());

      v = new TranslationVisitor(tf, code, frame, ctm, dT);;
      for (Enumeration e = quadGraph(lqm.get((Quad)code.getRootElement())[0]);
	   e.hasMoreElements();)
	((Quad)e.nextElement()).visit(v);

      stmList = ((TranslationVisitor)v).getStmtList();
      
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

  // Enumerates the Quad graph in depth-first order
  //
  private Enumeration quadGraph(final Quad head) {
    return new Enumeration() {
      Set  visited = new HashSet();
      Stack s = new Stack();
      { s.push(head); }
      public boolean hasMoreElements() { return !s.isEmpty(); }
      public Object nextElement() { 
	if (s.isEmpty()) throw new NoSuchElementException();
	Quad q = (Quad)s.pop();
	Quad[] next = q.next();
	for (int i=0; i<next.length; i++) {
	  if (!visited.contains(next[i])) {
	    s.push(next[i]);
	    visited.union(next[i]);
	  }
	}
	return q;
      }
    };
  }
}
  

class TranslationVisitor extends LowQuadVisitor 
{
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
  
  public TranslationVisitor(TreeFactory tf, LowQuadNoSSA code, Frame frame,
			    CloningTempMap ctm, Hashtable dT)
    {
      m_code         = code;
      m_ctm          = ctm;
      m_derivation   = code;
      m_dT           = dT;
      m_frame        = frame;
      m_labelMap     = new LabelMap();
      m_offm         = frame.offsetMap();
      m_stmList      = null;
      m_tempMap      = new TreeTempMap();
      m_tf           = tf;
      m_typeMap      = code;
    }

  public StmList getStmtList() { return m_stmList; }

  public void visit(Quad q) { /* Dont translate other quads */ }

  public void visit(harpoon.IR.Quads.ANEW q) {
    Exp classPtr, hashCode, length;
    Stm s0, s1, s2, s3;

    // Create the fields with which we'll initialize the array
    // 
    hashCode  = new UNOP(m_tf, q, Type.INT, Uop._2I, MAP(q.dst(), q));	
    length    = MAP(q.dims(0), q);
    classPtr  = new NAME(m_tf, q, m_offm.label(q.hclass()));

    // Allocate memory for the array
    // FIX:  needs to allocate memory for hashcode, classptr, length, and 
    //       finalization info
    //
    s0 = new MOVE
      (m_tf, q, 
       MAP(q.dst(), q), 
       m_frame.malloc(new BINOP
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
	 MAP(q.dst(), q),
	 new CONST(m_tf, q, m_offm.hashCodeOffset(q.hclass())))),
       hashCode);
    
    // Assign the array's length field
    //
    s2 = new MOVE
      (m_tf, q,
       new MEM
       (m_tf, q, Type.INT, 
	new BINOP
	(m_tf, q, Type.POINTER, Bop.ADD,
	 MAP(q.dst(), q),
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
	 MAP(q.dst(), q),
	 new CONST(m_tf, q, m_offm.classOffset(q.hclass())))), 
       classPtr);
    
    addStmt(new Stm[] { s0, s1, s2, s3 });      
  }

  public void visit(harpoon.IR.Quads.ARRAYINIT q) {
    Stm s0, s1, s2;
    TEMP nextPtr = extra(q.objectref(), q, Type.POINTER);

    s0 = new MOVE
      (m_tf, q, 
       nextPtr,
       new BINOP
       (m_tf, q, Type.POINTER, Bop.ADD,
	MAP(q.objectref(), q),
	new CONST
	(m_tf, q, 
	 m_offm.elementsOffset(type(q.objectref())) + 
	 (q.value().length * 
	  m_offm.size(q.type())))));

    addStmt(s0);
    
    for (int i=0; i<q.value().length; i++) {
      s0 = new MOVE
	(m_tf, q, 
	 new MEM(m_tf, q, Type.POINTER, nextPtr), 
	 mapconst(q, q.value()[i], q.type()));
      s1 = new MOVE
	(m_tf, q, 
	 nextPtr, 
	 new BINOP(m_tf, q, Type.POINTER, Bop.ADD, 
		   nextPtr, 
		   new CONST(m_tf, q, 4)));
      addStmt(new Stm[] { s0, s1 });
    }
  }

  public void visit(harpoon.IR.Quads.CJMP q) { 
    Util.assert(q.next().length==2 && 
		q.next(0) instanceof harpoon.IR.Quads.LABEL &&
		q.next(1) instanceof harpoon.IR.Quads.LABEL);		
    addStmt(new CJUMP(m_tf, q, MAP(q.test(), q),
		      (MAP((harpoon.IR.Quads.LABEL)q.next(0))).label,
		      (MAP((harpoon.IR.Quads.LABEL)q.next(1))).label));
  }
  
  public void visit(harpoon.IR.Quads.COMPONENTOF q) {
    addStmt
      (new MOVE 
       (m_tf, q, 
	MAP(q.dst(), q), 
	isInstanceOf(q, q.objectref(), 
		     type(q.arrayref()).getComponentType())));
  }

  public void visit(harpoon.IR.Quads.CONST q) {
    addStmt
      (new MOVE
       (m_tf, q, 
	MAP(q.dst(), q), 
	mapconst(q, q.value(), q.type())));
  }
  
  public void visit(harpoon.IR.Quads.INSTANCEOF q) {
    addStmt
      (new MOVE
       (m_tf, q, 
	MAP(q.dst(), q), 
	isInstanceOf(q, q.src(), q.hclass())));
  }
  
  public void visit(harpoon.IR.Quads.LABEL q) {
    addStmt(MAP(q));
  }

  public void visit(harpoon.IR.Quads.MONITORENTER q) {
    // Call to runtime libraries here
  }

  public void visit(harpoon.IR.Quads.MONITOREXIT q) {
    // Call to runtime libraries here
  }

  public void visit(harpoon.IR.Quads.MOVE q) {
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.src(), q)));
  }

  public void visit(harpoon.IR.Quads.NEW q) { 
    Stm s0, s1, s2;

    // Allocate memory for the new object
    s0 = new MOVE
      (m_tf, q, 
       MAP(q.dst(), q), 
       m_frame.malloc(new CONST(m_tf, q, m_offm.size(type(q.dst())))));

    // Assign the new object a hashcode
    s1 = new MOVE
      (m_tf, q, 
       new MEM
       (m_tf, q, Type.INT, 
	new BINOP
	(m_tf, q, Type.POINTER, Bop.ADD,
	 MAP(q.dst(), q),
	 new CONST(m_tf, q, m_offm.hashCodeOffset(q.hclass())))),
       new UNOP(m_tf, q, Type.POINTER, Uop._2I, MAP(q.dst(), q)));

    // Assign the new object a class pointer
    s2 = new MOVE
      (m_tf, q, 
       new MEM
       (m_tf, q, Type.POINTER,
	new BINOP
	(m_tf, q, Type.POINTER, Bop.ADD,
	 MAP(q.dst(), q),
	 new CONST(m_tf, q, m_offm.classOffset(q.hclass())))),
       new NAME(m_tf, q, m_offm.label(q.hclass())));

    addStmt(new Stm[] { s0, s1 });
  }

  public void visit(harpoon.IR.Quads.RETURN q) {
    if (q.retval()!=null)  // is this ok for void func?
      addStmt(new MOVE(m_tf, q, 
		       new TEMP(m_tf, q, 
				maptype(type(q.retval())), m_frame.RV()), 
		       MAP(q.retval(), q)));

    //addStmt(new JUMP(m_tf, q, (MAP((harpoon.IR.Quads.LABEL)q.next(0))).label));
  }

  public void visit(harpoon.IR.Quads.SWITCH q) { /* Naive implementation */
    Quad qNext;  CJUMP branch; LABEL lNext;
    TEMP discriminant = MAP(q.index(), q);
    for (int i=0; i<q.keysLength(); i++) {
      qNext  = q.next(i); Util.assert(qNext instanceof harpoon.IR.Quads.LABEL);
      lNext  = new LABEL(m_tf, q, new Label());
      branch = new CJUMP
	(m_tf, q, new BINOP(m_tf, q, Type.INT, Bop.CMPEQ, 
			    discriminant, 
			    new CONST(m_tf, q, q.keys(i))),
	 (MAP((harpoon.IR.Quads.LABEL)qNext)).label,
	 lNext.label);
      addStmt(new Stm[] { MAP((harpoon.IR.Quads.LABEL)qNext), branch });
    }
  }
  
  public void visit(harpoon.IR.Quads.THROW q) { 
    addStmt
      (new MOVE
       (m_tf, q, 
	new TEMP(m_tf, q, maptype(type(q.throwable())), m_frame.RX()), 
	MAP(q.throwable(), q)));
    //addStmt(new JUMP(m_tf, q, (MAP((harpoon.IR.Quads.LABEL)q.next(0))).label));
  }

  public void visit(harpoon.IR.Quads.TYPECAST q) {
    // Insert run-time type check
  }

  /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
   *                                                          *
   *                   LowQuad Translator                     *
   *                                                          *
   *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		  
  public void visit(PAOFFSET q) {
    addStmt
      (new MOVE
       (m_tf, q, MAP(q.dst(), q), 
	new BINOP
	(m_tf, q, Type.INT, Bop.MUL,
	 new CONST(m_tf, q, m_offm.size(q.arrayType().getComponentType())),
	 MAP(q.index(), q))));
  }

  public void visit(PARRAY q) {
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.objectref(), q)));
  }

  public void visit(PCALL q) {
      Util.assert(q.retex()!=null && q.ptr()!=null);

      ExpList params; Temp[] qParams; TEMP retval; 

      // If q.retval() is null, create a throwaway TEMP to store the retval
      retval = 
	(q.retval()==null) ? 
	extra(q.ptr(), q, maptype(type(q.retval()))) : MAP(q.retval(), q);
      qParams = q.params(); params = null; 
      for (int i=qParams.length-1; i >= 0; i--)
	params = new ExpList(MAP(qParams[i], q), params);      
      addStmt(new CALL(m_tf, q, retval, 
		       MAP(q.retex(), q), MAP(q.ptr(), q), params));
    }

  public void visit(PFCONST q) {
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q, Type.POINTER), 
		     new NAME(m_tf, q, m_offm.label(q.field()))));
  }

  public void visit(PFIELD q) { 
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q), MAP(q.objectref(), q)));
  }
  
  public void visit(PFOFFSET q) {
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q), 
		     new CONST(m_tf, q, m_offm.offset(q.field()))));
  }

  public void visit(PGET q) {
    addStmt(new MOVE(m_tf, q, 
		     MAP(q.dst(), q), 
		     new MEM(m_tf, q, maptype(type(q.dst())), 
			     MAP(q.ptr(), q, Type.POINTER))));
  }
  
  public void visit(PMCONST q) { 
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q, Type.POINTER), 
		     new NAME(m_tf, q, m_offm.label(q.method()))));
  }

  public void visit(PMETHOD q) {
    addStmt
      (new MOVE
       (m_tf, q, 
	MAP(q.dst(), q),              
	new MEM(m_tf, q, Type.POINTER,
		new BINOP(m_tf, q, Type.INT, Bop.ADD, 
			  MAP(q.objectref(), q), 
			  new CONST(m_tf, q, 
				    m_offm.classOffset(type(q.dst())))))));
  }

  public void visit(PMOFFSET q) {
    addStmt(new MOVE(m_tf, q, 
		     MAP(q.dst(), q),
		     new CONST(m_tf, q, m_offm.offset(q.method()))));
  }

  public void visit(POPER q) {
    Exp oper = null; int optype;
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
    
    if (operands.length==1)
      oper = new UNOP(m_tf, q, maptype(type(q.dst())), optype,
		      MAP(operands[0], q)); 
    else if (operands.length==2)
      oper = new BINOP(m_tf, q, maptype(type(q.dst())), optype,
		       MAP(operands[0], q), MAP(operands[1], q)); 
    else 
      throw new Error("Unexpected # of operands: " + q);
    
    addStmt(new MOVE(m_tf, q, MAP(q.dst(), q), oper));
  }
  
  public void visit(PSET q) {
    addStmt(new MOVE(m_tf, q, 
		     new MEM(m_tf, q, maptype(type(q.src())), MAP(q.ptr(), q)),
		     MAP(q.src(), q)));
  }

  /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
   *                                                          *
   *                   Utility Functions                      *
   *                                                          *
   *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

  private void addStmt(Stm stm) { m_stmList = new StmList(stm, m_stmList);  }
  private void addStmt(Stm[] stm) 
    { for (int i=0; i<stm.length; i++)addStmt(stm[i]); }

  private TEMP extra(Temp tOld, HCodeElement source, int type) {
    Temp tNew = new Temp(tOld.tempFactory(), "tr_");
    return m_tempMap.tempMap(tNew, source, type);
  }

  private LABEL MAP(harpoon.IR.Quads.LABEL label) 
    { return m_labelMap.labelMap(label); }

  private TEMP MAP(Temp t, HCodeElement source) 
    { return MAP(t, source, maptype(type(t))); }

  private TEMP MAP(Temp t, HCodeElement source, int type) 
    { return m_tempMap.tempMap(t, source, type); }

  private HClass type(Temp t) 
    { return (t==null)?null:m_typeMap.typeMap(m_code, t); }

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

  private Exp mapconst(HCodeElement src, Object value, HClass type) {
    Exp constant;

    if (type==HClass.Boolean ||
	type==HClass.Byte    ||
	type==HClass.Char    ||
	type==HClass.Short   ||
	type==HClass.Int) 
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
      constant = new NAME(m_tf, src, m_offm.label((String)value));
    else 
      throw new Error("Bad type for CONST " + type); 
    return constant;
  }

  // Returns true if (typeof(q.src()) instanceof type)
  //
  private Exp isInstanceOf(Quad q, Temp src, HClass type) {
    Exp result = null;

    if (type.isInterface()) {
      throw new Error("instanceof interface not impl");
    }
    else {
      Stm   s0, s1;
      TEMP  clsPtr   = extra(src, q, Type.POINTER);
      TEMP  clsValue = extra(src, q, Type.INT);
      TEMP  srcValue = extra(src, q, Type.INT);

      // s0:  srcValue <-- the unique ID of the class in the same depth in the class 
      //                   hierarchy as "type" is
      //
      s0 = new MOVE(m_tf, q, 
		    srcValue,
		    new MEM(m_tf, q, Type.INT, 
			    new BINOP(m_tf, q, Type.POINTER, Bop.ADD, 
				      new CONST(m_tf, q, m_offm.displayOffset(type)),
				      new MEM(m_tf, q, Type.POINTER,
					      new BINOP(m_tf, q, Type.POINTER, Bop.ADD,
							new CONST(m_tf, q, m_offm.classOffset(type(src))),
							MAP(src, q))))));
      s1 = new MOVE(m_tf, q, 
		    clsValue,
		    new MEM(m_tf, q, Type.INT, 
			    new BINOP(m_tf, q, Type.POINTER, Bop.ADD,
				      new CONST(m_tf, q, m_offm.displayOffset(type)),
				      clsPtr)));

      result = new ESEQ(m_tf, q, 
			new SEQ(m_tf, q, s0, s1),
			new BINOP(m_tf, q, Type.INT, Bop.CMPEQ, srcValue, clsValue));
    }
    return result;
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
}

class CloningVisitor extends LowQuadVisitor {
  private LowQuadMap m_lqm;
  public CloningVisitor(LowQuadMap lqm) {
    m_lqm = lqm;
  }
  public void visit(Quad q) {
    Quad qm = (Quad)q.clone();
    m_lqm.put(q, new Quad[] { qm } );
  }
}

/* Adds LABELs to the destination of every branch.  This actually modifies
 * the supplied Quad graph, so it is imperative that a previous transformation
 * clones the graph prior to using this visitor.      */
class LabelingVisitor extends LowQuadVisitor {  
  private Hashtable m_QToL;
  public LabelingVisitor() { m_QToL = new Hashtable(); }
  public void visit(Quad q) { } 
  public void visit(harpoon.IR.Quads.SIGMA q) {
    Quad[] successors = q.next();
    for (int i=0; i < successors.length; i++)
      toLabel(successors[i]);
  }
  private void toLabel(Quad q) {
    harpoon.IR.Quads.LABEL label;
    
    label  = (harpoon.IR.Quads.LABEL)m_QToL.get(q);
    if (label==null) {
      if (q instanceof harpoon.IR.Quads.PHI) {
	label = new harpoon.IR.Quads.LABEL
	  (q.getFactory(), (harpoon.IR.Quads.PHI)q, new Label().toString());
	Quad.replace(q, label);
      }
      else {
	Quad newQ = (Quad)q.clone();  /* IS THIS CORRECT???? */
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
      }
    }
  }
}

class LowQuadMap {
    final private Hashtable h = new Hashtable();
    void put(Quad qOld, Quad[] qNew)  { h.put(qOld, qNew); }
    Quad[] get(Quad old)              { return (Quad[])h.get(old); }
    boolean contains(Quad old)        { return h.containsKey(old); }
}
