package harpoon.Nate;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;

import java.util.*;

class ODSList {

  ClassMap HclassMap; // maps old HClass to the new HClassSyn with all fields made
  ClassMap interfaceMap; //maps HClass to their corresponding remote interfaces

  MethodMap methodMap;
  MethodMap methodInterfaceMap;

  Hashtable fieldMap;

  Hashtable newMap; // maps new Quads to machine id ints

  MethodMap newMethodMap;

  Hashtable getFieldInterfaceMap;
  Hashtable setFieldInterfaceMap;

  HClassSyn ods;
  HClassSyn remoteInterface;
  HClassSyn list[];

  ODSList(int numMachines, Hashtable newMap){
    this.HclassMap = new ClassMap();//stored here only for ease of access
    this.interfaceMap = new ClassMap();//stored here only for ease of access

    this.methodMap = new MethodMap();
    this.methodInterfaceMap = new MethodMap();//stored here only for ease of access
    this.fieldMap = new Hashtable();

    this.newMethodMap = new MethodMap();

    this.getFieldInterfaceMap = new Hashtable();
    this.setFieldInterfaceMap = new Hashtable();



    this.newMap = newMap;
    HClass oldODS = HClass.forName("ODS");
    ods = new HClassSyn (oldODS);
    list = new HClassSyn[numMachines];
    HClass oldodsModel = HClass.forName ("odsModel");
    
    HClassSyn odsModel = new HClassSyn (oldodsModel);

    HCodeElement hc = (HCodeElement) new harpoon.IR.Bytecode.InMerge ("natesNewODSStuff", 99, 0);
    
    HClass remoteClass = HClass.forName ("remoteODS");

    remoteInterface = new HClassSyn (remoteClass);

    //correct all the incorrect references in odsModel
    Hashtable phiTable = new Hashtable();
    ReplaceVisitor v = new ReplaceVisitor (oldODS, ods, remoteClass, remoteInterface, phiTable);


    HMethod methods[] =  remoteInterface.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      HClass[] paramTypes = methods[i].getParameterTypes();
      HClass[] newParamTypes = new HClass [paramTypes.length];
      for (int j = 0; j < paramTypes.length; j++){
	if (paramTypes[j].equals(oldODS)){
	  newParamTypes[j] = ods;
	} else if (paramTypes[j].equals (remoteClass)){
	  newParamTypes[j] = remoteInterface;
	} else {
	  newParamTypes[j] = paramTypes[j];
	}
      }
      if (methods[i].getReturnType().equals(oldODS)){
	((HMethodSyn) methods[i]).setReturnType(ods);
      } else if (methods[i].getReturnType().equals (remoteClass)){
	((HMethodSyn) methods[i]).setReturnType(remoteInterface);
      }
      ((HMethodSyn)methods[i]).setParameterTypes (newParamTypes);
    }


