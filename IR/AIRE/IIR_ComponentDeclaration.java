// IIR_ComponentDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentDeclaration.java,v 1.2 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMPONENT_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ComponentDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

