// IIR_SimultaneousAlternative.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternative</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternative.java,v 1.3 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SimultaneousAlternative extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  
    public IIR_SimultaneousStatementList sequence_of_statements;

// PROTECTED:
} // END class

