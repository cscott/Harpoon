// IIR_SuccAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SuccAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SuccAttribute.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SuccAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SUCC_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_SuccAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

