// CodeGenerator.java, created Wed Apr  2 14:17:32 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Analysis.ClassHierarchy;

import harpoon.Backend.Backend;
import harpoon.Backend.Generic.Frame;

import harpoon.IR.Tree.Data;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;

import harpoon.Analysis.Instr.RegAlloc;
import harpoon.Analysis.Instr.RegAllocOpts;

import harpoon.Temp.TempFactory;

import net.cscott.jutil.Default;
import net.cscott.jutil.CombineIterator;
import harpoon.Util.Options.Option;

import harpoon.Analysis.MemOpt.PreallocOpt;
import harpoon.Analysis.Realtime.Realtime;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * <code>CodeGenerator</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CodeGenerator.java,v 1.13 2005-09-12 18:22:30 salcianu Exp $
 */
public class CodeGenerator extends CompilerStage {
    
    public CodeGenerator() { super("code-generator"); }

    public List<Option> getOptions() {
	List<Option> opts = new LinkedList<Option>();

	if(Boolean.getBoolean("debug.reg-alloc"))
	    add_debug_options(opts);

	opts.add(new Option("no-code-gen", "Skip code generator - useful if you only want to run an analysis") {
	    public void action() { ENABLED = false; }
	});

	opts.add(new Option("L", "Outputs Local Register Allocated Instr IR") {
	    public void action() { REG_ALLOC = LOCAL_REG_ALLOC = true; }
	});

	opts.add(new Option("H", "Hacked register allocator") {
	    public void action() { HACKED_REG_ALLOC = true; }
	});

	opts.add(new Option
		 ("R", "", "<regAllocOptionsFilename>",
		  "Read Global Register Allocator options from file") {
	    public void action() {
		if(getOptionalArg(0) != null)
		    regAllocOptionsFilename = getOptionalArg(0);
		REG_ALLOC = true;
	    }
	});

	opts.add(new Option("o", "<outputDir>",
			    "Output directory for compilation result") {
	    public void action() { 
		ASSEM_DIR = new File(getArg(0));
		assert ASSEM_DIR.isDirectory() : 
		    ASSEM_DIR + " must be a directory";

	    }
	});

	opts.add(new Option("1", "", "<classname>", "Compiles only a single method or class.  Without a classname, only compiles <class>.main()") {
	    public void action() {
		String optclassname = getOptionalArg(0);
		if (optclassname != null)
		    singleClassStr = optclassname;
		else
		    ONLY_COMPILE_MAIN = true;
	    }
	});

	return opts;
    }


    // These options seem too debug-specific.  Not included by default [AS]
    private void add_debug_options(List/*<Option>*/ opts) {
	opts.add(new Option("D", "Outputs DATA information") {
	    public void action() { OUTPUT_INFO = PRINT_DATA = true; }
	});
	
	opts.add(new Option("O", "Outputs Original Tree IR") {
	    public void action() { OUTPUT_INFO = PRINT_ORIG = true; }
	});
	
	opts.add(new Option
		 ("P", "Outputs Pre-Register Allocated Instr IR") {
	    public void action() { OUTPUT_INFO = PRE_REG_ALLOC = true; }
	});
	
	opts.add(new Option
		 ("B", "Outputs Abstract Register Allocated Instr IR") {
	    public void action() {
		OUTPUT_INFO = ABSTRACT_REG_ALLOC = true;
	    }
	});
	
	opts.add(new Option("A", "Same as -O -P -R") {
	    public void action() {
		OUTPUT_INFO = PRE_REG_ALLOC = true;
		PRINT_ORIG = REG_ALLOC = true;
	    }
	});
    }


    /** code generation options ([TOO] MANY!) */
    static boolean PRINT_ORIG = false;
    static boolean PRINT_DATA = false;
    static boolean PRE_REG_ALLOC = false;
    static boolean REG_ALLOC = false;
    static boolean ABSTRACT_REG_ALLOC = false;
    static boolean HACKED_REG_ALLOC = false;
    static boolean LOCAL_REG_ALLOC = false;
    static boolean OUTPUT_INFO = false;
    static File ASSEM_DIR;

    static boolean ONLY_COMPILE_MAIN = false; // for testing small stuff
    static String  singleClassStr = null; 
    static HClass  singleClass = null; // for testing single classes

