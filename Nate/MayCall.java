// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
package harpoon.Nate;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Util.*;
//import harpoon.Main.*;
import harpoon.Analysis.*;
import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;

import java.util.*;



/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MayCall.java,v 1.1.2.1 1998-11-22 03:32:41 nkushman Exp $
 */

public final class MayCall {
  /** The compiler should be invoked with the names of classes
   *  extending <code>java.lang.Thread</code>.  These classes
   *  define the external interface of the machine. */


  MClassMap MClassMap;

  Hashtable nativeMethodMap;
  Hashtable nativeClassMap;
  Hashtable nativeReturnMap;

  Hashtable MmethodMap;

  Set methodSet;
  Set methodWorklist;

  ODSList odsList;


  public MayCall (MClassMap MClassMap, Hashtable nativeMethodMap, Hashtable nativeClassMap, 
		  Hashtable nativeReturnMap, ODSList odsList){

    this.odsList = odsList;
    this.MClassMap = MClassMap;
    this.nativeMethodMap = nativeMethodMap;
    this.nativeClassMap = nativeClassMap;
    this.nativeReturnMap = nativeReturnMap;

    this.MmethodMap = new Hashtable();

  }

  public void maycall(final HMethod hmethod, HClass hclass){

    methodSet = new Set();
    methodWorklist = new Set();

    System.out.println ("Pushing: " + hclass.getName() + "." + hmethod.getName() + 
			": Before the while loop");
    methodWorklist.push (getMmethod (hmethod, hclass));

    while (! methodWorklist.isEmpty()){
      MMethod newMmethod = (MMethod) methodWorklist.pull();
      
      HMethod newMethod = newMmethod.hmethod;


      if (methodSet.contains (newMmethod)){
	//do nothing I think
	continue;
      }
      methodSet.push (newMmethod);
      // need to find out the calling class somehow

      HClass callingClass = newMmethod.callingClass;


      System.out.println ("Out the other end seeing: " + callingClass.getName() + "." +
			  newMethod.getName());



      //check to see if the class that the method is called on,
      //actually has an instantiation of the method, or if a 
      //superclasses method could be be called.
      
      MClass tempMClass = null;
      if (newMethod.getDeclaringClass() != callingClass){
	//the superclasses method could be called, so we have to make
	//sure the superclass is in the MClassMap
	addClass(newMethod.getDeclaringClass(), false);
	//add the abstract method as well
	MClassMap.get(newMethod.getDeclaringClass()).addAbstractMethod (newMethod);

      }
      //add the calling class of the method to the MClassMap
      addClass (callingClass, false);
	
      //scan the method and look for class instantiations and method calls
      if (callingClass.isInterface()){
	
	Enumeration e = MClassMap.get (callingClass).getImplementingClasses().elements();
	
	  while (e.hasMoreElements()){
	    MClass currentClass = (MClass) e.nextElement();
	    currentClass.addDeclaredMethod(newMethod);
	    //check to see if the class implements the method to see
	    //which list it should be added to
	    try {
	      //this will throw a NoSuchMethodError if the method doesn't exist
	      HMethod implementedMethod = 
	      currentClass.hclass.getDeclaredMethod(newMethod.getName(),
						    newMethod.getParameterTypes());
	      currentClass.addImplementedMethod(implementedMethod);
	    
	      System.out.println ("Pushing: " + currentClass.hclass.getName() + "." + 
				  implementedMethod.getName() + ": cause it was called through an interface");
	      methodWorklist.push(getMmethod (implementedMethod, currentClass.hclass));
	    } catch (NoSuchMethodError nsme){
	      //this just means that the method doesn't exist, and we don't
	      //want to execute the rest of the code.
	    }
	  
	  }
      } else {
	analyze (newMethod);
	
	//find out all the classes that are subclasses of this class, so
	//that we can add this method to them
	//use a iterative Worklist method to do this
	if (newMethod.getName().equals ("<init>")){
	  MClass currentCallingClass = MClassMap.get(callingClass);
	  currentCallingClass.addImplementedMethod(newMethod);
	  currentCallingClass.addDeclaredMethod(newMethod);
	  MClass currentDeclaringClass = MClassMap.get(newMethod.getDeclaringClass());
	  currentDeclaringClass.addImplementedMethod(newMethod);
	  currentDeclaringClass.addDeclaredMethod(newMethod);
	} else {
	  Set addClassSet = new Set();
	  MClass nate = (MClass) MClassMap.get(callingClass);
	  addClassSet.push (nate);
	  while (!addClassSet.isEmpty()){
	    MClass currentClass = (MClass) addClassSet.pull();
	    currentClass.addDeclaredMethod(newMethod);
	    //check to see if the class implements the method to see
	    //which list it should be added to
	    try {
	      //this will throw a NoSuchMethodError if the method doesn't exist
	      HMethod implementedMethod = 
		currentClass.hclass.getDeclaredMethod(newMethod.getName(),
						      newMethod.getParameterTypes());
	      currentClass.addImplementedMethod(implementedMethod);
	      
	      System.out.println ("Pushing: " + currentClass.hclass.getName() + "." + 
				  implementedMethod.getName() + ": cause it's implemented by the subclass");
	      methodWorklist.push(getMmethod (implementedMethod, currentClass.hclass));
	    } catch (NoSuchMethodError e){
	      //this just means that the method doesn't exist, and we don't
	      //want to execute the rest of the code.
	    }
	    
	    //iteratively add the children of this class to the Worklist
	    Enumeration e = currentClass.getChildren().elements();
	    while (e.hasMoreElements()){
	      Object currentObj = e.nextElement();
	      addClassSet.push (currentObj);
	    }
	  }
	}
      }
    }
    Enumeration e = MClassMap.elements();

    System.out.println ("************************************************************");
    System.out.println ("************************************************************");
    System.out.println ("************************************************************");
    while (e.hasMoreElements()){
      MClass currentClass = (MClass) e.nextElement();
      System.out.print ("Class: " + currentClass.hclass.getName());
      if (currentClass.instantiated) {
	System.out.print("  --  new");
      }
      if (nativeClassMap.containsKey (currentClass.hclass)){
	System.out.println ( " -- native");
      } else {
	System.out.println ("");
      }
      Enumeration c = currentClass.getImplementedMethods().elements();
      MClass joey = currentClass;
      while (c.hasMoreElements()){
	HMethod currentMethod = (HMethod) c.nextElement();
	if (nativeMethodMap.containsKey (currentMethod)){
	  System.out.println ("  Native Method: " + currentMethod.getName() + ":" + currentMethod.getDescriptor());
	} else if (currentClass.getAbstractMethods().contains (currentMethod)){
	  System.out.println ("  Abstract Method: " + currentMethod.getName() + ":" + currentMethod.getDescriptor());
	} else {
	  System.out.println ("         Method: " + currentMethod.getName() + ":" +  currentMethod.getDescriptor());
	}
      }
      c = currentClass.getImplementedFields().elements();
      while (c.hasMoreElements()){
	HField currentMethod = (HField) c.nextElement();
	System.out.println ("          Field: " + currentMethod.getName());
      }
    }
    System.out.println ("************************************************************");
    System.out.println ("************************************************************");
    System.out.println ("************************************************************");
  }
  
