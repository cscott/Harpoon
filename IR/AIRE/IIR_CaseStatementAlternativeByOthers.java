// IIR_CaseStatementAlternativeByOthers.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByOthers.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByOthers extends IIR_CaseStatementAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

