// IIR_CaseStatementAlternative.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternative</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternative.java,v 1.5 1998-10-11 01:24:54 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_CaseStatementAlternative extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  
    public IIR_SequentialStatementList sequence_of_statements;

// PROTECTED:
} // END class