  /*Add newMClass to MClassMap, and updates everything in MClassMap accordingly*/
  
  private void addClass (HClass newHClass, boolean instantiated){
    MClass oldMClass = (MClass) MClassMap.get (newHClass);
    if (oldMClass != null){
      if (instantiated){
	oldMClass.instantiated = true;
      }
      //do nothing I think
      return;
    }
    //actually add the class to the set

    MClass newMClass = new MClass (newHClass, instantiated);
    MClassMap.put (newHClass, newMClass);


    if (newHClass.isInterface()){
      Enumeration e = MClassMap.elements();
      while (e.hasMoreElements()){
	MClass currentClass = (MClass) e.nextElement();
	HClass interfaces[] = currentClass.hclass.getInterfaces();
	for (int i = 0; i < interfaces.length; i++){
	  if (interfaces[i].getName().equals (newHClass.getName())){
	    newMClass.addImplementingClass(currentClass);
	  }
	}
      }
      return;
    }
    
    //don't do it for java.lang.Character cause the <clinit> method is so long that we run out of memory
    if (!newHClass.getName().equals ("java.lang.Character")){
      try {
	HMethod theMethod = newHClass.getDeclaredMethod ("<clinit>", new HClass[0]);
	methodWorklist.push (getMmethod (theMethod, newHClass));
      } catch (NoSuchMethodError nsme){
	//just want to continue in the code if that method doesn't exist
      }
    }
    //find the closest superclass of newMClass which is contained in MClassMap
    HClass superclass= newMClass.hclass;
    while ((superclass != null) && 
	   !MClassMap.containsKey(superclass)){
	superclass = superclass.getSuperclass();
    }
    
    if (superclass == null){
      // this means that no superclasses are already in the
      //set so do nothing
    } else {
      MClass Nsuperclass = (MClass) MClassMap.get (superclass);
      //add all methods of the superclass to the 
      //current class
      Enumeration e = Nsuperclass.getDeclaredMethods().elements(); 
      while (e.hasMoreElements()){
	HMethod nextMethod = (HMethod) e.nextElement();
	
	newMClass.addDeclaredMethod (nextMethod);
	
	
	
	try {
	  //this will throw a NoSuchMethodError if the method doesn't exist
	  HMethod implementedMethod = 
	    newMClass.hclass.getDeclaredMethod(nextMethod.getName(),
					       nextMethod.getParameterTypes());
	  newMClass.addImplementedMethod(nextMethod);
	  
	  System.out.println ("Pushing: " + newMClass.hclass.getName() + "." + 
			      implementedMethod.getName() + ": because my superclass implements it");
	  
	  methodWorklist.push(getMmethod (implementedMethod, newMClass.hclass));
	} catch (NoSuchMethodError nsme){
	    //this just means that the method doesn't exist, and we don't
	  //want to execute the rest of the code.
	}
	
	
	}
      
      //make sure no children of the superclass are not actually
      //supposed to be children of the current class now.
      Enumeration children = Nsuperclass.getChildren().elements();
      while (children.hasMoreElements()){
	MClass child = (MClass) children.nextElement();
	if (is_child (child.hclass, Nsuperclass.hclass, newMClass.hclass)){
	  Nsuperclass.removeChild (child);
	  newMClass.addChild (child);
	}
      }
    }
  }
  
