// IIR_ThroughAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ThroughAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ThroughAttribute.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ThroughAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_THROUGH_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ThroughAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

