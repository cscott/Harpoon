// -*-Mode: Java-*- 
// Profile.java -- Inserts profiling statements into QuadSSA CFG.
// Author: Mark Foltz <mfoltz@@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@@ai.mit.edu> 
// Version: 
// Created: <Tue Oct  6 12:41:25 1998> 
// Time-stamp: <1998-12-11 18:28:31 mfoltz> 
// Keywords: 

package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.RunTime.Monitor;
import harpoon.Analysis.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Hashtable;

/** A visitor that inserts profiling Quads into the CFG after <code>CALL</code> and <code>NEW</code> quads. 
 * 
 * @author  Mark A. Foltz <mfoltz@@ai.mit.edu>
 * @version 
 */

public class Profile {

  private Profile() { }

  /** After each <code>CALL</code> and <code>NEW</code> quad in <code>hc</code>, 
   * insert a call to the appropriate profiling method. 
   */
  public static void optimize(HCode hc) {

    Visitor v = new Visitor(hc);

    Enumeration e;

    // Make sure METHODHEADER visited first 
    for (e = hc.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad) e.nextElement();
      if (q instanceof METHODHEADER) 
	q.visit(v);
    }
    
    // then CALLs
    for (e = hc.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad) e.nextElement();
      if (q instanceof CALL) 
	q.visit(v);
    }

    //  NEWs last
    for (e = hc.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad) e.nextElement();
      if (q instanceof NEW) 
	q.visit(v);
    }

  }

  /** This class implements the <code>Visitor</code> for <code>optimize()</code> */
  static class Visitor extends QuadVisitor {

    /** calling method, class, and <code>this</code> for this quad graph */
    HMethod _method;
    HClass _class;
    HCode _hc;
    harpoon.Analysis.UseDef _usedef;

    /** Temps that hold the <code>this</code> reference, a <code>null</code> constant,
     * a <code>String</code> constant with the name of <code>_method</code>, and a 
     * <code>String</code> constant with the name of <code>_class</code>.
     */
    Temp _this, _null_temp, _calling_method_name_temp, _calling_class_name_temp;

    /** The <code>java.lang.String HClass</code>. */
    HClass _java_lang_string;

    /** The static profiling methods, from <code>harpoon.RunTime.Monitor</code>. */
    HMethod _call_profiling_method;
    HMethod _new_profiling_method;

    /** A static class that logs compile-time information about who might call whom. */
    StaticMonitor _static_monitor;

    /** A hashtable mapping Temps of NEW quads to the CALL <init> quads that initialize them. */
    // Hashtable _temp_init_map;
    Hashtable _temp_visited;

    Visitor(HCode hc) {

      _method = hc.getMethod();
      _class = _method.getDeclaringClass();
      _hc = hc;
      _usedef = new harpoon.Analysis.UseDef();

      _static_monitor = new StaticMonitor();

      // _temp_init_map = new Hashtable();

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
     * If this is a <code>Special &lt;init&gt;</code> call, add it to <code>_temp_init_map</code>.<p>
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

      // NOW if q is a CALL <init> we logNEW

      if (q.isSpecial && q.method.getName().equals("<init>")) {
	// _temp_init_map.put(q.objectref, q);
	// System.err.println("_TEMP_INIT_MAP: "+q.objectref+" --> "+q.toString());
	Temp[] new_parameters = new Temp[5];
	Temp new_t1 = new Temp();
	CONST new_c1 = new CONST(q.getSourceElement(), new_t1,
			     new Integer(q.getLineNumber()), 
 			   HClass.Int);
	new_parameters[1] = _calling_class_name_temp;
	new_parameters[2] = _calling_method_name_temp;
	new_parameters[3] = q.objectref;
	new_parameters[4] = new_t1;
      
	if (_this == null) new_parameters[0] = _null_temp;
	else new_parameters[0] = _this;

	CALL new_profiling_call = new CALL(q.getSourceElement(), _new_profiling_method,
 				     null, new_parameters, null, new Temp(), false);

	Quad.addEdge(new_c1,0,new_profiling_call,0);

	splice(q, new_c1, new_profiling_call);

      }
      
    }

    /** Visit the <code>NEW</code> quad <code>q</code>.<p>
     * Search for the quad initializing the object created in <code>q</code>. After it, insert an <code>int</code> 
     * <code>CONST</code> quad with the line number of 
     * the object creation site, and then a <code>CALL</code> quad invoking 
     * <code>_new_profile_method</code>.
     */
//     public void visit(NEW q) {

//       Temp[] parameters = new Temp[5];
//       Quad search_quad;
//       // CALL call_quad;
//       int no_splices;

//       System.err.print("#");

//       Temp t1 = new Temp();
//       CONST c1 = new CONST(q.getSourceElement(), t1,
// 			   new Integer(q.getLineNumber()), 
// 			   HClass.Int);

//       parameters[1] = _calling_class_name_temp;
//       parameters[2] = _calling_method_name_temp;
//       parameters[3] = q.dst;
//       parameters[4] = t1;
      
//       if (_this == null) parameters[0] = _null_temp;
//       else parameters[0] = _this;

//       CALL profiling_call = new CALL(q.getSourceElement(), _new_profiling_method,
// 				     null, parameters, null, new Temp(), false);

//       Quad.addEdge(c1,0,profiling_call,0);

//       // look for init in _temp_init_map

//       // call_quad = (CALL) _temp_init_map.get(q.dst);
//       _temp_visited = new Hashtable();
//       no_splices = spliceAfterInit(q.dst, c1, profiling_call);
//       // System.err.println("Number of splices for "+q.toString()+": "+no_splices);

//     }
    
    /** find the CALL <init> for the new object in t */
    private int spliceAfterInit(Temp t, Quad q1, Quad q2) {

      if (_temp_visited.get(t) == null) {

	_temp_visited.put(t,t);

      HCodeElement[] uses = _usedef.useMap(_hc, t);
      Quad q;
      Quad q1_clone, q2_clone;
      PHI p;
      SIGMA s;
      int i, j, k, sum;
      for (i = 0; i < uses.length; i++) {
	q = (Quad) uses[i];
	if (q instanceof CALL && ((CALL) q).isSpecial 
	    && ((CALL) q).method.getName().equals("<init>")) {
	  // System.err.println("Splicing after: "+q.toString());
	  q1_clone = (Quad) q1.clone();
	  q2_clone = (Quad) q2.clone();
	  Quad.addEdge(q1_clone,0,q2_clone,0);
	  splice(q, q1_clone, q2_clone);
	  return 1;
	}
      }
      sum = 0;
      for (i = 0; i < uses.length; i++) {
	q = (Quad) uses[i];
	if (q instanceof PHI) {
	  p = (PHI) q;
	  for (j = 0; j < p.src.length; j++) {
	    for (k = 0; k < p.src[j].length; k++) {
	      if (p.src[j][k] == t) {
		// System.err.println("Recurse on PHI: "+q.toString());
		sum = sum + spliceAfterInit(p.dst[j], q1, q2);
	      }
	    }
	  }
	} else if (q instanceof SIGMA) {
	  s = (SIGMA) q;
	  for (j = 0; j < s.src.length; j++)
	    if (s.src[j] == t) {
	      // System.err.println("Recurse on SIGMA: "+q.toString());
	      for (k = 0; k < s.dst[j].length; k++) 
		sum = sum + spliceAfterInit(s.dst[j][k], q1, q2);
	    }
	}
       }
      return sum;
      } else return 0;
    }
      

//       try {

// 	// look for init
      
// 	search_quad = q.next(0);
// 	while (!(search_quad instanceof CALL)) {
// 	  if (search_quad instanceof SIGMA || 
// 	      search_quad instanceof PHI ||
// 	      search_quad instanceof FOOTER) {
// 	    System.err.println("sigma/phi bogosity on quads:");
// 	    System.err.println(q.toString());
// 	    System.err.println(search_quad.toString());
// 	  }
// 	  Util.assert(!(search_quad instanceof SIGMA) && 
// 		      !(search_quad instanceof PHI) &&
// 		      !(search_quad instanceof FOOTER));
// 	  System.err.print(".");
// 	  search_quad = search_quad.next(0);
// 	}
// 	call_quad = (CALL) search_quad;
// 	if (!call_quad.method.getName().equals("<init>")) {
// 	  System.err.println("<init> bogosity on quads: ");
// 	  System.err.println(q.toString());
// 	  System.err.println(call_quad.toString());
// 	}
// 	Util.assert(call_quad.method.getName().equals("<init>"));
	
// 	// splice new quad into CFG
// 	splice(call_quad, c1, profiling_call);
//       } catch (Throwable e) {
// 	System.err.println("caught exception here in NEW");
// 	e.printStackTrace();
//       }


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