  void addField (HField hfield, HClass callingClass){
    HClass declaringClass = hfield.getDeclaringClass();
    addClass (declaringClass, false);
    //add the abstract method as well
    MClassMap.get(declaringClass).addImplementedField (hfield);
    
    
    Set addClassSet = new Set();
    MClass nate = (MClass) MClassMap.get(declaringClass);
    addClassSet.push (nate);
    while (!addClassSet.isEmpty()){
      MClass currentClass = (MClass) addClassSet.pull();
      currentClass.addDeclaredField(hfield);
      //iteratively add the children of this class to the Worklist
      Enumeration e = currentClass.getChildren().elements();
      while (e.hasMoreElements()){
	Object currentObj = e.nextElement();
	addClassSet.push (currentObj);
      }
    }
  }
  
  static boolean isSuperclass (HClass subClass, HClass superClass){
    while (subClass != null){
      if (subClass.getName().equals (superClass.getName())){
	return true;
      }
      subClass = subClass.getSuperclass();
    }
    return false;
  }

  
  void analyze (final HMethod method){

    System.out.println ("Looking at the code for: " + method.getDeclaringClass().getName() + "." + method.getName());

    
    final TypeInfo typeMap = new TypeInfo();
    final HCode hc = method.getCode("quad-ssa");
    
    //because the anonymous class doesn't have a pointer to the maycall

    if (java.lang.reflect.Modifier.isNative (method.getModifiers())){
      nativeMethodMap.put(method, method);
      HClass params[] = method.getParameterTypes();
      for (int i = 0; i < params.length; i++){
	nativeClassMap.put (params[i], params[i]);
      }
      nativeReturnMap.put (method.getReturnType(), method.getReturnType());
      nativeClassMap.put (method.getDeclaringClass(), method.getDeclaringClass());
    }

    QuadVisitor insideVisitor = new QuadVisitor () {
      public void visit (Quad q) {}
      
      //**ANEW**
      public void visit (ANEW q){
	((MClass)MClassMap.get (method.getDeclaringClass())).addAccessedClass (q.hclass);
      }
      

      //**GET**
      public void visit (GET q){
	((MClass)MClassMap.get (method.getDeclaringClass())).addAccessedClass (q.field.getDeclaringClass());

	HClass callType = null;
	if (q.objectref != null){
	  callType = typeMap.typeMap (hc, q.objectref);
	}
	//XXX Hack to get this work.... basically means that TypeInfo 
	//isn't working, and I have no idea why..
	if (callType == null || isSuperclass (q.field.getDeclaringClass(), callType)){
	  callType = q.field.getDeclaringClass();
	}
	addField (q.field, callType);
      }

      //**SET**
      public void visit (SET q){
	((MClass)MClassMap.get (method.getDeclaringClass())).addAccessedClass (q.field.getDeclaringClass());
	HClass callType = null;
	if (q.objectref != null){
	  callType = typeMap.typeMap (hc, q.objectref);
	}
	//XXX Hack to get this work.... basically means that TypeInfo 
	//isn't working, and I have no idea why..
	if (callType == null || isSuperclass (q.field.getDeclaringClass(), callType)){
	  callType = q.field.getDeclaringClass();
	}
	addField (q.field, callType);
      }
      
      //**CALL**
      public void visit (CALL q) {
	((MClass)MClassMap.get (method.getDeclaringClass())).addAccessedClass (q.method.getDeclaringClass());
	/*if (classMap.contains (q.method.getDeclaringClass())){
	  HClass oldParams[] = q.method.getParameterTypes();
	  HClass newParams[] = new HClass[oldParams.length];
	  for (int i = 0; i < newParams.length; i++){
	  newParams[i] = getClassSynHierarchy (oldParams[i]);
	  }
	  q.method = ((HClass)classMap.get (q.method.getDeclaringClass())).
	  getDeclaredMethod (q.method.getName(), newParams);
	  }*/
	
	HClass callType = null;
	if (q.objectref != null){
	  callType = typeMap.typeMap (hc, q.objectref);
	}
	//XXX Hack to get this work.... basically means that TypeInfo 
	//isn't working, and I have no idea why..
	
	if (callType == null || callType == HClass.Void || isSuperclass (q.method.getDeclaringClass(), callType)){
	  System.out.println ("The calltype is null");
	  callType = q.method.getDeclaringClass();
	} else {
	  System.out.println ("The calltype is not null");
	}
	
	System.out.println ("Seeing a call to method: " + callType.getName() + "." + q.method.getName());
	//check to see if this is our subclass of java/lang/Throwable (there should be only one, and all
	//others should subclass this one.
	//XXX I'm goind to do a hack here becuase I can't think of a clean way to get this to work...

	System.out.println ("Pushing: " + callType.getName() + "." + 
			    q.method.getName() + ": Cause I actually see it in the code");
	HMethod theCalledMethod = null;
	if (q.method.getName().equals ("<init>")){
	  theCalledMethod = q.method;
	} else {
	  theCalledMethod = callType.getMethod (q.method.getName(), q.method.getParameterTypes());
	}
	methodWorklist.push (getMmethod (theCalledMethod, callType));
	//System.out.println ("I found a call: " + q);
      }
      
      //**NEW**
      public void visit (NEW q) {
	((MClass)MClassMap.get (method.getDeclaringClass())).addAccessedClass (q.hclass);
	addClass (q.hclass, true);
      }
    };
    //System.out.println ("Getting code for: " + method.getDeclaringClass().getName() + "." + method.getName());
    //System.out.println ("After getting Code for: "+method.getDeclaringClass().getName() + "." + method.getName()); 
    System.out.println ("Looking at the code for: " + method.getDeclaringClass().getName() + "." + method.getName());
    if (hc != null){
      Enumeration elements = hc.getElementsE();
      while (elements.hasMoreElements()){
	Quad currentQuad = (Quad) elements.nextElement();
	currentQuad.visit(insideVisitor);
      }
    }
  }
  
  MMethod getMmethod (HMethod hmethod, HClass callingClass){
    MMethod newMmethod = (MMethod) MmethodMap.get (hmethod);
    if (newMmethod == null){
      newMmethod = new MMethod (hmethod, callingClass, this);
      MmethodMap.put (hmethod, newMmethod);
    }
    return newMmethod;
  }
  
  boolean is_child (HClass childClass, HClass parentClass, HClass newClass){
    HClass superclass = childClass.getSuperclass();
    
    while (superclass != parentClass){
      if (superclass  == newClass){
	return true;
      }
    }
    return false;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
