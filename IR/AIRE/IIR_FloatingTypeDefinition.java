// IIR_FloatingTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FloatingTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingTypeDefinition.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_FLOATING_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_FloatingTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

