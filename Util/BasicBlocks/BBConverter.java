// BBConverter.java, created Wed Mar  8 15:42:33 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.Analysis.BasicBlock; 
import harpoon.IR.Properties.CFGrapher;

/**
 * <code>BBConverter</code> is a convenient class that offers a function
 * which returns a basic block view of the code of a method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: BBConverter.java,v 1.1.2.1 2000-03-23 21:29:08 salcianu Exp $
 */
public class BBConverter{
    // the HCode factory used to generate the code of the methods.
    private HCodeFactory hcf;

    /** Creates a <code>BBConverter</code>. Receives as parameter the
	<code>HCodeFactory</code> that will be used to generated the code
	of the methods passed to <code>convert2bb</code>. */
    public BBConverter(HCodeFactory hcf) {
        this.hcf = hcf;
    }

    /** Converts the code of the method <code>hm</code> to
	<code>BasicBlock.Factory</code>s, a basic block view of
	<code>hm</code>'s code. The code of the method is obtained from
	the <code>HCodeFactory</code> that was passed to the constructor of
	<code>this</code>. */
    public BasicBlock.Factory convert2bb(HMethod hm){
	HCode hcode = hcf.convert(hm);
	return 
	    new BasicBlock.Factory(hcode,CFGrapher.DEFAULT);
    }
}
