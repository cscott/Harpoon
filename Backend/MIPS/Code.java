// Code.java, created Fri Jul 30 13:41:35 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Backend.StrongARM.TwoWordTemp;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Assem.Instr;
import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Temp.Temp;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>Code</code> is a code-view for MIPS assembly.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Code.java,v 1.3 2002-02-26 22:43:55 cananian Exp $
 */
class Code extends harpoon.Backend.Generic.Code {
    public static final String codename = "mips";

    private static final boolean DEBUG = false;

    Map tempInstrToRegisterMap;
    RegFileInfo regFileInfo;

    /** Creates a <code>Code</code>. */
    public Code(harpoon.IR.Tree.Code treeCode) {
        super(treeCode);

	// need to cast the return type to a MIPS.RegFileInfo
	regFileInfo = (RegFileInfo) this.frame.getRegFileInfo();
	Util.ASSERT(regFileInfo != null, "Need non-null regfileinfo");

	tempInstrToRegisterMap = new HashMap();
    }

    public String getName() { return codename; }

    /**
     * Returns a code factory for <code>Code</code>, given a 
     * code factory for <code>CanonicalTreeCode</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>CanonicalTreeCode</code>, then creates and returns a code
     *      factory for <code>Code</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>CanonicalTreeCode.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>Code</code> from the
     *      code factory returned by <code>CanonicalTreeCode</code>.
     * @see CanonicalTreeCode#codeFactory(HCodeFactory, Frame)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) {
	if(hcf.getCodeName().equals(CanonicalTreeCode.codename)){
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    harpoon.IR.Tree.Code tc = 
			(harpoon.IR.Tree.Code) hcf.convert(m);
		    return (tc == null) ? null : new Code(tc);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    // recursion can be ugly some times.
	    return codeFactory(CanonicalTreeCode.
			       codeFactory(hcf, frame), frame);
	}
    }
    
    public List getRegisters(Instr i, Temp val) {
	Util.ASSERT(i != null, "Code.getRegisters(null, Temp) undefined");
	if (val instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) val;
	    Temp low = get(i, t.getLow());
	    Temp high = get(i, t.getHigh());
	    Util.ASSERT(low != null,
			"low register for "+val+" in "+i+
			" should not be null");
	    Util.ASSERT(high != null,
			"high register for "+val+" in "+i+
			" should not be null");
	    return Arrays.asList(new Temp[]{ low, high });
				 
	} else {
	    Temp t;
	    if ( regFileInfo.isRegister(val) ) {
		t = val;
	    } else {
		t = get(i, val);
		Util.ASSERT(t != null, 
			    "register for "+val+" in "+i+
			    " should not be null");
	    }
	    return Collections.nCopies(1, t);
	}
    }

    /** This returns null or a register temp. */
    private Temp get(Instr instr, Temp val) {
	Temp reg = 
	    (Temp) tempInstrToRegisterMap.get
	    (new TempInstrPair(instr, val)); 
	if(reg == null) return null;
	Util.ASSERT( regFileInfo.isRegister(reg), 
		     "Temp: "+reg+" should be a reg in "+
		     "Instr: "+instr+", Val: "+val);
	return reg;
    }

    
    public String getRegisterName(final Instr instr,
				     final Temp val, 
				     final String suffix) {
	String s=null;
	if (val instanceof TwoWordTemp) {
	    // parse suffix
	    TwoWordTemp t = (TwoWordTemp) val;
	    Temp reg = null;
	    if (suffix.startsWith("l")) {
		// low
		reg = get(instr, t.getLow());
	    } else if (suffix.startsWith("h")) {
		// high
		reg = get(instr, t.getHigh());
	    } else if (suffix.trim().equals("")) {
		Util.ASSERT(false, "BREAK!  empty suffix " +
			    "suffix: " + suffix + "\n" +
			    "instr: " + instr + "\n" + 
			    "instr str: " + instr.getAssem() + "\n"+
			    "temp: " + val);
	    } else {
		Util.ASSERT(false, "BREAK!  This parsing needs to be "+
			    "fixed, strongarm has a lot more cases than this."+
			    "\n suffix: "+ suffix + "\n" +
			    "Alternatively, the pattern could be trying to "+
			    "use a TwoWordTemp without the appropriate "+
			    "double word modifier (l, h) in " + instr);
	    }
	    if(reg != null) {
		s = reg.name() + suffix.substring(1);
	    } else {
		s = val.name() + suffix;
	    }
	} else { // single word; nothing special
	    Temp reg = get(instr, val);
	    
	    Util.ASSERT(!suffix.startsWith("l") &&
			!suffix.startsWith("h"), "Shouldn't " +
			"have 'l' or 'h' suffix with Temp: " + 
			val + " Instrs: " + 
			instr.getPrev() + ", " + 
			instr + ", " + 
			instr.getNext());

	    if(reg != null) {
		s = reg.name() + suffix;
	    } else {
		s = val.name() + suffix;
	    }
	}
	// Util.ASSERT(s.indexOf("r0l") == -1 && s.indexOf("r0h") == -1 &&
	// s.indexOf("r1l") == -1 && s.indexOf("r1h") == -1, 
	// "Improper parsing of " + suffix + " in " + instr + " " + val.getClass().getName());

	return s;
    }


