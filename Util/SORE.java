// SORE.java, created Tue Oct 20 22:41:52 1998 by marinov
package harpoon.Util;

import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>SORE</code> represents class of "simple object regular expressions."
 * Basically, sore's represent "values" that a pointer may have during execution.
 * In the simplest form, SORE's are sets consisting of:
 * temp (indicating that a pointer has value of variable temp)
 * temp.field (pointer points to temp.field, where field is an exact field)
 * temp."any+" (pointer may point to whatever object is reachable from temp
 *              following the chain of pointer fileds; e.g. this.next.next
 *              or p.right.left.right or p(.next|.pred)+ etc.
 * "new" (special value to mark pointers that may point to newly allocated objects)
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SORE.java,v 1.1.2.3 1999-06-18 01:48:10 cananian Exp $
 */

public class SORE  {
    
    Hashtable h;

    public static final SORE emptySet;
    public static final SORE singletonNew;
    static { emptySet = new SORE(); singletonNew = new SORE(); singletonNew.h.put("new", "new"); }

    /** Creates an <code>SORE</code>. */
    public SORE(Temp t) { h = new Hashtable(); h.put(t, new SFRE()); }
    public SORE() { h = new Hashtable(); }

    // concatenate field f to sore's
    public SORE field(HField f) { 
	SORE r = new SORE();
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    if (o=="new") { r.h.put("new", "new"); continue; }
	    r.h.put(o, ((SFRE)h.get(o)).concat(f));
	}
	return r;
    }

    // returns true if sore's are equal
    public boolean equals(Object o) {
	SORE s;
	if (this==o) return true;
	if (null==o) return false;
	try {s = (SORE)o;} catch (ClassCastException e) { return false; }
	if (h.size()!=s.h.size()) return false;
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    Object k = e.nextElement();
	    if (!s.h.containsKey(k)) return false;
	    if (k=="new") continue;
	    if (!h.get(k).equals((SFRE)s.h.get(k))) return false;
	}   
	return true;
    }
    
    // clones the sore (the cloning is "deep" except for Temps are not cloned)
    public SORE copy() { 
	SORE r = new SORE();
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    if (o=="new") { r.h.put("new", "new"); continue; }
	    r.h.put(o, ((SFRE)h.get(o)).copy());
	}
	return r;
    }

    // add sore's from s to already existing
    public void union(SORE s) {
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    if (o=="new") continue;
	    SFRE f = (SFRE)s.h.get(o);
	    if (f!=null)
		h.put(o, ((SFRE)h.get(o)).union(f));
	}
	for (Enumeration e=s.h.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    if (!h.containsKey(o))
		h.put(o, s.h.get(o));
	}
    }

    /** Returns a string representation of this SORE.
     *  @return a string representation of this SORE. */
    public String toString() {
	StringBuffer sb = new StringBuffer("{");
	for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
	    //sb.append(e.nextElement().toString());
	    Object o = e.nextElement();
	    if (o=="new")
		sb.append("new");
	    else {
		sb.append(((Temp)o).name());
		sb.append(h.get(o).toString());
	    }
	    if (e.hasMoreElements())
		sb.append(", ");
	}
	sb.append("}");
	return sb.toString();
    }

} // class SORE