    methods =  ods.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      HClass[] paramTypes = methods[i].getParameterTypes();
      HClass[] newParamTypes = new HClass [paramTypes.length];
      for (int j = 0; j < paramTypes.length; j++){
	if (paramTypes[j].equals(oldODS)){
	  newParamTypes[j] = ods;
	} else if (paramTypes[j].equals (remoteClass)){
	  newParamTypes[j] = remoteInterface;
	} else {
	  newParamTypes[j] = paramTypes[j];
	}
      }
      if (methods[i].getReturnType().equals(oldODS)){
	((HMethodSyn) methods[i]).setReturnType(ods);
      } else if (methods[i].getReturnType().equals (remoteClass)){
	((HMethodSyn) methods[i]).setReturnType(remoteInterface);
      } 
      ((HMethodSyn)methods[i]).setParameterTypes (newParamTypes);
      replaceSuperBlock ((Quad) methods[i].getCode ("quad-ssa").getRootElement(),
			 phiTable, v);
    }

    methods =  odsModel.getDeclaredMethods();

    for (int i = 0; i < methods.length; i++){
      HClass[] paramTypes = methods[i].getParameterTypes();
      HClass[] newParamTypes = new HClass [paramTypes.length];
      for (int j = 0; j < paramTypes.length; j++){
	if (paramTypes[j].equals(oldODS)){
	  newParamTypes[j] = ods;
	} else if (paramTypes[j].equals (remoteClass)){
	  newParamTypes[j] = remoteInterface;
	} else {
	  newParamTypes[j] = paramTypes[j];
	}
      }
      if (methods[i].getReturnType().equals(oldODS)){
	((HMethodSyn) methods[i]).setReturnType(ods);
      } else if (methods[i].getReturnType().equals (remoteClass)){
	((HMethodSyn) methods[i]).setReturnType(remoteInterface);
      }
      ((HMethodSyn)methods[i]).setParameterTypes (newParamTypes);
      replaceSuperBlock ((Quad) methods[i].getCode ("quad-ssa").getRootElement(),
			 phiTable, v);
    }    



    HFieldSyn odsField = (HFieldSyn) ods.getDeclaredField ("ods");
    odsField.setType (remoteInterface);
    HFieldSyn machinesField = (HFieldSyn) ods.getDeclaredField ("machines");
    machinesField.setType (HClass.forDescriptor ("[" + remoteInterface.getDescriptor()));

    for (int i = 0; i < numMachines; i++){

      HClassSyn newODS = new HClassSyn (odsModel);
      newODS.setSuperclass (ods);
      newODS.removeAllInterfaces();
      newODS.addInterface (remoteInterface);
      
      phiTable = new Hashtable();
      ReplaceVisitor initReplace = new ReplaceVisitor (oldodsModel, newODS, remoteClass,
						       remoteInterface, phiTable);
      //replace everything int ehstatic initizalize method
      HClass initParams[] = {HClass.Int, HClass.Int};
      HMethodSyn initMethod = (HMethodSyn) newODS.getDeclaredMethod ("initialize", initParams);
      
      replaceSuperBlock ((Quad) initMethod.getCode ("quad-ssa").getRootElement(),
			 phiTable, initReplace);


      
      
      Quad quads[] = new Quad[7];
      HMethodSyn staticMethod = (HMethodSyn) newODS.getDeclaredMethod("<clinit>", new HClass[0]);
      METHODHEADER header = (METHODHEADER) staticMethod.getCode ("quad-ssa").getRootElement();
      FOOTER footer = header.footer;
      Temp numberMachines= new Temp();
      quads[0] = new CONST (hc, numberMachines, new Integer(numMachines), HClass.Int);
      Temp machineIndex = new Temp(); 
      quads[1] = new CONST (hc, machineIndex, new Integer(i), HClass.Int);
      HClass odsCallParmTypes[] = {HClass.Int, HClass.Int};
      Temp odsCallParams[] = {numberMachines, machineIndex};
      Temp retex0 = new Temp();
      quads[2] = new CALL (hc, newODS.getDeclaredMethod ("initialize", odsCallParmTypes),
	null, odsCallParams, null, retex0, false);
      /*quads[2] = new CONST (hc, retex0, null, HClass.Void);*/
      Temp null1 = new Temp();
      quads[3] = new CONST (hc, null1, null, HClass.Void);
      Temp temp5 = new Temp();
      Temp operands1[] = {null1, retex0};
      quads[4] = new OPER (hc, "acmpeq", temp5, operands1);
      Temp retex0a = new Temp();
      Temp retex0b = new Temp();    
      Temp dst0[][] = {{retex0a, retex0b}};
      Temp src0[] = {retex0};
      quads[5] = new CJMP (hc, temp5, dst0, src0);
      quads[6] = new THROW (hc, retex0a);


      Quad.addEdges (quads);
      Quad.addEdge (quads[5], 1, header.next(0), header.nextEdge(0).which_pred());
      Quad.addEdge (header, 0, quads[0], 0);
      footer.attach (quads[6], 0);
      
      list[i] = newODS;
    }
      
  }

  void addMethod (String name, HClass[] paramTypes, HClass retType, HClass[] excepTypes, int modifiers, Quad code){
    for (int i = 0; i < list.length; i++){
      HMethodSyn newMethod =  new HMethodSyn(list[i], 
					     name,
					     paramTypes,
					     retType);
      newMethod.setExceptionTypes(excepTypes);
      newMethod.setModifiers (modifiers | java.lang.reflect.Modifier.PUBLIC);
      newMethod.putCode ((HCode) new harpoon.IR.QuadSSA.Code (newMethod, code));
      //Don't need this next line cause the construction for the HMethodSyn adds it to the 
      //class, and we don't want to add it twice
      //list[i].addDeclaredMethod(newMethod);
    }
  }

  void replaceSuperBlock (Quad q, Hashtable phiTable, QuadVisitor v){
    q.visit(v);
    
    Quad next[] = q.next();
    
    //add the quads for all the paths other than the main path
    for (int i = 0; i < next.length; i++){
      if ((next[i].prev().length == 1) ||  !phiTable.containsKey(next[i])){
	//if the code hasn't been created anywhere, then get the label out of the label table
	//the label should have been put there when the CJMP or switch instruction was created
	//add the label, and create all the code to follow it
	//if (i > 0){
	//this will create the 
	replaceSuperBlock (next[i], phiTable, v);
      } 
    }
  }

  class ReplaceVisitor extends QuadVisitor {
    HClass oldODS;
    HClassSyn newODS;
    HClass oldInterface;
    HClassSyn newInterface;

    Hashtable phiTable;

    ReplaceVisitor (HClass oldODS, HClassSyn newODS, HClass oldInterface, HClassSyn newInterface, Hashtable phiTable){
      this.oldODS = oldODS;
      this.newODS = newODS;
      this.oldInterface = oldInterface;
      this.newInterface = newInterface;

      this.phiTable = phiTable;
      
    }
    public void visit (Quad q) {}
    public void visit (ANEW q) {
      if (q.hclass.getComponentType().equals (oldODS)){
	q.hclass = HClass.forDescriptor ("[" + newODS.getDescriptor());
      } else if (q.hclass.getComponentType().equals (oldInterface)){
	q.hclass = HClass.forDescriptor ("[" + newInterface.getDescriptor());
      }
    }
    public void visit (INSTANCEOF q){
      if (q.hclass.equals (oldODS)){
	q.hclass = newODS;
      } else if (q.hclass.equals (oldInterface)){
	q.hclass = newInterface;
      }
    }
    public void visit (CALL q) {
      if (q.method.getDeclaringClass().equals (oldODS)){
	HClass[] paramTypes = q.method.getParameterTypes();
	HClass[] newParamTypes = new HClass [paramTypes.length];
	for (int j = 0; j < paramTypes.length; j++){
	  if (paramTypes[j].equals(oldODS)){
	    newParamTypes[j] = ods;
	  } else if (paramTypes[j].equals (oldInterface)){
	    newParamTypes[j] = remoteInterface;
	  } else {
	    newParamTypes[j] = paramTypes[j];
	  }
	}
	q.method = newODS.getDeclaredMethod (q.method.getName(), newParamTypes);
      } else if (q.method.getDeclaringClass().equals (oldInterface)){
	HClass[] paramTypes = q.method.getParameterTypes();
	HClass[] newParamTypes = new HClass [paramTypes.length];
	for (int j = 0; j < paramTypes.length; j++){
	  if (paramTypes[j].equals(oldODS)){
	    newParamTypes[j] = ods;
	  } else if (paramTypes[j].equals (oldInterface)){
	    newParamTypes[j] = remoteInterface;
	  } else {
	    newParamTypes[j] = paramTypes[j];
	  }
	}
	q.method = newInterface.getDeclaredMethod (q.method.getName(), newParamTypes);
      }
    }
    public void visit (GET q) {
      if (q.field.getDeclaringClass().equals (oldODS)){
	q.field = newODS.getDeclaredField (q.field.getName());
      }
    }
    public void visit (SET q) {
      if (q.field.getDeclaringClass().equals (oldODS)){
	q.field = newODS.getDeclaredField(q.field.getName());
      }
    }
    public void visit (NEW q) {
      if (q.hclass.equals (oldODS)){
	q.hclass = newODS;
      } else if (q.hclass.equals (oldInterface)){
	q.hclass = newInterface;
      }
    }
    public void visit (PHI q){
      phiTable.put (q,q);
    }
  }
  

  HMethodSyn addInterfaceMethod (String name, HClass[] paramTypes, HClass retType, HClass[] excepTypes, int modifiers){
    //add the method to the remote interface for the ODS
    HMethodSyn newMethod =  new HMethodSyn(remoteInterface, 
					   name,
					   paramTypes,
					   retType);
    HClass newExceptionTypes[] = new HClass[excepTypes.length +1];
    for (int i = 0; i < excepTypes.length; i++){
      newExceptionTypes[i] = excepTypes[i];
    }
    newExceptionTypes[excepTypes.length] = HClass.forName ("java.rmi.RemoteException");
    newMethod.setExceptionTypes (newExceptionTypes);
    newMethod.setModifiers (modifiers);
    //don't need this line becasuse the method gets added when it is created.. this is a little funcky
    //XXX
    //remoteInterface.addDeclaredMethod (newMethod);
    return newMethod;
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
