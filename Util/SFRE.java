// SFRE.java, created Wed Oct 21 17:29:49 1998 by marinov
package harpoon.Util;

import harpoon.ClassFile.HField;

/**
 * <code>SFRE</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SFRE.java,v 1.1.2.2 1999-01-22 23:34:00 cananian Exp $
 */
public class SFRE  {
    
    boolean epsilon; // true if empty string is in the set
    HField field;    // if non-null then contains the field
    boolean anyPlus; // true if any field can be accessed any number of times

    /** Creates an empty <code>SFRE</code>. */
    public SFRE() { epsilon = true; field = null; anyPlus = false; }

    public SFRE concat(HField f) {
	SFRE r = new SFRE();
	r.epsilon = false;
	if (epsilon) r.field = f;
	if ((field!=null) || anyPlus) r.anyPlus = true;
	return r;
    }
    public boolean equals(Object o) {
	if (!(o instanceof SFRE)) return false;
	SFRE s = (SFRE)o;
	if (epsilon!=s.epsilon) return false;
	if (anyPlus) return s.anyPlus;
	if (s.anyPlus) return false;
	return field==s.field;
    }
    public SFRE copy() {
	SFRE r = new SFRE();
	r.epsilon = epsilon;
	r.field = field;
	r.anyPlus = anyPlus;
	return r;
    }
    public SFRE union(SFRE s) {
	SFRE r = new SFRE();
	r.epsilon = epsilon || s.epsilon;
	if (field==null) 
	    r.field = s.field;
	else {
	    if (s.field==null)
		r.field = field;
	    else
		r.anyPlus = true;
	}
	r.anyPlus = r.anyPlus || anyPlus || s.anyPlus;
	return r;
    }
    /** Returns a string representation of this SFRE.
     *  @return a string representation of this SFRE. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	if ((field!=null)||anyPlus) {
	    sb.append(epsilon?"[":"{");
	    if (anyPlus)
		sb.append("any+");
	    else if (field!=null)
		sb.append(field.getName());
	    sb.append(epsilon?"]":"}");
	}
	return sb.toString();
    }
}
