// IIR_RangeTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RangeTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RangeTypeDefinition.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RangeTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_RANGE_TYPE_DEFINITION
    //CONSTRUCTOR:
    public IIR_RangeTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

