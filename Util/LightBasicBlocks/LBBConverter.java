// LBBConverter.java, created Thu Mar 23 19:14:53 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import harpoon.ClassFile.HMethod;
import harpoon.Util.BasicBlocks.BBConverter;

/**
 * <code>LBBConverter</code> converts the body of a method
 into <code>LightBasicBlock</code>s.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: LBBConverter.java,v 1.1.2.1 2000-03-24 01:05:05 salcianu Exp $
 */
public class LBBConverter {
    
    BBConverter bbconv;

    /** Creates a <code>LBBConverter</code>. */
    public LBBConverter(BBConverter bbconv) {
        this.bbconv = bbconv;
    }

    /** Returns a <code>LighBasicBlock.Factory</code> for the body of
	a method. */
    public LightBasicBlock.Factory convert2lbb(HMethod hm){
	return new LightBasicBlock.Factory(bbconv.convert2bb(hm));
    }
    
}
