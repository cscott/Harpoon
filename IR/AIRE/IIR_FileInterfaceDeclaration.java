// IIR_FileInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FileInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FileInterfaceDeclaration.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FileInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_FILE_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_FileInterfaceDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

