// IIR_SliceName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SliceName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SliceName.java,v 1.2 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SliceName extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SLICE_NAME; }
    //CONSTRUCTOR:
    public IIR_SliceName() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

