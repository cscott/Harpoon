// UseDefer.java, created Thu Jan 27 15:33:54 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import java.util.Collection;

/**
 * <code>UseDefer</code> provides a means to externally associate use and
 * def information with elements of an intermediate representation.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: UseDefer.java,v 1.1.2.3 2001-01-13 21:12:11 cananian Exp $
 */
public abstract class UseDefer {
    
    /** Return all the <code>Temp</code>s used by <code>hce</code>. */ 
    public Temp[] use(HCodeElement hce) {
	Collection c = useC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }

    /** Return all the <code>Temp</code>s defined by
	<code>hce</code>. 
    */  
    public Temp[] def(HCodeElement hce) {
	Collection c = defC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }

    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s that may be read by <code>hce</code>. 
    */
    public abstract Collection useC(HCodeElement hce);

    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s that are defined by <code>hce</code>. 
    */
    public abstract Collection defC(HCodeElement hce);
    
    /** Default <code>UseDefer</code> for <code>HCodeElement</code>s
	which implement <code>UseDef</code>.  Does nothing but cast
	the supplied <code>HCodeElement</code> to a
	<code>UseDef</code> and invoke the appropriate corresponding
	method in the <code>UseDef</code> interface.
	@see java.util.Comparator
	@see java.lang.Comparable
	@see harpoon.Util.Default.comparator
    */
    public static final UseDefer DEFAULT = new UseDefer() {
	public Collection useC(HCodeElement hce) {
	    return ((UseDef)hce).useC();
	}
	public Collection defC(HCodeElement hce) {
	    return ((UseDef)hce).defC();
	}
    };

}
