// Derivation.java, created Fri Jan 22 16:46:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;

/**
 * <code>Derivation</code> provides a mean to access the derivation
 * of a particular derived pointer.  Given a compiler temporary, it
 * will enumerate the base pointers and signs needed to allow proper
 * garbage collection of the derived pointer.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Derivation.java,v 1.1.2.2 1999-02-06 22:39:22 duncan Exp $
 */
public interface Derivation  {

    /** Map compiler temporaries to their derivations.
     * @return <code>null</code> if the temporary has no derivation (is
     *         a base pointer, for example), or the derivation otherwise.
     */
    public DList derivation(Temp t);

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
      
      /** Returns a clone of this <code>DList</code> */
      public DList clone(CloningTempMap ctm) {
	return new DList(((this.base==null)?null:ctm.tempMap(this.base)),
			 this.sign, 
			 ((this.next==null)?null:this.next.clone(ctm)));
      }
    }
}
