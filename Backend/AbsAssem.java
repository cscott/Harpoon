// AbsAssem.java, created Mon Jul 24 15:10:07 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tests.Backend;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Temp.*;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Generic.*;
import harpoon.Backend.Generic.Runtime;
import harpoon.ClassFile.*;
import harpoon.Util.*;
import harpoon.Util.Collections.LinearSet;

import java.lang.reflect.Modifier;
import java.util.*;
import java.io.*;

/**
 * <code>AbsAssem</code> is a class that can parse an input stream
 * into an abstract <code>Backend.Generic.Code</code>.  The generated 
 * Code is not in the least bit executable; it merely acts as a stub 
 * for a standard Instr stream so that code can be tested
 * independently from all the extra classes/methods imported during 
 * the compilation of any standard Java program.
 * Note that many of the methods provided by the standard Flex classes
 * are not implemented.
 *
 * Created: Mon Jul 24 15:09:42 2000
 *
 * @author Felix S. Klock
 * @version
 */

public class AbsAssem  {
    public static boolean PRINT_PROGRESS = false;

    static String codename = "AbsAssem";
    
    private AbsAssem() {

    }
    
    static TempFactory tf;
    static Frame frm;
    static AbsHClass declClass;

    static int NUM_REGISTERS = 4;

    static { 
	tf = new AbsTempFactory();
	declClass = new AbsHClass();
	frm = new AbsFrame();
	declClass.hm = new AbsMethod(declClass, "main");	
    }

