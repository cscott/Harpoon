// IIR_ReferenceAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ReferenceAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ReferenceAttribute.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ReferenceAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_REFERENCE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ReferenceAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

