// IIR_IndexedName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IndexedName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IndexedName.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IndexedName extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_INDEXED_NAME
    //CONSTRUCTOR:
    public IIR_IndexedName() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

