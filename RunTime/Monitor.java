// -*-Mode: Java-*- 
// Monitor.java -- Static class that logs profiling info.
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Tue Oct  6 11:24:14 1998> 
// Time-stamp: <1998-12-02 22:10:02 mfoltz> 
// Keywords: 

package harpoon.RunTime;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * <code>Monitor</code> is the static class whose methods are called at run-time by
 * applications profiled for object partitioning.<p>
 * This class has a static initializer that 
 * reads a property file <code>Monitor.properties</code> containing the property <code>dynamicfile</code>, 
 * the filename for profile data.  It also invokes <code>System.runFinalizersOnExit(true)</code> 
 * to ensure that its finalizer is run on JVM exit.<p>
 * The finalizer flushes and closes <code>_logstream</code>, throwing <code>IOException</code> if 
 * if an input or output exception occurs.
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version 
 */

public class Monitor {

  /** Monitor's properties. */
  static Properties _properties = new Properties();
  /** The output stream for profiling data. */
  static DataOutputStream _logstream;

  static {
    try {
      System.runFinalizersOnExit(true);
      _properties.load(new FileInputStream("Monitor.properties"));
      _logstream = new DataOutputStream(new FileOutputStream(_properties.getProperty("dynamicfile")));
    } catch (Exception e) { 
      e.printStackTrace();
    }
  }

  /** Hide the constructor. */
  private Monitor() { }

  /** Logs a method call.
   * Writes a string of the form <code>CALL sender-id sender-method receiver-id receiver-method</code>
   * to <code>_logstream</code>. <code>sender-id</code> and <code>receiver-id</code> are strings unique among 
   * all objects in the JVM.<p
   * If <code>sender</code> or <code>receiver</code> is <code>null</code>, the string "-1" is used as the id.  If 
   * <code>sending_method</code> or <code>receiving_method</code> is null, then the string <code>UnknownMethod</code>
   * is used as the method name.
   * 
   * @param <code>sender</code>            The object making the method call.
   * @param <code>sending_method</code>    The name of the method with the call site.
   * @param <code>receiver</code>          The object on which the method is invoked.
   * @param <code>receiving_method</code>  The name of the method invoked.
   */
  public static synchronized void logCALL(Object sender, String sending_method, 
			     Object receiver, String receiving_method) {
    try {

      int sender_id, receiver_id;

      if (sender == null) sender_id = -1;
      else sender_id = System.identityHashCode(sender);

      if (receiver == null) receiver_id = -1;
      else receiver_id = System.identityHashCode(receiver);
      
      if (sending_method == null) sending_method = "UnknownMethod";
      if (receiving_method == null) receiving_method = "UnknownMethod";

      _logstream.writeBytes("CALL "+sender_id+" "+sending_method+" "+
			    receiver_id+" "+receiving_method+"\n");
    } catch (Throwable e) { 
      e.printStackTrace();
    }
  }

  /** Logs object creation.
   * Writes a string of the form 
   * <code>NEW creator-id creator-method creator-class created-id created-class site-id</code>
   * to <code>_logstream</code>. <code>creator</code>-id and <code>created-id</code> are strings 
   * unique among all objects in the JVM.<p>
   * If <code>creator</code> or <code>created</code> is null, the string <code>-1</code> is used as 
   * the <code>creator-id</code> or <code>created-id</code>, respectively.  If 
   * <code>sending_method</code> or <code>receiving_method</code> is null, then the string <code>UnknownMethod</code>
   * is used as the method name.  If <code>created</code> is null, then <code>Static</code> is used as the 
   * <code>created-class</code>.<p>
   * <code>site-id</code> is unique among all object creation sites within <code>creator-method</code>.
   * 
   * @param <code>creator</code>           The object creating the new object.
   * @param <code>creator_class</code>     The name of the class with the object creation site.
   * @param <code>creator_method</code>    The name of the method with the object creation site.
   * @param <code>created</code>           The object being created.  Must be initialized.
   * @param <code>id</code>                The identifier of the object creation site within <code>creator_method</code>.
   */
  public static synchronized void logNEW(Object creator, String creator_class, 
			    String creator_method, Object created, int id) {
    try {

      int creator_id, created_id;
      String created_class;

      if (creator == null) {
	creator_id = -1;
	// creator_class = "Static";
      } else {
	creator_id = System.identityHashCode(creator);
	// creator_class = creator.getClass().getName();
      }

      if (created == null) {
	created_id = -1;
	created_class = "Static";
      } else {
	created_id = System.identityHashCode(created);
	created_class = created.getClass().getName();
      }

      if (creator_class == null) creator_class = "UnknownClass";
      if (creator_method == null) creator_method = "UnknownMethod";
      if (created_class == null) created_class = "UnknownClass";

      _logstream.writeBytes("NEW "+creator_id+" "+creator_method+" "+creator_class+" "+
			    created_id+" "+created_class+" "+id+"\n");

    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  static void classFinalizer() throws Throwable {
    _logstream.flush();
    _logstream.close();
  }

}

