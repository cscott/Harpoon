// IIR_CaseStatementAlternativeByOthers.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByOthers.java,v 1.2 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByOthers extends IIR_CaseStatementAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS; }
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

