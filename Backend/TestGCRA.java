// TestGCRA.java, created Tue Jul 25 09:58:32 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tests.Backend;

import harpoon.Analysis.Instr.*;
import harpoon.ClassFile.*;
import harpoon.Backend.Generic.Code;

import java.io.*;
/**
 * <code>TestGCRA</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: TestGCRA.java,v 1.1 2000-07-26 21:33:11 pnkfelix Exp $
 */
public class TestGCRA {
    
    /** Creates a <code>TestGCRA</code>. */
    private TestGCRA() {
        
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Usage: TestGCRA <input-file>");
	    System.exit(-1);
	}
	final String infile = args[0];

	try {
	    Reader r = new FileReader(args[0]);
	    String methodnm = "methodName";
	    HCodeFactory hcf = AbsAssem.makeCodeFactory(r, methodnm);
	    Code c  = (Code) hcf.convert(AbsAssem.getHMethod());
	    c.print();

	    HCodeFactory racf = 
		RegAlloc.codeFactory(hcf, AbsAssem.getFrame(),
				     GraphColoringRegAlloc.FACTORY);
	    c  = (Code) racf.convert(AbsAssem.getHMethod());

	    c.print();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	

    }
    
}
