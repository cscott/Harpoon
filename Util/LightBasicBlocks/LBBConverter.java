// LBBConverter.java, created Thu Mar 23 19:14:53 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import harpoon.ClassFile.HMethod;
import harpoon.Util.BasicBlocks.BBConverter;

/**
 * <code>LBBConverter</code> converts the body of a method
 into <code>LightBasicBlock</code>s.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: LBBConverter.java,v 1.2 2002-02-25 21:09:33 cananian Exp $
 */
public class LBBConverter implements java.io.Serializable {
    
    BBConverter bbconv;

    /** Creates a <code>LBBConverter</code>. */
    public LBBConverter(BBConverter bbconv) {
        this.bbconv = bbconv;
    }

    /** Returns a <code>LightBasicBlock.Factory</code> for the body of
	a method. */
    public LightBasicBlock.Factory convert2lbb(HMethod hm){
	return new LightBasicBlock.Factory(bbconv.convert2bb(hm));
    }
    
}
