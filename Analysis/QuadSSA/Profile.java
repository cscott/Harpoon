// -*-Mode: Java-*- 
// Profile.java -- Inserts profiling statements into QuadSSA CFG.
// Author: Mark Foltz <mfoltz@@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@@ai.mit.edu> 
// Version: 
// Created: <Tue Oct  6 12:41:25 1998> 
// Time-stamp: <1998-12-03 00:39:31 mfoltz> 
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

/** A visitor that inserts profiling Quads into the CFG after <code>CALL</code> and <code>NEW</code> quads. 
 * 
 * @@author  Mark A. Foltz <mfoltz@@ai.mit.edu>
 * @@version 
 */

public class Profile {

  private Profile() { }

  /** After each <code>CALL</code> and <code>NEW</code> quad in <code>hc</code>, 
   * insert a call to the appropriate profiling method. 
   */
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

  /** This class implements the <code>Visitor</code> for <code>optimize()</code> */
  static class Visitor extends QuadVisitor {

    /** the worklist */
    Set _W;

    /** calling method, class, and <code>this</code> for this quad graph */
    HMethod _method;
    HClass _class;

    /** Temps that hold the <code>this</code> reference, a <code>null</code> constant,
     * a <code>String</code> constant with the name of <code>_method</code>, and a 
     * <code>String</code> constant with the name of <code>_class</code>.
     */
    Temp _this, _null_temp, _calling_method_name_temp, _calling_class_name_temp;

    /** The <code>java.lang.String HClass</code>.
    HClass _java_lang_string;

    /** The static profiling methods, from <code>harpoon.RunTime.Monitor</code>. */
    HMethod _call_profiling_method;
    HMethod _new_profiling_method;

    /** A static class that logs compile-time information about who might call whom. */
    StaticMonitor _static_monitor;

