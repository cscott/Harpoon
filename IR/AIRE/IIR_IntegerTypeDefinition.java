// IIR_IntegerTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IntegerTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerTypeDefinition.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_INTEGER_TYPE_DEFINITION
    //CONSTRUCTOR:
    public IIR_IntegerTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

