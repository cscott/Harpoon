// IIR_VariableInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_VariableInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableInterfaceDeclaration.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_VARIABLE_INTERFACE_DECLARATION
    //CONSTRUCTOR:
    public IIR_VariableInterfaceDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

