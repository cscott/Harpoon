// InlineMap.java, created Thu Jan 14 22:17:20 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HField;

/**
 * An <code>InlineMap</code> maps an <code>HField</code> to a boolean value 
 * indicated whether the <code>HField</code> can be inlined.
 * We leave the policy decision of whether it is wise to do so to other
 * code, which will also be an InlineMap.  So, the idea is that a core
 * InlineMap implements the conservative analysis of inline-safety, then
 * a second inline map takes the first inline map and twiddles the inlining
 * results according to whether or not it thinks inlining is really a good
 * idea, using some heuristic.  Or you can leave out the second inline map
 * and its heuristics, which just means you'd like to inline wherever
 * possible.  Have I confused everyone yet?
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InlineMap.java,v 1.1.2.6 2001-06-17 22:32:27 cananian Exp $
 */
public abstract class InlineMap // may need to be an interface later, but
                                // we'll try to keep it a class if we can.
{
  /**
   * @return <code>true</code> if the <code>HField</code> can be inlined
   * using type 1 inlining (the class descriptor for the inlined object is
   * preserved). This can be done even if the field escapes, as long as it
   * is a final field.
   */
  public abstract boolean canInline1(HField hf);
  /**
   * @return <code>true</code> if the <code>HField</code> should be inlined
   * using type 2 inlining (the class descriptor for the inlined object is
   * omitted). This can only be done if the field doesn't escape.
   * Returning <code>true</code> implies shouldInline1() returns
   * <code>true</code>, too.
   */
  public abstract boolean canInline2(HField hf);
}



