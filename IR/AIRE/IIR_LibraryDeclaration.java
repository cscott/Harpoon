// IIR_LibraryDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryDeclaration.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LIBRARY_DECLARATION; }
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

