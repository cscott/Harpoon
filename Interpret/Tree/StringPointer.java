package harpoon.Interpret.Tree;

import harpoon.Temp.Label;
import harpoon.Util.Tuple;

/**
 * The <code>StringPointer</code> class represents a pointer to an 
 * string constant.  This pointer can be dereferenced 
 * with <code>getValue()</code>.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: StringPointer.java,v 1.1.2.2 1999-05-10 00:01:17 duncan Exp $
 */
public class StringPointer extends Pointer {
    private StaticState ss;
    private final Object value;
    
    /** Class constructor. */
    StringPointer(StaticState ss, Label base) {
	super(new Object[] { base });
	this.ss = ss;
        this.value = Method.toNonNativeFormat
	    (ss.makeStringIntern
	     ((String)ss.map.decodeLabel((Label)getBase())));
    }

    /** Throws an error, as <code>StringPointer</code>s are constant. */
    public Pointer add(long offset) {
	throw new Error("Can't add to a String pointer");
    }

    /** Returns true if <code>obj</code> is a <code>StringPointer</code>
     *  that points to the same location as this <code>StringPointer</code>.
     */
    public boolean equals(Object obj) { 
	if (!(obj instanceof StringPointer)) return false;
	else { 
	    StringPointer ptr = (StringPointer)obj;
	    return ptr.getBase()==getBase();
	}
    }

    /** Returns a <code>Label</code> representing the base of this
     *  <code>StringPointer</code>  */
    public Object getBase() { 
	return proj(0);
    }

    /** Returns the offset of this <code>StringPointer</code>. */
    public long getOffset() { 
	throw new Error("String pointers have no offsets");
    }

    /** Returns the value obtained by dereferencing this 
     *  <code>StringPointer</code>.  This value is in non-native format.
     */
    public Object getValue() { 
	return this.value;
    }

    /** Always returns true. */
    public boolean isConst() { return true; }

    /** Always returns false. */
    public boolean isDerived() { return false; }

    /** Throws an error, as <code>StringPointer</code>s are constant. */
    public void updateValue(Object value) { 
	throw new Error("Can't update String pointer");
    }
  
    public String toString() { 
	return "StringPointer --> < " + value.toString() + " >";
    }
    
}




