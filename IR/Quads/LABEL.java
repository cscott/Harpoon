// LABEL.java, created Fri Dec 11 05:59:31 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>LABEL</code> marks a basic-block entrance.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LABEL.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
public class LABEL extends PHI {
    /** the (optional) name of this label. <p>
     *  <code>null</code> if the label has no name. */
    protected String label;

    /** Creates a <code>LABEL</code> representing the entrance to a basic
     *  block.
     * @param label the name of this label, or <code>null</code> for no name.
     */
    public LABEL(QuadFactory qf, HCodeElement source, String label,
		 Temp dst[], Temp src[][], int arity) {
	super(qf, source, dst, src, arity);
	this.label = label;
    }
    public LABEL(QuadFactory qf, HCodeElement source, String label,
		 Temp dst[], int arity) {
	super(qf, source, dst, arity);
	this.label = label;
    }
    /** Creates a <code>LABEL</code> to replace a <code>PHI</code>.
     * @param label the name of this label, or <code>null</code> for no name.
     */
    public LABEL(QuadFactory qf, PHI phi, String label) {
	this(qf, phi, label, phi.dst, phi.src, phi.arity());
	// SHOULD I REWRITE EDGES in this constructor?
    }
    /** Returns the optional name of this label, or <code>null</code> if
     *  this label has no name. */
    public String label() { return label; }
    
    public int kind() { return QuadKind.LABEL; }
    
    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new LABEL(qqf, this,
			 label, map(defMap,dst), map(useMap,src), arity());
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("LABEL("+prev.length+"): ");
	for (int i=0; i<dst.length; i++) {
	    sb.append(dst[i].toString() + "=(");
	    for (int j=0; j<src[i].length; j++) {
		if (src[i][j]==null)
		    sb.append("null");
		else
		    sb.append(src[i][j].toString());
		if (j < src[i].length-1)
		    sb.append(",");
	    }
	    sb.append(")");
	    if (i < dst.length-1)
		sb.append("; ");
	}
	return sb.toString();
    }
}
