// -*-Mode: Java-*- 
// Profile.java -- Inserts profiling statements into QuadSSA CFG.
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Tue Oct  6 12:41:25 1998> 
// Time-stamp: <1998-11-20 12:55:21 mfoltz> 
// Keywords: 

package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.RunTime.Monitor;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Enumeration;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version 
 */

public class Profile {

  private Profile() { }

  public static void optimize(HCode hc) {

    HMethod _method = hc.getMethod();
    HClass _class = _method.getDeclaringClass();
    Set W = new Set();

    Visitor v = new Visitor(W, _method, _class);

    Enumeration e;

    // put CALLs and NEWs on worklist
    for (e = hc.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad) e.nextElement();
      if (q instanceof CALL || q instanceof NEW) 
	W.push(q);
    }

    // Make sure METHODHEADER visited first 
    for (e = hc.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad) e.nextElement();
      if (q instanceof METHODHEADER) 
	q.visit(v);
    }
    
    // Grovel over worklist until empty
    while (!W.isEmpty()) {
      Quad q = (Quad) W.pull();
      q.visit(v);
    }
  }

  static class Visitor extends QuadVisitor {

    // worklist
    Set _W;

    // calling method, class, and this for this quad graph
    HMethod _method;
    HClass _class;
    String _calling_method_name;
    Temp _this;

    // classes for constants
    HClass _java_lang_string;
    //    HClass _java_lang_integer;

    // profiling callbacks
    HMethod _call_profiling_method;
    HMethod _new_profiling_method;

    // static monitor implementation
    StaticMonitor _static_monitor;

    Visitor(Set W, HMethod M, HClass C) {

      this._W = W;
      this._method = M;
      this._class = C;

      _static_monitor = new StaticMonitor();

      _calling_method_name = _method.getName();

      _java_lang_string = HClass.forName("java.lang.String");
      //      _java_lang_integer = HClass.forName("java.lang.Integer");

      // Get HMethods for profiling callbacks.
      HClass monitor = HClass.forName("harpoon.RunTime.Monitor");

//        HMethod[] monitor_methods = monitor.getMethods();
//        // dump method descriptiors
//        for (int i = 0; i < monitor_methods.length; i++) 
//  	System.out.println(monitor_methods[i].getDescriptor());

      HClass _java_lang_object = HClass.forName("java.lang.Object");
      try {

 	_call_profiling_method = 
	  monitor.getMethod("logCALL",
			    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)V");

 	_new_profiling_method = 
	  monitor.getMethod("logNEW",
			    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;I)V");

       } catch (Exception e) {
 	System.err.println("Couldn't find method in harpoon.RunTime.Monitor!!!!");
 	e.printStackTrace();
       }
      
    }

    // Do nothing to regular statements.
    public void visit(Quad q) { }

    // Insert quads calling the profiling procedure
    // before each CALL quad, and log some static info. 
    public void visit(CALL q) {

      // log static info
      _static_monitor.logMAYCALL(_class,_method,q.method.getDeclaringClass(),q.method);

      // Set up constants.
      Temp t1 = new Temp();
      CONST c1 = new CONST(q.getSourceElement(), t1, 
			   _calling_method_name, 
			   _java_lang_string);

      Temp t2 = new Temp();
      CONST c2 = new CONST(q.getSourceElement(), t2,
			   q.method.getName(), _java_lang_string);

      Temp[] parameters = new Temp[4];

      // Set up other parameters
      parameters[1] = t1;
      parameters[3] = t2;

      // if this is a static method call then we need a null constant
      if (q.objectref == null || _this == null) {

	Temp t3 = new Temp();
	CONST c3 = new CONST(q.getSourceElement(), t3,
			     null, HClass.Void);

	if (_this == null) parameters[0] = t3;
	else parameters[0] = _this;
	if (q.objectref == null) parameters[2] = t3;
	else parameters[2] = q.objectref;

	CALL profiling_call = new CALL(q.getSourceElement(), _call_profiling_method,
				       null, parameters, null, new Temp(), false);

	Edge c1_c2 = Quad.addEdge(c1, 0, c2, 0);
	Edge c2_c3 = Quad.addEdge(c2, 0, c3, 0);
	Edge c3_profiling_call = Quad.addEdge(c3, 0, profiling_call, 0);

	// splice new quads into CFG
	splice(q, c1, profiling_call);

      } else {

	parameters[0] = _this;
	parameters[2] = q.objectref;	

	CALL profiling_call = new CALL(q.getSourceElement(), _call_profiling_method,
				       null, parameters, null, new Temp(), false);

	Edge c1_c2 = Quad.addEdge(c1, 0, c2, 0);
	Edge c2_profiling_call = Quad.addEdge(c2, 0, profiling_call, 0);

	// splice new quads into CFG
	splice(q, c1, profiling_call);

      }

    }

    public void visit(NEW q) {

      Temp[] parameters = new Temp[4];

      Temp t1 = new Temp();
      CONST c1 = new CONST(q.getSourceElement(), t1,
			     new Integer(q.getLineNumber()), 
			   HClass.Int);

      Temp t2 = new Temp();
      CONST c2 = new CONST(q.getSourceElement(), t2,
			   _calling_method_name, _java_lang_string);

      if (_this == null) {

	Temp t3 = new Temp();
	CONST c3 = new CONST(q.getSourceElement(), t3,
			     null, HClass.Void);

	parameters[0] = t3;
	parameters[1] = t2;
	parameters[2] = q.dst;
	parameters[3] = t1;

	CALL profiling_call = new CALL(q.getSourceElement(), _new_profiling_method,
				       null, parameters, null, new Temp(), false);

	Quad.addEdge(c1,0,c2,0);
	Quad.addEdge(c2,0,c3,0);
	Quad.addEdge(c3,0,profiling_call,0);

	// splice new quad into CFG
	Util.assert(q.next().length==1);
	splice(q.next(0), c1, profiling_call);

      } else {

	parameters[0] = _this;
	parameters[1] = t2;
	parameters[2] = q.dst;
	parameters[3] = t1;
      
	CALL profiling_call = new CALL(q.getSourceElement(), _new_profiling_method,
				       null, parameters, null, new Temp(), false);


	Quad.addEdge(c1,0,c2,0);
	Quad.addEdge(c2,0,profiling_call,0);
	
	// splice new quad into CFG
	Util.assert(q.next().length==1);
	splice(q.next(0), c1, profiling_call);

      }

    }

    public void visit(METHODHEADER q) {
      _this = q.def()[0];
    }

    // Splice quads r to s before q in the CFG.  r is assumed
    // to have no predecessors and s no successors.
    private void splice(Quad q, Quad r, Quad s) {
      for (int i = 0; i < q.prev().length; i++) {
	Quad.addEdge(q.prev(i), q.prevEdge(i).which_succ(),
		     r, i);
      }
      Quad.addEdge(s, 0, q, 0);
    }

  }

  static class StaticMonitor {
    
    Properties _properties = new Properties();
    DataOutputStream _logstream;

    StaticMonitor() {
      try {
	System.runFinalizersOnExit(true);
	_properties.load(new FileInputStream("/home/mfoltz/Harpoon/Code/RunTime/Monitor.properties"));
	_logstream = new DataOutputStream(new FileOutputStream(_properties.getProperty("staticfile"),true));
      } catch (Exception e) { }
    }

    // static MAYCALL graph
    public void logMAYCALL(HClass sending_class, HMethod sending_method, 
			   HClass receiving_class, HMethod receiving_method) {
      try {
	_logstream.writeBytes("MAYCALL "+sending_class.getName()+" "+sending_method.getName()+" "+
			      receiving_class.getName()+" "+receiving_method.getName()+"\n");
      } catch (Exception e) { }
    }

    void classFinalizer() throws Throwable {
      _logstream.flush();
      _logstream.close();
    }

  }

}




			   


      
      

      
