// IIR_ConstantInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConstantInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConstantInterfaceDeclaration.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConstantInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONSTANT_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ConstantInterfaceDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

