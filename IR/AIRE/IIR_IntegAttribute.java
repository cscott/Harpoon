// IIR_IntegAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IntegAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegAttribute.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_INTEG_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_IntegAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

