// Data.java, created Wed Sep  8 16:13:24 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>Data</code> is an abstract superclass with handy useful methods
 * for the <code>harpoon.IR.Tree.Data</code> subclasses in
 * <code>Runtime1</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Data.java,v 1.2.2.1 2002-03-10 23:16:34 cananian Exp $
 */
public class Data extends harpoon.IR.Tree.Data {
    final Linker linker;
    final HClass hc;
    // All subclasses should be able to set this.
    protected HDataElement root = null;
    
    /** Creates a <code>Data</code>. */
    protected Data(String desc, HClass hc, Frame f) {
	super(desc, f);
	this.linker = f.getLinker();
        this.hc = hc;
    }
    public HClass getHClass() { return hc; }
    public HDataElement getRootElement() { return root; }

    public int hashCode() { return hc.hashCode() ^ desc.hashCode(); }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                    Utility methods                       *
     *                                                          *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    protected DATUM _DATUM(Exp e) { 
	return new DATUM(tf, null, e); 
    }

    protected DATUM _DATUM(Label l) {
	return new DATUM(tf,null,new NAME(tf,null,l));
    }

    // UTF-8 handling methods -----------------------------------

    /** Make a java-style UTF-8 encoded byte array for a string. */
    public static byte[] toUTF8(String str) {
	// first count how many bytes we'll need.
	int len=0;
	for (int i=0; i<str.length(); i++) {
	    int c = (int) str.charAt(i);
	    if (c >= 0x0001 && c <= 0x007f) len+=1; // one byte format
	    else if (c == 0 || (c >= 0x0080 && c <= 0x07FF)) len+=2; //two byte
	    else len += 3; // three byte format.
	}
	// allocate byte array for result.
	byte[] r = new byte[len];
	// now make the UTF-8 encoding.
	len=0;
	for (int i=0; i<str.length(); i++) {
	    int c = (int) str.charAt(i);
	    if (c >= 0x0001 && c <= 0x007f) { // one byte format
		r[len++] = (byte) c;
	    } else if (c == 0 || (c >= 0x0080 && c <= 0x07FF)) { // two byte
		r[len++] = (byte) (0xC0 | (c>>>6));
		r[len++] = (byte) (0x80 | (c & 0x3F));
	    } else { // three byte format
		r[len++] = (byte) (0xE0 | (c>>>12));
		r[len++] = (byte) (0x80 | ((c>>>6) & 0x3F));
		r[len++] = (byte) (0x80 | (c & 0x3F));
	    }
	}
	// okay, done.
	return r;
    }

    /* Return a <code>Stm</code> representing a string constant in UTF-8
     * encoded form. */
    protected Stm emitUtf8String(String str) {
	byte[] bytes = toUTF8(str);
	List stmlist = new ArrayList(bytes.length+1);
	for (int i=0; i<bytes.length; i++)
	    stmlist.add(_DATUM(new CONST(tf, null, 8, false,
					((int)bytes[i])&0xFF)));
	// null-terminate the string.
	stmlist.add(_DATUM(new CONST(tf, null, 8, false, 0)));
	return Stm.toStm(stmlist);
    }
}
