// IIR_SimultaneousAlternative.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternative</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternative.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternative extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  
    public IIR_SimultaneousStatementList sequence_of_statements;

// PROTECTED:
} // END class

