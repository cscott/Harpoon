// Realtime.java, created by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

/**
 * <code>Realtime</code> is the top-level access point for the rest of the Harpoon compiler to
 * provide support for the Realtime Java MemoryArea extensions described in the 
 * <a href="http://java.sun.com/aboutJava/communityprocess/first/jsr001/rtj.pdf">Realtime Java Specification</a>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Realtime {
  /** Is Realtime JAVA support turned on? 
   *
   *  If <code>REALTIME_JAVA</code> == false, 
   *  then all methods in this class have no effect.
   */

  public static boolean REALTIME_JAVA = false;

  /** Provides all of the Object-level support for RTJ.
   * <ul> 
   * <li>Creates a field memoryArea on <code>java.lang.Object<//code>.</li>
   * <li>Converts <code>java.lang.Thread</code> into realtime.RealtimeThread.</li>
   * </ul>
   */
  
  public static void setupObjects(Linker linker) {
    if (REALTIME_JAVA) {
      linker.forName("java.lang.Object").getMutator().addDeclaredField("memoryArea",
                                                                       linker.forName("realtime.MemoryArea"));
      HClass oldThread = linker.forName("java.lang.Thread");
      HClass oldThreadCopy = ((Relinker)linker).createMutableClass("java.lang.ThreadCopy", oldThread);
      HClass newThread = linker.forName("realtime.RealtimeThread");
      ((Relinker)linker).relink(oldThread, newThread);
      newThread.getMutator().setSuperclass(oldThreadCopy);
      //System.out.println(newThread.getName());
      //System.out.println(newThread.getSuperclass().getName());
      //System.out.println(newThread.getSuperclass().getSuperclass().getName());
      Util.assert(!newThread.getName().equals(newThread.getSuperclass().getName()),
                  "RealtimeThread should not inherit from itself.");
    }
  }

  /** Creates an array (and attaches the current memory area to it).
   *
   *  new foo[4] becomes:  
   *  RealtimeThread.currentRealtimeThread().getMemoryArea().newArray(Class.forName("foo"), 4)
   *
   *  new foo[4][5] becomes:
   *  RealtimeThread.currentRealtimeThread().getMemoryArea().newArray(Class.forName("foo", {4, 5}))
   */
  
  public static void newArray() {
    if (REALTIME_JAVA) {


    }
  }

  /** Creates an instance of an object (and attaches the current memory area to it).
   *  
   *  new foo() becomes:
   *  RealtimeThread.currentRealtimeThread().getMemoryArea().newInstance(Class.forName("foo"));
   *
   */

  public static void newObject() {
    if (REALTIME_JAVA) {

    }
  }
  
  /** Optionally adds a MemoryArea access check to loads, if the analysis indicates
   *  that the check is really needed.
   *
   *  foo=bar becomes:
   *  foo.memoryArea.checkAccess(bar)
   *  foo=bar
   */

  public static void checkAccess() {
    if (needsCheck()) {
      
    }
  }

  /** Runs pointer analysis */

  public static void runAnalysis() {
    if (REALTIME_JAVA) {

    }
  }

  /** Indicates if the given instruction needs an access check wrapped around it. */

  public static boolean needsCheck() {
    if (REALTIME_JAVA) {
      return true;
    }
    return false;
  }


}
