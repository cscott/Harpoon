// IIR_LibraryUnit.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryUnit</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryUnit.java,v 1.5 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_LibraryUnit extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

