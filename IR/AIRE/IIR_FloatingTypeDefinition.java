// IIR_FloatingTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FloatingTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingTypeDefinition.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FLOATING_TYPE_DEFINITION
    //CONSTRUCTOR:
    public IIR_FloatingTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

