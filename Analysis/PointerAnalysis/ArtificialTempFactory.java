// ArtificialTempFactory.java, created Wed Jan 12 19:03:03 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;

import harpoon.Util.Util;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory ;
import harpoon.ClassFile.HField;

/** 
 * <code>ArtificialTempFactory</code> provides support for the static fields
 in the context of the Pointer Analysis algorithm of Martin & Whaley.
 In that algorith, these fields are modeled by some special class variables
 (CL in the original paper) that point to class nodes that serves as
 containers for the static fields. <br>
 Ex: Suppose we have the classes A and B and that A contains a static field
 called f. Then, to model A.f, we need a variable vA pointing to a special
 <i>class</i> node nC_A which contains the field f. Note that no class 
 variable and no class node are generated for the class B which doesn't have
 any static field.<br>
 Another alternative would be to have a special variable cl.f for each 
 static field cl.f but this would lead to some irregularities in the
 algorithm and will generate a bigger number of <i>artificial</i> nodes.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ArtificialTempFactory.java,v 1.3 2002-02-26 22:41:18 cananian Exp $
 */
public abstract class ArtificialTempFactory implements java.io.Serializable {

    // TODO: Make sure no one is using this evil string
    /** Totally artificial scope, used to generate unique (and artificial)
     *  <code>Temp</code>s for the class names (used in the pointer analysis
     *  algorithm as a container for the static fields of each class)
     *  You should NEVER use this scope in your stuff! */
    private static final String global_scope = "@$#";

    /** The temp factory we are using to generate the <code>Temp</code>s */
    private static final TempFactory temp_factory = 
	Temp.tempFactory(global_scope);

    // TODO: A String key is quite heavyweight. Think about something better:
    //  HClass maybe (I need something whose "equals()" consists of just an 
    //  address comparison).
    /** A cache of the already generated <code>Temp</code>s */
    private static Hashtable cache = new Hashtable();

    /** Returns a <code>Temp</code> representing the &quot;container&quot;
     *  class variable (a variable which will point to a static node) 
     *  for the static fields of the declaring class */
    public static final Temp getTempFor(HField hf){
	Util.ASSERT(hf.isStatic());
	String classname = hf.getDeclaringClass().getName();
	Temp temp = (Temp)cache.get(classname);
	if(temp==null){
	    temp = new Temp(temp_factory);
	    cache.put(classname,temp);
	}
	return temp;
    }
}
