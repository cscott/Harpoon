// IIR_SimultaneousProceduralStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousProceduralStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousProceduralStatement.java,v 1.4 1998-10-11 01:25:03 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousProceduralStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIMULTANEOUS_PROCEDURAL_STATEMENT).
     * @return <code>IR_Kind.IR_SIMULTANEOUS_PROCEDURAL_STATEMENT</code>
     */
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

