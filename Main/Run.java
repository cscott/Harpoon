// Run.java, created Mon Dec 28 02:34:11 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.Interpret.Quads.Method;
import harpoon.IR.Quads.QuadWithTry;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * <code>Run</code> invokes the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Run.java,v 1.2 2002-02-25 21:06:10 cananian Exp $
 */
public abstract class Run extends harpoon.IR.Registration {
    public static void main(String args[]) throws IOException {
	java.io.InputStream startup = null;
	java.io.OutputStream dump = null;
	boolean trace=true;
	Linker linker = Loader.systemLinker;
	HCodeFactory hf = // default code factory.
	    harpoon.IR.Quads.QuadWithTry.codeFactory();
	int i=0; // count # of args/flags processed.
	// check for "-prof" and "-code" flags in arg[i]
	for (; i < args.length ; i++) {
	    if (args[i].startsWith("-pass")) {
		if (++i < args.length)
		    hf = Options.cfFromString(args[i], hf);
		else throw new Error("-pass option needs codename");
	    } else if (args[i].startsWith("-prof")) {
		String filename = "./java.prof";
		if (args[i].startsWith("-prof:"))
		    filename = args[i].substring(6);
		try {
		    FileOutputStream fos = new FileOutputStream(filename);
		    Options.profWriter =
			new PrintWriter(new GZIPOutputStream(fos));
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for profiling: "+ e.toString());
		}
	    } else if (args[i].startsWith("-stat")) {
		String filename = "./phisig.data";
		if (args[i].startsWith("-stat:"))
		    filename = args[i].substring(6);
		try {
		    Options.statWriter = 
			new PrintWriter(new FileWriter(filename), true);
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for statistics: " + e.toString());
		}
	    } else if (args[i].startsWith("-startup")) {
		String filename = "./startup-dump";
		if (args[i].startsWith("-startup:"))
		    filename = args[i].substring(9);
		try {
		    startup = new java.io.FileInputStream(filename);
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for startup: " + e.toString());
		}
	    } else if (args[i].startsWith("-dump")) {
		String filename = "./startup-dump";
		if (args[i].startsWith("-dump:"))
		    filename = args[i].substring(6);
		try {
		    dump = new java.io.FileOutputStream(filename);
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for dump: " + e.toString());
		}
	    } else if (args[i].startsWith("-q")) { // for -quiet
		trace=false;
	    } else break; // no more command-line options.
	}
	if (dump!=null) { // make quick-startup dump.
	    harpoon.Interpret.Quads.Method.makeStartup(linker, hf, dump,
						       trace);
	    return;
	}
	// arg[i] is class name.  Load its main method.
	if (args.length < i) throw new Error("No class name.");
	HClass cls = linker.forName(args[i]);
	i++;
	// construct caching code factory.
	final HCodeFactory hf0 = hf;
	hf = new CachingCodeFactory(new SerializableCodeFactory() {
	    public String getCodeName() { return hf0.getCodeName(); }
	    public HCode convert(HMethod m) { return hf0.convert(m); }
	    public void clear(HMethod m) { /* don't clear. */ }
	});
	//////////// now call interpreter with truncated args list.
	String[] params = new String[args.length-i];
	System.arraycopy(args, i, params, 0, params.length);
	if (startup!=null)
	    harpoon.Interpret.Quads.Method.run(Options.profWriter,
					       hf, cls, params, startup,
					       trace);
	else
	    harpoon.Interpret.Quads.Method.run(Options.profWriter,
					       hf, cls, params, trace);
	if (Options.profWriter!=null) Options.profWriter.close();
	if (Options.statWriter!=null) Options.statWriter.close();
    }
}
