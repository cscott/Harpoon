// IIR_LastEventAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LastEventAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LastEventAttribute.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LastEventAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LAST_EVENT_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_LastEventAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