    // FSK: contains specialization options to be used during
    // RegAlloc.  Take out when no longer necessary.  
    // May be null (in which case no options are being passed).
    static String regAllocOptionsFilename; 
    private static RegAlloc.Factory regAllocFactory;

    /** @return <code>true</code> */
    public boolean enabled() { return ENABLED; }

    /** Tells whether the code generator stage is enabled.  Default is
        <code>true</code>. */
    public boolean ENABLED = true;

    public CompilerState action(CompilerState cs) {
	// always enabled; unpack cs and go to work!
	mainM = cs.getMain();
	linker = cs.getLinker();
	hcf = cs.getCodeFactory();
	classHierarchy = cs.getClassHierarchy();
	frame = cs.getFrame();

	generate_code();

	mainM = null;
	linker = null;
	hcf = null;
	classHierarchy = null;
	frame = null;

	return cs;
    }

    private static HMethod mainM;
    private static Linker linker;
    private static HCodeFactory hcf;
    private static ClassHierarchy classHierarchy;
    private static Frame frame;
    private static PrintWriter out = new PrintWriter(System.out, true);


    // take a tree code factory and generate native / preciseC code
    private void generate_code() {
	// sahcf = "Strong-Arm HCodeFactory"
	HCodeFactory sahcf = frame.getCodeFactory(hcf);
	if (sahcf != null)
	    sahcf = new harpoon.ClassFile.CachingCodeFactory(sahcf);
	
	regAllocFactory = LOCAL_REG_ALLOC ? RegAlloc.LOCAL : RegAlloc.GLOBAL;
	regAllocFactory = (new RegAllocOpts
			   (regAllocOptionsFilename)).factory(regAllocFactory);

	Set callableMethods = classHierarchy.callableMethods();
	Iterator classes = new TreeSet(classHierarchy.classes()).iterator();

	if (ONLY_COMPILE_MAIN)
	    classes = Default.singletonIterator(mainM.getDeclaringClass());
	if (singleClassStr != null) {
	    singleClass = linker.forName(singleClassStr);
	    classes = Default.singletonIterator(singleClass);
	}

	while(classes.hasNext()) {
	    HClass hclass = (HClass) classes.next();
	    if (singleClass != null && singleClass != hclass) continue; //skip
	    messageln("Compiling: " + hclass.getName());
	    try {
		generate_code_for_class(hclass, callableMethods, sahcf);
	    } catch (IOException e) {
		System.err.println("Error outputting class "+
				   hclass.getName() + ": " + e);
		System.exit(1);
	    }
	}
	
	if (Realtime.REALTIME_JAVA)
	    Realtime.printStats();

	generate_Makefile();
    }


    // Generate class data + code for all methods of
    // <code>hclass</code> that are in the set
    // <code>callableMethods</code>.
    // QUESTION: What's <code>sahcf</code>.
    //    "StrongArm HCodeFactory" -- the hcode factory for the backend;
    //    the name is left over from when StrongArm was our only backend. [CSA]
    private void generate_code_for_class
	(HClass hclass, Set callableMethods, HCodeFactory sahcf)
	throws IOException {

	String filesuffix = (SAMain.BACKEND == Backend.PRECISEC) ? ".c" : ".s";
	String filename = frame.getRuntime().getNameMap().mangle(hclass);
	java.io.Writer w;
	try {
	    w = new FileWriter
		(new File(ASSEM_DIR, filename + filesuffix));
	} catch (java.io.FileNotFoundException ffe) {
	    // filename too long?  try shorter, unique, name.
	    // XXX: sun's doc for File.createTempFile() states
	    // "If the prefix is too long then it will be
	    // truncated" but it is actually not.  We must
	    // truncate it ourselves, for now.  200-chars?
	    if (filename.length()>200)
		filename=filename.substring(0, 200);
	    w = new FileWriter
		(File.createTempFile(filename, filesuffix, ASSEM_DIR));
	}
	out = new PrintWriter(new BufferedWriter(w));
	
	if (SAMain.BACKEND == Backend.PRECISEC)
	    out = new harpoon.Backend.PreciseC.TreeToC(out);
	
	HMethod[] hmarray = hclass.getDeclaredMethods();
	Set<HMethod> hmset = new TreeSet<HMethod>(Arrays.asList(hmarray));
	hmset.retainAll(callableMethods);
	Iterator<HMethod> hms = hmset.iterator();
	if (ONLY_COMPILE_MAIN)
	    hms = Default.singletonIterator(mainM);
	while(hms.hasNext()) {
	    HMethod m = hms.next();
	    messageln("\t" + m);
	    if (!Modifier.isAbstract(m.getModifiers()))
		outputMethod(m, hcf, sahcf, out);
	}
	messageln("");
	
	//out.println();
	messageln("Writing data for " + hclass.getName());
	outputClassData(hclass, out);
       
	out.close();
    }


