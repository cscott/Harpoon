// IIR_SimultaneousStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousStatement.java,v 1.3 1998-10-11 00:32:26 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SimultaneousStatement extends IIR_Statement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

