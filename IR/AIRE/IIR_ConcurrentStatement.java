// IIR_ConcurrentStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentStatement.java,v 1.4 1998-10-11 01:24:55 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ConcurrentStatement extends IIR_Statement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

