// IIR_AcrossAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AcrossAttribute</code> %
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AcrossAttribute.java,v 1.2 1998-10-11 00:32:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AcrossAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ACROSS_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_AcrossAttribute( ){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

