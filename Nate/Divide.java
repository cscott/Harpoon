package harpoon.Nate;

import harpoon.IR.QuadSSA.*;
import harpoon.IR.QuadNoSSA.ByteCodeClass;
import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.IR.Bytecode.*;
import harpoon.Util.*;


import java.util.*;
import java.io.*;

class Divide {
  
  //XXX FIXME FIX ME XXX
  // INTERFACES WILL NOT WORK RIGHT NOW!!!
  // STATIC INITIALIZERS WON'T WORK EITHER...

  static Hashtable nativeMethodMap;
  static Hashtable nativeClassMap;
  static Hashtable nativeReturnMap;

  static MClassMap MClassMap;  //maps HClasses to MClasses
  static ClassMap HclassMap; // maps hclasses to Hclasssyns

  static MethodMap methodMap; // maps hmethods to hmethodsyns
  static Hashtable fieldMap;

  static ClassMap interfaceMap; //maps hclasssyns to interfaces
  static Hashtable getFieldInterfaceMap; //maps hfields to interface methods
  static Hashtable setFieldInterfaceMap; //maps hfields to interface methods
  static MethodMap newMethodMap;  //maps init methods to the corresponding "new_" interface method

  static ODSList odsList;
  static HCodeElement hc;


  static void divide(Hashtable newMap, HMethod method, HClass hclass, String filename){
    //newMap maps calls to new to machineIDS
    Hashtable machineMap = new Hashtable(); //maps machineIDs to the int's used to 
    //reference the machines in the "machines" array of the ODS
    Hashtable resultMap = new Hashtable(); //the map we produce Maps new Quads to the
    //ints used in the machines array of the ODS.
    Hashtable sourceMap = new Hashtable();
    //sourceMap maps strings of the form "sourcefile:linenumber" to indices in the 
    int machineCount = 0;
    Enumeration e = null;

    harpoon.IR.QuadNoSSA.Code.touch ();
    harpoon.IR.QuadNoSSA.Code.write (method.getDeclaringClass());
    
    BufferedReader input = null;
    try {
      input = new BufferedReader (new FileReader (filename));
    } catch (IOException exception){
      exception.printStackTrace();
    }
    String currentLine =  null;
    try {
      currentLine = input.readLine();
    } catch (IOException exception){
      exception.printStackTrace();
    }

    Integer mainIndex = new Integer (currentLine);
    int max = 0;


    try {
      currentLine = input.readLine();
    } catch (IOException exception){
      exception.printStackTrace();
    }

    while (currentLine != null){
      if (currentLine.equals ("")){
	continue;
      }
      int split = currentLine.lastIndexOf (':');
      String fileNameandLine = currentLine.substring (0, split);
      Integer machineNumber = new Integer (currentLine.substring (split+1));
      sourceMap.put (fileNameandLine, machineNumber);

      System.out.println ("*********************************");
      System.out.println ("*Adding: " + fileNameandLine + " to the code********************************");
      System.out.println ("*********************************");

      if (machineNumber.intValue() > max){
	max = machineNumber.intValue();
      }

      try {
	currentLine = input.readLine();
      } catch (IOException exception){
	exception.printStackTrace();
      }
    }
    try {
      input.close();
    } catch (java.io.IOException exception){
      //just continue, and hope that the file is fine..
      throw new Error("Could not close the input file");
    }
    //now I have a map from Quads to ints, and I know the number of machines
    /*e = newMap.keys();
      while (e.hasMoreElements()){
      Quad currentQuad = (Quad) e.nextElement();
      Integer currentID = (Integer) newMap.get (currentQuad);
      Integer currentIndex = (Integer) machineMap.get (currentID);
      if (currentIndex == null){
      machineMap.put (currentID, new Integer(machineCount));
      resultMap.put (currentQuad, new Integer(machineCount));
      machineCount++;
      } else {
      resultMap.put (currentQuad, currentIndex);
      }
      }*/


    odsList = new ODSList (max+1, resultMap);


    NateVisitor dv = new NateVisitor (odsList, sourceMap);

    //create the methodMap out here so that we can pass it to the write proc.

    nativeMethodMap = new Hashtable();
    nativeClassMap = new Hashtable();
    nativeReturnMap = new Hashtable();
    MClassMap = new MClassMap();

    //do all the real work
    MayCall mayCall = new MayCall (MClassMap, nativeMethodMap, nativeClassMap, nativeReturnMap, odsList);
    mayCall.maycall(method, hclass);

    //go through and create chains of hclasses
    //create a hashtable of header nodes... nodes for which we are not going to create an HClassSyn for any of their superclasses
    //if (true){
    //return;
    //}
    HclassMap = odsList.HclassMap;
    interfaceMap = odsList.interfaceMap;
    getFieldInterfaceMap = odsList.getFieldInterfaceMap;
    setFieldInterfaceMap = odsList.setFieldInterfaceMap;
    methodMap = odsList.methodMap;
    newMethodMap = odsList.newMethodMap;
    fieldMap = odsList.fieldMap;

    hc = (HCodeElement) new InMerge ("natesNewODSStuff", 666, 0);
    

    boolean changed = true;
    while (changed){
      changed = false;
      e = MClassMap.elements();
      while (e.hasMoreElements()){
	//need to test if the class has any methods which have parameters that we are making hclassSyns of....
	
	MClass currentElement = (MClass) e.nextElement();
	//if it's throwable, then we don't want to make a remote version of it
	if (currentElement.hclass.getName().equals("java.lang.Throwable") ||
	    currentElement.hclass.getName().equals("java.lang.String")){
	  System.out.println ("Removing: " + currentElement.hclass.getName());
	  MClassMap.remove (currentElement.hclass);
	  changed = true;
	  continue;
	}
	
	if (currentElement.instantiated){
	  if (isThrowable (currentElement.hclass)){
	    currentElement.instantiated = false;
	  } else {
	    //if we are creating a remote version of it, then we need to create a clone of it
	    continue;
	  }
	}
	Enumeration methodsEnumeration = currentElement.getImplementedMethods().elements();
	boolean remove = true;
	while (methodsEnumeration.hasMoreElements()){
	  HMethod currentMethod = (HMethod) methodsEnumeration.nextElement();
	  HClass parameters[] = currentMethod.getParameterTypes();
	  for (int i = 0; i < parameters.length; i++){
	    MClass paramClass = MClassMap.get (parameters[i]);
	    if (paramClass != null && paramClass.instantiated &&
		paramClass != currentElement){
	      remove = false;
	    }
	  }
	}
	Enumeration classEnumeration = currentElement.getAccessedClasses().elements();
	while (classEnumeration.hasMoreElements()){
	  HClass currentClass = (HClass) classEnumeration.nextElement();
	  if (MClassMap.containsKey (currentClass) && 
	      currentClass != currentElement.hclass){
	    remove = false;
	  }
	}
	if (remove) {
	  System.out.println ("Removing: " + currentElement.hclass.getName());
	  MClassMap.remove (currentElement.hclass);
	  changed = true;
	}
      }
    }
    
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    e = MClassMap.elements();
    while (e.hasMoreElements()){
      MClass currentElement = (MClass) e.nextElement();
      System.out.print ("Class: " + currentElement.hclass.getName());
      if (currentElement.instantiated){
	System.out.println ("  --  instantiated");
      } else {
	System.out.println ("");
      }
    }    
    e = MClassMap.elements();
    while (e.hasMoreElements()){
      MClass currentElement = (MClass) e.nextElement();
      MClass superClass = new MClass (currentElement.hclass.getSuperclass(), false);
      Vector superClasses = new Vector(0,0);
      System.out.println ("!!!Doing stuff for the Class: " + currentElement.hclass.getName());
      while (superClass.hclass != null){
	superClasses.addElement (superClass);
	if (MClassMap.containsKey (superClass.hclass)){
	  superClasses.setElementAt ((MClass) MClassMap.get (superClass.hclass), superClasses.size()-1);
	  //go through each class in the superclasses and create an HClassSyn for it
	  //and point them all to the correct superclass
	  //start with the the highest superclass in the hierarcy
	  for (int i = superClasses.size() - 2; i >= 0; i--){
	    System.out.println ("!!!Adding the class: " + ((MClass) superClasses.elementAt(i)).hclass.getName());
	    MClassMap.put (((MClass)superClasses.elementAt(i)).hclass, (MClass)superClasses.elementAt(i));
	  }
	  break;
	}
	superClass = new MClass (superClass.hclass.getSuperclass(), false);
      }
    }

    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    System.out.println ("**********<<<<<<<<<<<<<<>>>>>>>>>>>>>***********");
    e = MClassMap.elements();
    while (e.hasMoreElements()){
      MClass currentElement = (MClass) e.nextElement();
      System.out.print ("Class: " + currentElement.hclass.getName());
      if (currentElement.instantiated){
	System.out.println ("  --  instantiated");
      } else {
	System.out.println ("");
      }
    }    


    e = MClassMap.elements();
    while (e.hasMoreElements()){
      getHClassSyn (((MClass) e.nextElement()).hclass);
    }


    e = HclassMap.getElements();
    while (e.hasMoreElements()){
      analyzeClass ((HClassSyn) e.nextElement(), dv);
    }
    
    e = nativeReturnMap.elements();
    while (e.hasMoreElements()){
      HClass currentClass = (HClass) e.nextElement();
      //in this case, I need to clone a class with the same name that is serializable
      //HClassSyn newClass = new HClassSyn (currentClass, false);
      //newClass.addInterface (HClass.forName ("java.io.Serializable"));
      //odsList.HclassMap.put (hclass, newClass);
    }
    
    
    HMethodSyn newMainMethod = (HMethodSyn) odsList.methodMap.get (method);
    harpoon.Util.Util.assert (newMainMethod != null);

    addInitCall (newMainMethod, odsList, mainIndex);


    //check that all classes that are in the nativeClassMap are also in the parent classMap.... if they are not, then
    //throw an exception stating that JNI is fucking us something hard....
    
    //Create a HClassSyn for all classes I call procs on for which any of the procs called have parameters

    //Create a map that maps HClasses to HClassSyn's

    //Also change the parameters of all the proc calls according the HClass Map... this can be done as a recursive proc.

    //Create a remote interface for each of the HClasses that we instantiate and make the HClassSyn instantiate it

    //Create a map for interfaces as well

    //Create a single HMethod map to map methods, and a single map to map classes

    //the old classMap will still have to be used for calls to new though.

    //*****

    //go through the code and change all references to the old procs and old classes to the new references...
    //this should be all the same stuff as the old maycall...


    //add the call to initialize the ODS before the main procedure is executed
    //addInitCall (transformedMain, odsList, mainIndex);
    //need to fix up the classfiles so they all have the right superclasse
    //right now they all point to the old superclasses, and we need to make them
    // point to the new superclasses

    //write out all the new classfiles
    e = odsList.HclassMap.getElements();
    while (e.hasMoreElements()){
      HClassSyn currentClass = (HClassSyn) e.nextElement();
      System.out.println ("Writing: " + currentClass.getName());
      harpoon.IR.QuadNoSSA.Code.write (currentClass);
      System.out.println ("After Writing");
    }
    //write out all the remote interfaces
    e = odsList.interfaceMap.getElements();
    while (e.hasMoreElements()){
      HClassSyn currentClass = (HClassSyn) e.nextElement();
      harpoon.IR.QuadNoSSA.Code.write (currentClass);
    }

    //write out all the ODS's
    System.out.println ("*******Right before printing the ODS's******");
    for (int i = 0; i < odsList.list.length; i++){
      System.out.println ("*******Actually printing an ODS******");
      harpoon.IR.QuadNoSSA.Code.write (odsList.list[i]);
      System.out.println ("*******Immeadiately after printing an ODS******");
    }
    System.out.println ("*******Right after printing the ODS's******");

    //write out the actuall ods
    harpoon.IR.QuadNoSSA.Code.write (odsList.ods);

   
    //write out the ODS remote interface
    if (odsList.remoteInterface == null){
      System.out.println ("Well I guess it's null here");
    } else {
      System.out.println ("It certainly ain't null here");
    }
    harpoon.IR.QuadNoSSA.Code.write (odsList.remoteInterface);
     
    //rmic all the new classfiles 
    //need to do this as a serate pass, because we need both the classes and 
    //interfaces to be written out before we can do it
    e = odsList.HclassMap.getElements();
    while (e.hasMoreElements()){
      HClassSyn currentClass = (HClassSyn) e.nextElement();
      try {
	String runString = "";
	runString = "rmic " + currentClass.getName();
	System.out.println ("@$#%@@%$%$%@@$%@$@@$##%#@$#$Running: " + runString);
	Runtime.getRuntime().exec (runString).waitFor();
      } catch (Exception exception){
	exception.printStackTrace();
      }
    }

    for (int i = 0; i < odsList.list.length; i++){
      HClassSyn currentClass = odsList.list[i];
      try {
	String runString = "";
	runString = "rmic " + currentClass.getName();
	System.out.println ("@$#%@@%$%$%@@$%@$@@$##%#@$#$Running: " + runString);
	Runtime.getRuntime().exec (runString).waitFor();
      } catch (Exception exception){
	exception.printStackTrace();
      }
    }      
				 
    // need to write out all the ODS's 
    // need to write out the ODS remote interface
    // need to write out the new interfaces
    // need to write out the new Class files
    // need to rmic all the new class files
				 
  }
  
