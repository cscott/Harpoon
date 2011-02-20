// UniqueName.java, created Mon Jan 10 22:00:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.util.HashMap;
import java.util.Map;
/**
 * <code>UniqueName</code> contains methods to create unique class,
 * field, or method names.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UniqueName.java,v 1.2 2002-02-25 21:03:04 cananian Exp $
 */
public abstract class UniqueName {
  private final static Map suffixMap = new HashMap(); // efficiency hack.

  /** Make a unique class name from a given suggestion.
   *  The suggestion string may be null or empty.
   */
  public static String uniqueClassName(String suggestion, Linker context) {
    if (suggestion==null || suggestion.equals("")) suggestion="FLEXc";
    // remove trailing dollar-signs.
    while (suggestion.charAt(suggestion.length()-1)=='$')
      suggestion = suggestion.substring(0, suggestion.length()-1);
    // remove anything after a double dollar sign.
    if (suggestion.indexOf("$$")!=-1)
      suggestion = suggestion.substring(0, suggestion.lastIndexOf("$$"));
    // try unadorned name & return it if unique.
    try { context.forName(suggestion); }
    catch (NoSuchClassException e) { return suggestion; }
    // find lowest unique number for class.
    // the goal here is determinism.  the suffixMap makes it efficient.
    Integer lastsuffix = (Integer) suffixMap.get(suggestion);
    for (int i=(lastsuffix==null)?0:(lastsuffix.intValue()+1); true; i++) {
      String className = suggestion + "$$" + i;
      try { context.forName(className); }
      catch (NoSuchClassException e) {
	suffixMap.put(suggestion, new Integer(i));
	return className;
      }
    }
  }
  
  /** Make a unique field name from a given suggestion.
   *  The suggestion string may be null or empty.
   */
  public static String uniqueFieldName(String suggestion, HClass context)
  {
    if (suggestion==null || suggestion.equals("")) suggestion="FLEXm";
    // remove trailing dollar-signs.
    while (suggestion.charAt(suggestion.length()-1)=='$')
      suggestion = suggestion.substring(0, suggestion.length()-1);
    // remove anything after a double dollar sign.
    if (suggestion.indexOf("$$")!=-1)
      suggestion = suggestion.substring(0, suggestion.lastIndexOf("$$"));
    // find lowest unique number for method.
  L1:
    for (int i=-1; true; i++) {
      String fieldname = (i<0)?suggestion:(suggestion+"$$"+i);
      // search class for existing method.
      HField[] hf = context.getDeclaredFields();
      for (int j=0; j<hf.length; j++)
	if (hf[j].getName().equals(fieldname))
	  continue L1;
      // found a valid name.
      return fieldname;
    }
  }
  
  /** Make a unique method name from a given suggestion.
   *  The suggestion string may be null or empty.
   */
  public static String uniqueMethodName(String suggestion, HClass context)
  {
    if (suggestion==null || suggestion.equals("")) suggestion="FLEXm";
    // remove trailing dollar-signs.
    while (suggestion.charAt(suggestion.length()-1)=='$')
      suggestion = suggestion.substring(0, suggestion.length()-1);
    // remove anything after a double dollar sign.
    if (suggestion.indexOf("$$")!=-1)
      suggestion = suggestion.substring(0, suggestion.lastIndexOf("$$"));
    // find lowest unique number for method.
  L1:
    for (int i=-1; true; i++) {
      String methodname = (i<0)?suggestion:(suggestion+"$$"+i);
      // search class for existing method.
      HMethod[] hm = context.getDeclaredMethods();
      for (int j=0; j<hm.length; j++)
	if (hm[j].getName().equals(methodname)
	    /* && hm[j].getDescriptor().equals(descriptor)*/)
	  continue L1;
      // found a valid name.
      return methodname;
    }
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