    public static void main(final String[] args) {
	if (args.length > 2 || args.length < 1) {
	    System.out.println("Usage: AbsAssem input-file [num-regs]");
	    System.exit(-1);
	}
	final String infile = args[0];

	if (args.length > 1) {
	    NUM_REGISTERS = Integer.parseInt(args[1]);
	}

	try {
	    Reader r = new FileReader(args[0]);
	    Code c = new Code(makeCode(r, "methodName"));
	    c.print();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
    }

    public static Frame getFrame() { return frm; }
    public static HMethod getHMethod() { return declClass.hm; }

    public static HCodeFactory makeCodeFactory(final Reader r, 
					       final String name) {
	final TCode tc = makeCode(r, name);
	return new HCodeFactory() {
	    public HCode convert(HMethod hm) {
		System.out.println("AbsAssem: code request for "+hm.getName());
		return new Code(tc);
	    }
	    public String getCodeName() { return codename; }
	    public void clear(HMethod hm) { }
	};
    }

    public static TCode makeCode(Reader r, String name) {
	rfi = new AbsRegFileInfo(NUM_REGISTERS);
	BufferedReader br = new BufferedReader(r);
	TCode tc = new TCode(declClass.hm, frm, br);
	return tc;
    }

    private static Instr buildCode(BufferedReader br, InstrFactory inf) {
	Instr i = null, first = null;
	try {	
	    for(String l = br.readLine(); l != null; l = br.readLine()){ 
		if (PRINT_PROGRESS)
		    System.out.print("interpreting "+l+" \t::\t");
		Instr curr = interpret(l, inf);
		if (curr != null) {
		    if (PRINT_PROGRESS) 
			System.out.println(new ArrayList(curr.defC()) + 
					   " <- " + 
					   new ArrayList(curr.useC()) + 
					   (curr.canFallThrough?""
					    :", "+curr.getTargets()));
		    
		    curr.layout(i, null);
		    i = curr;
		    if (first == null) first = curr;
		} else {
		    if (PRINT_PROGRESS)
			System.out.println();
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	return first;

    }

    static HashMap idToTemp  = new HashMap();
    static HashMap idToLabel = new HashMap();

    private static Instr interpret(String line, InstrFactory inf) {
	StringTokenizer pt = new StringTokenizer(line);
	List defs = new ArrayList(5);
	List uses = new ArrayList(5);
	List jumps = new ArrayList(5);
	while (pt.hasMoreTokens()) {
	    String s1 = pt.nextToken();
	    // System.out.print(s1 + "\t");

	    if (s1.equalsIgnoreCase("int") ||
		s1.equalsIgnoreCase("float") ||
		s1.equalsIgnoreCase("ref")) {
		addIdentifiers(false, pt);
		return null;
	    } else if (s1.equalsIgnoreCase("long") ||
		       s1.equalsIgnoreCase("double")) {
		addIdentifiers(true, pt);
		return null;
	    } else {
		StringTokenizer st = new StringTokenizer(s1, "():,", true);
		try {
		    while(st.hasMoreTokens()) {
			String s = st.nextToken();
			// System.out.print(s + " ");
			
			if (s.equalsIgnoreCase("def")) {
			    defs.addAll(defs(st));
			} else if (s.equalsIgnoreCase("use")) {
			    uses.addAll(uses(st));
			} else if (s.equalsIgnoreCase("mov")) {
			    return addMove(s1.trim(), st, inf);
			} else if (s.equalsIgnoreCase("jump")) {
			    jumps.addAll(jumps(st));
			} else {
			    Util.assert(defs.isEmpty() &&
					uses.isEmpty() &&
					jumps.isEmpty(), "line:"+line+" token:"+s);
			    return addLabel(s, inf);
			}
		    }
		} catch (RuntimeException e) {
		    System.out.println("line:"+line+" word: "+s1);
		    e.printStackTrace();
		    System.exit(-1);
		}
	    }
	}
	if (false) System.out.println("defs:"+defs+
				      " uses:"+uses+
				      " jumps:"+jumps);

	StringBuffer assemSB = new StringBuffer();
	if (defs.isEmpty() && uses.isEmpty()) {
	    assemSB.append("\t\t");
	} else {
	    assemSB.append(getDefStr(defs));
	    assemSB.append(" <- ");
	    assemSB.append(getSrcStr(uses));
	    assemSB.append("\t");
	}
	if (!jumps.isEmpty()) {
	    assemSB.append(" => ");
	    assemSB.append(getJmpStr(jumps));
	}
	return new Instr(inf, null, assemSB.toString(),
			 (Temp[]) defs.toArray(new Temp[defs.size()]),
			 (Temp[]) uses.toArray(new Temp[uses.size()]),
			 jumps.isEmpty(), jumps.isEmpty()?null:jumps);
			 
    }

    private static String getDefStr(Collection defs) {
	StringBuffer sb = new StringBuffer(defs.size() * 3);
	sb.append("{ ");
	for(int i=0; i<defs.size() ; i++) {
	    sb.append("`d");
	    sb.append(i);
	    if (i+1 < defs.size()) sb.append(", ");
	}
	sb.append(" }");
	return sb.toString();
    }
    private static String getSrcStr(Collection uses) {
	StringBuffer sb = new StringBuffer(uses.size() * 3);
	sb.append("{ ");
	for(int i=0; i<uses.size() ; i++) {
	    sb.append("`s");
	    sb.append(i);	    
	    if (i+1 < uses.size()) sb.append(", ");
	}
	sb.append(" }");
	return sb.toString();
    }
    private static String getJmpStr(Collection jmps) {
	StringBuffer sb = new StringBuffer(jmps.size() * 3);
	for(Iterator ji=jmps.iterator(); ji.hasNext(); ) {
	    sb.append(ji.next());
	    if (ji.hasNext()) sb.append(',');
	}
	return sb.toString();
    }

    private static void addIdentifiers(boolean twoWord, StringTokenizer st) {
	while(st.hasMoreTokens()) {
	    String s = st.nextToken();
	    Temp t;
	    if (twoWord) {
		t = new DoubleTemp(tf, s);
	    } else {
		t = new Temp(tf, s);
	    }
	    idToTemp.put(s, t);
	    // System.out.println("created "+t+" for "+s);
	}
    }

    private static Label getLabel(String s) {
	Label l = (Label) idToLabel.get(s);
	if (l == null) {
	    l = new Label(s);
	    idToLabel.put(s, l);
	}
	return l;
    }
    
    private static Instr addLabel(String s, InstrFactory inf) {
	
	InstrLABEL il = new InstrLABEL(inf, null, s+":", getLabel(s));
	// System.out.println(il);
	return il;
    }

    private static Instr addMove(String s1, 
				 StringTokenizer st, 
				 InstrFactory inf) {
	String s = st.nextToken();
	Util.assert(s.equals("("));
	
	s = st.nextToken();
	Temp d = (Temp) idToTemp.get(s);
	Util.assert(d != null, s+" needs explicit declaration");
	s = st.nextToken();
	Util.assert(s.equals(","));
	s = st.nextToken();
	Temp u = (Temp) idToTemp.get(s);
	Util.assert(u != null, s+" needs explicit declaration");
	s = st.nextToken();
	Util.assert(s.equals(")"));
	return new InstrMOVE(inf, null, "mov `d0, `s0", 
			     new Temp[]{ d }, 
			     new Temp[]{ u });
    }

    private static List defs(StringTokenizer st) {
	ArrayList ds = new ArrayList(5);
	while(st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.equals("(") || 
		s.equals(",")) {
		// skip 
	    } else if (s.equals(")")) {
		break;
	    } else {
		Temp t = (Temp) idToTemp.get(s);
		Util.assert(t != null, s+" needs explicit declaration");
		ds.add(t);
	    }
	}
	return ds;
    }
    private static List uses(StringTokenizer st) {
	ArrayList us = new ArrayList(5);
	while(st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.equals("(") ||
		s.equals(",")) {
		// skip 
	    } else if (s.equals(")")) {
		break;
	    } else {
		Temp t = (Temp) idToTemp.get(s);
		Util.assert(t != null, s+" needs explicit declaration");
		us.add(t);
	    }
	}	
	return us;
    }
    private static List jumps(StringTokenizer st) {
	ArrayList js = new ArrayList(5);
	while(st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.equals("(") ||
		s.equals(",")) {
		// skip 
	    } else if (s.equals(")")) {
		break;
	    } else {	    
		js.add(getLabel(s));
	    }
	}
	return js;
    }
    

    static class AbsFrame extends Frame {
	public Linker getLinker() { return null; }
	public boolean pointersAreLong() { return false; }
	public Runtime getRuntime() { return null; }
	public InstrBuilder getInstrBuilder() { return ib; }
	public RegFileInfo getRegFileInfo() { return rfi; }
	public LocationFactory getLocationFactory() { return null; }
	public CodeGen getCodeGen() { return new AbsCodeGen(this); }
	public HCodeFactory getCodeFactory(HCodeFactory hf) { return null; }
	public TempBuilder getTempBuilder() { return null; }
	public GCInfo getGCInfo() { return null; }
    }


    public static InstrBuilder ib = new AbsInstrBuilder();
    public static class AbsInstrBuilder extends InstrBuilder {
	// Identity Temp Map
	static TempMap idTempMap = new TempMap() {
	    public Temp tempMap(Temp t) { return t; }
	};
	
	protected List makeLoad(Temp reg, int offset, Instr template){
	    return Collections.nCopies(1, template.rename(idTempMap));
	}
	protected List makeStore(Temp reg,int offset, Instr template){
	    return Collections.nCopies(1, template.rename(idTempMap));
	}
    }
    
    public static RegFileInfo rfi;
    public static class AbsRegFileInfo extends RegFileInfo {
	private Temp[] regs;
	private Set swAssigns;
	private Set dwAssigns;
	AbsRegFileInfo(final int numRegs) {
	    regs = new Temp[numRegs];
	    swAssigns = new LinearSet(numRegs);
	    dwAssigns = new LinearSet(numRegs/2);
	    for(int i=0; i<numRegs; i++) {
		String name = "r"+i;
		regs[i] = new RegTemp(tf, name);
		// System.out.println("adding "+regs[i]);
		idToTemp.put(name, regs[i]);
	    }
	    for(int i=0; i<numRegs; i++) 
		swAssigns.add(Collections.nCopies(1,regs[i]));
	    for(int i=0; i<numRegs-1; i+=2) 
		dwAssigns.add(Default.pair(regs[i], regs[i+1]));
	}    


	public Set liveOnExit() { 
	    return Collections.singleton(regs[0]); 
	}
	public Set callerSave() { Util.assert(false); return null;}
	public Set calleeSave() { Util.assert(false); return null;}

	public boolean isRegister(Temp t) {
	    return (t instanceof RegTemp);
	}
	public Set getRegAssignments(Temp t) {
	    if(!isRegister(t)) {
		if (t instanceof DoubleTemp) {
		    return new HashSet(dwAssigns);
		} else {
		    Util.assert(!(t instanceof RegTemp));
		    return new HashSet(swAssigns);		
		}
	    } else {
		return Collections.singleton(Collections.nCopies(1, t));
	    }
	}
	public Iterator suggestRegAssignment(Temp t, Map regfile) {
	    return getRegAssignments(t).iterator();
	}
	public Temp[] getAllRegisters() {
	    return (Temp[]) regs.clone();
	}
	public Temp[] getGeneralRegisters() {
	    return (Temp[]) regs.clone();
	}
    }

    public static class AbsCodeGen extends CodeGen {
	AbsCodeGen(Frame frm) { super(frm); }
	public Instr emit(Instr i) {
	    return i;
	}
	public void declare(Temp t, HClass hc) { }
	public void declare(Temp t, Derivation.DList dlst) { }
	public Instr procFixup(HMethod hm, Instr i, int s, Set r) {
	    return i;
	}
	public List genCode(harpoon.IR.Tree.Code code, 
			    InstrFactory inf) {
	    TCode tc = (TCode) code;
	    Instr i = buildCode
		(new BufferedReader(new StringReader(tc.contents)), 
		 inf);

	    Derivation d = new Derivation() {
		public Derivation.DList derivation(HCodeElement hce,
						   Temp t) {
		    return null;
		}
		public HClass typeMap(HCodeElement hce, Temp t) {
		    return null;
		}
	    };

	    return Default.pair(i, d);
	}
	public Instr genData(harpoon.IR.Tree.Data data, 
			     InstrFactory inf) {
	    return null;
	}
	
    }

    static class TCode extends harpoon.IR.Tree.Code {
	String contents;
	TCode(HMethod parent, Frame f, BufferedReader br) {
	    super(parent, null, f);
	    StringWriter sw = new StringWriter();
	    try {
		for(String l=br.readLine(); l!=null; l=br.readLine()) {
		    sw.write(l);
		    sw.write('\n');
		}
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    contents = sw.toString();
	    // System.out.println("TCode contents:\n"+contents);
	}
	public harpoon.IR.Tree.Code clone(HMethod m, Frame f) {
	    return null;
	}
	public String getName() { return "stub-tree-code"; }
	public harpoon.IR.Tree.TreeDerivation getTreeDerivation() {
	    return null;
	}
	public boolean isCanonical() { return true; }
    }

    public static class Code extends harpoon.Backend.Generic.Code {
	HashMap ti2regList; 

	public Code(TCode code) {
	    super(code);
	    ti2regList = new HashMap();
	}
	public String getName() {
	    return codename;
	}
	public void assignRegister(Instr i, Temp t, List regs) {
	    ti2regList.put(Default.pair(t,i), regs);
	}
	public String getRegisterName(Instr i, Temp t, String s) {
	    List key = Default.pair(t,i);
	    if (ti2regList.containsKey(key)) {
		List l = (List) ti2regList.get(key);
		if (t instanceof DoubleTemp) {
		    return (l.get(0) +":"+ l.get(1));
		} else {
		    return l.get(0).toString();
		}
	    } else {
		return t.toString()+s;
	    }
	}
	public boolean registerAssigned(Instr i, Temp t) {
	    return ti2regList.containsKey(Default.pair(t,i));
	}
	public List getRegisters(Instr i, Temp t) {
	    return (List) ti2regList.get(Default.pair(t,i)); 
	}
	public void print(java.io.PrintWriter pw) {
	    myPrint(pw, true, true);
	}

    }

    public static class DoubleTemp extends Temp {
	private final Temp low, high;
	DoubleTemp(TempFactory tf, String s) {
	    super(tf, s);
	    low = new Temp(tf);
	    high = new Temp(tf);
	}
    }

    public static class RegTemp extends Temp {
	RegTemp(TempFactory tf, String s) {
	    super(tf, s);
	}
    }

    static class AbsTempFactory extends TempFactory {
	public String getScope() {return "absassem";}
	protected String getUniqueID(String sugg) {
	    return sugg;
	}
    }

    static class AbsMethod implements HMethod {
	HClass hclass;
	String nm;
	public AbsMethod(HClass declClass, 
			 String name) { 
	    hclass = declClass; nm = name;
	}
	public HClass getDeclaringClass(){return hclass;}
	public String getName() { return nm; }
	public int getModifiers() {
	    return
		Modifier.STATIC | Modifier.PUBLIC |
		Modifier.NATIVE | Modifier.FINAL;
	}
	public HClass getReturnType() { return HClass.Void; }
	public String getDescriptor() { return ""; }
	public int compareTo(Object s) { return -1; }
	public HClass[] getParameterTypes(){return new HClass[0];}
	public String[] getParameterNames(){return new String[0];}
	public HClass[] getExceptionTypes(){return new HClass[0];}
	public boolean isSynthetic(){return true;}
	public boolean isInterfaceMethod(){return false;}
	public boolean isStatic(){return true;}
	public HMethodMutator getMutator(){return null;}
    }

} // AbsAssem
