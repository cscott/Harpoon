package harpoon.IR.QuadNoSSA;

import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Util.Util;

import java.util.*;
 
class ByteCodeQuadVisitor extends QuadVisitor{

  private static final int D = 0;
  private static final int F = 1;
  private static final int I = 2;
  private static final int L = 3;
  private static final int A = 4;
  private static final boolean printTypes = false;
  private static final boolean writeTypes = true;

  NMethod method;
  TypeMap map;
  HCode quadform;
  Hashtable phiTable;

  int indexCount;
  int labelCount;

  Hashtable indexTable;
  Hashtable labelTable;
  
  
  ByteCodeQuadVisitor(NMethod myMethod, TypeMap myMap, HCode myQuadform, 
		      Hashtable myLabelTable, Hashtable myPhiTable, Hashtable myIndexTable){
    
    method = myMethod;
    map = myMap;
    quadform = myQuadform;
    labelTable = myLabelTable;
    phiTable = myPhiTable;
    indexTable = myIndexTable;

    indexCount = 0;
    labelCount = 0;
  }
  
  //auxilary function used to load a temp from a local variable onto the stack
  void addLoad (NMethod method, TypeMap map, HCode quadform, Temp obj, 
		Hashtable indexTable) {
    putIndex (obj, indexTable);
    HClass tempClass = map.typeMap(quadform, obj);
    if (tempClass == null){
    }
    if (tempClass.isPrimitive()){
      if (tempClass == HClass.Boolean){
	method.addInsn(new NInsn ("iload", obj));
      } else if (tempClass == HClass.Byte){
	method.addInsn(new NInsn ("iload", obj));
      } else if (tempClass == HClass.Char){
	method.addInsn(new NInsn ("iload", obj));
      } else if (tempClass == HClass.Double){
	method.addInsn(new NInsn ("dload", obj));
      } else if (tempClass == HClass.Float){
	method.addInsn(new NInsn ("fload", obj));
      } else if (tempClass == HClass.Int){
	method.addInsn(new NInsn ("iload", obj));
      } else if (tempClass == HClass.Long){
	method.addInsn(new NInsn ("lload", obj));
      } else if (tempClass == HClass.Short){
	method.addInsn(new NInsn ("iload", obj));
      } else if (tempClass == HClass.Void){
	  method.addInsn(new NInsn ("aconst_null"));
	//this should never happen
	//System.out.println ("Trying to load from a void Temp");
      }
    } else if (tempClass.isArray()){
      method.addInsn(new NInsn ("aload", obj));
    } else {
      method.addInsn(new NInsn ("aload", obj));
    }
  }

