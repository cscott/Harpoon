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
import harpoon.Temp.Label;

import java.util.ArrayList;
/**
 * <code>Data</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Data.java,v 1.1.2.1 1999-09-09 05:12:17 cananian Exp $
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

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                    Utility methods                       *
     *                                                          *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    void add(int index, Tree elem, ArrayList up, ArrayList down) { 
	int size, requiredSize;
	if (index<0) { 
	    requiredSize = (-index);
	    if (requiredSize > up.size()) { 
		up.ensureCapacity(requiredSize);
		for (int i=up.size(); i<requiredSize; i++) 
		    up.add(new DATA(elem.getFactory(), elem));
	    }	    
	    up.set(-index-1, elem);
	}
	else {
	    requiredSize = index+1;
	    if (requiredSize > down.size()) { 
		down.ensureCapacity(requiredSize);
		for (int i=down.size(); i<requiredSize; i++) 
		    down.add(new DATA(elem.getFactory(), elem));
	    }	    
	    down.set(index, elem);
	}
    }
  
    DATA _DATA(Exp e) { 
	return new DATA(tf, null, e); 
    }

    DATA _DATA(Label l) {
	return new DATA(tf,null,new NAME(tf,null,l));
    }

    int wordsize() { return frame.pointersAreLong() ? 8 : 4; }
}
