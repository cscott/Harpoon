// IIR_SelectedNameByAll.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SelectedNameByAll</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SelectedNameByAll.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SelectedNameByAll extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SELECTED_NAME_BY_ALL
    //CONSTRUCTOR:
    public IIR_SelectedNameByAll() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

