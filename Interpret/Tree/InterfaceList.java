// InterfaceList.java, created Tue Apr 27 18:05:08 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

/** 
 * The <code>InterfaceList</code> class is a linked list used to represent
 * a list of interfaces. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterfaceList.java,v 1.1.2.3 1999-08-04 05:52:35 cananian Exp $
 */
public class InterfaceList { 

    private ConstPointer[] interfaces;

    /** Class constructor. */
    public InterfaceList(int size) {
	interfaces = new ConstPointer[size];
    }

    /** Adds the interface pointed to by <code>iFace</code> to this
     *  list of interfaces. */
    public void addInterface(ConstPointer iFace, int index) {
	interfaces[index] = iFace;
    }

    /** Returns the i'th element of this <code>InterfaceList</code> */
    public ConstPointer getInterface(int i) { 
	return interfaces[i]; 
    }
}


