// -*-Mode: Java-*- 
// Monitor.java -- Static class that logs profiling info.
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Tue Oct  6 11:24:14 1998> 
// Time-stamp: <1998-11-28 13:07:55 mfoltz> 
// Keywords: 

package harpoon.RunTime;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version 
 */

public class Monitor {

  static Properties _properties = new Properties();
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

  private Monitor() { }

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

