// IIR_PredAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PredAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PredAttribute.java,v 1.1 1998-10-10 07:53:40 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PredAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_PRED_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_PredAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

