package harpoon.IR.QuadNoSSA;

import harpoon.ClassFile.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class NMethod {
  
  HMethod myMethod;
  private Vector instructions;
  private Vector catchTable;
  private Hashtable myIndexTable;
  int locallimit;
  int stacklimit;
  
  NMethod(HMethod method, Hashtable indexTable){
    instructions = new Vector();
    catchTable = new Vector();
    myIndexTable = indexTable;
    myMethod = method;
    int locallimit = -1;
    int stacklimit = -1;
  }
    
  void limitLocals (int limit){
    locallimit = limit;
  }

  void limitStack (int limit){
    stacklimit = limit;
  }

  void addInsn (NInsn insn){
    instructions.addElement(insn);
  }

  void addCatch (NLabel start, NLabel end, NLabel handler, String exception){
    catchTable.addElement(new NCatch (start, end, handler, exception));
  }

  void writeMethod (PrintWriter out) throws IOException{
    
    out.print (".method ");
    //add the abstract keyword to classes which have no code... this is mainly for 
    //interfaces since the verifier seems to want the abstract keyword to be there 
    // even though the JVM spec claims it's optional for interfaces...
    if (instructions.size() == 0){
	String modifiers = java.lang.reflect.Modifier.toString (myMethod.getModifiers());
	int index = modifiers.lastIndexOf ("native");
	System.out.println ("***********************************************************");
	System.out.println ("***********************************************************");
	System.out.println ("The method has no code: " + myMethod.toString());
	if (index >= 0){
	    System.out.println ("And it's native too!!!");
	} else {
	    System.out.println ("HOwever... it's not native");
	}
	/*if (index >= 0){
	  if (index+6 < modifiers.length()){
	  if (index == 0){
	  modifiers = modifiers.substring (index+6);
	  } else {
	  modifiers = modifiers.substring (0, index-1) + modifiers.substring (index+6);
	  }
	  } else {
	  if (index == 0){
	  modifiers = "";
	  } else {
	  modifiers = modifiers.substring (0, index-1);
	  }
	  }
	  }*/
	if (index >= 0){
	    if (java.lang.reflect.Modifier.isInterface(myMethod.getDeclaringClass().getModifiers())){
		out.print (modifiers + " abstract " + myMethod.getName() + myMethod.getDescriptor() + "\n");
	    } else {
		out.print (modifiers + " " + myMethod.getName() + myMethod.getDescriptor() + "\n");
	    }
	} else {
	    out.print (modifiers + " abstract " + myMethod.getName() + myMethod.getDescriptor() + "\n");
	}
    } else {
	out.print (java.lang.reflect.Modifier.toString (myMethod.getModifiers())
		   + " " + myMethod.getName() + myMethod.getDescriptor() + "\n");
    }

    HClass exceptions[] = myMethod.getExceptionTypes();
    for (int i = 0; i < exceptions.length; i++){
      out.print (".throws " + exceptions[i].getName().replace ('.', '/') + "\n");
    }

    if (locallimit >= 0){
      out.print (".limit locals " + locallimit + "\n");
    } 

    if (stacklimit >= 0){
      out.print (".limit stack " + stacklimit + "\n");
    }

    //print out all the catch blocks
    for (int i = 0; i < catchTable.size(); i++){
      NCatch curexcept = (NCatch)catchTable.elementAt(i);
      out.print (".catch " + curexcept.myException +  
		 " from " + curexcept.myStart + " to " + curexcept.myEnd + " using " 
		 + curexcept.myHandler + "\n");
    }
    
    //print out all the instructions
    for (int i = 0; i < instructions.size(); i++){
	if (instructions.elementAt(i) instanceof LookupswitchInsn){
	    out.println (";I actually thing this is a switch");
	}
	((NInsn)instructions.elementAt(i)).writeInsn (out, myIndexTable);
    }

    out.print (".end method\n");

  }
}

  class NCatch {

    NLabel myStart;
    NLabel myEnd;
    NLabel myHandler;
    String myException;


    NCatch (NLabel start, NLabel end, NLabel handler, String exception){
      myStart = start;
      myEnd = end;
      myHandler = handler;
      myException = exception;
    }

  }
