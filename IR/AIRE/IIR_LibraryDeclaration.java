// IIR_LibraryDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryDeclaration.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LIBRARY_DECLARATION
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

