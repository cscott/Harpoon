// IIR_LibraryDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryDeclaration.java,v 1.3 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_LIBRARY_DECLARATION).
     * @return <code>IR_Kind.IR_LIBRARY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_LIBRARY_DECLARATION; }
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

