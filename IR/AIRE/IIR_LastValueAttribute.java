// IIR_LastValueAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LastValueAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LastValueAttribute.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LastValueAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LAST_VALUE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_LastValueAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

