// IIR_ValueAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ValueAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ValueAttribute.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ValueAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_VALUE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ValueAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

