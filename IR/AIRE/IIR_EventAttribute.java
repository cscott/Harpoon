// IIR_EventAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EventAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EventAttribute.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EventAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_EVENT_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_EventAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

