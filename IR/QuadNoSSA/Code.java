// Code.java, created by nkushman
package harpoon.IR.QuadNoSSA;

//import harpoon.Analysis.QuadSSA.DeadCode;
import harpoon.IR.QuadSSA.*;
import harpoon.ClassFile.*;
import harpoon.Temp.*;
import harpoon.Util.Set;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.TypeInfo;
import harpoon.IR.QuadSSA.SIGMA.*;
import harpoon.IR.QuadSSA.*;

//import jas.*;

import java.io.*;
import java.util.*;
/**
 * <code>QuadNoSSA.Code</code> is <blink><b>fill me in</b></blink>.
 * 
 * @author  Nate Kushman <nkushman@lcs.mit.edu>
 * @version $Id: Code.java,v 1.2.4.1 1998-11-22 03:32:39 nkushman Exp $
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
    try {
      this.quads = Quad.clone((Quad) ssa.getRootElement());
    } catch (Exception e){
      e.printStackTrace();
    }

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

  public static void touch(){
    return;
  }
    
  public static void write (HClass hclass){
    System.out.println ("Inside the write procl");
    if (hclass == null){
      System.out.println ("Well inside the proc it's null at least");
    }
    // Do something intelligent with these classes. XXX
    System.out.println ("Before creating the ByteCodeClass");
    ByteCodeClass byteCodeClass = new ByteCodeClass(hclass);
    System.out.println ("After creating the ByteCodeClass");
    HMethod hm[] = hclass.getDeclaredMethods();
    for (int j=0; j<hm.length; j++) {
      //SCCAnalysis scc = new SCCAnalysis (new UseDef());
      //SCCOptimize sco = new SCCOptimize (scc,scc,scc);
      if (hm[j].isInterfaceMethod()){
	System.out.println ("I seem to think it's an Interface Method");
	byteCodeClass.addMethod (new NMethod(hm[j], new java.util.Hashtable()));
	//} else if (java.lang.reflect.Modifier.isNative(hm[j].getModifiers())){
	//XXX what the hell do I do if the method is native??
	//System.out.println ("I seem to think it's a Native Method");
	//byteCodeClass.addMethod (new NMethod(hm[j], new java.util.Hashtable()));
      } else if (java.lang.reflect.Modifier.isAbstract(hm[j].getModifiers())) {
	System.out.println ("I seem to think it's an abstract Method");
	byteCodeClass.addMethod (new NMethod(hm[j], new java.util.Hashtable()));
      }else {
	HCode hc1 =  hm[j].getCode("quad-ssa");
		
	if (hc1 == null) {
	  System.out.println ("Yep.. this is Null!");
	  System.out.println ("Class is: " + hm[j].getDeclaringClass().getName());
	  System.out.println ("Method is: " + hm[j].getName());
	  byteCodeClass.addMethod (new NMethod(hm[j], new java.util.Hashtable()));
	} else {
	  //part of the scc optimization stuff
	  //sco.optimize (hc1);
	  //try {
	  //String graphFileName = hclass.getName().replace ('.', '_') + "__" + hm[j].getName() + ".vcg";
	  //PrintWriter graphOut = new PrintWriter (new FileOutputStream (graphFileName));
	  //harpoon.Util.Graph.printCFG(hc1, graphOut, graphFileName);
	  //graphOut.close();
	  //} catch (Exception e) {
	  //e.printStackTrace();
	  //}
	    
	  System.out.println ("Class is: " + hm[j].getDeclaringClass().getName());
	  System.out.println ("Method is: " + hm[j].getName());
	  
	  System.out.println ("Modifiers is: " + hm[j].getModifiers());
	  System.out.println ("Interface is: " + java.lang.reflect.Modifier.INTERFACE);
	  harpoon.IR.QuadNoSSA.Code hc = new harpoon.IR.QuadNoSSA.Code((harpoon.IR.QuadSSA.Code)hc1);
	  System.out.println ("Right before calling create Java on: " +
			      hm[j].getName());
	  try {
	    //NMethod method = hc.createJavaByte (scc, hm[j].getCode("quad-ssa"));
	    NMethod method = hc.createJavaByte (new TypeInfo(), hc1);
	    System.out.println ("Well... it looks like create Java actually worked");
	    byteCodeClass.addMethod (method);
	  } catch (Exception e){
	    e.printStackTrace();
	  }
	}
      }
      
    }
    String assemblyFileName = hclass.getName().replace ('.', '_') + ".j";
    try {
      PrintWriter out = new PrintWriter (new FileOutputStream (assemblyFileName));
      byteCodeClass.writeClass (out);
      out.close();
    } catch (Exception e){
      e.printStackTrace();
    }
    try {
      Runtime.getRuntime().exec ("jasmin " + assemblyFileName.replace ('/', '.')).waitFor();
    } catch (Exception exception){
      exception.printStackTrace();
    }
  }
    
    

}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