  static boolean isThrowable (HClass hclass){
    String origName = hclass.getName();
    while (hclass != null){
      if (hclass.getName().equals ("java.lang.Throwable")){
	return true;
      }
      hclass = hclass.getSuperclass();
    }
    return false;
  }

  static void fixNativeMethods (HClassSyn hclass){
    HMethod methods[] = hclass.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      HMethod currentMethod = methods[i];
      if (java.lang.reflect.Modifier.isNative(currentMethod.getModifiers())){
	HCode theCode = currentMethod.getCode("quad-ssa");

	HMethod superClassMethod = hclass.getSuperclass().getMethod (currentMethod.getName(), currentMethod.getParameterTypes());
	Quad quads[] = new Quad[3];
	FOOTER footer = new FOOTER (hc);
	//the only param is the implicit param "this"
	Temp params[] = null;
	Temp callParams[] = null;
	if (currentMethod.isStatic()){
	  params = new Temp[currentMethod.getParameterTypes().length];
	  callParams = params;
	} else {
	  params = new Temp[currentMethod.getParameterTypes().length+1];
	  callParams = new Temp[params.length-1];
	  for (int j = 0; j < callParams.length; j++){
	    callParams[i] = params[i+1];
	  }
	}
	for (int j = 0; j < params.length; j++){
	  params[j] = new Temp();
	}
	
	

	quads[0] = new METHODHEADER(hc, footer, params);
	Temp retval = null;
	if (currentMethod.getReturnType() != HClass.Void){
	  retval = new Temp();
	}
	Temp objectref = null;
	if (!currentMethod.isStatic()){
	  objectref = params[0];
	}
	Temp retex = new Temp();
	quads[1] = new CALL(hc, superClassMethod, objectref, callParams, retval, retex, false);
	quads[2] = new RETURN(hc, retval);
	Quad.addEdges(quads);
	footer.attach (quads[2], 0);
	currentMethod.putCode (new harpoon.IR.QuadSSA.Code (currentMethod, quads[0]));
	theCode = currentMethod.getCode("quad-ssa");
      }
    }
  }


  static void addInitCall (HMethodSyn method, ODSList odsList, Integer mainIndex){

    //define to use as a replacement ODS
    HCodeElement hc = (HCodeElement) new InMerge ("natesNewODSStuff", 666, 0);

    Quad methodHeader = (Quad)method.getCode("quad-ssa").getRootElement();
    
    Quad quads[] = new Quad[5];
    
    String lameStringHack[] = new String[0];
    HClass odsCallParmTypes[] = {HClass.forClass(lameStringHack.getClass())};
    

    Temp temp4 = new Temp();
    quads[0] = new CONST (hc, temp4, null, HClass.Void);
    Temp odsCallParams[] = {temp4};
    Temp retex = new Temp();
    quads[1] = new CALL (hc, odsList.list[mainIndex.intValue()].getDeclaredMethod ("main", odsCallParmTypes),
			   null, odsCallParams, null, retex, false);
    Temp temp5 = new Temp();
    Temp operands1[] = {temp4, retex};
    quads[2] = new OPER (hc, Qop.ACMPEQ, temp5, operands1);
    Temp retexa = new Temp();
    Temp retexb = new Temp();    
    Temp dst[][] = {{retexa, retexb}};
    Temp src[] = {retex};
    quads[3] = new CJMP (hc, temp5, dst, src);
    quads[4] = new THROW (hc, retexa);
    Quad.addEdges (quads);
    getFooter (methodHeader, new Hashtable()).attach (quads[4], 0);

    Quad.addEdge (quads[3], 1, methodHeader.next(0), methodHeader.nextEdge(0).which_pred());
    Quad.addEdge (methodHeader, 0, quads[0], 0);

  }



  static void analyzeClass (HClassSyn hclass, QuadVisitor visitor){
    System.out.println ("Analyzing Class: " + hclass.getName());

    HMethod methods[] =  hclass.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      HMethodSyn currentMethod = (HMethodSyn) methods[i];
      System.out.println ("     Method: " + currentMethod.toString());
      HCode hc = currentMethod.getCode("quad-ssa");
      if (hc != null){

	if (!(hclass.getSuperclass() instanceof HClassSyn) &&
	    currentMethod.getName().equals ("<init>") &&
	    hclass.getName().startsWith(hclass.getSuperclass().getName())){//XXX hack.... relying on the current naming convenction..


	  METHODHEADER theHeader = (METHODHEADER) hc.getRootElement();
	  Temp params[] = theHeader.params;
	  Temp callParams[] = new Temp [params.length - 1];
	  for (int j = 0; j < callParams.length; j++){
	    callParams[j] = params[j+1];
	  }
	  HCodeElement hce = theHeader.getSourceElement();
	  FOOTER theFooter = new FOOTER(theHeader.footer.getSourceElement());
	  theHeader.footer = theFooter;
	  HMethod theMethod = hclass.getSuperclass().getDeclaredMethod("<init>",currentMethod.getParameterTypes());
	  
	  
	  Temp retex = new Temp();
	  CALL theCall = new CALL (hce, theMethod, params[0], callParams, 
				   null, retex, true);
	  Temp temp8 = new Temp();
	  Quad quad1 = new CONST (hce, temp8, null, HClass.Void);
	  Temp temp9 = new Temp();
	  Temp operands2[] = {temp8, retex};
	  Quad quad2 = new OPER (hce, Qop.ACMPEQ, temp9, operands2);
	  Temp retexa = new Temp();
	  Temp retexb = new Temp();    
	  Temp dst[][] = {{retexa, retexb}};
	  Temp src[] = {retex};
	  Quad quad3 = new CJMP (hce, temp9, dst, src);
	  Quad quad4 = new THROW (hce, retexa);
	  Quad quad5 = new RETURN (hce);
	  Quad.addEdge (theHeader, 0, theCall, 0);
	  Quad.addEdge (theCall, 0, quad1, 0);
	  Quad.addEdge (quad1, 0, quad2, 0);
	  Quad.addEdge (quad2, 0, quad3, 0);
	  Quad.addEdge (quad3, 0, quad4, 0);
	  Quad.addEdge (quad3, 1, quad5, 0);
	  theFooter.attach (quad4, 0);
	  theFooter.attach (quad5, 0);
	} else if (currentMethod.getName().startsWith ("set_") || 
		   currentMethod.getName().startsWith ("get_") ){
	  //do nothing: we don't need reevaluate the methods we just created
	  } else{
	  Enumeration elements = hc.getElementsE();
	  while (elements.hasMoreElements()){
	    Quad currentQuad = (Quad) elements.nextElement();
	    currentQuad.visit (visitor);
	  }
	}
      }
    }
  }




  static HClass getClassPointer (HClass hclass){
    MClass MClass = MClassMap.get (hclass);
    if (MClass == null){
      //if we aren't cloning the class, then just return the original
      return hclass;
    }
    HClassSyn newHClass = (HClassSyn) getHClassSyn (hclass);
    if (interfaceMap.contains (newHClass)){
      newHClass = (HClassSyn) interfaceMap.get (newHClass);
    }
    return newHClass;
  }

  static void getFieldCall (HField oldField, HClassSyn newClass, HClassSyn newInterface){
    // I need to add the new accessorMethod, and add that method to the map
    //System.out.println ("$$$Callikng it from getFieldCall");
    HField newField = (HField) odsList.fieldMap.get (oldField);
    
    //in case we are just allowing the fields to pass through...
    if (newField == null){
      newField = oldField;
    }
    HMethodSyn newMethod = new HMethodSyn (newClass,"get_"+ newField.getName(), new HClass[0], newField.getType());
    HMethodSyn newMethodInterface = new HMethodSyn (newInterface, "get_"+ newField.getName(), new HClass[0], newField.getType());
    HClass newExceptions[] = {HClass.forName ("java.rmi.RemoteException")};
    newMethodInterface.setExceptionTypes (newExceptions);
    newMethod.setModifiers(newField.getModifiers() | java.lang.reflect.Modifier.PUBLIC);
    newMethodInterface.setModifiers(newField.getModifiers() | java.lang.reflect.Modifier.PUBLIC);
    Quad quads[] = new Quad[3];
    FOOTER footer = new FOOTER (hc);
    //the only param is the implicit param "this"
    Temp params[] = null;
    if (newField.isStatic()){
      params = new Temp[0];
    } else {
      params = new Temp[1];
      params[0] = new Temp();
    }
    quads[0] = new METHODHEADER(hc, footer, params);
    Temp retval = new Temp();
    if (newField.isStatic()){
      quads[1] = new GET(hc, retval, newField);
    } else {
      quads[1] = new GET(hc, retval, newField, params[0]);
    }
    quads[2] = new RETURN(hc, retval);
    Quad.addEdges(quads);
    footer.attach (quads[2], 0);
    newMethod.putCode (new harpoon.IR.QuadSSA.Code (newMethod, quads[0]));
    
    //not neccesary to explicitly add them because they should be added by their constructors...
    //newClass.addDeclaredMethod(newMethod);
    //newInterface.addDeclaredMethod(newMethodInterface);
    getFieldInterfaceMap.put (oldField, newMethodInterface);
  }
  


  static void setFieldCall (HField oldField, HClassSyn newClass, HClassSyn newInterface){
    // I need to add the new accessorMethod, and add that method to the map
    //System.out.println ("$$$Callikng it from getFieldCall");
    HField newField = (HField) odsList.fieldMap.get (oldField);
    
    if (newField == null){
      newField = oldField;
    }

    HClass newParams[] = {newField.getType()};
    
    HMethodSyn newMethod = new HMethodSyn (newClass,"set_"+ newField.getName(), newParams, HClass.Void);
    HMethodSyn newMethodInterface = new HMethodSyn (newInterface, "set_"+ newField.getName(), newParams, HClass.Void);
    HClass newExceptions[] = {HClass.forName ("java.rmi.RemoteException")};
    newMethodInterface.setExceptionTypes (newExceptions);
    newMethod.setModifiers(newField.getModifiers() | java.lang.reflect.Modifier.PUBLIC);
    newMethodInterface.setModifiers(newField.getModifiers() | java.lang.reflect.Modifier.PUBLIC);
    Quad quads[] = new Quad[3];
    FOOTER footer = new FOOTER (hc);
    //the only param is the implicit param "this", and the value to set the field to;
    Temp params[] = null;
    if (newField.isStatic()){
      params = new Temp[1];
      params[0] = new Temp();
    } else {
      params = new Temp[2];
      params[0] = new Temp();
      params[1] = new Temp();
    }
    quads[0] = new METHODHEADER(hc, footer, params);
    if (newField.isStatic()){
      quads[1] = new SET(hc, newField, params[0]);
    } else {
      quads[1] = new SET(hc, newField, params[0], params[1]);
    }
    quads[2] = new RETURN(hc);
    Quad.addEdges(quads);
    footer.attach (quads[2], 0);
    newMethod.putCode (new harpoon.IR.QuadSSA.Code (newMethod, quads[0]));
    
    //not neccesary to explicitly add them because they should be added by their constructors...
    //newClass.addDeclaredMethod(newMethod);
    //newInterface.addDeclaredMethod(newMethodInterface);
    setFieldInterfaceMap.put (oldField, newMethodInterface);
  }
   
  static HClass getHClassSyn (HClass hclass){


    MClass MClass = MClassMap.get (hclass);
    if (MClass == null){
      //if we aren't cloning the class, then just return the original
      return hclass;
    }

    HClass oldClass = MClass.hclass;

    //I'm pretty sure that oldClass should never be an HClassSyn, but if it can be
    //then I should probably just return it.
    harpoon.Util.Util.assert (!(oldClass instanceof HClassSyn));

    HClassSyn newClass = (HClassSyn) HclassMap.get (oldClass);
    
    if (newClass != null){
      return newClass;
    }
    
    System.out.println ("********Creating a Syn of: " + oldClass.getName());

    newClass = new HClassSyn (oldClass.getName(), "aintnoSourceFile");

    if (oldClass.getName().equals (newClass.getName())){
      throw new Error ("What the Hell.. the names are the same..");
    }

    //Util.assert (!(oldClass.getName().equals (newClass.getName())));

    HclassMap.put (oldClass, newClass);

    //create the remote Interface early, so that the pointers will work out.

    HClassSyn remoteInterface = null;
    if (MClass.instantiated){
      System.out.println ("***Creating a remote interface too");
      StringBuffer nameBuffer = new StringBuffer (oldClass.getName());
      int index = oldClass.getName().lastIndexOf ('.');
      nameBuffer.insert (index+1, "remoteInterface");
      remoteInterface = new HClassSyn (nameBuffer.toString(), "aintNoSouceFile");
      remoteInterface.setModifiers ((oldClass.getModifiers() & 
				     ~java.lang.reflect.Modifier.PRIVATE)  | 
				    java.lang.reflect.Modifier.INTERFACE |
				    java.lang.reflect.Modifier.PUBLIC);

      interfaceMap.put (newClass, remoteInterface);
    }

    newClass.setModifiers (oldClass.getModifiers());

    //initialize the new HClassSyn
    Enumeration e = null;

    //hack to allow me to clone Object...
    if (nativeClassMap.contains (oldClass) || oldClass.isInterface()){
      e = MClass.getDeclaredMethods().elements();
    } else {
      e = MClass.getImplementedMethods().elements();
    }

    while (e.hasMoreElements()){
      HMethod currentMethod = (HMethod) e.nextElement();

      HMethodSyn newMethod = new HMethodSyn (newClass, currentMethod);
      
      HCode theCode = newMethod.getCode("quad-ssa");
      
      methodMap.put (currentMethod, newMethod);

      HClass oldParams[] = currentMethod.getParameterTypes();
      HClass newParams[] = new HClass[oldParams.length];
      for (int i = 0; i < oldParams.length; i++){
	newParams[i] = getClassPointer (oldParams[i]);
      }
      newMethod.setParameterTypes (newParams);

      newMethod.setReturnType (getClassPointer (currentMethod.getReturnType()));

      HClass oldExceptions[] = currentMethod.getExceptionTypes();
      HClass newExceptions[] = new HClass[oldExceptions.length];
      for (int i = 0; i < oldExceptions.length; i++){
	newExceptions[i] = getClassPointer (oldExceptions[i]);
      }
      newMethod.setExceptionTypes(newExceptions);
      newMethod.setModifiers (currentMethod.getModifiers());

      newMethod.setSynthetic (currentMethod.isSynthetic());
    }


    //do my check to make sure I'm not vioating the native method
    HClass superClass = oldClass.getSuperclass();
    if (superClass != null){
      if (oldClass.getName().equals ("java.lang.System")){
	superClass = new HClassSyn (oldClass, false);
	((HClassSyn)superClass).setModifiers (superClass.getModifiers() & 
				 (~java.lang.reflect.Modifier.FINAL));
	System.out.println ("Right before doing the vile setsuperclass");
	System.out.println ("OldClass name is: " + oldClass.getName());
	System.out.println ("NewClass name is: " + newClass.getName());
	System.out.println ("Superclass name is: " + superClass.getName());
	newClass.setSuperclass (superClass);
      } else {
	superClass = getHClassSyn (superClass);
	if (nativeClassMap.contains (oldClass)){
	  if (superClass instanceof HClassSyn){
	    //throw new Error ("Dude!! You're requiring me to create a clone of the Superclass" + 
	    //" of a class which has Native methods called on it");
	    //the hack above should take care of this problem..
	    newClass.setSuperclass (oldClass);
	  } else {
	    newClass.setSuperclass (oldClass);
	  }
	} else {
	  if (oldClass.getName().equals ("java.lang.Error") || (oldClass.getName().equals("java.lang.Exception"))){
	    newClass.setSuperclass (oldClass);
	  } else {
	    if (!(superClass instanceof HClassSyn) && 
		!java.lang.reflect.Modifier.isFinal(superClass.getModifiers())){
	      newClass.setSuperclass (oldClass);
	    } else {
	      newClass.setSuperclass (superClass);
	    }
	  }
	}
      }
    } else {
      newClass.setSuperclass (oldClass);
    }
    
    
    if (nativeClassMap.contains (oldClass)){
      //I shouldn't have to do this.. I should be able to do it implicitly..
      //fixNativeMethods (newClass);
    }

    
    if (remoteInterface != null){
      newClass.addInterface (remoteInterface);
    }
    HClass oldInterfaces[] = oldClass.getInterfaces();
    for (int i = 0; i < oldInterfaces.length; i++){
      newClass.addInterface (oldInterfaces[i]);
    }
    
    if (superClass instanceof HClassSyn && 
	(!superClass.getName().equals ("java.lang.System"))){
      e = MClass.getImplementedFields().elements();
      while (e.hasMoreElements()){
	HField oldField = (HField) e.nextElement();
	HFieldSyn newField = new HFieldSyn (newClass, oldField );
	newField.setType (getClassPointer (newField.getType()));
	odsList.fieldMap.put (oldField, newField);
      }
    }
      
    if (remoteInterface != null){
      //initialize the remote interface
      Vector declaredMethods = MClass.getDeclaredMethods();
      Vector implementedMethods = MClass.getImplementedMethods();
      
      //do the declared methods first 
      HMethod newDeclaredMethods[] = newClass.getDeclaredMethods();
      for (int j = 0; j < newDeclaredMethods.length; j++){
	HMethod oldMethod = newDeclaredMethods[j];
	if (oldMethod.getName().equals ("<init>")){
	  continue;
	}
	HMethod newMethod = oldMethod;//newClass.getMethod (oldMethod.getName(), oldMethod.getParameterTypes());
	HMethodSyn interfaceMethod = new HMethodSyn (remoteInterface, newMethod);
	HClass oldExceptions[] = interfaceMethod.getExceptionTypes();
	HClass newExceptions[] = new HClass[oldExceptions.length +1];
	for (int k = 0; k < oldExceptions.length; k++){
	  newExceptions[k] = oldExceptions[k];
	}
	newExceptions[oldExceptions.length] = HClass.forName("java.rmi.RemoteException");
	interfaceMethod.setExceptionTypes (newExceptions);
      }


      e = implementedMethods.elements();
      while (e.hasMoreElements()){
	HMethod oldMethod = (HMethod) e.nextElement();
	if (oldMethod.getName().equals ("<init>")){
	  continue;
	}
	if (declaredMethods.contains (oldMethod)){
	  continue;
	}
	try {
	  HMethod newMethod = newClass.getMethod (oldMethod.getName(), oldMethod.getParameterTypes());
	  HMethodSyn interfaceMethod = new HMethodSyn (remoteInterface, newMethod);
	  HClass oldExceptions[] = interfaceMethod.getExceptionTypes();
	  HClass newExceptions[] = new HClass[oldExceptions.length +1];
	  for (int j = 0; j < oldExceptions.length; j++){
	    newExceptions[j] = oldExceptions[j];
	  }
	  newExceptions[oldExceptions.length] = HClass.forName("java.rmi.RemoteException");
	  interfaceMethod.setExceptionTypes (newExceptions);
	} catch (NoSuchMethodError nsme){
	  //this method will be included in a superinterface, so we do nothing
	}
      }
      
      remoteInterface.setSuperclass (HClass.forName("java.lang.Object"));
      HClassSyn superInterface = (HClassSyn) interfaceMap.get (newClass.getSuperclass());
      if (superInterface != null){
	remoteInterface.addInterface (superInterface);
      }
      remoteInterface.addInterface (HClass.forName("java.rmi.Remote"));
      remoteInterface.setModifiers ((newClass.getModifiers() & 
				     ~java.lang.reflect.Modifier.PRIVATE)  | 
				    java.lang.reflect.Modifier.INTERFACE |
				    java.lang.reflect.Modifier.PUBLIC);


      e = MClass.getDeclaredFields().elements();
      while (e.hasMoreElements()){
	HField currentField = (HField) e.nextElement();
	getFieldCall(currentField, newClass, remoteInterface);
	setFieldCall(currentField, newClass, remoteInterface);
      }
      
    }
    
    if (MClass.instantiated){
      //add new method to the ods..
      HMethod methods[] = newClass.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++){
	if (methods[i].getName().equals ("<init>")){
	  newODSMethod (methods[i], newClass, remoteInterface);

	  if (!(newClass.getSuperclass() instanceof HClassSyn)){
	    System.out.println ("(((((((((((((((((())))))))))))))))))))");
	    System.out.println ("I'm decideing that the superclass is an HClassSyn: " + newClass.getName() + ": and writing it out");
	    System.out.println ("(((((((((((((((((())))))))))))))))))))");

	    final HCode hc = methods[i].getCode("quad-ssa");
	    METHODHEADER header = (METHODHEADER) hc.getRootElement();
	    FOOTER footer = header.footer;
	    Quad prev[] = footer.prev();
	    System.out.println ("There are " + prev.length + " prev quads");
	    for (int j = 0; j < prev.length; j++){
	      if (prev[j] instanceof RETURN){
		System.out.println ("Finding a return!!!");
		Quad quads[] = new Quad[5];
		HClass callParamTypes[] = {HClass.forName("java.rmi.Remote")};
		Temp callParams[] = {header.params[0]};
		//we'll make a retval temp because the call returns a value
		//but we'll never use it cause we dont' want the value
		Temp retval = new Temp();
		Temp retex0 = new Temp();
		quads[0] = 
		  new CALL (prev[j].getSourceElement(), 
			    HClass.forName ("java.rmi.server.UnicastRemoteObject").
			    getMethod ("exportObject", callParamTypes),
			    null, callParams, retval, retex0, false);
		Temp null1 = new Temp();
		quads[1] = new CONST (prev[j].getSourceElement(), null1, null, HClass.Void);
		Temp temp5 = new Temp();
		Temp operands1[] = {null1, retex0};
		quads[2] = new OPER (prev[j].getSourceElement(), Qop.ACMPEQ, temp5, operands1);
		Temp retex0a = new Temp();
		Temp retex0b = new Temp();    
		Temp dst0[][] = {{retex0a, retex0b}};
		Temp src0[] = {retex0};
		quads[3] = new CJMP (prev[j].getSourceElement(), temp5, dst0, src0);
		quads[4] = new THROW (prev[j].getSourceElement(), retex0a);
		Quad.addEdges (quads);
		Quad.addEdge (prev[j].prev(0), prev[j].prevEdge(0).which_succ(),
			      quads[0], 0);
		Quad.addEdge (quads[3], 1, prev[j], 0);
		footer.attach (quads[4], 0);
	      } else {
		System.out.println ("Ok.. so it's not a return.. it's a: " + prev[j].toString());
	      }
	    }
	  } else {
	    System.out.println ("(((((((((((((((((())))))))))))))))))))");
	    System.out.println ("I'm decideing that the superclass is not an HClassSyn: " + newClass.getName());
	    System.out.println ("(((((((((((((((((())))))))))))))))))))");
	  }
	}
      }
    }


    return newClass;
    
  }

  static HMethodSyn newODSMethod (HMethod method, HClassSyn newClass, HClassSyn newInterface){

    HClass oldParamTypes[] = method.getParameterTypes();
    HClass newParamTypes[] = new HClass[oldParamTypes.length+1];
    newParamTypes[oldParamTypes.length] = HClass.Int;
    for (int i = 0; i < oldParamTypes.length; i++){
      newParamTypes[i] = oldParamTypes[i];
    }

    String newName = "new_" + method.getDeclaringClass().getName().replace ('.', '_');
    HMethodSyn remoteInterfaceMethod = odsList.addInterfaceMethod(newName,
								  newParamTypes, newInterface,
								  method.getExceptionTypes(),
								  method.getModifiers());
								  
    newMethodMap.put (method, remoteInterfaceMethod); 

    //*Actually create the code for the meethod*

    //need to create new temps for the paramters
    int paramslength = newParamTypes.length + 1;    

    Temp headerParams[] = new Temp[paramslength];
    for (int i = 0; i < paramslength; i++){
      headerParams[i] = new Temp();
    }

    Temp callParams[] = new Temp[newParamTypes.length];
    for (int i = 0; i < callParams.length; i++){
      callParams[i] = headerParams[i+1];
    }

    Temp shortNewParams[] = new Temp[oldParamTypes.length];
    for (int i = 0; i < oldParamTypes.length; i++){
      shortNewParams[i] = headerParams[i+1];
    }

    //need to create new paramtypes in order to find the right method
    Quad quads[] = new Quad[10];
    FOOTER footer = new FOOTER(hc);
    quads[0] = new METHODHEADER(hc, footer, headerParams);

    Temp temp1 = new Temp();
    quads[1] = new GET(hc, temp1, odsList.ods.getField("machineIndex"));
    
    Temp temp2 = new Temp();
    Temp operands[] = {temp1, headerParams[headerParams.length-1]};
    quads[2] = new OPER(hc, Qop.ICMPEQ, temp2, operands);
    
    //XX need to add the correct things to the SIGMA
    //Don't think I need to add anything to the Sigma here 
    quads[3] = new CJMP(hc, temp2, new Temp[0][0], new Temp[0]);

    Temp temp3 = new Temp();
    quads[4] = new GET (hc, temp3, odsList.ods.getField("ods"));

    HClass odsCallParmTypes[] = {HClass.Int};
    Temp odsCallParams[] = {headerParams[headerParams.length-1]};
    Temp retval0 = new Temp();
    Temp retex0 = new Temp();
    quads[5] = new CALL (hc, odsList.remoteInterface.getDeclaredMethod ("getMachine", odsCallParmTypes),
			 temp3, odsCallParams, retval0, retex0, false);
    Temp null1 = new Temp();
    quads[6] = new CONST (hc, null1, null, HClass.Void);
    Temp temp5 = new Temp();
    Temp operands1[] = {null1, retex0};
    quads[7] = new OPER (hc, Qop.ACMPEQ, temp5, operands1);
    Temp retval0a = new Temp();
    Temp retval0b = new Temp();
    Temp retex0a = new Temp();
    Temp retex0b = new Temp();    
    Temp dst0[][] = {{retval0a, retval0b}, {retex0a, retex0b}};
    Temp src0[] = {retval0, retex0};
    quads[8] = new CJMP (hc, temp5, dst0, src0);
    quads[9] = new THROW (hc, retex0a);

    Quad quads1[] = new Quad[5];
    Temp retval = new Temp();
    Temp retex = new Temp();
    //need to use the new temps here
    //need to get the paramtypes in order to find the right hmethod
    quads1[0] = new CALL (hc, 
			  remoteInterfaceMethod,
			  retval0b, callParams, retval, retex,
			  false);
    Temp temp9 = new Temp();
    Temp null2 = new Temp();
    quads1[1] = new CONST (hc, null2, null, HClass.Void);
    Temp operands2[] = {null2, retex};
    quads1[2] = new OPER (hc, Qop.ACMPEQ, temp9, operands2);
    Temp retvala = new Temp();
    Temp retvalb = new Temp();
    Temp retexa = new Temp();
    Temp retexb = new Temp();    
    Temp dst[][] = {{retvala, retvalb}, {retexa, retexb}};
    Temp src[] = {retval, retex};
    quads1[3] = new CJMP (hc, temp9, dst, src);
    quads1[4] = new THROW (hc, retexa);
    Quad ret = new RETURN (hc, retvalb);
    
    Quad quads2[] = new Quad[6];
    Temp temp10 = new Temp();
    quads2[0] = new NEW (hc, temp10, method.getDeclaringClass());
    Temp retex2 = new Temp();

    quads2[1] = new CALL (hc, method, temp10, shortNewParams,
			  null, retex2, true);
    Temp null3 = new Temp();
    quads2[2] = new CONST (hc, null3, null, HClass.Void);
    Temp temp13 = new Temp();
    Temp operands3[] = {null3, retex2};
    quads2[3] = new OPER (hc, Qop.ACMPEQ, temp13, operands3);
    Temp temp10a = new Temp();
    Temp temp10b = new Temp();
    Temp retex2a = new Temp();
    Temp retex2b = new Temp();    
    Temp dst2[][] = {{temp10a, temp10b}, {retex2a, retex2b}};
    Temp src2[] = {temp10, retex2};
    quads2[4] = new CJMP (hc, temp13, dst2, src2);
    quads2[5] = new THROW (hc, retex2a);
    Quad ret2 = new RETURN (hc, temp10b);

    Quad.addEdges(quads);
    footer.attach (quads[9], 0);

    Quad.addEdges(quads1);
    Quad.addEdge (quads[8], 1, quads1[0], 0);
    Quad.addEdge (quads1[3],1, ret, 0);
    footer.attach (quads1[4], 0);
    footer.attach (ret, 0);

    Quad.addEdges(quads2);
    Quad.addEdge (quads[3], 1, quads2[0], 0);
    Quad.addEdge (quads2[4], 1, ret2, 0);
    footer.attach (quads2[5], 0);
    footer.attach (ret2, 0);
    //Quad1: methodheader
    //Quad2: getfield
    //Quad3: branch_ifeq field == param_0 to Label 1
    //Quad3: aget machineTable.get(machineName);
    //Quad3: call the method on the other machine (the one we just got from array)
    //Quad3: return the return of the call
    // Label 1:
    //Quad3: new objectype
    //Quad3: init object with same params
    //Quad3: return new object
    
    //use a bogus hclass (HClass.Int) because this is just a methodTemplate
    //XX do I need to add the param names??
    //map the <init> function to the remoteInterface for that method
    odsList.addMethod ("new_" +
		       method.getDeclaringClass().getName().replace ('.', '_'),
		       newParamTypes, newInterface,
		       method.getExceptionTypes(),
		       method.getModifiers(),
		       quads[0]);
    return remoteInterfaceMethod;
  }




  static FOOTER getFooter (Quad q, Hashtable alreadySeen){
    System.out.println ("Looking for the footer from: " + q.toString());
    if (q instanceof METHODHEADER){
      return ((METHODHEADER) q).footer;
    }
    alreadySeen.put (q, q);
    Quad prev[] = q.prev();
    for (int i = 0; i < prev.length; i++){
      if (!alreadySeen.containsKey(prev[0])){
	FOOTER footer = getFooter (prev[i], alreadySeen);
	if (footer != null){
	  return footer;
	}
      }
    }
    return null;
  }
  
  /*static FOOTER getFooter (Quad q){
    while (!(q instanceof FOOTER)){
    q = q.next(0);
    }
    return (FOOTER) q;
    }
  */
  

}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
