// IIR_CaseStatementAlternativeByChoices.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByChoices</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByChoices.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByChoices extends IIR_CaseStatementAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByChoices() { }
    //METHODS:  
    //MEMBERS:  
    IIR_ChoiceList choices;

// PROTECTED:
} // END class

