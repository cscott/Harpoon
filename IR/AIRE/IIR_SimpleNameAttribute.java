// IIR_SimpleNameAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimpleNameAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimpleNameAttribute.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimpleNameAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMPLE_NAME_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_SimpleNameAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

