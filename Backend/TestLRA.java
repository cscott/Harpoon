// AbsAssem.java, created Mon Jul 24 15:10:07 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tests.Backend;

import harpoon.Analysis.Instr.RegAlloc;
import harpoon.ClassFile.*;
import harpoon.Backend.Generic.Code;

import java.io.*;
/**
 * <code>TestLRA</code> tests Local Register Allocation.  (FSK: LRA
 * already works; this class is to test the testing strategy itself). 
 *
 * Created: Mon Jul 24 19:57:55 2000
 *
 * @author Felix S. Klock
 * @version
 */

public class TestLRA  {
    
    private TestLRA() {
	
    }
    
    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Usage: TestLRA <input-file>");
	    System.exit(-1);
	}
	final String infile = args[0];

	try {
	    Reader r = new FileReader(args[0]);
	    String methodnm = "methodName";
	    HCodeFactory hcf = AbsAssem.makeCodeFactory(r, methodnm);
	    HCodeFactory racf = RegAlloc.codeFactory(hcf, AbsAssem.getFrame());
	    Code c  = (Code) racf.convert(AbsAssem.getHMethod());

	    c.print();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	

    }

} // TestLRA
