package harpoon.Interpret.Tree;

/** 
 * The <code>InterfaceList</code> class is a linked list used to represent
 * a list of interfaces. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterfaceList.java,v 1.1.2.1 1999-03-27 22:05:08 duncan Exp $
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

