// PrintFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

import java.io.PrintWriter;

/**
 * <code>PrintFactory</code> allows you to print the code as it's being converted
 * for debugging purposes only.
 * <code>new PrintFactory(new Foo(new PrintFactory(parent, "BEFORE")), "AFTER")</code>
 * will produce an <code>HCodeFactory</code> which will print before and after
 * code for the transformation <code>Foo</code>
 *
 * It doesn't care what type of HCodeFactory it's given.
 */
public class PrintFactory implements HCodeFactory {
    private HCodeFactory parent;
    private String comment;

    /** Construct a <code>PrintFactory</code> that will label the code produced
     *  by <code>parent</code> with <code>comment</code>.
     */
    public PrintFactory(HCodeFactory parent, String comment) {
	this.parent = parent;
	this.comment = comment;
    }

    /* Convert <code>m</code>, printing it's code. 
     */
    public HCode convert(HMethod m) {
	HCode code = parent.convert(m);
	if (code != null) {
	    System.out.println();
	    System.out.println("------------ " + comment + " -------------");
	    code.print(new PrintWriter(System.out));
	    System.out.println("------------------------------------------");
	}
	return code;
    }
    
    /* The codename of this <code>PrintFactory</code> is whatever the 
     * codename of <code>parent</code> is.
     */
    public String getCodeName() { return parent.getCodeName(); }

    /* Pass on <code>clear</code> to parent.
     */
    public void clear(HMethod m) { parent.clear(m); }
}
