// IIR_SequentialStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SequentialStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SequentialStatement.java,v 1.4 1998-10-11 01:25:01 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SequentialStatement extends IIR_Statement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

