// UpdateCodeFactory.java, created Sat Oct 30 19:51:30 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.util.Hashtable;

/**
 * <code>UpdateCodeFactory</code> makes an <code>HCode</code> from an 
 * <code>HMethod</code>, and returns updated code that has been registered
 * using the <code>update</code> method. Otherwise returns the 
 * <code>HCode</code> from the parent <code>HCodeFactory</code>.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: UpdateCodeFactory.java,v 1.1.2.1 1999-11-06 17:15:50 kkz Exp $
 */
public class UpdateCodeFactory implements HCodeFactory {
    protected HCodeFactory parent;
    protected Hashtable implementation;
    
    /** Creates a <code>UpdateCodeFactory</code> using the default
     *  <code>HCodeFactory</code>. 
     */
    public UpdateCodeFactory(HCodeFactory parent) {
	this.parent = parent;
        this.implementation = new Hashtable();
    }

    /** Registers an <code>HMethod</code>-<code>HCode</code> pair
     *  with the <code>UpdateCodeFactory</code>.
     */
    public void update(HMethod m, HCode c) {
	this.implementation.put(m, c);
    }

    /** Removes the registered <code>HMethod</code> from the
     *  <code>UpdateCodeFactory</code>, if it exists, and returns it.
     *  Otherwise returns null.
     */
    public HCode remove(HMethod m) {
	return (HCode)this.implementation.remove(m);
    }
    
    /** Clears any cached <code>HCode</code> results for <code>m</code>
     *  from the parents of this factory.
     */
    public void clear(HMethod m) {
	this.parent.clear(m);
    }

    /** Makes an <code>HCode</code> from the <code>HMethod</code>. Returns
     *  an updated <code>HCode</code> when one has been registered using
     *  the <code>update</code> method. Otherwise returns the 
     *  <code>HCode</code> from the parent <code>HCodeFactory</code>.
     */
    public HCode convert(HMethod m) {
	if (implementation.containsKey(m))
	    return (HCode)this.implementation.get(m);
	return this.parent.convert(m);
    }

    /** Returns a string naming the type of <code>HCode</code> that this
     *  factory produces.
     */
    public String getCodeName() {
	return this.parent.getCodeName();
    }
}
