// IIR_SimultaneousStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousStatement.java,v 1.4 1998-10-11 01:25:03 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SimultaneousStatement extends IIR_Statement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

