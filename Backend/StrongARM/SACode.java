// SACode.java, created Fri Jul 30 13:41:35 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Temp.Temp;

import java.util.Map;

/**
 * <code>SACode</code> is a code-view for StrongARM assembly.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SACode.java,v 1.1.2.10 1999-08-03 23:53:19 pnkfelix Exp $
 */
public class SACode extends harpoon.Backend.Generic.Code {
    public static final String codename = "strongarm";

    /** Creates a <code>SACode</code>. */
    public SACode(harpoon.IR.Tree.Code treeCode) {
        super(treeCode.getMethod(), 
	      null,
	      treeCode.getFrame());
	// treeCode.print(new java.io.PrintWriter(System.out));
	instrs = treeCode.getFrame().codegen()
	    .gen(treeCode, newINF(treeCode.getMethod()));
	Util.assert(instrs != null);
	instrs=treeCode.getFrame()
	    .procAssemDirectives(treeCode.getFrame()
				 .procLiveOnExit(instrs));
    }

    public String getName() { return codename; }

    public HCode clone(HMethod m) throws CloneNotSupportedException {
	throw new CloneNotSupportedException(this.toString());
    }
    
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	Util.assert(hcf.getCodeName().equals("canonical-tree"),
		    "Cannot make an code factory for SACode without "+
		    "a code factory for canonical tree form");
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		harpoon.IR.Tree.Code tc = 
		    (harpoon.IR.Tree.Code) hcf.convert(m);
		return (tc == null) ? null : new SACode(tc);
	    }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public String getCodeName() { return codename; }
	};
    }
    
    public static HCodeFactory codeFactory() {
	return codeFactory(CanonicalTreeCode.codeFactory( new SAFrame() ));
    }
    
    protected String getRegisterName(Temp val, 
				     String suffix, 
				     Map valToRegMap) {
	Util.assert(false, "SACode.getRegisterName() not implemented yet");
	return null;
    }
}
