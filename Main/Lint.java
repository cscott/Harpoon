// Lint.java, created Sun Sep 12 09:49:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.Temp.Temp;

import java.util.Iterator;
/**
 * <code>Lint</code> is a quick Java code-checker.<p>
 * Currently it checks only two things:<ul>
 * <li>Uses of <code>Label</code><code>.toString()</code> (which should
 *     almost always be <code>Label.name</code> instead), and
 * <li>Testing objects for equality using <code>==</code> (instead of
 *     <code>equals()</code>).
 * </ul><p>
 * Invoke with:<pre>
 * java harpoon.Main.Lint [package1] [package2] ...
 * </pre><p>
 * Output is a list of source files and line numbers which are being
 * flagged as possibly incorrect.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Lint.java,v 1.1.2.2 1999-09-12 16:38:38 cananian Exp $
 */
public abstract class Lint extends harpoon.IR.Registration {
    public static void main(String[] args) {
	// Command-line should have list of packages
	if (args.length==0) {
	    System.err.println("Usage: java "+Lint.class.getName()+" "+
			       "[package] {packages} ...");
	    System.exit(1);
	}
	HCodeFactory hcf = harpoon.IR.Quads.QuadSSI.codeFactory();
	hcf = new CheckLabels(hcf); // check uses of Label.toString()
	hcf = new CheckEquals(hcf); // check usage of == for objects.
	for (int i=0; i<args.length; i++) {
	    System.err.println("CHECKING PACKAGE "+args[i]);
	    for (Iterator it=Loader.listClasses(args[i]); it.hasNext(); ) {
		HClass hc = HClass.forName((String)it.next());
		System.err.println(" - " + hc);
		HMethod[] hms = hc.getDeclaredMethods();
		for (int j=0; j<hms.length; j++) {
		    hcf.convert(hms[j]);
		    hcf.clear(hms[j]); // free memory.
		}
	    }
	}
    }
    /** Find all places where Label.toString() is called. */
    static class CheckLabels implements HCodeFactory {
	final HCodeFactory hcf;
	CheckLabels(HCodeFactory hcf) { this.hcf = hcf; }
	public void clear(HMethod hm) { hcf.clear(hm); }
	public String getCodeName() { return hcf.getCodeName(); }
	public HCode convert(HMethod hm) {
	    HCode c = hcf.convert(hm);
	    if (c==null) return c;
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		harpoon.IR.Quads.Quad q = (harpoon.IR.Quads.Quad) it.next();
		if (q instanceof harpoon.IR.Quads.CALL &&
		    ((harpoon.IR.Quads.CALL)q).method().equals(label_toString))
		    System.out.println("A\t"+q.getSourceFile()+"\t"+q.getLineNumber()+"\t"+hm.getDeclaringClass().getName()+"\t"+hm.getName());
	    }
	    return c;
	}
	private static final HMethod label_toString =
	    HClass.forName("harpoon.Temp.Label")
                  .getMethod("toString",new HClass[0]);
    }
    /** Find all places where object equality is checked with == and
     *  where we're not comparing something against null. */
    static class CheckEquals implements HCodeFactory {
	final HCodeFactory hcf;
	CheckEquals(HCodeFactory hcf) { this.hcf = hcf; }
	public void clear(HMethod hm) { hcf.clear(hm); }
	public String getCodeName() { return hcf.getCodeName(); }
	public HCode convert(HMethod hm) {
	    HCode c = hcf.convert(hm);
	    if (c==null) return c;
	    harpoon.Analysis.Quads.SCC.SCCAnalysis tm =
		new harpoon.Analysis.Quads.SCC.SCCAnalysis(c);
	    new harpoon.Analysis.Quads.SCC.SCCOptimize(tm).optimize(c);
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		harpoon.IR.Quads.Quad q = (harpoon.IR.Quads.Quad) it.next();
		if (!(q instanceof harpoon.IR.Quads.OPER)) continue;
		harpoon.IR.Quads.OPER oper = (harpoon.IR.Quads.OPER)q;
		if (oper.opcode()!=harpoon.IR.Quads.Qop.ACMPEQ) continue;
		Temp l=oper.operands(0), r=oper.operands(1);
		if (tm.typeMap(q,l)==HClass.Void ||
		    tm.typeMap(q,r)==HClass.Void) continue;
		System.out.println("B\t"+q.getSourceFile()+"\t"+q.getLineNumber()+"\t"+hm.getDeclaringClass().getName()+"\t"+hm.getName());
	    }
	    return c;
	}
    }
}
