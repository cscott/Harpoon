// Data.java, created Wed Sep  8 16:13:24 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.DATA;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;

import java.util.ArrayList;
/**
 * <code>Data</code> is an abstract superclass with handy useful methods
 * for the <code>harpoon.IR.Tree.Data</code> subclasses in
 * <code>Runtime1</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Data.java,v 1.1.2.4 1999-10-12 20:04:50 cananian Exp $
 */
public class Data extends harpoon.IR.Tree.Data {
    final HClass hc;
    HDataElement root = null;
    
    /** Creates a <code>Data</code>. */
    Data(String desc, HClass hc, Frame f) {
	super(desc, f);
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

    DATA _DATA(Exp e) { 
	return new DATA(tf, null, e); 
    }

    DATA _DATA(Label l) {
	return new DATA(tf,null,new NAME(tf,null,l));
    }
}
