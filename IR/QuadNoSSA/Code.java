// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
package harpoon.IR.QuadNoSSA;

//import harpoon.Analysis.QuadSSA.DeadCode;
import harpoon.IR.QuadSSA.*;
import harpoon.ClassFile.*;
import harpoon.Temp.*;
import harpoon.Util.Set;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.IR.QuadSSA.SIGMA.*;
import harpoon.IR.QuadSSA.*;

//import jas.*;

import java.util.*;
/**
 * <code>QuadSSA.Code</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1 1998-10-14 07:07:58 nkushman Exp $
 */

public class Code extends HCode{
  /** The name of this code view. */
  private static final String codename = "quad-nossa";


  /** The method that this code view represents. */
  HMethod parent;
  /** The quadruples composing this code view. */
  Quad quads;

  /** Creates a <code>Code</code> object from a bytecode object. */
  public Code(harpoon.IR.QuadSSA.Code ssa) 
  {
    this.parent = ssa.getMethod();
    //harpoon.Temp.Temp.clear(); /* debug */
    this.quads = (Quad) ssa.getRootElement();

    removeMagic();
    //CleanUp.cleanup(this); // cleanup null predecessors of phis.
    //Peephole.optimize(this); // peephole optimizations.
    //FixupFunc.fixup(this); // add phi/sigma functions.
    //DeadCode.optimize(this); // get rid of unused phi/sigmas.
  }

  void removeMagic(){
    QuadVisitor v = new QuadVisitor () {
      public void visit (Quad q) {}
      public void visit (CALL q) {
      }
      public void visit (PHI q) {	
	//go through each each of each PHI node within the quad
	for (int i = 0; i < q.src.length; i++){
	  for (int j = 0; j < q.src[i].length; j++){
	    //add a move edge along each preceding edge.
	    MOVE newMove = new MOVE (q.getSourceElement(), 
				     q.dst[i], q.src[i][j]);
	    Edge predE = q.prevEdge(j);
	    
	    Quad.addEdge ((Quad) predE.from(), predE.which_succ(), newMove, 0);
	    Quad.addEdge (newMove, 0, (Quad) predE.to(), predE.which_pred());
	  }
	}
      }
      public void visit (SIGMA q) {
	  //System.out.println ("Sigma thing: " + q.toString());
	for (int i = 0; i < q.dst.length; i++){
	  for (int j = 0; j < q.dst[i].length; j++){
	      //System.out.println ("Moving " + q.src[i].name() + " to " + q.dst[i][j].name());
	    MOVE newMove = new MOVE (q.getSourceElement(), 
				     q.dst[i][j], q.src[i]);
	    Edge nextEdge = q.nextEdge(j);
	    //add a move edge along each following edge
	    Quad.addEdge ((Quad) nextEdge.from(), nextEdge.which_succ(), newMove, 0);
	    Quad.addEdge (newMove, 0, (Quad) nextEdge.to(), nextEdge.which_pred());
	  }
	}
      }
    };
    
    Enumeration elements = getElementsE();
    while (elements.hasMoreElements()){
      ((Quad)elements.nextElement()).visit(v);
    }
  }


  public NMethod createJavaByte(final TypeMap map, final HCode quadform){
    Hashtable labelTable = new Hashtable();
    Hashtable phiTable = new Hashtable();
    Hashtable indexTable = new Hashtable();
    NMethod method = new NMethod (this.getMethod(), indexTable);

    final QuadVisitor v = (QuadVisitor) new ByteCodeQuadVisitor (method, map, quadform,
								 labelTable, phiTable,
								 indexTable);
    
    //now actually create the bytecode
    addSuperBlock (this.quads, labelTable, phiTable, method, v);
    method.limitLocals (((ByteCodeQuadVisitor)v).indexCount);
    method.limitStack(10);
    return method;
  }

  void addSuperBlock(Quad q, Hashtable labelTable, Hashtable phiTable, NMethod method, QuadVisitor v){
    //visit the current quad
    //if ((q instanceof RETURN) || (q instanceof CALL) || (q instanceof METHODHEADER) ||
    //(q instanceof MOVE)){
      method.addInsn (new NLabel ("; ID#: " + q.getID()));
      if (q instanceof SWITCH){
	  System.out.println ("Found the switch");
      }
      q.visit(v);
      //}
    Quad next[] = q.next();

    //add the quads for all the paths other than the main path
    for (int i = 0; i < next.length; i++){
      if ((next[i].prev().length == 1) ||  !phiTable.containsKey(next[i])){
	//if the code hasn't been created anywhere, then get the label out of the label table
	//the label should have been put there when the CJMP or switch instruction was created
	//add the label, and create all the code to follow it
	//if (i > 0){
	NLabel label = (NLabel) labelTable.get(next[i]);
	if (label != null){
	  method.addInsn (label);
	  if (label.myLabel.startsWith ("Label335:")){
	    System.out.println ("Printing label 335");
	  }
	} else {
	  //I'm pretty sure this should never happen
	}
	//}
	//this will create the 
	addSuperBlock (next[i], labelTable, phiTable, method, v);
      } else {
	//if this code has already been output somewhere else, then just branch to it
	method.addInsn (new NInsn ("goto", (NLabel) labelTable.get(next[i])));      
      }
    }
  }

  
  /**
   * Return the <code>HMethod</code> this codeview
   * belongs to.
   */
  public HMethod getMethod() { return parent; }
  
  /**
   * Return the name of this code view.
   * @return the string <code>"quad-ssa"</code>.
   */
  public String getName() { return codename; }
  
  public static void register() {
      HCodeFactory f = new HCodeFactory() {
	  public HCode convert(HMethod m) {
	      HCode c = m.getCode("quad-ssa");
	      return (c==null)?null:new Code((harpoon.IR.QuadSSA.Code) c);
	  }
	  public String getCodeName() {
	      return codename;
	  }
      };
      HMethod.register(f);
    }
  
  /** Returns the root of the control flow graph. */
  public HCodeElement getRootElement() { return quads; }
  /** Returns the leaves of the control flow graph. */
  public HCodeElement[] getLeafElements() {
    HEADER h = (HEADER) getRootElement();
    return new Quad[] { h.footer };
  }

  /**
   * Returns an ordered list of the <code>Quad</code>s
   * making up this code view.  The root of the graph
   * is in element 0 of the array.
   */
  public HCodeElement[] getElements() { 
    UniqueVector v = new UniqueVector();
    traverse(quads, v);
    HCodeElement[] elements = new Quad[v.size()];
    v.copyInto(elements);
    return (HCodeElement[]) elements;
  }
  /** scan through quad graph and keep a list of the quads found. */
  private void traverse(Quad q, UniqueVector v) {
    // If this is a 'real' node, add it to the list.
    if (v.contains(q)) return;
    v.addElement(q);

    // move on to successors.
    Quad[] next = q.next();
    for (int i=0; i<next.length; i++)
      traverse(next[i], v);
  }

  public Enumeration getElementsE() {
    return new Enumeration() {
      Set visited = new Set();
      Stack s = new Stack();
      { s.push(quads); visited.union(quads); } // initialize stack/set.
      public boolean hasMoreElements() { return !s.isEmpty(); }
      public Object nextElement() {
	Quad q = (Quad) s.pop();
	// push successors on stack before returning.
	Quad[] next = q.next();
	for (int i=next.length-1; i>=0; i--)
	  if (!visited.contains(next[i])) {
	    s.push(next[i]);
	    visited.union(next[i]);
	  }
	// okay.
	return q;
      }
    };
  }
}
