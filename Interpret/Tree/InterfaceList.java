// InterfaceList.java, created Sat Mar 27 17:05:08 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

/** 
 * The <code>InterfaceList</code> class is a linked list used to represent
 * a list of interfaces. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterfaceList.java,v 1.2 2002-02-25 21:05:57 cananian Exp $
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


