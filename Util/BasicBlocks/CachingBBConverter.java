// CachingBBConverter.java, created Wed Mar  8 15:57:12 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;

import java.util.Map;
import java.util.HashMap;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.BasicBlockFactoryInterf;

/**
 * <code>CachingBBConverter</code> provides some caching for the
 * <code>BBConverter</code>. This is THE class to use if you need to obtain
 * the <code>BasicBlock</code> view of the same method multiple times and
 * don't want to litter all your code with caching mechanisms.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingBBConverter.java,v 1.1.2.3 2001-12-16 05:16:34 salcianu Exp $
 */
public class CachingBBConverter extends BBConverter{
    
    Map cache;

    /** Creates a <code>CachingBBConverter</code>. */
    public CachingBBConverter(HCodeFactory hcf){
        super(hcf);
	cache = new HashMap();
    }

    /** Converts the code of the method <code>hm</code> to
	<code>BasicBlock.Factory</code>, a basic block view of
	<code>hm</code>'s code. The code of the method is obtained from
	the <code>HCodeFactory</code> that was passed to the constructor of
	<code>this</code>.<br>
	<b>Note</b>: the results are cached. */
    public BasicBlockFactoryInterf convert2bb(HMethod hm){
	BasicBlockFactoryInterf bb = (BasicBlockFactoryInterf) cache.get(hm);
	if(bb == null){
	    bb = super.convert2bb(hm);
	    cache.put(hm,bb);
	}
	return bb;
    }

    /** Remove from the internal cache the result for <code>hm</code>.
	This is useful if <code>hm</code> was modified and a new
	<code>BasicBlock</code> view needs to be generated for it. */
    public void clear(HMethod hm){
	cache.remove(hm);
    }
    
    /** Completely clears the internal cache. */
    public void clear(){
	cache.clear();
    }
}
