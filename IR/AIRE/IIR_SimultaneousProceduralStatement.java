// IIR_SimultaneousProceduralStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousProceduralStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousProceduralStatement.java,v 1.3 1998-10-11 00:32:26 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousProceduralStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_PROCEDURAL_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SimultaneousProceduralStatement() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_DeclarationList procedural_declarative_part;
    public IIR_SequentialStatementList procedural_statement_part;

// PROTECTED:
} // END class

