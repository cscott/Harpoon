// BBConverter.java, created Wed Mar  8 15:42:33 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.Analysis.BasicBlock; 
import harpoon.Analysis.FCFGBasicBlock; 
import harpoon.Analysis.BasicBlockFactoryInterf; 

import harpoon.Util.Util;

/**
 * <code>BBConverter</code> is a convenient class that offers a function
 * which returns a basic block view of the code of a method.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: BBConverter.java,v 1.1.2.5 2002-01-09 13:45:14 salcianu Exp $
 */
public class BBConverter implements java.io.Serializable{
    // the HCode factory used to generate the code of the methods.
    protected HCodeFactory hcf;
    // hcf is a "quad-with-try" code view
    protected boolean quad_with_try;

    /** Creates a <code>BBConverter</code>. Receives as parameter the
	<code>HCodeFactory</code> that will be used to generated the code
	of the methods passed to <code>convert2bb</code>. */
    public BBConverter(HCodeFactory hcf) {
	quad_with_try = hcf.getCodeName().equals("quad-with-try");
        this.hcf = hcf;
    }

    /** Converts the code of the method <code>hm</code> to basic
	blocks. The code of the method is obtained from the
	<code>HCodeFactory</code> that was passed to the constructor
	of <code>this</code>. */
    public BasicBlockFactoryInterf convert2bb(HMethod hm){
	HCode hcode = hcf.convert(hm);
	// special case: methods with no available code (e.g., native methods)
	if(hcode == null) return null;
	// normal case
	if(quad_with_try)
	    return new FCFGBasicBlock.Factory(hcode);
	else
	    return new BasicBlock.Factory(hcode);
    }
}