    Visitor(Set W, HMethod M, HClass C) {

      this._W = W;
      this._method = M;
      this._class = C;

      _static_monitor = new StaticMonitor();

      _java_lang_string = HClass.forName("java.lang.String");
      //      _java_lang_integer = HClass.forName("java.lang.Integer");

      _null_temp = new Temp();
      _calling_method_name_temp = new Temp();
      _calling_class_name_temp = new Temp();

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
			    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;I)V");

       } catch (Exception e) {
 	System.err.println("Couldn't find method in harpoon.RunTime.Monitor!!!!");
 	e.printStackTrace();
       }
      
    }

    /** Do nothing to regular statements. */
    public void visit(Quad q) { }

    /** Visit the <code>CALL</code> quad <code>q</code>.<p>
     * Insert a <code>CONST</code> quad with a <code>String</code>
     * naming the method in <code>q<code>, then a <code>CALL</code> quad invoking 
     * <code>_call_profile_method</code>.<p>
     * Also, log the fact in <code>_static_monitor</code>that this method may invoke the method in 
     * <code>q</code>. */
    public void visit(CALL q) {

      System.err.print("@");

      // log static info
      _static_monitor.logMAYCALL(_class,_method,q.method.getDeclaringClass(),q.method);

      // Set up constants.

      Temp t1 = new Temp();
      CONST c1 = new CONST(q.getSourceElement(), t1,
			   q.method.getName(), _java_lang_string);

      Temp[] parameters = new Temp[4];

      // Set up other parameters
      parameters[1] = _calling_method_name_temp;
      parameters[3] = t1;

      // if this is a static method call then we need a null constant
      if (_this == null) parameters[0] = _null_temp;
      else parameters[0] = _this;
	
      if (q.objectref == null) parameters[2] = _null_temp;
      else parameters[2] = q.objectref; 

      CALL profiling_call = new CALL(q.getSourceElement(), _call_profiling_method,
				     null, parameters, null, new Temp(), false);

      Quad.addEdge(c1, 0, profiling_call, 0);

      // splice new quads into CFG
      splice(q, c1, profiling_call);

    }

    /** Visit the <code>NEW</code> quad <code>q</code>.<p>
     * Search for the quad initializing the object created in <code>q</code>. After it, insert an <code>int</code> 
     * <code>CONST</code> quad with the line number of 
     * the object creation site, and then a <code>CALL</code> quad invoking 
     * <code>_new_profile_method</code>.
     */
    public void visit(NEW q) {

      Temp[] parameters = new Temp[5];
      Quad search_quad;
      CALL call_quad;

      System.err.print("#");

      Temp t1 = new Temp();
      CONST c1 = new CONST(q.getSourceElement(), t1,
			   new Integer(q.getLineNumber()), 
			   HClass.Int);

      parameters[1] = _calling_class_name_temp;
      parameters[2] = _calling_method_name_temp;
      parameters[3] = q.dst;
      parameters[4] = t1;
      
      if (_this == null) parameters[0] = _null_temp;
      else parameters[0] = _this;

      CALL profiling_call = new CALL(q.getSourceElement(), _new_profiling_method,
				     null, parameters, null, new Temp(), false);

      Quad.addEdge(c1,0,profiling_call,0);

      try {

	// look for init
      
	search_quad = q.next(0);
	while (!(search_quad instanceof CALL)) {
	  if (search_quad instanceof SIGMA || 
	      search_quad instanceof PHI ||
	      search_quad instanceof FOOTER) {
	    System.err.println("sigma/phi bogosity on quads:");
	    System.err.println(q.toString());
	    System.err.println(search_quad.toString());
	  }
	  Util.assert(!(search_quad instanceof SIGMA) && 
		      !(search_quad instanceof PHI) &&
		      !(search_quad instanceof FOOTER));
	  System.err.print(".");
	  search_quad = search_quad.next(0);
	}
	call_quad = (CALL) search_quad;
	if (!call_quad.method.getName().equals("<init>")) {
	  System.err.println("<init> bogosity on quads: ");
	  System.err.println(q.toString());
	  System.err.println(call_quad.toString());
	}
	Util.assert(call_quad.method.getName().equals("<init>"));
	
	// splice new quad into CFG
	splice(call_quad, c1, profiling_call);
      } catch (Throwable e) {
	System.err.println("caught exception here in NEW");
	e.printStackTrace();
      }

    }

    /** Visit the <code>METHODHEADER</code> quad <code>q</code>.<p>
     * After it, insert <code>String<code> <code>CONST</code> quads with the names
     * of this class and method. 
     */
    public void visit(METHODHEADER q) {

      System.err.print("!");

      CONST _null_CONST = new CONST(q.getSourceElement(), _null_temp,
				    null, HClass.Void);

      CONST _calling_method_name_CONST = 
	new CONST(q.getSourceElement(), _calling_method_name_temp,
		  _method.getName(), _java_lang_string);

      CONST _calling_class_name_CONST =
	new CONST(q.getSourceElement(), _calling_class_name_temp,
		  _class.getName(), _java_lang_string);

      Quad.addEdge(_null_CONST,0,_calling_method_name_CONST,0);
      Quad.addEdge(_calling_method_name_CONST,0,_calling_class_name_CONST,0);

      splice(q, _null_CONST, _calling_class_name_CONST);

      // make sure _this is null for static methods
      if (_method.isStatic()) _this = null;
      else _this = q.def()[0];
      // System.err.println(q);
    }

    // Splice quads r to s after q in the CFG.  r is assumed
    // to have no predecessors, s no successors, and q 1 successor.
    private void splice(Quad q, Quad r, Quad s) {
      Util.assert(q.next().length==1);
      for (int i = 0; i < q.next().length; i++) {
	Quad.addEdge(s, i, q.next(i), q.nextEdge(i).which_pred());
      }
      Quad.addEdge(q, 0, r, 0);
    }

  }

  /** This class logs compile-time information about the call graph of a method. */
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
