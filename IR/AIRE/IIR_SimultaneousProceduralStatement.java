// IIR_SimultaneousProceduralStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousProceduralStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousProceduralStatement.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousProceduralStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMULTANEOUS_PROCEDURAL_STATEMENT
    //CONSTRUCTOR:
    public IIR_SimultaneousProceduralStatement() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_DeclarationList procedural_declarative_part;
    public IIR_SequentialStatementList procedural_statement_part;

// PROTECTED:
} // END class

