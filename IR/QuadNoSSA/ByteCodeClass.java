package harpoon.IR.QuadNoSSA;

import harpoon.ClassFile.*;

import java.io.*;
import java.util.*;

public class ByteCodeClass {

  
  Vector methods;
  HClass myClass;
    
  public ByteCodeClass (HClass hclass){
    System.out.println ("Starting the Init method for byteCodeclass");
    methods = new Vector();
    System.out.println ("Middle the Init method for byteCodeclass");
    myClass = hclass;
    System.out.println ("Ending  the Init method for byteCodeclass");
  }

  public void addMethod (NMethod method){
    methods.addElement (method);
  }

  public void writeClass (PrintWriter out) throws IOException{
    if (myClass.isInterface()){
      out.print (".interface ");
    } else {
      out.print (".class ");
    }
    out.print (java.lang.reflect.Modifier.toString(myClass.getModifiers()) + " " 
		    + myClass.getName().replace('.', '/') + "\n");
    if (myClass.getSuperclass() == null){
	out.println (".super java/lang/Object");
    } else {
	out.print (".super " + myClass.getSuperclass().getName().replace ('.', '/') + "\n");
    }
    HField fields[] = myClass.getFields();

    HClass interfaces[] = myClass.getInterfaces();
    for (int i=0; i < interfaces.length; i++){
      out.print (".implements " + " " + interfaces[i].getName().replace ('.', '/') + "\n");
    }

    for (int i=0; i < fields.length; i++){
      out.print (".field " + java.lang.reflect.Modifier.toString(fields[i].getModifiers()) + 
		 " " + fields[i].getName() + " " + fields[i].getDescriptor() + "\n");
    }
    
    System.out.println ("Writing methods");
    for (int i= 0; i < methods.size(); i++){
      System.out.println ("Writing Method: " + ((NMethod)methods.elementAt(i)).myMethod.getName());
      if (!(myClass.isInterface() && 
	   (!java.lang.reflect.Modifier.isPublic (((NMethod)methods.elementAt(i)).myMethod.getModifiers())))){
	((NMethod)methods.elementAt(i)).writeMethod(out);
      }
    }
  }


}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
