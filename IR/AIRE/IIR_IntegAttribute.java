// IIR_IntegAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IntegAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegAttribute.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEG_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_IntegAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

