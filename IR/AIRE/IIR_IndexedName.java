// IIR_IndexedName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IndexedName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IndexedName.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IndexedName extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_INDEXED_NAME; }
    //CONSTRUCTOR:
    public IIR_IndexedName() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