    // generate the Makefile that can be used to assemble / compile the
    // generated files into a single executable
    private void generate_Makefile() {	
	// put a proper makefile in the directory.
	File makefile = new File(ASSEM_DIR, "Makefile");
	InputStream templateStream;
	String resourceName = "harpoon/Support/nativecode-makefile.template";

	if (SAMain.BACKEND == Backend.PRECISEC)
	    // see TreeToC for more details on the NO_SECTION_SUPPORT option
	    resourceName="harpoon/Support/precisec-"+
		(Boolean.getBoolean("harpoon.precisec.no_sections") ?
		 "no-sect-" : "")+"makefile.template";
	if (SAMain.BACKEND == Backend.MIPSDA)
	    resourceName="harpoon/Support/mipsda-makefile.template";
	if (makefile.exists())
	    System.err.println("WARNING: not overwriting pre-existing " +
			       "file " + makefile);
	else if ((templateStream = 
		  ClassLoader.getSystemResourceAsStream(resourceName)) == null)
	    System.err.println("WARNING: can't find Makefile template.");
	else try {
	    BufferedReader in = new BufferedReader
		(new InputStreamReader(templateStream));
	    out = new PrintWriter
		(new BufferedWriter(new FileWriter(makefile)));
	    String line;
	    while ((line=in.readLine()) != null)
		out.println(line);
	    in.close(); out.close();
	} catch (IOException e) {
	    System.err.println("Error writing "+makefile+".");
	    System.exit(1);
	}
    }


