// IIR_ConcurrentStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentStatement.java,v 1.3 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ConcurrentStatement extends IIR_Statement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

