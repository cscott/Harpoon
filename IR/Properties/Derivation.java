// Derivation.java, created Fri Jan 22 16:46:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>Derivation</code> provides a means to access the derivation
 * of a particular derived pointer.  Given a compiler temporary, it
 * will enumerate the base pointers and signs needed to allow proper
 * garbage collection of the derived pointer.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Derivation.java,v 1.1.2.7 1999-06-24 01:04:41 cananian Exp $
 */
public interface Derivation  {

    /** Map compiler temporaries to their derivations.
     * @return <code>null</code> if the temporary has no derivation (is
     *         a base pointer, for example), or the derivation otherwise.
     */
    public DList derivation(HCodeElement hce, Temp t);

    /** Structure of the derivation information. */
    public class DList {
	/** Base pointer. */
	public final Temp base;
	/** Sign of base pointer.  <code>true</code> if derived pointer
	 *  equals offset <b>+</b> base pointer, <code>false</code> if
	 *  derived pointer equals offset <b>-</b> base pointer. */
	public final boolean sign;
	/** Pointer to a continuation of the derivation, or <Code>null</code>
	 *  if there are no more base pointers associated with this derived
	 *  pointer. */
	public final DList next;
	/** Constructor. */
	public DList(Temp base, boolean sign, DList next) {
	    this.base = base; this.sign = sign; this.next = next;
	}
      
      /* Like <code>Quad.clone()</code>, does not clone
       * <code>Temp</code>s when not supplied with a
       * <code>TempMap</code>. */
      public static DList clone(DList dlist) {
	if (dlist==null) return null;
	else return new DList(dlist.base, dlist.sign, clone(dlist.next));
      }

      /** Returns a clone of this <code>DList</code> */
      public static DList clone(DList dlist, CloningTempMap ctm) {
	if (dlist==null) return null;
	else
	  return new DList(((dlist.base==null)?null:ctm.tempMap(dlist.base)),
			   dlist.sign, 
			   clone(dlist.next, ctm));
      }

      /** Returns a new <code>DList</code> with the <code>Temp</code>s 
       *  renamed by the supplied mapping */
      public static DList rename(DList dlist, TempMap tempMap)
	{
	  if (dlist==null) return null;
	  else return new DList
		 (((dlist.base==null)?null:tempMap.tempMap(dlist.base)),
		  dlist.sign,
		  rename(dlist.next, tempMap));
	}
    }
}