  //set of auxilary function used to load temps from local variables
  //added so that this can later be optimized to use the load_1, load_2 and load_3
  //bytecodes
  void addiload (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("iload", putIndex(obj, indexTable)));
  }


  void addlload (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("lload", putIndex (obj, indexTable)));
  }

  void adddload (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("dload", putIndex (obj, indexTable)));
  }

  void addaload (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("aload", putIndex (obj, indexTable)));
  }

  void addfload (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("fload", putIndex (obj, indexTable)));
  }


  void addistore (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("istore", putIndex (obj, indexTable)));
  }


  void addlstore (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("lstore", putIndex (obj, indexTable)));
  }

  void adddstore (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("dstore", putIndex (obj, indexTable)));
  }

  void addfstore (NMethod method, Temp obj, Hashtable indexTable)  {
    method.addInsn(new NInsn ("fstore", putIndex (obj, indexTable)));
  }

  //auxilarry function for loading from local variables
  final void addStore (NMethod method, TypeMap map, HCode quadform, Temp obj,
		       Hashtable indexTable) {
    putIndex (obj, indexTable);
    HClass tempClass = map.typeMap(quadform, obj);
    if (tempClass.isPrimitive()){
      if (tempClass == HClass.Boolean){
	method.addInsn(new NInsn ("istore", obj));
      } else if (tempClass == HClass.Byte){
	method.addInsn(new NInsn ("istore", obj));
      } else if (tempClass == HClass.Char){
	method.addInsn(new NInsn ("istore", obj));
      } else if (tempClass == HClass.Double){
	method.addInsn(new NInsn ("dstore", obj));
      } else if (tempClass == HClass.Float){
	method.addInsn(new NInsn ("fstore", obj));
      } else if (tempClass == HClass.Int){
	method.addInsn(new NInsn ("istore", obj));
      } else if (tempClass == HClass.Long){
	method.addInsn(new NInsn ("lstore", obj));
      } else if (tempClass == HClass.Short){
	method.addInsn(new NInsn ("istore", obj));
      } else if (tempClass == HClass.Void){
	  method.addInsn(new NInsn ("astore", obj));
	//this should never happen
	//System.out.println ("Trying to store into a void reference");
      }
    } else if (tempClass.isArray()){
      method.addInsn(new NInsn ("astore", obj));
    } else {
      method.addInsn(new NInsn ("astore", obj));
      //System.out.println ("Not recognizing the type of the store");
    }
  }
  
  //auxillary function used to get the index of a temp in the local variable table.  It
  //creates a new slot in the LVTable if one does not already exist for it.
  final Temp putIndex (Temp obj, Hashtable table){
    Object lvIndex = table.get (obj);
    if (lvIndex == null){
      lvIndex = new Integer (indexCount);
      table.put (obj, lvIndex);
      //System.out.println ("Assigning " + lvIndex + " to a Temp");
      indexCount++;
    }
    //just return the obj to make the program semantics cleaner;
    return obj;
  }
  
  //auxillyary function used to add the code for a quad of type OPER.
  //it loads both of the operands, preforms the operation, and then stores the answer.
  void addOper (NMethod method, String opc, OPER o, Hashtable indexTable, int src, int dst){
    try {
      if (src == D){
	for (int i = 0; i < o.operandsLength(); i++){
	  adddload (method, o.operands(i), indexTable);
	}
      } else if (src == F){
	for (int i = 0; i < o.operandsLength(); i++){
	  addfload (method, o.operands(i), indexTable);
	}
      } else if (src == I){
	for (int i = 0; i < o.operandsLength(); i++){
	  addiload (method, o.operands(i), indexTable);
	}
      } else if (src == L){
	for (int i = 0; i < o.operandsLength(); i++){
	  addlload (method, o.operands(i), indexTable);
	}
      }

      method.addInsn(new NInsn (opc));
      if (dst == D){
	adddstore (method, o.dst(), indexTable);
      } else if (dst == F){
	addfstore (method, o.dst(), indexTable);
      } else if (dst == I){
	addistore (method, o.dst(), indexTable);
      } else if (dst == L){
	addlstore (method, o.dst(), indexTable);
      }
    } catch (Exception e){
      e.printStackTrace();
    }

  }
    
  
  //an auxillary function used to hack around the fact that the only compare bytecodes that exist for many data-types
  //are if_cmp bytecodes, which require use to produce really stupid bytecode that branhces and then either pushes 1 or
  //0 on the stack.
  //opc1 is used if there is another instruction that we need to run first.
  void hackOper (NMethod method, String opc1, String opc2, OPER o, Hashtable indexTable, Hashtable labelTable, int src){
    try {
      //create all the neccessary labels,
      
      NLabel label1 = new NLabel ("Label" + labelCount++);
      NLabel label2 = new NLabel ("Label" + labelCount++);
      if (src == D){
	for (int i = 0; i < o.operandsLength(); i++){
	  adddload (method, o.operands(i), indexTable);
	}
      } else if (src == F){
	for (int i = 0; i < o.operandsLength(); i++){
	  addfload (method, o.operands(i), indexTable);
	}
      } else if (src == I){
	for (int i = 0; i < o.operandsLength(); i++){
	  addiload (method, o.operands(i), indexTable);
	}
      } else if (src == L){
	for (int i = 0; i < o.operandsLength(); i++){
	  addlload (method, o.operands(i), indexTable);
	}
      } else if (src == A){
	for (int i = 0; i < o.operandsLength(); i++){
	  addaload (method, o.operands(i), indexTable);
	}
      }
      if (opc1 != ""){
	method.addInsn(new NInsn (opc1));
      }

      method.addInsn(new NInsn (opc2, label1));//label
      method.addInsn(new NInsn ("iconst_0")); //push
      method.addInsn(new NInsn ("goto", label2));//goto
      method.addInsn(label1);//label
      method.addInsn(new NInsn ("iconst_1"));//push
      method.addInsn(label2);//label
      
      method.addInsn(new NInsn ("istore", putIndex (o.dst(), indexTable)));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
    
  //auxillary function used to add the shift lshift instructinos because we need to load 
  // an extra int
  void addShift (NMethod method, String opc, OPER o, Hashtable indexTable){
    try {
      addlload (method, o.operands(0), indexTable);
      addiload (method, o.operands(1), indexTable);
      
      method.addInsn(new NInsn (opc));

      addlstore (method, o.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }

  }


  public void visit (Quad q) {}
  //**AGET
  public void visit (AGET q) {
    if (writeTypes){
      method.addInsn (new NLabel ("AGET" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting AGET");
    }
    try {
      method.addInsn(new NInsn ("aload", putIndex(q.objectref(), 
						  indexTable)));
      method.addInsn(new NInsn ("iload", putIndex(q.index(), indexTable)));
      HClass tempClass = map.typeMap(quadform, q.objectref());
      if (tempClass.isPrimitive()){
	if (tempClass == HClass.Boolean){
	  method.addInsn(new NInsn ("i2b"));
	  method.addInsn(new NInsn ("baload"));
	} else if (tempClass == HClass.Byte){
	  method.addInsn(new NInsn ("i2b"));
	  method.addInsn(new NInsn ("baload"));
	} else if (tempClass == HClass.Char){
	  method.addInsn(new NInsn ("i2c"));
	  method.addInsn(new NInsn ("caload"));
	} else if (tempClass == HClass.Double){
	  method.addInsn(new NInsn ("daload"));
	} else if (tempClass == HClass.Float){
	  method.addInsn(new NInsn ("faload"));
	} else if (tempClass == HClass.Int){
	  method.addInsn(new NInsn ("iaload"));
	} else if (tempClass == HClass.Long){
	  method.addInsn(new NInsn ("laload"));
	} else if (tempClass == HClass.Short){
	  method.addInsn(new NInsn ("i2s"));
	  method.addInsn(new NInsn ("saload"));
	}
      } else {
	method.addInsn(new NInsn ("aaload"));
      }
      addStore (method, map, quadform, q.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**ALENGTH
  public void visit (ALENGTH q) {
    if (writeTypes){
      method.addInsn (new NLabel ("ALENGTH" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting ALENGTH");
    }
    try {
      method.addInsn(new NInsn ("aload", putIndex(q.objectref(), 
						  indexTable)));
      method.addInsn(new NInsn ("arraylength"));
      method.addInsn(new NInsn ("istore", putIndex (q.dst(), indexTable)));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**ANEW
  public void visit (ANEW q) {
    if (writeTypes){
      method.addInsn (new NLabel ("ANEW" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting ALENGTH");
    }
    try {
      int i = 0;
      for (; i < q.dimsLength(); i++){
	addLoad (method, map, quadform, q.dims(i), indexTable);
      }
      method.addInsn(new MultiarrayInsn (q.hclass(), i));
      addStore (method, map, quadform, q.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**ASET
  //store into the array in addition to converting it to stort char or byte if neccessary.
  public void visit (ASET q) {
    if (writeTypes){
      method.addInsn (new NLabel ("ASET" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting ASET");
    }

    try {
      method.addInsn(new NInsn ("aload", putIndex(q.objectref(), 
						  indexTable)));
      method.addInsn(new NInsn ("iload", putIndex(q.index(), indexTable)));
      addLoad (method, map, quadform, q.src(), indexTable);
      HClass tempClass = map.typeMap(quadform, q.objectref());
      if (tempClass.isPrimitive()){
	if (tempClass == HClass.Boolean){
	  method.addInsn(new NInsn ("i2b"));
	  method.addInsn(new NInsn ("bastore"));
	} else if (tempClass == HClass.Byte){
	  method.addInsn(new NInsn ("i2b"));
	  method.addInsn(new NInsn ("bastore"));
	} else if (tempClass == HClass.Char){
	  method.addInsn(new NInsn ("i2c"));
	  method.addInsn(new NInsn ("castore"));
	} else if (tempClass == HClass.Double){
	  method.addInsn(new NInsn ("dastore"));
	} else if (tempClass == HClass.Float){
	  method.addInsn(new NInsn ("fastore"));
	} else if (tempClass == HClass.Int){
	  method.addInsn(new NInsn ("iastore"));
	} else if (tempClass == HClass.Long){
	  method.addInsn(new NInsn ("lastore"));
	} else if (tempClass == HClass.Short){
	  method.addInsn(new NInsn ("i2s"));
	  method.addInsn(new NInsn ("sastore"));
	}
      } else {
	method.addInsn(new NInsn ("aastore"));
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**CALL
  public void visit (CALL q) {
    if (writeTypes){
      method.addInsn (new NLabel (";CALL exceptions go in " + q.retex().name()));
    }
    if (printTypes){
      System.out.println ("Visiting CALL");
    }
    try {
      //create all the neccessary labels
      NLabel start = new NLabel ("Label" + labelCount++);

      NLabel end = new NLabel ("Label" + labelCount++);
      
      NLabel continueLabel = new NLabel ("Label" + labelCount++);
      
      NLabel handler = new NLabel ("Label" + labelCount++);
      
      //put the null value into the local variable for returning exceptions
      method.addInsn (new NInsn ("aconst_null"));
      method.addInsn (new NInsn ("astore", putIndex(q.retex(), indexTable)));
      
      //and the one for normal returns if there is a return value
      if (q.retval() != null){
	HClass tempClass = map.typeMap(quadform, q.retval());
	if (tempClass.isPrimitive()){
	  if (tempClass == HClass.Boolean){
	    method.addInsn(new NInsn ("iconst_0"));
	  } else if (tempClass == HClass.Byte){
	    method.addInsn(new NInsn ("iconst_0"));
	  } else if (tempClass == HClass.Char){
	    method.addInsn(new NInsn ("iconst_0"));
	  } else if (tempClass == HClass.Double){
	    method.addInsn(new NInsn ("dconst_0"));
	  } else if (tempClass == HClass.Float){
	    method.addInsn(new NInsn ("fconst_0"));
	  } else if (tempClass == HClass.Int){
	    method.addInsn(new NInsn ("iconst_0"));
	  } else if (tempClass == HClass.Long){
	    method.addInsn(new NInsn ("lconst_0"));
	  } else if (tempClass == HClass.Short){
	    method.addInsn(new NInsn ("iconst_0"));
	  } else if (tempClass == HClass.Void){
	    method.addInsn(new NInsn ("aconst_null", q.retval()));
	    //this should never happen
	    //System.out.println ("Trying to store into a void reference");
	  }
	} else if (tempClass.isArray()){
	  method.addInsn(new NInsn ("aconst_null"));
	} else {
	  method.addInsn(new NInsn ("aconst_null"));
	  //System.out.println ("Not recognizing the type of the store");
	}
	addStore (method, map, quadform, q.retval(), indexTable);
      }
      //put the object ref and all the arguments onto the stack
      Util.assert(false, "this code needs to be fixed.");
      /* I BELIEVE THIS IS ALL UNNECESSARY DUE TO THE CALL PARAM REWRITE. CSA.
      if (!q.isStatic()){
	  method.addInsn(new NInsn ("aload", putIndex(q.objectref(), 
						      indexTable)));
	  if (!q.method().getDeclaringClass().getName().replace('.', '/').startsWith("java/lang/Object")
	      && !q.method().getName().equals ("<init>")){
	      //System.out.println ("The method name is:" + q.method.getDeclaringClass().getName().replace('.', '/') + ":end");
	      method.addInsn(new NInsn ("checkcast", q.method().getDeclaringClass()));
	  }
      }
      */
      for (int i = 0; i < q.paramsLength(); i++){
	addLoad (method, map, quadform, q.params(i), indexTable);
	HClass tempClass = q.method().getParameterTypes()[i];
	if (!tempClass.getName().replace('.', '/').startsWith("java/lang/Object") &&
	    !tempClass.isPrimitive()){
	    method.addInsn(new NInsn ("checkcast", tempClass));
	}
      }
      //add extra label to use to add the try catch block stuff.
      method.addInsn(start);
      if (q.isStatic()){
	method.addInsn(new NInsn ("invokestatic", q.method()));
      } else if (q.isInterfaceMethod()){
	  //System.out.println ("Calling invokeinterface");
	  method.addInsn(new NInsn("invokeinterface", q.method()));
      } else if (!q.isVirtual()){
	method.addInsn(new NInsn ("invokespecial", q.method()));
      } else {
	method.addInsn(new NInsn ("invokevirtual", q.method()));
      }
      
      //two instructions added to make sure that the exection area si
      //null
      //method.addInsn (new NInsn ("aconst_null"));
      //addStore (method, map, quadform, q.retex, indexTable);
      method.addInsn(end);
      //if the method actually returns something, then store it
      if (q.retval() != null){
	addStore (method, map, quadform, q.retval(), indexTable);
      }
      method.addInsn(new NInsn ("goto", continueLabel));
      
      method.addInsn(handler);
      
      addStore (method, map, quadform, q.retex(), indexTable);
      
      method.addInsn(continueLabel);
      if (!(q.method() instanceof HConstructor)){
	  if (q.method().getName() == "<init>"){
	      System.out.println ("opps, somebody fucked up");
	  } else {
	      method.addInsn (new NLabel (";The proc is called: " + q.method().getName()));
	      method.addCatch (start, end, handler, "all");
	  }
      } else {
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**CJMP
  public void visit (CJMP q) {  
    if (writeTypes){
      method.addInsn (new NLabel ("CJMP" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting CJMP");
    }
    try {
      NLabel label = new NLabel ("Label" + labelCount++);
      if (!labelTable.containsKey (q.next(1))){
	method.addInsn (new NLabel ("; label for Quad " + q.next(1).getID() + " not there "));
	labelTable.put (q.next(1), label);
      } else {
	label = (NLabel) labelTable.get (q.next(1));
      }
      addLoad (method, map, quadform, q.test(), indexTable);
      method.addInsn(new NInsn ("ifne", label));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**COMPONENTOF
  public void visit (COMPONENTOF q) {
    if (writeTypes){
      method.addInsn (new NLabel ("COMPONENTOF" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting COMPONENTOF");
    }
    //***Big Hack #2*** should always evaluate to true from the bytecode perspective,  
    //because the JVM will take care of it
    try{
      method.addInsn(new NInsn ("iconst_1"));
      method.addInsn(new NInsn ("istore", putIndex(q.dst(), indexTable)));
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**CONST
  public void visit (CONST q) {
    if (writeTypes){
      method.addInsn (new NLabel ("CONST" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting CONST");
    }

    try {
      if (q.type().isPrimitive()){
	if (q.type() == HClass.Int){
	  method.addInsn(new NInsn ("ldc", new NLabel (q.value().toString())));
	} else if (q.type() == HClass.Long){
	  method.addInsn(new NInsn ("ldc_w", new NLabel (q.value().toString())));
	} else if (q.type() == HClass.Float){
	  method.addInsn(new NInsn ("ldc", new NLabel (q.value().toString())));
	} else if (q.type() == HClass.Double){
	  method.addInsn(new NInsn ("ldc_w", new NLabel (q.value().toString())));
	} else if (q.type() == HClass.Void){
	  method.addInsn(new NInsn ("aconst_null"));
	}
      }
      if (q.type() == HClass.forName ("java.lang.String")){
	method.addInsn(new NInsn ("ldc", q.value().toString()));
      }
      addStore (method, map, quadform, q.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  //**FOOTER
  public void visit (FOOTER q) {
    if (writeTypes){
      method.addInsn (new NLabel ("FOOTER" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting FOOTER");
    }
  }

  //**FixupFunc
  /*public void visit (FixupFunc q) {
    //I "should NEVER EVER see this." C. Ananian
    }*/

  //**GET
  public void visit (GET q) {
    if (writeTypes){
      method.addInsn (new NLabel ("GET" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting GET");
    }
    try {
      if (q.field().isStatic()){
	method.addInsn(new NInsn ("getstatic", q.field()));
      } else if (q.objectref() == null){
	System.out.println ("Hmm.. I think Scott has fucked up");
      } else {
	method.addInsn(new NInsn ("aload", putIndex (q.objectref(), indexTable)));
       method.addInsn(new NInsn ("checkcast", q.field().getDeclaringClass()));
	method.addInsn(new NInsn ("getfield", q.field()));
      }
      addStore (method, map, quadform, q.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  
  //**INSTANCEOF
  public void visit (INSTANCEOF q) {
    if (writeTypes){
      method.addInsn (new NLabel ("INSTANCEOF" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting INSTANCEOF");
    }
    try {
      method.addInsn(new NInsn ("aload", putIndex (q.src(), indexTable)));
      method.addInsn(new NInsn ("checkcast", q.hclass()));
      method.addInsn(new NInsn ("instanceof", q.hclass()));
      method.addInsn(new NInsn ("istore", putIndex (q.dst(), indexTable)));
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**METHODHEADER
  public void visit (HEADER q) {
    if (writeTypes){
      method.addInsn (new NLabel ("METHODHEADER" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting METHODHEADER");
    }
    for (int i = 0; i < q.paramsLength(); i++){
      putIndex (q.params(i), indexTable);
    }
    
    //need to come back to this, and store all the parameters into the 
    //indexTable in the correct spots
  }
  
  ///**MONITOR ENTER
    public void visit (MONITORENTER q) {
    if (writeTypes){
      method.addInsn (new NLabel ("MONITORENTER" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting MONITORENTER");
    }
      try {
	method.addInsn(new NInsn ("aload", putIndex (q.lock(), indexTable)));
	method.addInsn(new NInsn ("monitorenter"));
      } catch (Exception e){
	e.printStackTrace();
      }
    }
  //**MONITOR EXIT
  public void visit (MONITOREXIT q) {
    if (writeTypes){
      method.addInsn (new NLabel ("GET" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting MONITOREXIT");
    }
    try {
      method.addInsn(new NInsn ("aload", putIndex (q.lock(), indexTable)));
      method.addInsn(new NInsn ("monitorexit"));
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**MOVE
  public void visit (MOVE q) {
    if (writeTypes){
      method.addInsn (new NLabel ("MOVE" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting MOVE");
    }
    try {
      addLoad (method, map, quadform, q.src(), indexTable);
      addStore (method, map, quadform, q.dst(), indexTable);
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**NEW
  public void visit (NEW q) {
    if (writeTypes){
      method.addInsn (new NLabel ("NEW" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting NEW");
    }
    try {
      method.addInsn (new NInsn ("aconst_null"));
      method.addInsn (new NInsn ("astore", putIndex (q.dst(), indexTable)));
      method.addInsn(new NInsn ("new", q.hclass()));
      method.addInsn(new NInsn ("astore", putIndex (q.dst(), indexTable)));
    } catch (Exception e){
      e.printStackTrace();
      }
  }

  //**NOP  
  public void visit (NOP q) {
    if (writeTypes){
      method.addInsn (new NLabel ("NOP" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting NOP");
    }
    try {
      method.addInsn(new NInsn ("nop"));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
      

  //**OPER**
  public void visit (OPER q) {
    if (writeTypes){
      method.addInsn (new NLabel ("OPER" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting OPER");
    }
    OperVisitor v = new OperVisitor (){
      public void visit_acmpeq (OPER o)  {
	  //method.addInsn (new NInsn ("iconst_0"));
	  //method.addInsn (new NInsn ("istore", o.dst));
	    hackOper (method, "", "if_acmpeq", o, indexTable, labelTable, A);
      }
      public void visit_d2f(OPER o)  {addOper (method, "d2f", o, indexTable, D, F);} 
      public void visit_d2i(OPER o)  {addOper (method, "d2i", o, indexTable, D, I);} 
      public void visit_d2l(OPER o)  {addOper (method, "d2l", o, indexTable, D, L);} 
      public void visit_dadd(OPER o)  {addOper (method, "dadd", o, indexTable, D, D);} 
      public void visit_dcmpeq(OPER o)  {hackOper (method, "dcmpg", "ifeq", o, indexTable, labelTable, D);} 
      public void visit_dcmpge(OPER o)  {hackOper (method, "dcmpg", "ifge", o, indexTable, labelTable, D);} 
      public void visit_dcmpgt(OPER o)  {hackOper (method, "dcmpg", "ifgt", o, indexTable, labelTable, D);} 
      public void visit_ddiv(OPER o)  {addOper (method, "ddiv", o, indexTable, D, D);} 
      public void visit_default(OPER o)  {
	//what do we want to do for this??
      } 
      public void visit_dmul(OPER o)  {addOper (method, "dmul", o, indexTable, D, D);} 
      public void visit_dneg(OPER o)  {addOper (method, "dneg", o, indexTable, D, D);} 
      public void visit_drem(OPER o)  {addOper (method, "drem", o, indexTable, D, D);} 
      public void visit_dsub(OPER o)  {addOper (method, "dsub", o, indexTable, D, D);} 
      public void visit_f2d(OPER o)  {addOper (method, "f2d", o, indexTable, F, D);} 
      public void visit_f2i(OPER o)  {addOper (method, "f2i", o, indexTable, F, I);} 
      public void visit_f2l(OPER o)  {addOper (method, "f2l", o, indexTable, F, L);} 
      public void visit_fadd(OPER o)  {addOper (method, "fadd", o, indexTable, F, F);} 
      public void visit_fcmpeq(OPER o)  {hackOper (method, "fcmpg", "ifeq", o, indexTable, labelTable, F);} 
      public void visit_fcmpge(OPER o)  {hackOper (method, "fcmpg", "ifge", o, indexTable, labelTable, F);} 
      public void visit_fcmpgt(OPER o)  {hackOper (method, "fcmpg", "ifgt", o, indexTable, labelTable, F);} 
      public void visit_fdiv(OPER o)  {addOper (method, "fdiv", o, indexTable, F, F);} 
      public void visit_fmul(OPER o)  {addOper (method, "fmul", o, indexTable, F, F);} 
      public void visit_fneg(OPER o)  {addOper (method, "fneg", o, indexTable, F, F);} 
      public void visit_frem(OPER o)  {addOper (method, "frem", o, indexTable, F, F);} 
      public void visit_fsub(OPER o)  {addOper (method, "fsub", o, indexTable, F, F);} 
      public void visit_i2b(OPER o)  {}//this is taken care of by array store
      public void visit_i2c(OPER o)  {}//this is also taken care of by array store
      public void visit_i2d(OPER o)  {addOper (method, "i2d", o, indexTable, I, D);} 
      public void visit_i2f(OPER o)  {addOper (method, "i2f", o, indexTable, I, F);} 
      public void visit_i2l(OPER o)  {addOper (method, "i2l", o, indexTable, I, L);} 
      public void visit_i2s(OPER o)  {}//this is taken care of by array load and store
      public void visit_iadd(OPER o)  {addOper (method, "iadd", o, indexTable, I, I);} 
      public void visit_iand(OPER o)  {addOper (method, "iand", o, indexTable, I, I);} 
      public void visit_icmpeq(OPER o)  {hackOper (method, "", "if_icmpeq", o, indexTable, labelTable, I);} 
      public void visit_icmpge(OPER o)  {hackOper (method, "", "if_icmpge", o, indexTable, labelTable, I);} 
      public void visit_icmpgt(OPER o)  {hackOper (method, "", "if_icmpgt", o, indexTable, labelTable, I);} 
      public void visit_idiv(OPER o)  {addOper (method, "idiv", o, indexTable, I, I);} 
      public void visit_imul(OPER o)  {addOper (method, "imul", o, indexTable, I, I);} 
      public void visit_ineg(OPER o)  {addOper (method, "ineg", o, indexTable, I, I);} 
      public void visit_ior(OPER o)  {addOper (method, "ior", o, indexTable, I, I);} 
      public void visit_irem(OPER o)  {addOper (method, "irem", o, indexTable, I, I);} 
      public void visit_ishl(OPER o)  {addOper (method, "ishl", o, indexTable, I, I);} 
      public void visit_ishr(OPER o)  {addOper (method, "ishr", o, indexTable, I, I);} 
      public void visit_isub(OPER o)  {addOper (method, "isub", o, indexTable, I, I);} 
      public void visit_iushr(OPER o)  {addOper (method, "iushr", o, indexTable, I, I);} 
      public void visit_ixor(OPER o)  {addOper (method, "ixor", o, indexTable, I, I);} 
      public void visit_l2d(OPER o)  {addOper (method, "l2d", o, indexTable, L, D);} 
      public void visit_l2f(OPER o)  {addOper (method, "l2f", o, indexTable, L, F);} 
      public void visit_l2i(OPER o)  {addOper (method, "l2i", o, indexTable, L, I);} 
      public void visit_ladd(OPER o)  {addOper (method, "ladd", o, indexTable, L, L);} 
      public void visit_land(OPER o)  {addOper (method, "land", o, indexTable, L, L);} 
      public void visit_lcmpeq(OPER o)  {hackOper (method, "lcmp", "ifeq", o, indexTable, labelTable, L);} 
      public void visit_lcmpge(OPER o)  {hackOper (method, "lcmp", "ifge", o, indexTable, labelTable, L);} 
      public void visit_lcmpgt(OPER o)  {hackOper (method, "lcmp", "ifgt",  o, indexTable, labelTable, L);} 
      public void visit_ldiv(OPER o)  {addOper (method, "ldiv", o, indexTable, L, L);} 
      public void visit_lmul(OPER o)  {addOper (method, "lmul", o, indexTable, L, L);} 
      public void visit_lneg(OPER o)  {addOper (method, "lneg", o, indexTable, L, L);} 
      public void visit_lor(OPER o)  {addOper (method, "lor", o, indexTable, L, L);} 
      public void visit_lrem(OPER o)  {addOper (method, "lrem", o, indexTable, L, L);} 
      public void visit_lshl(OPER o)  {addShift (method, "lshl", o, indexTable);} 
      public void visit_lshr(OPER o)  {addShift (method, "lshr", o, indexTable);} 
      public void visit_lsub(OPER o)  {addOper (method, "lsub", o, indexTable, L, L);} 
      public void visit_lushr(OPER o)  {addShift (method, "lushr", o, indexTable);} 
      public void visit_lxor(OPER o)  {addOper (method, "lxor", o, indexTable, L, L);} 
      public void visit_unknown(OPER o)  {} //do nothing with these for now
    };
    q.visit (v);
  }
  //**RETURN
  public void visit (RETURN q) {
    if (writeTypes){
      method.addInsn (new NLabel ("RETURN" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting RETUR");
    }
    try {
      if (q.retval() == null){
	method.addInsn(new NInsn ("return"));
      } else {
	HClass tempClass = map.typeMap(quadform, q.retval());
	if (tempClass.isPrimitive()){
	  if (tempClass == HClass.Boolean){
	    addiload (method, q.retval(), indexTable);
	    //method.addInsn(new NInsn ("i2b"));
	    method.addInsn(new NInsn ("ireturn"));
	  } else if (tempClass == HClass.Byte){
	    addiload (method, q.retval(), indexTable);
	    //method.addInsn(new NInsn ("i2b"));
	    method.addInsn(new NInsn ("ireturn"));
	  } else if (tempClass == HClass.Char){
	    addiload (method, q.retval(), indexTable);
	    //method.addInsn(new NInsn ("i2c"));
	    method.addInsn(new NInsn ("ireturn"));
	  } else if (tempClass == HClass.Double){
	    adddload (method, q.retval(), indexTable);
	    method.addInsn(new NInsn ("dreturn"));
	  } else if (tempClass == HClass.Float){
	    addfload (method, q.retval(), indexTable);
	    method.addInsn(new NInsn ("freturn"));
	  } else if (tempClass == HClass.Int){
	    addiload (method, q.retval(), indexTable);
	    method.addInsn(new NInsn ("ireturn"));
	  } else if (tempClass == HClass.Long){
	    addlload (method, q.retval(), indexTable);
	    method.addInsn(new NInsn ("lreturn"));
	  } else if (tempClass == HClass.Short){
	    addiload (method, q.retval(), indexTable);
	    //method.addInsn(new NInsn ("i2s"));
	    method.addInsn(new NInsn ("ireturn"));
	  } else {
	      method.addInsn (new NLabel ("; I have no idea what type this is"));
	      method.addInsn (new NInsn ("aconst_null"));
	      method.addInsn (new NInsn ("checkcast", method.myMethod.getReturnType()));
	      method.addInsn(new NInsn ("areturn"));
	  }
	}else {
	  addaload (method, q.retval(), indexTable);
	  method.addInsn (new NInsn ("checkcast", method.myMethod.getReturnType()));
	  method.addInsn(new NInsn ("areturn"));
	}
	
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**SET**
  public void visit (SET q) {
    if (writeTypes){
      method.addInsn (new NLabel ("SET" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting SET");
    }
    try {
      if (!q.field().isStatic()){
	method.addInsn(new NInsn ("aload", putIndex (q.objectref(), indexTable)));
	method.addInsn(new NInsn ("checkcast", q.field().getDeclaringClass()));
      }
      addLoad (method, map, quadform, q.src(), indexTable);
      if (!q.field().getType().isPrimitive()){
	  method.addInsn (new NInsn ("checkcast", q.field().getType()));
      }
      //method.addInsn(new NInsn ("aload", putIndex (q.src, indexTable)));
      if (q.field().isStatic()){
	method.addInsn(new NInsn ("putstatic", q.field()));
      } else {
	method.addInsn(new NInsn ("putfield", q.field()));
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**SWITCH 
  public void visit (SWITCH q) {
    if (writeTypes){
      method.addInsn (new NLabel ("SWITCH" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting SWITCH");
    }
    //Assuming that the switch keys arrive in sorted order, which should
    //be the case unless an optimization pass has fucked with them
    //I should come back and change this to explicitly check that they're sorted
    NLabel targets[] = new NLabel[q.keysLength()];
    int i = 0;
    for (i = 0; i < q.keysLength(); i++){	
      NLabel label = new NLabel ("Label" + labelCount++);
      if (labelTable.put (q.next(i), label) != null){
	  System.out.println ("Switch: Hmm... it seems like there was already a label there");
      }
      targets[i] = label;
    }
    NLabel defaultLabel = new NLabel("Label" + labelCount++);
    if (labelTable.put (q.next(i), defaultLabel) != null){
	  System.out.println ("Switch2: Hmm... it seems like there was already a label there");
      }
	

    try {
      method.addInsn(new NLabel ("; before iload"));
      method.addInsn(new NInsn ("iload", putIndex (q.index(), indexTable)));
      method.addInsn(new NLabel (";after iload, before Lookupswitch"));
      method.addInsn((NInsn) new LookupswitchInsn (defaultLabel, q.keys(), targets));
      method.addInsn(new NLabel (";after switch"));
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**THROW
  public void visit (THROW q) {
    if (writeTypes){
      method.addInsn (new NLabel ("THROW" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting THROW");
    }
    try {
      method.addInsn(new NInsn ("aload", putIndex (q.throwable(), indexTable)));
      method.addInsn(new NInsn ("athrow"));
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  //**PHI
  public void visit (PHI q) {
    if (writeTypes){
      method.addInsn (new NLabel ("PHI" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting PHI");
    }
    //need to get the label for this node (creating one if neccessry)
    //and then add the label into the text
    if (!labelTable.containsKey(q)){
      NLabel label = new NLabel ("Label" + labelCount++);
      labelTable.put (q, label);
      method.addInsn(label);
    }
    phiTable.put (q, q);
  }

  //**SIGMA
  public void visit (SIGMA q) {
    if (writeTypes){
      method.addInsn (new NLabel ("SIGMA" + labelCount++));
    }
    if (printTypes){
      System.out.println ("Visiting SIGMA");
    }
    //there shouldn't be any of these
    //they should all be taken care of by CJMP and SWITCH
  }
    }
