package harpoon.Nate;

import harpoon.IR.QuadSSA.*;
import harpoon.IR.Bytecode.*;
import harpoon.ClassFile.*;
import harpoon.Temp.Temp;


import java.util.*;

class NateVisitor extends QuadVisitor{
  Hashtable newMap; // maps new Quads to machine id ints
  Hashtable tempMap; //a hashtable maping temps to the new calls that defined them

  Hashtable sourceMap; //sourceMap maps file:linenumber strings to machine numbers
  //HClassSyn ods; // the ods class that we have created
  HCodeElement hc; //bogus hcodelement for all quads I create as part of the ODS
  ODSList odsList;
  private static final boolean printTypes = false;

  NateVisitor (ODSList odsList, Hashtable sourceMap){
    this.newMap = odsList.newMap;
    

    this.odsList = odsList;
    this.sourceMap = sourceMap;
    this.tempMap = new Hashtable();
    hc = (HCodeElement) new InMerge ("natesNewODSStuff", 666, 0);
  }

  FOOTER getFooter (Quad q, Hashtable alreadySeen){
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
  
  
  /* FOOTER getFooter (Quad q){
     while (!(q instanceof FOOTER)){
     try {
     System.out.println ("Looking for the footer from: " + q.toString());
     q = q.next(0);
     } catch (NullPointerException e){
     System.out.println ("The quad is: " + q.toString());
     throw e;
     }
     }
     return (FOOTER) q;
     }
  */
  

  HClass getClassPointer (HClass hclass){
    HClassSyn newClass = odsList.HclassMap.get (hclass);
    if (newClass == null){
      //if we aren't cloning the class, then just return the original
      return hclass;
    }
    if (odsList.interfaceMap.contains (newClass)){
      newClass = (HClassSyn) odsList.interfaceMap.get (newClass);
    }
    return newClass;
  }

  HMethod getMethodPointer (HMethod oldMethod){
    HMethodSyn newMethod = odsList.methodMap.get (oldMethod);
    if (newMethod == null){
      //if we aren't cloning the class, then just return the original
      return oldMethod;
    }
    if (!newMethod.getName().equals ("<init>")){
      HClassSyn remoteInterface = odsList.interfaceMap.get (newMethod.getDeclaringClass());
      if (remoteInterface != null){
	newMethod = (HMethodSyn) remoteInterface.getDeclaredMethod (newMethod.getName(), newMethod.getParameterTypes());
	harpoon.Util.Util.assert (newMethod != null);
      }
    }
    return newMethod;
  }

  HField getFieldPointer (HField oldField){
    HFieldSyn newField = (HFieldSyn) odsList.fieldMap.get (oldField);
    if (newField == null){
      //if we aren't cloning the field, then just return the original
      return oldField;
    }
    return newField;
  }

  public void visit (Quad q){}

  //**ANEW**
  public void visit (ANEW q){
    q.hclass = getClassPointer (q.hclass);
  }

  //**CALL**
  public void visit (CALL q){
    
    //System.out.println ("This is a call to: " + q.method.getName() + " in class: " + q.method.getDeclaringClass().getName());
    //I should only do this if the new call is being deleted
    if (q.method.getName().equals ("<init>") && tempMap.containsKey(q.objectref)) {

      //replace with call to ODS
      //System.out.println ("And I seem to think that I should being doing smart stuff with this method");
      HMethodSyn newMethod = odsList.newMethodMap.get (odsList.methodMap.get(q.method));
      harpoon.Util.Util.assert (newMethod != null);

      NEW newCall = (NEW) tempMap.get (q.objectref);
      //get the machine index for where the object should be created
      Integer index = (Integer) newMap.get (newCall);

      Temp indexCONST = new Temp();
      Quad newConst = new CONST (hc, indexCONST, index, HClass.Int);
      Temp ods = new Temp();
      Quad newGet = new GET (hc, ods, odsList.ods.getDeclaredField ("ods"));
      Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), newConst, 0);
      Quad.addEdge (newConst, 0, newGet, 0);
      Quad.addEdge (newGet, 0, q, 0);
      Temp newParams[] = new Temp[q.params.length+1];
      newParams[q.params.length] = indexCONST;
      for (int i = 0; i < q.params.length; i++){
	newParams[i] = q.params[i];
      }
      q.retval = q.objectref;
      q.objectref = ods;
      q.method = newMethod;
      q.isSpecial = false;
      q.params = newParams;
    } else {
      q.method = getMethodPointer (q.method);
    }
  }

  //**CONST**

  public void visit (CONST q){
    q.type = getClassPointer (q.type);
  }

  //**GET**
  public void visit (GET q){
    

    if (odsList.getFieldInterfaceMap.containsKey (q.field)){
      HMethodSyn accessorMethod = (HMethodSyn) odsList.getFieldInterfaceMap.get (q.field);
      FOOTER footer = getFooter(q, new Hashtable());
      // I need to replace getfield with a call to the accessor function
      Temp retval = new Temp();
      Temp retex = new Temp();
      CALL accessorCall = new CALL (hc, accessorMethod, q.objectref, new Temp[0], retval, retex, false);
      Temp temp8 = new Temp();
      Quad quad1 = new CONST (hc, temp8, null, HClass.Void);
      Temp temp9 = new Temp();
      Temp operands2[] = {temp8, retex};
      Quad quad2 = new OPER (hc, Qop.ACMPEQ, temp9, operands2);
      Temp retvala = new Temp();
      Temp retexa = new Temp();
      Temp retexb = new Temp();    
      Temp dst[][] = {{retvala, q.dst}, {retexa, retexb}};
      Temp src[] = {retval, retex};
      Quad quad3 = new CJMP (hc, temp9, dst, src);
      Quad quad4 = new THROW (hc, retexa);
      Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), accessorCall, 0);
      Quad.addEdge (accessorCall, 0, quad1, 0);
      Quad.addEdge (quad1, 0, quad2, 0);
      Quad.addEdge (quad2, 0, quad3, 0);
      Quad.addEdge (quad3, 0, quad4, 0);
      footer.attach (quad4, 0);
      Quad.addEdge(quad3, 1, q.next(0), q.nextEdge(0).which_pred());
    } else {
      q.field = getFieldPointer (q.field);
    }
  }

  //**INSTANCEOF**
  public void visit (INSTANCEOF q){
    q.hclass = getClassPointer (q.hclass);
  }
  


  //**NEW**
  public void visit (NEW q){
    String FileandLine = q.getSourceFile() + ":" + q.getLineNumber();
    if (sourceMap.containsKey(FileandLine)){
      System.out.println ("**********************************");
      System.out.println ("Finding the New at least!!!");
      System.out.println ("**********************************");
      
      Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), 
		   q.next(0), q.nextEdge(0).which_pred());
      //add the temp to the tempTable, so we know to change the init function
      newMap.put (q, sourceMap.get (FileandLine));
      tempMap.put (q.dst, q);
      } else { 
	q.hclass = getClassPointer (q.hclass);
      }
    
  }

  //**SET**
  public void visit (SET q){
    if (odsList.setFieldInterfaceMap.containsKey (q.field)){
      HMethodSyn accessorMethod = (HMethodSyn) odsList.setFieldInterfaceMap.get (q.field);
      FOOTER footer = getFooter (q, new Hashtable());
      // I need to replace getfield with a call to the accessor function
      Temp arguments[] = {q.src};
      Temp retex = new Temp();
      CALL accessorCall = new CALL (hc, accessorMethod, q.objectref, arguments, null, retex, false);
      Temp temp8 = new Temp();
      Quad quad1 = new CONST (hc, temp8, null, HClass.Void);
      Temp temp9 = new Temp();
      Temp operands2[] = {temp8, retex};
      Quad quad2 = new OPER (hc, Qop.ACMPEQ, temp9, operands2);
      Temp retvala = new Temp();
      Temp retexa = new Temp();
      Temp retexb = new Temp();    
      Temp dst[][] = {{retexa, retexb}};
      Temp src[] = {retex};
      Quad quad3 = new CJMP (hc, temp9, dst, src);
      Quad quad4 = new THROW (hc, retexa);
      Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), accessorCall, 0);
      Quad.addEdge (accessorCall, 0, quad1, 0);
      Quad.addEdge (quad1, 0, quad2, 0);
      Quad.addEdge (quad2, 0, quad3, 0);
      Quad.addEdge (quad3, 0, quad4, 0);
      footer.attach (quad4, 0);
      Quad.addEdge(quad3, 1, q.next(0), q.nextEdge(0).which_pred());
    } else {
      q.field = getFieldPointer (q.field);
    }
  }


}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:

