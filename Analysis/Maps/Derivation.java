// Derivation.java, created Fri Jan 22 16:46:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
/**
 * <code>Derivation</code> provides a means to access the derivation
 * of a particular derived pointer.  Given a compiler temporary, it
 * will enumerate the base pointers and signs needed to allow proper
 * garbage collection of the derived pointer.<p>
 * See Diwan, Moss, and Hudson, <A
 * HREF="http://www.acm.org/pubs/citations/proceedings/pldi/143095/p273-diwan/"
 * >"Compiler Support for Garbage Collection in a Statically Typed
 * Language"</A> in PLDI'92 for background on the derivation structure
 * and its motivations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Derivation.java,v 1.5 2002-09-02 19:23:26 cananian Exp $
 */
public interface Derivation<HCE extends HCodeElement>
    extends harpoon.Analysis.Maps.TypeMap<HCE> {

    /** Map compiler temporaries to their derivations.
     * @param t The temporary to query.
     * @param hce A definition point for <code>t</code>.
     * @return <code>null</code> if the temporary has no derivation (is
     *         a base pointer, for example), or the derivation otherwise.
     * @exception NullPointerException if <code>t</code> or <code>hc</code>
     *            is <code>null</code>.
     * @exception TypeNotKnownException if the <code>Derivation</code>
     *            has no information about <code>t</code> as defined
     *            at <code>hce</code>.
     */
    public DList derivation(HCE hce, Temp t)
	throws TypeMap.TypeNotKnownException;

    /** Structure of the derivation information.<p>
     *  Given three <code>Temp</code>s <code>t1</code>, <code>t2</code>,
     *  and <code>t3</code>, a derived pointer <code>d</code> whose value
     *  can be described by the equation:<pre>
     *   d = K + t1 - t2 + t3
     *  </pre> for some (non-constant) integer K at every point during
     *  runtime can be represented as the <code>DList</code><pre>
     *   new DList(t1, true, new DList(t2, false, new DList(t3, true)))
     *  </pre>.<p>
     * <b>NOTE</b> that the temporaries named in the <code>DList</code>
     * refer to the <i>reaching definitions</i> of those temporaries at
     * the <i>definition point</i> of the variable with the derived
     * type.  In SSI/SSA forms, this does not matter, as every variable
     * has exactly one reaching definition, but in other forms <b>it
     * is the responsibility of the implementor</b> to ensure that the
     * base pointers are not overwritten while the derived value is
     * live.
     * <p><b>ALSO NOTE</b> that the temporaries named in the
     * <code>DList</code> are <i>base pointers</i> -- that is,
     * they have pure types, <i>not derived types</i>.  In particular,
     * the <code>derivation()</code> method applied to any temporary
     * named in a <code>DList</code> should return <code>null</code>.
     * Derived types derived from other derived types are not allowed.
     */
    public static class DList {
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
	/** Specifies whether this <code>DList</code> is in canonical form.
	 */
	private final boolean canonical;
	/** Constructor. */
	public DList(Temp base, boolean sign, DList next) {
	    this.base = base; this.sign = sign; this.next = next;
	    // this is the rule for canonicalization:
	    this.canonical = (next == null) ||
		(next.canonical && base==next.base && sign==next.sign) ||
		(next.canonical && base.compareTo(next.base) < 0);
	    assert base!=null : "Null base pointer in DList.";
	}
      
	/** Returns a human-readable description of this <code>DList</code>. */
	public String toString() {
	    return "<k>"+toString(canonicalize());
	}
	// helper function for toString().
	private static String toString(DList dl) {
	    if (dl==null) return "";
	    else return (dl.sign?"+":"-") + dl.base + toString(dl.next);
	}
	/** Equality test.  Compares the canonical forms of the
	 *  <code>DList</code>s for strict equality. */
	public boolean equals(Object o) {
	    if (!canonical) return canonicalize().equals(o);
	    if (!(o instanceof DList)) return false;
	    DList dl = (DList) o;
	    if (!dl.canonical) return this.equals(dl.canonicalize());
	    if (this.base != dl.base) return false;
	    if (this.sign != dl.sign) return false;
	    if (this.next == null || dl.next == null)
		return this.next == dl.next;
	    else return this.next.equals(dl.next);
	}
	/** Canonicalize a <code>DList</code>.  The canonicalized
	 *  form of a <code>DList</code> has all components sorted
	 *  by <code>Temp</code> (using the natural ordering of
	 *  <code>Temp</code>s), and is algebraically simplified
	 *  --- that is, components with the same <code>Temp</code>
	 *  and opposite signs cancel out. */
	public DList canonicalize() {
	    if (canonical) return this;
	    List<DList> l = new ArrayList<DList>();
	    for (DList dl=this; dl!=null; dl=dl.next)
		l.add(dl);
	    Collections.sort(l, new Comparator<DList> () {
		// reverse order by dl.temp natural ordering.
		public int compare(DList dl1, DList dl2) {
		    int order = dl1.base.compareTo(dl2.base);
		    if (order==0) order = (dl1.sign?1:-1) - (dl2.sign?1:-1);
		    return -order; // reverse
		}
	    });
	    DList result = null;
	    for (Iterator<DList> it=l.iterator(); it.hasNext(); it.next()) {
		DList dl = it.next();
		if (result!=null &&
		    result.base == dl.base &&
		    result.sign != dl.sign)
		    result = result.next; // cancel out component.
		else
		    result = new DList(dl.base, dl.sign, result);
	    }
	    assert result!=null && result.canonical;
	    return result;
	}

      /** Returns a clean copy of this <code>DList</code>.  Does
       *  not rename <code>Temp</code>s in any way. */
      public static DList clone(DList dlist) {
	if (dlist==null) return null;
	else return new DList(dlist.base, dlist.sign, clone(dlist.next));
      }

      /** Returns a new <code>DList</code> with the <code>Temp</code>s 
       *  renamed by the supplied mapping */
      public static DList rename(DList dlist, TempMap tempMap)
	{
	  if (dlist==null) return null;
	  else return new DList
		 ( tempMap==null ? dlist.base : tempMap.tempMap(dlist.base),
		  dlist.sign,
		  rename(dlist.next, tempMap));
	}
    }
}
