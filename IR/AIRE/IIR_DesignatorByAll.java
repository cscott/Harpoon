// IIR_DesignatorByAll.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignatorByAll</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorByAll.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorByAll extends IIR_Designator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DESIGNATOR_BY_ALL
    //CONSTRUCTOR:
    public IIR_DesignatorByAll() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

