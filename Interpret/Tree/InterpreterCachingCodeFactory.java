// InterpreterCachingCodeFactory.java, created Sun May  9 20:01:15 1999 by duncan 
// Copyright (C) 1999 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;


/** 
 * A code factory designed specifically for use by the Tree 
 * interpreter.  Probably shouldn't be used for anything else.  
 * The main feature of the <code>InterpreterCachingCodeFactory</code> class
 * is that its <code>convert()</code> chooses between 2 code factories
 * based on which method it is converting.  For class initializers, 
 * it uses a non-caching, non-optimizing codefactory to save memory.  
 * For all other methods, it uses a caching, optimizing code factory. 
 *
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterpreterCachingCodeFactory.java,v 1.2 2002-02-25 21:05:57 cananian Exp $
 */
class InterpreterCachingCodeFactory implements HCodeFactory { 
    private HCodeFactory factory;
    private HCodeFactory optimizingFactory;

    public InterpreterCachingCodeFactory
	(HCodeFactory factory, HCodeFactory optimizingFactory) {
	this.factory           = factory;
	this.optimizingFactory = new CachingCodeFactory(optimizingFactory);
    }

    /** Convert a method to an <code>HCode</code>.  If the method is 
     *  a class initializer, it is neither optimized, nor cached.  
     *  However, all other methods are both optimized (by this class's
     *  optimizing code factory) and cached. 
     */
    public HCode convert(HMethod m) {
	HCode hc;

	if (m.getName().equals("<clinit>")) 
	    hc = factory.convert(m);	    
	else 
	    hc = optimizingFactory.convert(m);
	
	return hc;
    }

    public String getCodeName() { return optimizingFactory.getCodeName(); } 

    public void clear(HMethod m) { this.optimizingFactory.clear(m); }
}
