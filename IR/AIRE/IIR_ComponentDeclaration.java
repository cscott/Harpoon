// IIR_ComponentDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentDeclaration.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_COMPONENT_DECLARATION
    //CONSTRUCTOR:
    public IIR_ComponentDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

