package harpoon.IR.QuadNoSSA;


import harpoon.Temp.Temp;
import harpoon.ClassFile.*;


import java.io.*;
import java.util.*;

class NInsn {

    int type;

    String myOpc;

    Temp myTemp;
    HClass myClass;
    HMethod myMethod;
    HField myField;

    String myString;

    NLabel myLabel;

    int myIndex;
    

    NInsn (){}; //for the subclasses

    
    NInsn (String opc){
	myOpc = opc;
	type = 0;
    }

    NInsn (String opc, int index){
	myOpc = opc;
	myIndex = index;
	type = 1;
    }	
 
    NInsn (String opc, HClass hclass){
	myOpc = opc;
	myClass = hclass;
	type = 2;
    }

    NInsn (String opc, HMethod hmethod){
	myOpc = opc;
	myMethod = hmethod;
	type = 3;
    }

    NInsn (String opc, HField hfield){
	myOpc = opc;
	myField = hfield;
	type = 4;
    }
    
    //for const instructions with strings
    NInsn (String opc, String string){
	myOpc = opc;
	myString = string;
	type = 5;
    }

    //for const instructions
    NInsn (String opc, NLabel label){
	myOpc = opc;
	myLabel = label;
	type = 6;
    }
  
  NInsn (String opc, Temp t){
    myOpc = opc;
    myTemp = t;
    type = 7;
  }
  

    void writeInsn (PrintWriter out, Hashtable indexTable) throws IOException{

	switch (type) {
	case 0:
	    out.print(myOpc + "\n");
	    break;
	case 1:
	    out.print(myOpc + " " + myIndex + "\n");
	    break;
	case 2:
	    out.print(myOpc + " " + myClass.getName().replace ('.', '/') + "\n");
	    break;
	case 3:
	    out.print(myOpc + " " + myMethod.getDeclaringClass().getName().replace ('.', '/') 
		      + "/" + myMethod.getName() + myMethod.getDescriptor());
	    if (myOpc == "invokeinterface") {
		System.out.println ("Doing the right thing with invoke interface");
		//add an add one for the this variable to the number of args if the method is not static
		if (myMethod.isStatic()){		    
		    out.print (" " + myMethod.getParameterNames().length + "\n");
		} else {
		    out.print (" " + (myMethod.getParameterNames().length+1) + "\n");
		}
	    } else {
		out.print ("\n");
	    }
	    break;
	case 4:
	    out.print(myOpc + " " + myField.getDeclaringClass().getName().replace ('.', '/')   + "/" + 
		      myField.getName().replace ('.', '/') + " " + myField.getDescriptor() + "\n");
	    break;
	case 5:
	    out.print(myOpc + " \"" + myString + "\"\n");
	    break;
	case 6:
	    out.print(myOpc + " " + myLabel.toString() + "\n");
	    break;
	case 7:
	    out.print("; " + myTemp.name() + "\n");
	    out.print(myOpc + " " + indexTable.get(myTemp)  + "\n");
	    break;
	}
    }
}