    public void outputMethod(final HMethod hmethod, 
			     final HCodeFactory hcf,
			     final HCodeFactory sahcf,
			     final PrintWriter out) throws IOException {
	if (PRINT_ORIG) {
	    HCode hc = hcf.convert(hmethod);
	    
	    info("\t--- TREE FORM ---");
	    if (hc!=null) hc.print(out); 
	    else 
		info("null returned for " + hmethod);
	    info("\t--- end TREE FORM ---");
	    out.println();
	    out.flush();
	}
	    
	if (PRE_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (no register allocation)  ---");
	    if (hc!=null) {
		info("Codeview \""+hc.getName()+"\" for "+
		     hc.getMethod()+":");
		// myPrint avoids register-allocation-dependant
		// peephole optimization code in print
		((harpoon.IR.Assem.Code)hc).myPrint(out,false); 
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (no register allocation)  ---");
	    out.println();
	    out.flush();
	}
	
	if (ABSTRACT_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    
	    info("\t--- INSTR FORM (register allocation)  ---");
	    HCodeFactory regAllocCF = 
		RegAlloc.abstractSpillFactory(sahcf, frame, regAllocFactory);
	    HCode rhc = regAllocCF.convert(hmethod);

	    if (rhc != null) {
		info("Codeview \""+rhc.getName()+"\" for "+
		     rhc.getMethod()+":");
		rhc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (register allocation)  ---");
	    out.println();
	    out.flush();
	}

	if (REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    info("\t--- INSTR FORM (register allocation)  ---");
	    HCodeFactory regAllocCF;
	    
	    regAllocCF = RegAlloc.codeFactory(sahcf,frame,regAllocFactory);

	    HCode rhc = regAllocCF.convert(hmethod);
	    if(SAMain.BACKEND == Backend.MIPSYP && rhc != null) {
		harpoon.Backend.Generic.Code cd = 
		    (harpoon.Backend.Generic.Code) rhc;
		harpoon.Backend.MIPS.BypassLatchSchedule b = 
		    new harpoon.Backend.MIPS.BypassLatchSchedule(cd, frame);
	    }
	    
	    if (rhc != null) {
		info("Codeview \"" + rhc.getName() + "\" for " +
		     rhc.getMethod() + ":");
		rhc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (register allocation)  ---");
	    out.println();
	    out.flush();
	}
	
	if (HACKED_REG_ALLOC) {
	    HCode hc = sahcf.convert(hmethod);
	    info("\t--- INSTR FORM (hacked register allocation)  ---");
	    HCode rhc = (hc==null) ? null :
		new harpoon.Backend.CSAHack.RegAlloc.Code
		(hmethod, (Instr) hc.getRootElement(),
		 ((harpoon.IR.Assem.Code) hc).getDerivation(), frame);
	    if (rhc != null) {
		info("Codeview \"" + rhc.getName() + "\" for " +
		     rhc.getMethod() + ":");
		rhc.print(out);
	    } else {
		info("null returned for " + hmethod);
	    }
	    info("\t--- end INSTR FORM (register allocation)  ---");
	    out.println();
	    out.flush();
	}

	if (SAMain.BACKEND == Backend.PRECISEC) {
	    HCode hc = hcf.convert(hmethod);
	    if (hc != null)
		((harpoon.Backend.PreciseC.TreeToC) out).translate(hc);
	}

	// free memory associated with this method's IR:
	hcf.clear(hmethod);
	if (sahcf!=null) sahcf.clear(hmethod);
    }
    
    public void outputClassData(HClass hclass, PrintWriter out) 
	throws IOException {
      Iterator<HData> it=frame.getRuntime().classData(hclass).iterator();
      // output global data with the java.lang.Object class.
      if (hclass==linker.forName("java.lang.Object")) {
	  HData data=frame.getLocationFactory().makeLocationData(frame);
	  it=new CombineIterator<HData>(it, Default.singletonIterator(data));
	  if (WriteBarriers.WB_STATISTICS) {
	      assert WriteBarriers.writeBarrierStats != null :
		  "WriteBarrierStats need to be run before WriteBarrierData.";
	      HData wbData = 
		  WriteBarriers.writeBarrierStats.getData(hclass, frame);
	      it=new CombineIterator<HData>
		  (it, Default.singletonIterator(wbData));
	  }
	  if(PreallocOpt.PREALLOC_OPT) {
	      HData poData = PreallocOpt.getData(hclass, frame);
	      it = new CombineIterator<HData>
		  (it, Default.singletonIterator(poData));
	  }
      }
      // additional data required.
      if (Transactions.DO_TRANSACTIONS) {
	  it = Transactions.filterData(frame, it);
      }
      while (it.hasNext() ) {
	final Data data = (Data) it.next();
	
	if (PRINT_ORIG) {
	    info("\t--- TREE FORM (for DATA)---");
	    data.print(out);
	    info("\t--- end TREE FORM (for DATA)---");
	}		
	
	if (SAMain.BACKEND == Backend.PRECISEC)
	    ((harpoon.Backend.PreciseC.TreeToC)out).translate(data);

	if (!PRE_REG_ALLOC && !REG_ALLOC && !HACKED_REG_ALLOC) continue;

	if (data.getRootElement()==null) continue; // nothing to do here.

	final Instr instr = 
	    frame.getCodeGen().genData((harpoon.IR.Tree.Data) data,
				       new InstrFactory() {
		private int id = 0;
		public TempFactory tempFactory() { return null; }
		public harpoon.IR.Assem.Code getParent() {
		    return null /*data*/;  // FIXME!
		}
		public Frame getFrame() {return frame;}
		public synchronized int getUniqueID() { return id++; }
		public HMethod getMethod() { return null; }
		public int hashCode() { return data.hashCode(); }
	    });
	
	assert instr != null : 
	    "no instrs generated; check that CodeGen.java was built " +
	    "from spec file";
	// messageln("First data instruction " + instr);


	/* trying different method. */
	Instr di = instr; 
	info("\t--- INSTR FORM (for DATA)---");
	while(di!=null) { 
	    //messageln("Writing " + di);
	    out.println(di); 
	    di = di.getNext(); 
	}
	info("\t--- end INSTR FORM (for DATA)---");
      }
    }

    protected static void message(String msg) { SAMain.message(msg); }
    protected static void messageln(String msg) { SAMain.messageln(msg); }

    protected static void info(String str) {
	if(OUTPUT_INFO) out.println(str);
    }
}
