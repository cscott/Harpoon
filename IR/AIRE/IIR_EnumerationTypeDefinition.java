// IIR_EnumerationTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EnumerationTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationTypeDefinition.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ENUMERATION_TYPE_DEFINITION
    //CONSTRUCTOR:
    public IIR_EnumerationTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

