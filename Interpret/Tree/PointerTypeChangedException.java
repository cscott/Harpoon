package harpoon.Interpret.Tree;

/**
 * This exception is thrown when the type of a <code>Pointer</code>
 * within the Tree interpreter is changed.  This is necessary because 
 * the type of a <code>Pointer</code> cannot be determined when it is
 * created.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: PointerTypeChangedException.java,v 1.1.2.2 1999-06-23 22:53:27 pnkfelix Exp $
 */
public class PointerTypeChangedException extends RuntimeException {
    /** The <code>Pointer</code> whose type is changed.
	@serial insert something here that "describes the meaning of the field and its
	        acceptable values" see <A
		HREF="http://java.sun.com/products/jdk/1.2/docs/guide/serialization/spec/serial-arch.doc6.html">
		Documenting Serializable Fields and Data for a Class</A>.
     */
    public Pointer ptr;

    /** Class constructor */
    public PointerTypeChangedException(Pointer ptr) {
	super();
	this.ptr = ptr;
    }
}
