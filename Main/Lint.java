// Lint.java, created Sun Sep 12 09:49:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.Temp.Temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * @version $Id: Lint.java,v 1.2 2002-02-25 21:06:05 cananian Exp $
 */
public abstract class Lint extends harpoon.IR.Registration {
    public final static Linker linker = Loader.systemLinker;
    
    public static void usage(String errmsg) {
	System.err.println(errmsg);
	System.err.println("Usage: java "+Lint.class.getName()+" "+
			   "{-use sourcepath} {-e} {-v} {-l} "+
			   "[packages] ...");
	System.err.println("     -e\tShow possibly-incorrect object comparisons using ==");
	System.err.println("     -v\tShow possibly-incorrect QuadVisitor.visit(CALL) methods");
	System.err.println("     -l\tShow uses of (deprecated) Label.toString()");
	System.exit(1);
    }
    public static void main(String[] args) {
	boolean check_visit=false,check_labels=false,check_equals=false;
	// Command-line should have list of packages, & opt. sourcepath
	int s;
	for (s=0; args.length>s && args[s].charAt(0)=='-'; s++) {
	    if (args[s].startsWith("-use") && args.length>s+1)
	        SourceLineReader.sourcepath=args[++s];
	    else if (args[s].startsWith("-v"))
		check_visit=true;
	    else if (args[s].startsWith("-l"))
		check_labels=true;
	    else if (args[s].startsWith("-e"))
		check_equals=true;
	    else
		usage("Unrecognized option: "+args[s]);
	}
	if (!(args.length>s))
	    usage("No packages specified.");

	HCodeFactory hcf = harpoon.IR.Bytecode.Code.codeFactory();
	// rule-checkers using any IR at all.
	if (check_visit) // check implementations of visit(CALL)
	    hcf = new CheckVisitCALL(hcf);
	// rule-checkers using quad-no-ssa
	if (check_labels) // check uses of Label.toString()
	    hcf = new CheckLabels(hcf);
	// rule-checkers using quad-ssi
	if (check_equals) // check usage of == for objects.
	    hcf = new CheckEquals(hcf);

	for (int i=s; i<args.length; i++) {
	    System.err.println("CHECKING PACKAGE "+args[i]);
	    for (Iterator it=Loader.listClasses(args[i]); it.hasNext(); ) {
		HClass hc = linker.forName((String)it.next());
		System.err.println(" - " + hc);
		HMethod[] hms = hc.getDeclaredMethods();
		for (int j=0; j<hms.length; j++) {
		    hcf.convert(hms[j]);
		    hcf.clear(hms[j]); // free memory.
		}
	    }
	}
    }
    /** Allow reference to particular lines of a class file */
    static class SourceLineReader {
	public static String sourcepath=".";
	private List linelist = new ArrayList();
	SourceLineReader(HClass cls, String sourcefile) {
	    String filesep = System.getProperty("file.separator");
	    String packagepath =
		cls.getPackage().replace('.',filesep.charAt(0));
	    File f = new File(new File(sourcepath,packagepath),sourcefile);
	    try {
	    BufferedReader br = new BufferedReader(new FileReader(f));
	    for (String line = br.readLine(); line!=null; line=br.readLine())
		linelist.add(line);
	    br.close();
	    } catch (java.io.IOException e) { linelist=null; }
	}
	public String getLine(int lineno) {
	    if (linelist==null || linelist.size()<lineno)
		return "[--- unable to read file ---]";
	    return (String) linelist.get(lineno-1);
	}
    }
    /** method for caching sourcelinereaders */
    public static SourceLineReader getSLR(HClass cls, String sourcefile) {
	if (last_slr==null || last_cls!=cls || last_sourcefile!=sourcefile) {
	    last_slr = new SourceLineReader(cls, sourcefile);
	    last_cls = cls; last_sourcefile=sourcefile;
	}
	return last_slr;
    }
    static SourceLineReader last_slr = null;
    static HClass last_cls = null;
    static String last_sourcefile = null;
    /** print out a standardized error message */
    public static void printError(String msg, HMethod hm, HCodeElement hce) {
	System.out.println("WARNING: "+msg+" in " +
			   hm.getDeclaringClass().getName()+"."+hm.getName() +
			   ":");
	System.out.println(hce.getSourceFile()+"("+hce.getLineNumber()+"): " +
			   getSLR(hm.getDeclaringClass(), hce.getSourceFile())
			   .getLine(hce.getLineNumber()).trim());
	System.out.println();
    }

    /////////// RULE-CHECKING CODE FACTORIES:

    /** Find all methods overriding QuadVisitor.visit(CALL q). */
    static class CheckVisitCALL implements HCodeFactory {
	final HCodeFactory hcf;
	CheckVisitCALL(HCodeFactory hcf) { this.hcf = hcf; }
	public void clear(HMethod hm) { hcf.clear(hm); }
	public String getCodeName() { return hcf.getCodeName(); }
	public HCode convert(HMethod hm) {
	    HCode c = hcf.convert(hm);
	    if (c==null) return c;
	    if (hm.getDeclaringClass().isInstanceOf(HCqv) &&
		hm.getName().equals(HMvC.getName()) &&
		hm.getDescriptor().equals(HMvC.getDescriptor()))
		Lint.printError("Possibly incorrect implementation of "+
				"QuadVisitor.visit(CALL q)",
				hm, c.getRootElement());
	    return c;
	}
	private static final HClass HCqv =
	    linker.forName("harpoon.IR.Quads.QuadVisitor");
        private static final HMethod HMvC =
	    HCqv.getMethod("visit", new HClass[]
			   { linker.forName("harpoon.IR.Quads.CALL") } );
    }

    /** Find all places where Label.toString() is called. */
    static class CheckLabels implements HCodeFactory {
	final HCodeFactory hcf;
	CheckLabels(HCodeFactory hcf) {
	    if (!hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename))
		hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	    this.hcf = hcf;
	}
	public void clear(HMethod hm) { hcf.clear(hm); }
	public String getCodeName() { return hcf.getCodeName(); }
	public HCode convert(HMethod hm) {
	    HCode c = hcf.convert(hm);
	    if (c==null) return c;
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		harpoon.IR.Quads.Quad q = (harpoon.IR.Quads.Quad) it.next();
		if (q instanceof harpoon.IR.Quads.CALL &&
		    ((harpoon.IR.Quads.CALL)q).method().equals(label_toString))
		    Lint.printError("Use of deprecated Label.toString() method",
			       hm, q);
	    }
	    return c;
	}
	private static final HMethod label_toString =
	    linker.forName("harpoon.Temp.Label")
                  .getMethod("toString",new HClass[0]);
    }
    /** Find all places where object equality is checked with == and
     *  where we're not comparing something against null. */
    static class CheckEquals implements HCodeFactory {
	final HCodeFactory hcf;
	CheckEquals(HCodeFactory hcf) {
	    if (!hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSI.codename))
		hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	    this.hcf = hcf;
	}
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
		if (refunique.isSuperinterfaceOf(tm.typeMap(q,l)) ||
		    refunique.isSuperinterfaceOf(tm.typeMap(q,r))) continue;
		Lint.printError("Possibly incorrect use of == ("+tm.typeMap(q,l)+" / "+tm.typeMap(q,r)+")", hm, q);
	    }
	    return c;
	}
	private static final HClass refunique =
	    linker.forName("harpoon.Util.ReferenceUnique");
    }
}
