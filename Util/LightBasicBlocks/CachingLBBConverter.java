// CachingLBBConverter.java, created Thu Mar 23 19:27:58 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import java.util.Map;
import java.util.HashMap;

import harpoon.ClassFile.HMethod;
import harpoon.Util.BasicBlocks.BBConverter;

/**
 * <code>CachingLBBConverter</code> adds some caching to the
 <code>LBBConverter</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingLBBConverter.java,v 1.1.2.3 2001-06-17 22:37:13 cananian Exp $
 */
public class CachingLBBConverter extends LBBConverter
    implements java.io.Serializable {
    
    private final Map cache;

    /** Creates a <code>CachingLBBConverter</code>. */
    public CachingLBBConverter(BBConverter bbconv) {
        super(bbconv);
	cache = new HashMap();
    }

    /** Returns a <code>LighBasicBlock.Factory</code> for the body of
	a method. Uses the already precomputed result if one exists. */
    public LightBasicBlock.Factory convert2lbb(HMethod hm){
        LightBasicBlock.Factory lbb = (LightBasicBlock.Factory) cache.get(hm);
        if(lbb == null){
            lbb = super.convert2lbb(hm);
            cache.put(hm,lbb);
        }
        return lbb;	
    }

    /** Remove from the internal cache the result for <code>hm</code>.
        This is useful if <code>hm</code> was modified and a new
        <code>LightBasicBlock</code> view needs to be generated for it. */
    public void clear(HMethod hm){
        cache.remove(hm);
    }
    
    /** Completely clears the internal cache. */
    public void clear(){
        cache.clear();
    }
    
}
