// IIR_AcrossAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AcrossAttribute</code> %
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AcrossAttribute.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AcrossAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ACROSS_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_AcrossAttribute( ){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