    // overriding superclass implementation to add
    // architecture-dependant assertions (in an effort to fail-fast)
    public String toAssem(Instr instr) {
	if (DEBUG) {
	    // these constraint checks may be flawed; I'm getting
	    // assertion failures on good code, i think...

	    // check that constants are all contained in eight-bit chunks
	    String assem = instr.getAssem();
	    int begin = assem.lastIndexOf('#');
	    
	    
	    if (begin != -1) {
		int end = assem.length();
		final char[] endChars = new char[]{ '\n', '@', ']', ' '};
		for(int i=0; i<endChars.length; i++) {
		    int e = assem.lastIndexOf(endChars[i], end);
		    if (e > begin) end = e;
		    // System.out.println(assem.substring(begin+1, end));
		}
		
		String constStr = assem.substring(begin+1, end);
		int v = 0;
		try { 
		    v = Integer.parseInt(constStr);
		} catch (NumberFormatException e) {
		    System.out.println("bad const extraction");
		    System.out.println("\'"+constStr+"\'");
		    System.out.println(" from "+instr+"("+begin+","+end+")");
		    Util.ASSERT(false);
		}
	    
		Util.ASSERT(isValidConst(v) || isValidConst(-v),
			    "const form err of "+v+" in "+
			    instr+"("+begin+","+end+")");
	    }
	} // end if(DEBUG)

	return super.toAssem(instr);
    }

    public static boolean isValidConst(final int val) {
	// FSK: stealing code from CSA again...
	int v;
	int r;
	
	v = val;
	r=0;
	for ( ; v!=0; r++) 
	    v &= ~(0xFF << ((Util.ffs(v)-1) & ~1));
	return (r <= 1) ;
    }

    /** Assigns register(s) to the <code>Temp</code> pseudoReg. 
	<BR><B>requires:</B> <code>regs</code> is one of the
	    <code>List</code>s returned by
	    <code>SAFrame.suggestRegAssignment()</code> for
	    <code>pseudoReg</code>.
	<BR><B>effects:</B> Associates the register <code>Temp</code>s
	    in <code>regs</code> with (<code>instr</code> x
	    <code>pseudoReg</code>) in such a manner that
	    <code>getRegisterName(instr, psuedoReg, suffix)</code>
	    will return the name associated with the appropriate
	    register in <code>regs</code>.  
    */
    public void assignRegister(final Instr instr, 
			       final Temp pseudoReg,
			       final List regs) {
	if (pseudoReg instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) pseudoReg;
	    tempInstrToRegisterMap.put
		(new TempInstrPair(instr, t.getLow()), regs.get(0));
	    tempInstrToRegisterMap.put
		(new TempInstrPair(instr, t.getHigh()), regs.get(1));
	} else {
	    tempInstrToRegisterMap.put
		(new TempInstrPair(instr, pseudoReg), regs.get(0));
	}
    }

    public void removeAssignment(Instr instr, Temp preg) {
	if (preg instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) preg;
	    tempInstrToRegisterMap.remove
		(new TempInstrPair(instr, t.getLow()));
	    tempInstrToRegisterMap.remove
		(new TempInstrPair(instr, t.getHigh()));
	} else {
	    tempInstrToRegisterMap.remove
		(new TempInstrPair(instr, preg));
	}
    }


    public boolean registerAssigned(Instr instr, Temp pr) {
	if (pr instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) pr;
	    return 
		(tempInstrToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, t.getLow()))
		 &&
		 tempInstrToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, t.getHigh())));
	} else {
	    return 
		(tempInstrToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, pr)));
	}
    }
}
