// UseDefer.java, created Thu Jan 27 15:33:54 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import java.util.Collection;

/**
 * <code>UseDefer</code> provides a means to externally associate use and
 * def information with elements of an intermediate representation.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: UseDefer.java,v 1.3 2002-04-10 03:05:09 cananian Exp $
 */
public abstract class UseDefer<HCE extends HCodeElement> {
    
    /** Return all the <code>Temp</code>s used by <code>hce</code>. */ 
    public Temp[] use(HCE hce) {
	Collection c = useC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }

    /** Return all the <code>Temp</code>s defined by
	<code>hce</code>. 
    */  
    public Temp[] def(HCE hce) {
	Collection c = defC(hce);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }

    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s that may be read by <code>hce</code>. 
    */
    public abstract Collection<Temp> useC(HCE hce);

    /** Returns a <code>Collection</code> of all the
	<code>Temp</code>s that are defined by <code>hce</code>. 
    */
    public abstract Collection<Temp> defC(HCE hce);
    
    /** Default <code>UseDefer</code> for <code>HCodeElement</code>s
	which implement <code>UseDefable</code>.  Does nothing but cast
	the supplied <code>HCodeElement</code> to a
	<code>UseDefable</code> and invoke the appropriate corresponding
	method in the <code>UseDefable</code> interface.
	@see java.util.Comparator
	@see java.lang.Comparable
	@see harpoon.Util.Default.comparator
    */
    // see commentary on CFGrapher.DEFAULT.
    public static final UseDefer DEFAULT = new UseDefer() {
	public Collection<Temp> useC(HCodeElement hce) {
	    return ((UseDefable)hce).useC();
	}
	public Collection<Temp> defC(HCodeElement hce) {
	    return ((UseDefable)hce).defC();
	}
    };

}
