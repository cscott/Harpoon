// IIR_RangeTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RangeTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RangeTypeDefinition.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RangeTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_RANGE_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RangeTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

