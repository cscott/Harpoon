// IIR_ZOHAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ZOHAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ZOHAttribute.java,v 1.2 1998-10-11 00:32:29 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ZOHAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ZOH_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ZOHAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

